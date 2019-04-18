package net.yazeed44.imagepicker.ui;

import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.collect_up.c_up.imagepicker.R;
import com.yalantis.ucrop.UCrop;

import net.yazeed44.imagepicker.model.ImageEntry;
import net.yazeed44.imagepicker.util.AdapterRecycler;
import net.yazeed44.imagepicker.util.AdapterViewPager;
import net.yazeed44.imagepicker.util.Events;
import net.yazeed44.imagepicker.util.Picker;
import net.yazeed44.imagepicker.util.RecyclerItemClickListener;
import net.yazeed44.imagepicker.util.Util;

import java.io.File;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by collect-up3 on 5/16/2016.
 */
public class ActivityImageEdit extends AppCompatActivity {

    private int imagePosition = 0;
    private ArrayList<ImageEntry> images;
    private AdapterRecycler adapterRecycler;
    private AdapterViewPager adapterViewPager;
    private Picker mPickOptions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mPickOptions = (EventBus.getDefault().getStickyEvent(Events.OnPublishPickOptionsEvent.class)).options;

        Bundle bundle = getIntent().getExtras();
        images = (ArrayList<ImageEntry>) bundle.getSerializable("images");
        Toolbar mToolbar = new Toolbar(new ContextThemeWrapper(this, Util.getToolbarThemeResId(this)));

        setTheme(mPickOptions.popupThemeResId);
        mToolbar = new Toolbar(new ContextThemeWrapper(mPickOptions.context, Util.getToolbarThemeResId(this)));
        mToolbar.setPopupTheme(mPickOptions.popupThemeResId);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);

        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        final AppBarLayout.LayoutParams toolbarParams = new AppBarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Util.getActionBarHeight(this));
        toolbarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);

        appBarLayout.addView(mToolbar, toolbarParams);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        final RecyclerView recycler = (RecyclerView) findViewById(R.id.recyclerView);
        adapterRecycler = new AdapterRecycler(this, images, new RecyclerItemClickListener() {
            @Override
            public void recyclerViewListClicked(View v, int position) {
                viewPager.setCurrentItem(position);
            }
        });

        recycler.setAdapter(adapterRecycler);
        recycler.addItemDecoration(new SpacesItemDecoration(5));

        viewPager.setOffscreenPageLimit(images.size());
        adapterViewPager = new AdapterViewPager(this, images);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

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
    }

    private int fetchAccentColor(int attrId) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{attrId});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int i = item.getItemId();
        if (i == R.id.action_crop) {
            UCrop.Options options = new UCrop.Options();
            options.setFreeStyleCropEnabled(true);
            //  final TypedValue value = new TypedValue();
            //  getTheme().resolveAttribute(R.attr.colorAccent, value, true);
            options.setCompressionQuality(100);
            options.setActiveWidgetColor(fetchAccentColor(R.attr.colorAccent));
            options.setToolbarColor(fetchAccentColor(R.attr.colorPrimary));
            options.setStatusBarColor(fetchAccentColor(R.attr.colorPrimaryDark));
            Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped" + System.currentTimeMillis()));
            UCrop.of(Uri.fromFile(new File(images.get(imagePosition).path)), destination).withOptions(options).start(this);

        } else if (i == android.R.id.home) {
            onBackPressed();

        } else if (i == R.id.action_done) {
            mPickOptions.pickListener.onPickedSuccessfully(new ArrayList<>(images));
            finish();
            setResult(10);

        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_cropper, menu);
        return true;
    }
}
