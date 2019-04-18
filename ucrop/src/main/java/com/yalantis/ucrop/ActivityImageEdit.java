package com.yalantis.ucrop;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.yalantis.ucrop.adapter.AdapterRecycler;
import com.yalantis.ucrop.adapter.AdapterViewPager;
import com.yalantis.ucrop.callback.RecyclerItemClickListener;
import com.yalantis.ucrop.events.Events;
import com.yalantis.ucrop.model.MediaContent;
import com.yalantis.ucrop.util.ImageEntry;
import com.yalantis.ucrop.util.SpacesItemDecoration;
import com.yalantis.ucrop.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 *
 */

public class ActivityImageEdit extends Fragment {

    private static final String EXTRA_PREFIX = BuildConfig.APPLICATION_ID;
    public static final String EXTRA_ASPECT_RATIO_X = EXTRA_PREFIX + ".AspectRatioX";
    public static final String EXTRA_ASPECT_RATIO_Y = EXTRA_PREFIX + ".AspectRatioY";

    private int imagePosition = 0;
    private ArrayList<ImageEntry> images = new ArrayList<>();
    private ArrayList<MediaContent> contents;
    private AdapterRecycler adapterRecycler;
    private AdapterViewPager adapterViewPager;

    Picker.PickerBuilder pickerBuilder;

    boolean editpressed = false;

    View v;

    Menu menu;

    public static int lastPos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (v == null) {
            Bundle bundle = getArguments();
            contents = bundle.getParcelableArrayList("contents");
            pickerBuilder = bundle.getParcelable("builder");
            EventBus.getDefault().register(this);

            if (contents != null) {
                for (MediaContent content: contents) {
                    ImageEntry entry;
                    ImageEntry.Builder builder = new ImageEntry.Builder(content.getUri().getPath());
                    entry = builder.build();
                    entry.isVideo = content.getType() == MediaContent.IS_VIDEO;
                    images.add(entry);
                }
            }

            /*images = (ArrayList<ImageEntry>) bundle.getSerializable("images");*/
            Toolbar mToolbar = new Toolbar(new ContextThemeWrapper(getActivity(), Util.getToolbarThemeResId(getContext())));

            getActivity().findViewById(R.id.firstToolbar).setVisibility(View.VISIBLE);

            v = inflater.inflate(R.layout.ucrop_image_edit, container, false);

            final AppBarLayout appBarLayout = (AppBarLayout) v.findViewById(R.id.app_bar_layout);
            final AppBarLayout.LayoutParams toolbarParams =
                    new AppBarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Util.getActionBarHeight(getContext()));
            toolbarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);

            final ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewPager);
            final RecyclerView recycler = (RecyclerView) v.findViewById(R.id.recyclerView);
            adapterRecycler = new AdapterRecycler(getContext(), images, new RecyclerItemClickListener() {
                @Override
                public void recyclerViewListClicked(View v, int position) {
                    viewPager.setCurrentItem(position);
                    if (images.get(position).isVideo) {
                        menu.findItem(R.id.action_crop).setVisible(false);
                    } else {
                        menu.findItem(R.id.action_crop).setVisible(true);
                    }
                }
            });

            Toolbar mainToolbar = (Toolbar) getActivity().findViewById(R.id.firstToolbar);
            mainToolbar.setVisibility(View.VISIBLE);

            recycler.setAdapter(adapterRecycler);
            recycler.addItemDecoration(new SpacesItemDecoration(5));

            if (images != null) {
                viewPager.setOffscreenPageLimit(images.size());
            } else {
                viewPager.setOffscreenPageLimit(0);
            }

            adapterViewPager = new AdapterViewPager(getContext(), images);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (images.get(position).isVideo) {
                        menu.findItem(R.id.action_crop).setVisible(false);
                        EventBus.getDefault().post(new Events.StopOnBack());
                    } else {
                        menu.findItem(R.id.action_crop).setVisible(true);
                    }
                }

                @Override
                public void onPageSelected(int position) {
                    recycler.scrollToPosition(position);
                    adapterRecycler.setSelecton(position);
                    imagePosition = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            viewPager.setAdapter(adapterViewPager);
        } else {
            getActivity().findViewById(R.id.firstToolbar).setVisibility(View.VISIBLE);
            getFragmentManager().popBackStack("ucrop", 1);
            EventBus.getDefault().post(new Events.updateContent(lastPos));
        }

        return v;
    }

    /*private int fetchAccentColor(int attrId) {
        TintTypedArray typedValue = obtainStyledAttributes(getContext(), );
        TypedArray a = obtainStyledAttributes(getContext(), typedValue.data, new int[]{attrId});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }*/

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int i = item.getItemId();
        if (i == R.id.action_crop) {

            final ImageEntry entry = images.get(imagePosition);
            if (!entry.isVideo && !editpressed) {
                final File[] output = {null};
                editpressed = true;

                try {
                    Bundle bundle = new Bundle();
                    output[0] = File.createTempFile("output", ".jpg");
                    bundle.putParcelable("input", Uri.fromFile(new File(entry.path)));
                    bundle.putParcelable("output", Uri.fromFile(output[0]));
                    if (pickerBuilder.gethAspect() != -1) {
                        bundle.putFloat(EXTRA_ASPECT_RATIO_X, pickerBuilder.getwAspect());
                        bundle.putFloat(EXTRA_ASPECT_RATIO_Y, pickerBuilder.gethAspect());
                    }
                    EventBus.getDefault().post(new Events.OnCrop(bundle, imagePosition));
                    lastPos = imagePosition;
                    editpressed = false;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    editpressed = false;
                }

            }

        } else if (i == android.R.id.home) {
            getFragmentManager().popBackStack();
        } else if (i == R.id.action_done) {

            EventBus.getDefault().post(new Events.OnImageEditResult(contents));

        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            //images.remove(imagePosition);
            images.get(imagePosition).path = UCrop.getOutput(data).getPath();
            adapterViewPager.notifyDataSetChanged();
            adapterRecycler.notifyDataSetChanged();
            // UCrop.getOutput(data);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        getActivity().findViewById(R.id.spinner_place).setVisibility(View.GONE);
        inflater.inflate(R.menu.ucrop_menu_cropper, menu);
        this.menu = menu;
    }

    @Subscribe
    public void onMenuChange (Events.OnMenuChange onMenuChange) {
        menu.findItem(R.id.action_crop).setVisible(!onMenuChange.isVideo);
    }
}
