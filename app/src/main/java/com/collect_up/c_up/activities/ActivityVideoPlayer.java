
package com.collect_up.c_up.activities;


import android.Manifest;
import android.Manifest.permission;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.view.CustomPermissionDialog;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ActivityVideoPlayer extends Activity {


  private ImageButton settingsMenu;
  private View toolbarView;
  private String mFilePathInLocal;
  private ImageButton backButton;
  private SimpleExoPlayerView videoFrame;
  private Uri contentUri;
  final boolean[] isPlaying = {false};
  SimpleExoPlayer exoPlayer;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final SimpleExoPlayer player;

    setContentView(R.layout.activity_player);
    Intent intent = getIntent();
    contentUri = intent.getData();
    toolbarView = findViewById(R.id.player_title);


    videoFrame = (SimpleExoPlayerView) findViewById(R.id.videoView);


    player = ExoPlayerFactory.newSimpleInstance(this,
      new DefaultTrackSelector(new Handler()), new DefaultLoadControl());

    videoFrame.setUseController(true);
    videoFrame.setPlayer(player);
    videoFrame.setControllerVisibilityListener(new PlaybackControlView.VisibilityListener() {
      @Override
      public void onVisibilityChange(int visibility) {
        if (visibility == SimpleExoPlayerView.GONE)
        {
          toolbarView.setVisibility(View.INVISIBLE);
        } else
        {
          toolbarView.setVisibility(View.VISIBLE);
        }

      }
    });

    player.addListener(new ExoPlayer.EventListener() {
      @Override
      public void onLoadingChanged(boolean isLoading) {

      }

      @Override
      public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED)
        {
          player.seekTo(0);
          player.setPlayWhenReady(false);
          isPlaying[0] = false;
        } else
        {
          isPlaying[0] = true;
          exoPlayer = player;
        }
      }

      @Override
      public void onTimelineChanged(Timeline timeline, Object manifest) {

      }

      @Override
      public void onPlayerError(ExoPlaybackException error) {

      }

      @Override
      public void onPositionDiscontinuity() {

      }
    });


    playerPrepare(contentUri, player);


    backButton = (ImageButton) findViewById(R.id.backImageButton);
    backButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        onBackPressed();
      }
    });

    settingsMenu = (ImageButton) findViewById(R.id.playerMenuButton);
    settingsMenu.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        PopupMenu popup = new PopupMenu(getApplicationContext(), view);

        popup.getMenuInflater().inflate(R.menu.menu_player, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
          public File source;

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

                    source = new File(contentUri.getPath());

                    mFilePathInLocal = Constants.General.APP_FOLDER_VIDEO_PATH + source.getName();
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
                      Toast.makeText(getApplicationContext(), "video saved to gallery", Toast.LENGTH_LONG).show();
                    } catch (IOException e)
                    {
                      e.printStackTrace();
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
                Dexter.withActivity(ActivityVideoPlayer.this)
                  .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                  .withListener(compositePermissionListener)
                  .check();


                break;

              case R.id.action_share:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("video");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(sharingIntent, "Share video using"));
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

  }

  private void playerPrepare(Uri uri, SimpleExoPlayer player) {
    player.prepare(new ExtractorMediaSource(uri,
      new DefaultDataSourceFactory(this, "MOZILLA"),
      new DefaultExtractorsFactory(), new Handler(),
      new ExtractorMediaSource.EventListener() {
        @Override
        public void onLoadError(IOException error) {

        }
      }));
  }


  public void refreshAndroidGallery(Uri fileUri) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
    {
      Intent mediaScanIntent = new Intent(
        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
      mediaScanIntent.setData(fileUri);
      this.sendBroadcast(mediaScanIntent);
    } else
    {
      this.sendBroadcast(new Intent(
        Intent.ACTION_MEDIA_MOUNTED,
        Uri.parse("file://" + Environment.getExternalStorageDirectory())));
    }
  }


  private void releasePlayer() {
    if (exoPlayer != null)
    {
      exoPlayer.setPlayWhenReady(false);
      exoPlayer.seekTo(0);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    releasePlayer();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    releasePlayer();
    finish();
  }

}

