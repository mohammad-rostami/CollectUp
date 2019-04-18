package com.collect_up.c_up.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.collect_up.c_up.MyApplication;
import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.view.CustomPermissionDialog;
import com.collect_up.c_up.view.TouchImageView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.rey.material.widget.ProgressView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Created by collect-up3 on 6/18/2016.
 */
public class ActivityGalleryPager extends AppCompatActivity {

  public static final int INDEX = 2;


  private String[] titles;
  private ArrayList<String> images;
  private int page;
  private View toolbarView;


  @Override
  public void onCreate(Bundle savedInstanceState) {


    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_pager);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    {
      getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.black));

      postponeEnterTransition();

      final View decor = getWindow().getDecorView();
      decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
          decor.getViewTreeObserver().removeOnPreDrawListener(this);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
          {
            startPostponedEnterTransition();
          }
          return true;
        }
      });
    }

    images = getIntent().getStringArrayListExtra("images");
    page = getIntent().getIntExtra("page", 0);
    final ViewPager pager = (ViewPager) findViewById(R.id.pager);


    pager.setAdapter(new ImageAdapter());
    pager.setCurrentItem(page);
    pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      }

      @Override
      public void onPageSelected(int position) {

      }

      @Override
      public void onPageScrollStateChanged(int state) {

      }
    });

  }

  @Override
  public void onBackPressed() {
    supportFinishAfterTransition();
    super.onBackPressed();
  }

  private class ImageAdapter extends PagerAdapter {

    private LayoutInflater inflater;


    ImageAdapter() {

      inflater = LayoutInflater.from(getApplicationContext());
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

      container.removeView((View) object);
    }


    @Override
    public int getCount() {

      return images.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, final int position) {

      View imageLayout = inflater.inflate(R.layout.item_pager_gallery, view, false);

      toolbarView = imageLayout.findViewById(R.id.player_title);

      ImageButton backButton = (ImageButton) imageLayout.findViewById(R.id.backImageButton);
      backButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          supportFinishAfterTransition();
        }
      });

      ImageButton settingsMenu = (ImageButton) imageLayout.findViewById(R.id.playerMenuButton);
      settingsMenu.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          PopupMenu popup = new PopupMenu(getApplicationContext(), view);

          popup.getMenuInflater().inflate(R.menu.menu_player, popup.getMenu());
          popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
              switch (menuItem.getItemId())
              {
                case R.id.action_save_to_gallery:
                  PermissionListener dialogPermissionListener =
                    CustomPermissionDialog.Builder
                      .withContext(getApplicationContext())
                      .withTitle(R.string.permission_title)
                      .withMessage(R.string.permission_storage)
                      .withButtonText(android.R.string.ok)
                      .build();
                  PermissionListener basePermission = new PermissionListener() {

                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                      File source = new File(images.get(position));
                      String mFilePathInLocal = Constants.General.APP_FOLDER_IMAGE_PATH + source.getName();
                      File mediaFile = new File(mFilePathInLocal);

                      FileChannel src = null;
                      try
                      {
                        src = new FileInputStream(source).getChannel();

                        FileChannel dst = new FileOutputStream(mediaFile).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        refreshAndroidGallery(Uri.fromFile(mediaFile));
                        src.close();
                        dst.close();
                        Toast.makeText(getApplicationContext(), "Image saved to gallery", Toast.LENGTH_LONG).show();
                      } catch (IOException e)
                      {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Image not saved to gallery", Toast.LENGTH_LONG).show();
                      }

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                      token.continuePermissionRequest();

                    }
                  };
                  PermissionListener compositePermissionListener = new CompositePermissionListener(basePermission, dialogPermissionListener);
                  Dexter.withActivity(ActivityGalleryPager.this).
                    withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).
                    withListener(compositePermissionListener).
                    check();


                  break;
                case R.id.action_share:
                  Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                  sharingIntent.setType("image/*");
                  sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(images.get(position))));
                  startActivity(Intent.createChooser(sharingIntent, "Share image using"));
                  break;
                default:
                  break;
              }
              return false;
            }
          });
          popup.show();
        }
      });
      assert imageLayout != null;

      final TouchImageView imageView = (TouchImageView) imageLayout.findViewById(R.id.image_view_image);
      final ProgressView spinner = (ProgressView) imageLayout.findViewById(R.id.loading);
      ViewCompat.setTransitionName(imageView, "profile");

      imageView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
          if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
          {
            showToolbar();
          } else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
          {
            view.performClick();
          }
          return true;
        }
      });

      if (!images.get(position).equalsIgnoreCase(""))
      {
        if (!URLUtil.isNetworkUrl(images.get(position)))
        {
          Glide
            .with(getApplicationContext())

            .load(new File(images.get(position)))
            .placeholder(R.drawable.placeholder)
            .crossFade()
            .into(new SimpleTarget<GlideDrawable>() {
              @Override
              public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                imageView.setImageDrawable(resource);
                spinner.setVisibility(View.GONE);


              }
            });
        } else
        {
          MyApplication.getInstance().getImageLoader().loadImage(images.get(position), new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
              imageView.setImageBitmap(loadedImage);
              spinner.setVisibility(View.GONE);

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
          });
        }
      }


      view.addView(imageLayout, 0);
      return imageLayout;
    }

    private void showToolbar() {
      if (toolbarView.getVisibility() == View.VISIBLE)
      {
        toolbarView.setVisibility(View.GONE);
      } else
      {
        toolbarView.setVisibility(View.VISIBLE);
      }

    }

    @Override
    public boolean isViewFromObject(View view, Object object) {

      return view.equals(object);
    }


    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {

    }


    @Override
    public Parcelable saveState() {

      return null;
    }

    public void refreshAndroidGallery(Uri fileUri) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
      {
        Intent mediaScanIntent = new Intent(
          Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(fileUri);
        getApplicationContext().sendBroadcast(mediaScanIntent);
      } else
      {
        getApplicationContext().sendBroadcast(new Intent(
          Intent.ACTION_MEDIA_MOUNTED,
          Uri.parse("file://" + Environment.getExternalStorageDirectory())));
      }
    }
  }


}
