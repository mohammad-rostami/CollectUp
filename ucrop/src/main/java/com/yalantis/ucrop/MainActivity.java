package com.yalantis.ucrop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.yalantis.ucrop.callback.onSelectImage;
import com.yalantis.ucrop.callback.onUCropFinished;
import com.yalantis.ucrop.events.Events;
import com.yalantis.ucrop.model.MediaContent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import id.zelory.compressor.Compressor;



/**
 *
 */
public class MainActivity extends AppCompatActivity implements onSelectImage, onUCropFinished {

    private static final String EXTRA_PREFIX = BuildConfig.APPLICATION_ID;
    public static final String EXTRA_ASPECT_RATIO_X = EXTRA_PREFIX + ".AspectRatioX";
    public static final String EXTRA_ASPECT_RATIO_Y = EXTRA_PREFIX + ".AspectRatioY";

    public static final String EXTRA_OUTPUT_URI = EXTRA_PREFIX + ".OutputUri";

    private static final String TAG = "UCropMain";
    Spinner spinner;
    Toolbar toolbar;
    private int ucropImagePosition;
    private ArrayList<MediaContent> mediaContents = new ArrayList<>();

    private Picker.PickerBuilder pickerBuilder;

    boolean backstack = false;

    ArrayList<MediaContent> contents = new ArrayList<>();

