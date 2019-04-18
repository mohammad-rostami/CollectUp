package com.yalantis.ucrop.events;

import android.os.Bundle;

import com.yalantis.ucrop.model.MediaContent;
import com.yalantis.ucrop.util.AlbumEntry;
import com.yalantis.ucrop.util.ImageEntry;

import java.util.ArrayList;

/**
 *
 */
public final class Events {

    private Events() {

    }


    public final static class OnClickAlbumEvent {
        public final AlbumEntry albumEntry;

        public OnClickAlbumEvent(final AlbumEntry albumEntry) {
            this.albumEntry = albumEntry;
        }
    }

    public final static class OnPickImageEvent {
        public final ImageEntry imageEntry;

        public OnPickImageEvent(final ImageEntry imageEntry) {
            this.imageEntry = imageEntry;
        }
    }

    public final static class OnUnpickImageEvent {
        public final ImageEntry imageEntry;

        public OnUnpickImageEvent(final ImageEntry imageEntry) {
            this.imageEntry = imageEntry;
        }
    }


    public final static class OnAlbumsLoadedEvent {
        public final ArrayList<AlbumEntry> albumList;

        public OnAlbumsLoadedEvent(final ArrayList<AlbumEntry> albumList) {
            this.albumList = albumList;
        }
    }

    public final static class OnChangingDisplayedImageEvent {
        public final ImageEntry currentImage;

        public OnChangingDisplayedImageEvent(ImageEntry currentImage) {

            this.currentImage = currentImage;
        }
    }

    public final static class OnUpdateImagesThumbnailEvent {

        public OnUpdateImagesThumbnailEvent() {

        }
    }

    public final static class OnShowingToolbarEvent {
    }

    public final static class OnHidingToolbarEvent {
    }

    public final static class OnReloadAlbumsEvent {

    }


    public final static class OnCrop {

        public final Bundle bundle;
        public final int position;

        public OnCrop(Bundle bundle, int position) {
            this.bundle = bundle;
            this.position = position;
        }
    }

    public final static class OnImageEditResult {

        public final ArrayList<MediaContent> contents;

        public OnImageEditResult(ArrayList<MediaContent> contents) {
            this.contents = contents;
        }
    }

    public final static class OnDeselect {

        public final MediaContent content;

        public OnDeselect(MediaContent content) {
            this.content = content;
        }
    }

    public final static class newLimit {
        public final int ImageLimit;
        public final int VideoLimit;

        public newLimit(int imageLimit, int videoLimit) {
            ImageLimit = imageLimit;
            VideoLimit = videoLimit;
        }
    }

    public final static class onLimitChange {
        public final int ImageSelected;
        public final int VideoSelected;

        public onLimitChange(int imageSelected, int videoSelected) {
            ImageSelected = imageSelected;
            VideoSelected = videoSelected;
        }
    }

    public final static class OnMenuChange {

        public final boolean isVideo;

        public OnMenuChange(boolean isVideo) {
            this.isVideo = isVideo;
        }
    }

    public final static class updateContent {

        public final int pos;


        public updateContent(int pos) {
            this.pos = pos;
        }
    }

    public final static class ReBindData {

    }

    public final static class StopOnBack {

    }
}

