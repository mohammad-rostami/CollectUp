package com.yalantis.ucrop.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.yalantis.ucrop.R;
import com.yalantis.ucrop.events.Events;
import com.yalantis.ucrop.util.ImageEntry;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;


/**
 *
 */
public class AdapterViewPager extends PagerAdapter {

    ArrayList<ImageEntry> _Images;
    Context _Context;

    SimpleExoPlayer exoPlayer;
    ImageView playImage;

    public AdapterViewPager(Context context, ArrayList<ImageEntry> images) {
        EventBus.getDefault().register(this);
        _Images = images;
        _Context = context;
    }

    @Override

    public int getCount() {

        if (_Images != null) {
            return _Images.size();
        } else {
            return 0;
        }

    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        if (_Images.get(position).isVideo) {

            final boolean[] isPlaying = {false};

            final SimpleExoPlayer player;

            player = ExoPlayerFactory.newSimpleInstance(_Context,
                    new DefaultTrackSelector(new Handler()), new DefaultLoadControl());

            final SimpleExoPlayerView videoview = new SimpleExoPlayerView(_Context);
            videoview.setUseController(false);

            final Uri uri = Uri.parse(_Images.get(position).path);

            videoview.setPlayer(player);

            final ImageView imageView = new ImageView(_Context);
            imageView.setImageDrawable(ContextCompat.getDrawable(_Context, R.drawable.ic_play_arrow));
            imageView.setScaleType(ImageView.ScaleType.CENTER);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isPlaying[0] = playPause(isPlaying[0], player, imageView);
                }
            });

            videoview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isPlaying[0] = playPause(isPlaying[0], player, imageView);
                }
            });

            player.addListener(new ExoPlayer.EventListener() {
                @Override
                public void onLoadingChanged(boolean isLoading) {

                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    if (playbackState == ExoPlayer.STATE_ENDED) {
                        player.seekTo(0);
                        player.setPlayWhenReady(false);
                        isPlaying[0] = false;
                        imageView.setImageDrawable(ContextCompat.getDrawable(_Context, R.drawable.ic_play_arrow));
                        imageView.animate().alpha(1).start();
                    } else {
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

            videoview.addView(imageView);

            playerPrepare(uri, player);

            ((ViewPager) container).addView(videoview, 0);

            return videoview;

        } else {
            ImageView imageView = new ImageView(_Context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            Glide.with(_Context)
                    .load(_Images.get(position).path)
                    .asBitmap()
                    .into(imageView);
            ((ViewPager) container).addView(imageView, 0);

            return imageView;
        }


    }

    private boolean playPause(boolean isPlaying, SimpleExoPlayer player, ImageView imageView) {
        if (isPlaying) {
            player.setPlayWhenReady(false);
            exoPlayer = player;
            this.playImage = imageView;
            imageView.setImageDrawable(ContextCompat.getDrawable(_Context, R.drawable.ic_play_arrow));
            imageView.animate().alpha(1).start();
            return false;
        } else {
            player.setPlayWhenReady(true);
            imageView.setImageDrawable(ContextCompat.getDrawable(_Context, R.drawable.ic_pause_white_48dp));
            imageView.animate().alpha(0).start();
            return true;
        }
    }

    private void playerPrepare(Uri uri, SimpleExoPlayer player) {
        player.prepare(new ExtractorMediaSource(uri,
                new DefaultDataSourceFactory(_Context, "MOZILLA"),
                new DefaultExtractorsFactory(), new Handler(),
                new ExtractorMediaSource.EventListener() {
                    @Override
                    public void onLoadError(IOException error) {

                    }
                }));
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((ImageView) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((View) object);
    }

    @Subscribe
    public void onBackPressed (Events.StopOnBack stopOnBack) {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.seekTo(0);
            playImage.setImageDrawable(ContextCompat.getDrawable(_Context, R.drawable.ic_play_arrow));
            playImage.animate().alpha(1).start();
        }
    }
}
