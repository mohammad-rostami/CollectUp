package com.yalantis.ucrop.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.yalantis.ucrop.Picker;
import com.yalantis.ucrop.R;
import com.yalantis.ucrop.adapter.ImageAdapter;
import com.yalantis.ucrop.callback.onSelected;
import com.yalantis.ucrop.events.Events;
import com.yalantis.ucrop.events.SelectionEvent;
import com.yalantis.ucrop.events.SendDataToTabs;
import com.yalantis.ucrop.model.MediaContent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Images Tab
 */
public class TabIamgeFragment extends Fragment {

    static final int REQUEST_CAPTURE = 100;
    private static final String TAG = "TABFragment";
    static ArrayList<MediaContent> ImageData = new ArrayList<>();
    private static int type;
    private static boolean CameraIsWorking = false;
    ImageAdapter adapter;
    private String pictureImagePath = "";
    private RecyclerView recyclerView;
    private int videoLimit;
    private int ImageLimit;

    private Picker.PickerBuilder builder;

    View v;

    public TabIamgeFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        v = inflater.inflate(R.layout.ucrop_content_tab, container, false);

        EventBus.getDefault().register(this);

        recyclerView = (RecyclerView) v.findViewById(R.id.choose_recyclerview);

        Bundle bundle = getArguments();

        ImageData = bundle.getParcelableArrayList("data");
        type = bundle.getInt("type");
        videoLimit = bundle.getInt("VideoLimit");
        ImageLimit = bundle.getInt("ImageLimit");
        builder = bundle.getParcelable("builder");

        adapter = new ImageAdapter(ImageData, builder, new onSelected() {
            @Override
            public void onSelect(int position, int t) {
                if (position > 0) {
                    EventBus.getDefault().post(new SelectionEvent(ImageData.get(position)));
                } else if (position == 0) {

                    Intent TakePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (TakePhotoIntent.resolveActivity(getContext().getPackageManager()) != null) {
                        if (!CameraIsWorking) {
                            try {
                                openBackCamera();
                            } catch (Exception e){
                                requestPermissions(new String[]{Manifest.permission.CAMERA},
                                        REQUEST_CAPTURE);
                            }
                        }
                    }

                }
            }

            @Override
            public void onDeSelect(MediaContent mediaContent) {
                EventBus.getDefault().post(new Events.OnDeselect(mediaContent));
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Subscribe
    public void onSendDataToTabs(SendDataToTabs sendDataToTabs) {

        int t = sendDataToTabs.type;
        ArrayList<MediaContent> contents = sendDataToTabs.contents;

        if (t == type) {
            ImageData = contents;
            adapter.notifyDataSetChanged();
        }

    }

    public boolean hasPermissionInManifest(Context context, String permissionName) {
        final String packageName = context.getPackageName();
        try {
            final PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            final String[] declaredPermisisons = packageInfo.requestedPermissions;
            if (declaredPermisisons != null && declaredPermisisons.length > 0) {
                for (String p : declaredPermisisons) {
                    if (p.equals(permissionName)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAPTURE) {
            File imgFile = new  File(pictureImagePath);
            if(imgFile.exists()){
                Bitmap b = BitmapFactory.decodeFile(pictureImagePath);
                MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
                        b, Uri.parse(pictureImagePath).getLastPathSegment(), "Pictures");
                Log.d(TAG, "onActivityResult: ");
                ImageData.add(1, new MediaContent(Uri.parse(pictureImagePath), MediaContent.IS_IMAGE,
                        false, MediaContent.CameraTypes.Image));
                adapter.notifyDataSetChanged();
                CameraIsWorking = false;
            }

        }

    }

    private void openBackCamera() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File file = new File(pictureImagePath);
        Uri outputFileUri = Uri.fromFile(file);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, REQUEST_CAPTURE);
        CameraIsWorking = true;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        /*ScrollView.LayoutParams layoutParams = new ScrollView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);*/

        if (isVisibleToUser && adapter != null) {
            adapter.notifyDataSetChanged();
            /*ScrollView scrollView = (ScrollView) v.findViewById(R.id.scrollView);
            scrollView.updateViewLayout(recyclerView, layoutParams);*/
        }
    }
}

