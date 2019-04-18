package com.yalantis.ucrop;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.yalantis.ucrop.adapter.ViewPagerAdapter;
import com.yalantis.ucrop.callback.onSelectImage;
import com.yalantis.ucrop.events.Events;
import com.yalantis.ucrop.events.SelectionEvent;
import com.yalantis.ucrop.events.SendDataToTabs;
import com.yalantis.ucrop.events.UpdateContent;
import com.yalantis.ucrop.model.MediaContent;
import com.yalantis.ucrop.util.Utility;
import com.yalantis.ucrop.view.TabIamgeFragment;
import com.yalantis.ucrop.view.TabVideoFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ChooseActivity extends Fragment {

    private static final String TAG = "ChooserActivity";

    private ArrayList<MediaContent> mediaContents = new ArrayList<>();
    private ArrayList<MediaContent> backupContents = new ArrayList<>();

    private ArrayList<MediaContent> Videos = new ArrayList<>();
    private final ArrayList<MediaContent> Images = new ArrayList<>();

    private Spinner spinner;
    private ArrayAdapter<String> spinnerArrayAdapter;
    private ArrayList<String> spinnerData = new ArrayList<>();

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private ArrayList<MediaContent> SelectedImages = new ArrayList<>();
    private ArrayList<MediaContent> SelectedVideos = new ArrayList<>();

    private int ImageSelected, VideoSelected, ImageLimit, VideoLimit;

    private Picker.PickerBuilder builder;

    View v;

    private onSelectImage selectImage;

    private Tab currentTab = Tab.Images;

    int selectLimit;

    private Cursor ImageCursur, VideoCursur;

    Menu menu;

    public ChooseActivity() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (v == null) {

            v = inflater.inflate(R.layout.ucrop_choose_image, container, false);

            Bundle bundle = getArguments();
            builder = bundle.getParcelable("data");
            if (builder != null) {
                ImageLimit = builder.getImageLimit();
                VideoLimit = builder.getVideoLimit();
            }

            selectImage = (onSelectImage) getActivity();

            if (spinnerData.size() == 0) {
                spinnerData.add(0, "All");
            }

            spinner = (Spinner) getActivity().findViewById(R.id.spinner_place);
            viewPager = (ViewPager) v.findViewById(R.id.tabPager);
            tabLayout = (TabLayout) v.findViewById(R.id.tabs);


            SetupTabs(viewPager);

            getContents();

            if (tabLayout != null && !builder.isOnlyImages()) {
                tabLayout.setupWithViewPager(viewPager);
            } else if (tabLayout != null && builder.isOnlyImages()) {
                tabLayout.setVisibility(View.GONE);
            }

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) {
                        currentTab = Tab.Images;
                    } else {
                        currentTab = Tab.Videos;
                    }
                    /*EventBus.getDefault().post(new Events.ReBindData());*/
                    changeLimitView();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            ImageSelected = 0;
            VideoSelected = 0;

            onClickListeners();

        } else {
            EventBus.getDefault().post(new Events.StopOnBack());
            int a = getFragmentManager().getBackStackEntryCount();
            Log.d(TAG, "onCreateView: " + a);
        }
        getActivity().findViewById(R.id.spinner_place).setVisibility(View.VISIBLE);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onViewCreated(view, savedInstanceState);
    }

    private void onClickListeners() {

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    filterData("");
                    try {
                        SplitVideoAndImages();
                    } catch (Exception ex) {
                    }

                    EventBus.getDefault().post(new SendDataToTabs(Images, MediaContent.IS_IMAGE));
                    EventBus.getDefault().post(new SendDataToTabs(Videos, MediaContent.IS_VIDEO));
                } else {
                    filterData(spinnerData.get(i));
                    try {
                        SplitVideoAndImages();
                    } catch (Exception ex) {
                    }

                    EventBus.getDefault().post(new SendDataToTabs(Images, MediaContent.IS_IMAGE));
                    EventBus.getDefault().post(new SendDataToTabs(Videos, MediaContent.IS_VIDEO));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void SetupTabs(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());

        TabVideoFragment images = new TabVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("data", Videos);
        bundle.putInt("type", MediaContent.IS_IMAGE);
        bundle.putInt("ImageLimit", builder.getImageLimit());
        bundle.putInt("VideoLimit", builder.getVideoLimit());
        bundle.putParcelable("builder", builder);
        images.setArguments(bundle);

        TabIamgeFragment videos = new TabIamgeFragment();
        Bundle bundle1 = new Bundle();
        bundle1.putParcelableArrayList("data", Images);
        bundle.putInt("type", MediaContent.IS_VIDEO);
        bundle1.putInt("ImageLimit", builder.getImageLimit());
        bundle1.putInt("VideoLimit", builder.getVideoLimit());
        bundle1.putParcelable("builder", builder);
        videos.setArguments(bundle1);

        adapter.addFragment(videos, "Images");

        if (!builder.isOnlyImages()) {
            adapter.addFragment(images, "Videos");
        }

        viewPager.setAdapter(adapter);

    }

    private void filterData(String path) {
        try {
            mediaContents.clear();

            for (MediaContent content : backupContents) {

                if (getDirectory(content.getUri()).equals(path)) {
                    mediaContents.add(content);
                } else if (path.equals("")) {
                    mediaContents.add(content);
                }

            }
        } catch (Exception ex) {

        }
    }

    private void SplitVideoAndImages() {
        Images.clear();
        Videos.clear();

        Images.add(0, new MediaContent(null, MediaContent.IS_IMAGE, true, MediaContent.CameraTypes.Image));
        Videos.add(0, new MediaContent(null, MediaContent.IS_VIDEO, true, MediaContent.CameraTypes.Video));

        for (MediaContent content : mediaContents) {

            if (content.getType() == MediaContent.IS_IMAGE) {
                Images.add(content);
            } else {
                Videos.add(content);
            }

        }

    }

    private String getDirectory(Uri uri) {

        List<String> u = uri.getPathSegments();
        return u.get(u.size() - 2);

    }

    private void SpinnerData(String data) {
        try {
            boolean duplicate = false;
            if (spinnerData.size() > 0) {
                for (String t : spinnerData) {

                    if (t.equals(data)) {
                        duplicate = true;
                    }

                }
                if (!duplicate) {
                    spinnerData.add(data);
                }
            } else {
                spinnerData.add(data);
            }
        } catch (Exception ex) {

        }
    }

    private Cursor getImagesCursor() {

        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        CursorLoader cursorLoader = new CursorLoader(
                getContext(),
                queryUri,
                null,
                null,
                null, // Selection args (none).
                MediaStore.Images.Media.DATE_ADDED + " DESC" // Sort order.
        );

        return cursorLoader.loadInBackground();

    }

    private Cursor getVideoCursor() {

        Uri queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        CursorLoader cursorLoader = new CursorLoader(
                getContext(),
                queryUri,
                null,
                null,
                null, // Selection args (none).
                MediaStore.Video.Media.DATE_ADDED + " DESC" // Sort order.
        );

        return cursorLoader.loadInBackground();

    }

    private void getImageContents() {

        final Cursor contents = ImageCursur;

        if ((contents != null && contents.moveToFirst())) {

            if (mediaContents.isEmpty()) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        do {
                            //get uri
                            int UriColumn = contents.getColumnIndex(MediaStore.Images.Media.DATA);
                            Log.d(TAG, "onCreate: " + contents.getString(UriColumn));

                            Uri uri = Uri.parse(contents.getString(UriColumn));

                            SpinnerData(getDirectory(uri));

                            Images.add(new MediaContent(uri, MediaContent.IS_IMAGE));
                            mediaContents.add(new MediaContent(uri, MediaContent.IS_IMAGE));
                            backupContents.add(new MediaContent(uri, MediaContent.IS_IMAGE));

                        } while (contents.moveToNext());
                        spinnerArrayAdapter = new ArrayAdapter<>(
                                getActivity(), R.layout.ucrop_spinner_item, spinnerData);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        spinner.setAdapter(spinnerArrayAdapter);
                        spinner.setSelection(0);

                        spinner.setVisibility(View.VISIBLE);
                    }
                });
            }


        }

    }

    private void getVideoContents() {

        final Cursor contents = VideoCursur;

        if ((contents != null && contents.moveToFirst())) {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    do {
                        //get uri
                        int UriColumn = contents.getColumnIndex(MediaStore.Video.Media.DATA);
                        Log.d(TAG, "onCreate: " + contents.getString(UriColumn));

                        Uri uri = Uri.parse(contents.getString(UriColumn));

                        SpinnerData(getDirectory(uri));

                        Videos.add(new MediaContent(uri, MediaContent.IS_VIDEO, false,
                                MediaContent.CameraTypes.Video, false, Utility.getMediaTime(uri, getContext())));
                        mediaContents.add(new MediaContent(uri, MediaContent.IS_VIDEO, false,
                                MediaContent.CameraTypes.Video, false, Utility.getMediaTime(uri, getContext())));
                        backupContents.add(new MediaContent(uri, MediaContent.IS_VIDEO, false,
                                MediaContent.CameraTypes.Video, false, Utility.getMediaTime(uri, getContext())));

                    } while (contents.moveToNext());
                }
            });

            thread.run();

            /*spinnerArrayAdapter = new ArrayAdapter<>(
                    getActivity(), R.layout.ucrop_spinner_item, spinnerData);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinner.setAdapter(spinnerArrayAdapter);
            spinner.setSelection(0);

            spinner.setVisibility(View.VISIBLE);*/
        }

    }

    private void getContents() {

/*    requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);*/

        Images.clear();
        Videos.clear();

        Images.add(0, new MediaContent(null, MediaContent.IS_IMAGE, true, MediaContent.CameraTypes.Image));
        Videos.add(0, new MediaContent(null, MediaContent.IS_VIDEO, true, MediaContent.CameraTypes.Video));

        if (mediaContents.isEmpty()) {

            ImageCursur = getImagesCursor();
            VideoCursur = getVideoCursor();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    getImageContents();
                    getVideoContents();

                }
            });
            thread.start();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        this.menu = menu;
        menu.clear();
        getActivity().findViewById(R.id.spinner_place).setVisibility(View.VISIBLE);
        inflater.inflate(R.menu.ucrop_menu_activity, menu);

        if (builder.getMinimum() != -1) {
            menu.findItem(R.id.menu_crop).setVisible(false);
        }
        if (ImageSelected == 0 && VideoSelected == 0) {
            menu.findItem(R.id.menu_crop).setVisible(false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /*if (item.getItemId() == android.R.id.home) {
            getFragmentManager().popBackStack();
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        MediaContent content = selectionEvent.content;

        if (content.getType() == MediaContent.IS_IMAGE) {
            SelectedImages.add(content);
            selectImage.onItemSelected(content);
        } else {
            SelectedVideos.add(content);
            selectImage.onItemSelected(content);
        }
        if (builder.getMinimum() != -1) {
            if ((SelectedImages.size() + SelectedVideos.size() < builder.getMinimum())) {
                menu.findItem(R.id.menu_crop).setVisible(false);
            } else if ((SelectedImages.size() + SelectedVideos.size() > builder.getMinimum())) {
                menu.findItem(R.id.menu_crop).setVisible(false);
            } else {
                menu.findItem(R.id.menu_crop).setVisible(true);
            }
        }

    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        /*EventBus.getDefault().unregister(this);*/
    }

    @Subscribe
    public void onUpdateContent(UpdateContent updateContent) {
        getContents();
        SetupTabs(viewPager);
    }

    @Subscribe
    public void UpdateLimitView(Events.onLimitChange limitChange) {
        if (limitChange.ImageSelected != -1) {
            ImageSelected = limitChange.ImageSelected;
        }
        if (limitChange.VideoSelected != -1) {
            VideoSelected = limitChange.VideoSelected;
        }
        changeLimitView();
    }

    @Subscribe
    public void setLimit(Events.newLimit limit) {
        VideoLimit = limit.VideoLimit;
        ImageLimit = limit.ImageLimit;
        changeLimitView();
    }

    private void changeLimitView() {

        LinearLayout layout = (LinearLayout) v.findViewById(R.id.limitLayout);
        TextView text = (TextView) v.findViewById(R.id.limitText);
        text.setText("");

        if (currentTab == Tab.Images) {

            if (builder.getMinimum() != -1) {
                if (ImageLimit == -1) {
                    layout.setVisibility(View.VISIBLE);
                    String txt = String.format("Selected %d/%d ", ImageSelected + VideoSelected, builder.getMinimum());
                    text.setText(txt);
                } else {
                    layout.setVisibility(View.VISIBLE);
                    String txt = String.format("Selected %d/%d Image", ImageSelected, ImageLimit);
                    text.setText(txt);
                }
            } else {
                if (ImageLimit == -1) {
                    layout.setVisibility(View.GONE);
                } else {
                    layout.setVisibility(View.VISIBLE);
                    String txt = String.format("Selected %d/%d Image", ImageSelected, ImageLimit);
                    text.setText(txt);
                }
            }

        } else if (currentTab == Tab.Videos) {

            if (builder.getMinimum() != -1) {
                if (VideoLimit == -1) {
                    layout.setVisibility(View.VISIBLE);
                    String txt = String.format("Selected %d/%d ", ImageSelected + VideoSelected, builder.getMinimum());
                    text.setText(txt);
                } else {
                    layout.setVisibility(View.VISIBLE);
                    String txt = String.format("Selected %d/%d Video", VideoSelected, VideoLimit);
                    text.setText(txt);
                }
            } else {
                if (VideoLimit == -1) {
                    layout.setVisibility(View.GONE);
                } else {
                    layout.setVisibility(View.VISIBLE);
                    String txt = String.format("Selected %d/%d Video", VideoSelected, VideoLimit);
                    text.setText(txt);
                }
            }
        }

    }

    enum Tab {
        Images, Videos
    }

}