    Menu menu;
    private String filePath;
    private File compressedImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ucrop_main_activity);


        EventBus.getDefault().register(this);

        spinner = (Spinner) findViewById(R.id.spinner_place);
        spinner.setVisibility(View.GONE);




       /* try {
            CopyRawToFile(R.raw.ffmpeg, "ffmpeg");
            MakeExecutable("ffmpeg");
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Intent intent = getIntent();

        if (intent != null) {
            pickerBuilder = intent.getParcelableExtra("data");
        }

        setupAppBar();

        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int backstackcount = getSupportFragmentManager().getBackStackEntryCount();

                if (backstackcount == 1) {
                    toolbar.setVisibility(View.VISIBLE);
                } else if (backstackcount == 0) {
                    fragmentManager.popBackStack();
                    finish();
                }

            }
        });

        ChooseFragment();


    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();


    }

    private void ChooseFragment() {
        ChooseActivity chooseActivity = new ChooseActivity();
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", pickerBuilder);
        chooseActivity.setArguments(bundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.Place_holder, chooseActivity);
        ft.addToBackStack("choose");
        ft.commit();
    }

    private void setupAppBar() {
        toolbar = (Toolbar) findViewById(R.id.firstToolbar);
        // Set all of the Toolbar coloring
        /*toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                R.color.ucrop_color_toolbar));
        toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(),
                R.color.ucrop_color_toolbar_widget));*/

        toolbar.inflateMenu(R.menu.ucrop_chooser_menu);

        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            actionBar.setDisplayShowTitleEnabled(false);
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (backstack) {
            menu.clear();
            return true;
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.menu = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.ucrop_menu_activity, menu);

        if (mediaContents == null || mediaContents.size() == 0) {
            MenuItem menuItem = menu.findItem(R.id.menu_crop);
            menuItem.setVisible(false);
        } else {
            this.menu.findItem(R.id.menu_crop).setVisible(false);
        }

        return true;

    }

    /**
     * This method is called whenever the user chooses to navigate Up within your application's
     * activity hierarchy from the action bar.
     * <p/>
     * <p>If a parent was specified in the manifest for this activity or an activity-alias to it,
     * default Up navigation will be handled automatically. See
     * {@link #getSupportParentActivityIntent()} for how to specify the parent. If any activity
     * along the parent chain requires extra Intent arguments, the Activity subclass
     * should override the method {@link #onPrepareSupportNavigateUpTaskStack(TaskStackBuilder)}
     * to supply those arguments.</p>
     * <p/>
     * <p>See <a href="{@docRoot}guide/topics/fundamentals/tasks-and-back-stack.html">Tasks and
     * Back Stack</a> from the developer guide and
     * <a href="{@docRoot}design/patterns/navigation.html">Navigation</a> from the design guide
     * for more information about navigating within your app.</p>
     * <p/>
     * <p>See the {@link TaskStackBuilder} class and the Activity methods
     * {@link #getSupportParentActivityIntent()}, {@link #supportShouldUpRecreateTask(Intent)}, and
     * {@link #supportNavigateUpTo(Intent)} for help implementing custom Up navigation.</p>
     *
     * @return true if Up navigation completed successfully and this Activity was finished,
     * false otherwise.
     */
    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(TAG, "onOptionsItemSelected: " + item.getItemId());

        if (item.getItemId() == android.R.id.home) {
            int backstackcount = getSupportFragmentManager().getBackStackEntryCount();

            if (backstackcount == 1) {
                finish();
            }

        } else {
            setFragment();
        }

        return super.onOptionsItemSelected(item);
    }

    public void setFragment() {

        int backstackcount = getSupportFragmentManager().getBackStackEntryCount();

        if (backstackcount == 0) {
            getFragmentManager().popBackStack();
            ChooseFragment();
        } else if (backstackcount == 1) {

            if (mediaContents.size() > 1) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ActivityImageEdit imageEdit;
                imageEdit = new ActivityImageEdit();
                Bundle bundle = new Bundle();
                contents.clear();
                bundle.putParcelableArrayList("contents", mediaContents);
                bundle.putParcelable("builder", pickerBuilder);
                imageEdit.setArguments(bundle);
                ft.replace(R.id.Place_holder, imageEdit);
                ft.addToBackStack("first");
                try {
                    ft.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (mediaContents.get(0).getType() == MediaContent.IS_IMAGE) {

                    try {
                        //FOR IMAGES WE COMPRESS THE IMAGE FILE
                        File file = new File(mediaContents.get(0).getUri().getPath());
                        compressedImage = Compressor.getDefault(this).compressToFile(file);
//                        File compressedImage = new Compressor.Builder(getApplicationContext())
//                                .setQuality(100)
//                                .setCompressFormat(Bitmap.CompressFormat.JPEG)
//                                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
//                                        Environment.DIRECTORY_PICTURES).getAbsolutePath())
//                                .build()
//                                .compressToFile(file);

//                        OnCompressListener listener = new OnCompressListener() {
//                            @Override
//                            public void onStart() {
//
//                            }
//
//                            @Override
//                            public void onSuccess(File file) {
//                                compressedImage = file;
//
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//
//                            }
//                        };
//                        Luban.compress(getApplicationContext(), file)
//                                .putGear(Luban.THIRD_GEAR)      // set the compress mode, default is : THIRD_GEAR
//                                .launch(listener);

                        //    compresedBitmap = SiliCompressor.with(getContext()).getCompressBitmap(String.valueOf(imageUri));
//    Log.i("image size", String.valueOf(compresedBitmap.getByteCount()));
                        filePath = compressedImage.getPath();

                    } catch (Exception e) {
                        filePath = mediaContents.get(0).getUri().getPath();
                    }
//                    bitmap = BitmapFactory.decodeFile(filePath);

                    File output = null;
                    try {
                        Bundle bundle = new Bundle();
                        UCropFragment uCropFragment = new UCropFragment();
                        output = File.createTempFile("output", ".jpg");
                        bundle.putBoolean(UCrop.Options.EXTRA_FREE_STYLE_CROP, pickerBuilder.isFreeStyle());
                        bundle.putParcelable("input", Uri.fromFile(new File(filePath)));
//                        bundle.putParcelable("input", Uri.fromFile(new File(mediaContents.get(0).getUri().getPath())));
                        bundle.putParcelable("output", Uri.fromFile(output));
                        bundle.putInt("step", 1);
                        if (pickerBuilder.gethAspect() != -1) {
                            bundle.putFloat(EXTRA_ASPECT_RATIO_X, pickerBuilder.getwAspect());
                            bundle.putFloat(EXTRA_ASPECT_RATIO_Y, pickerBuilder.gethAspect());
                        }
                        uCropFragment.setArguments(bundle);
                        EventBus.getDefault().post(new Events.OnCrop(bundle, 0));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }

                } else {

                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("data", mediaContents);

                    Intent intent = new Intent();
                    intent.putExtra("data", bundle);

                    setResult(-1, intent);
                    finish();

                }
            }
        }
    }

    @Override
    public void onItemSelected(MediaContent item) {
        menu.findItem(R.id.menu_crop).setVisible(true);
        mediaContents.add(item);
    }

    @Override
    public void onResult(Intent intent) {

        if (intent.getIntExtra("step", 0) == 1) {

            Bundle bundle = new Bundle();
            Uri uri = intent.getParcelableExtra(EXTRA_OUTPUT_URI);
            mediaContents.clear();
            mediaContents.add(new MediaContent(uri, MediaContent.IS_IMAGE, false, MediaContent.CameraTypes.Image));
            bundle.putParcelableArrayList("data", mediaContents);

            Intent s = new Intent();
            s.putExtra("data", bundle);
            setResult(RESULT_OK, s);
            finish();

        } else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            ActivityImageEdit imageEdit = new ActivityImageEdit();
            Bundle bundle = new Bundle();
            Uri uri = intent.getParcelableExtra(EXTRA_OUTPUT_URI);

            if (contents.size() == 0) {
                for (MediaContent content : mediaContents) {
                    contents.add(content);
                }
            }

            contents.set(ucropImagePosition, new MediaContent(uri, MediaContent.IS_IMAGE));

            getFragmentManager().popBackStack();
            bundle.putParcelableArrayList("contents", contents);
            bundle.putParcelable("builder", pickerBuilder);
            imageEdit.setArguments(bundle);
            ft.replace(R.id.Place_holder, imageEdit);
            ft.addToBackStack("second");
            ft.commit();
        }

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data != null) {
            Log.d(TAG, "onActivityResult: " + data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

 /*   private void CopyRawToFile(int resourceId, String filename) throws IOException {
        Context m_context = getApplicationContext();
        InputStream input = m_context.getResources().openRawResource(resourceId);
        OutputStream output = m_context.openFileOutput(filename, Context.MODE_PRIVATE);

        byte[] buffer = new byte[1024 * 4];
        int a;
        while((a = input.read(buffer)) > 0)
            output.write(buffer, 0, a);

        input.close();
        output.close();
    }

    private String MakeExecutable(String filename) {
        Context m_context = getApplicationContext();
        //First get the absolute path to the file
        File folder = m_context.getFilesDir();

        String fullpath = "";
        String filefolder = null;
        try {
            filefolder = folder.getCanonicalPath();
            if (!filefolder.endsWith("/"))
                filefolder += "/";

            fullpath = filefolder + filename;

            Runtime.getRuntime().exec("chmod 770 " + fullpath).waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return fullpath;
    }*/

    @Subscribe
    public void OnCrop(Events.OnCrop onCrop) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        UCropFragment uCropFragment = new UCropFragment();
        ucropImagePosition = onCrop.position;
        uCropFragment.setArguments(onCrop.bundle);
        ft.add(R.id.Place_holder, uCropFragment);
        ft.addToBackStack("ucrop");
        ft.commit();
    }

    @Subscribe
    public void OnResult(Events.OnImageEditResult onImageEditResult) {

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("data", onImageEditResult.contents);

        Intent intent = new Intent();
        intent.putExtra("data", bundle);

        setResult(RESULT_OK, intent);
        finish();

    }

    @Subscribe
    public void OnDeselect(Events.OnDeselect onDeselect) {

        for (int i = 0; i <= mediaContents.size(); i++) {

            if (mediaContents.get(i).getUri() == onDeselect.content.getUri()) {
                mediaContents.remove(i);
            }

        }

        if (mediaContents.size() == 0) {
            menu.findItem(R.id.menu_crop).setVisible(false);
        }

    }


}