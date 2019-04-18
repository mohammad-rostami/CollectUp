package com.yalantis.ucrop.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yalantis.ucrop.Picker;
import com.yalantis.ucrop.R;
import com.yalantis.ucrop.SmoothCheckBox;
import com.yalantis.ucrop.callback.onSelected;
import com.yalantis.ucrop.events.Events;
import com.yalantis.ucrop.model.MediaContent;
import com.yalantis.ucrop.util.Utility;
import com.yalantis.ucrop.view.CameraPreview;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 *
 */
public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int CAMERA_TYPE = 0;
    private static final int NORMAL_TYPE = 1;

    /*private static final String TAG = "ImageAdapter";*/
    private boolean MultipleSelect = true;

    private int VideoLimit = -1;
    private int ImageLimit = -1;

    private int ImageSelected = 0;
    private int VideoSelected = 0;

    private static int LastSelected = -1;
    private static SmoothCheckBox LastCheckbox = null;

    private onSelected onSelected;
    private ArrayList<MediaContent> mediaContents = new ArrayList<>();
    private Context context;

    private Picker.PickerBuilder pickerBuilder;

    public ImageAdapter(ArrayList<MediaContent> mediaContents, com.yalantis.ucrop.callback.onSelected onSelected) {
        this.mediaContents.clear();
        this.mediaContents = mediaContents;
        this.onSelected = onSelected;
        EventBus.getDefault().post(new Events.newLimit(ImageLimit, VideoLimit));
    }

    public ImageAdapter(ArrayList<MediaContent> mediaContents, int imageLimit,
                        int videoLimit, com.yalantis.ucrop.callback.onSelected onSelected) {
        this.mediaContents.clear();
        this.mediaContents = mediaContents;
        this.onSelected = onSelected;
        this.ImageLimit = imageLimit;
        this.VideoLimit = videoLimit;
        EventBus.getDefault().post(new Events.newLimit(ImageLimit, VideoLimit));
    }

    public ImageAdapter(ArrayList<MediaContent> mediaContents,
                        Picker.PickerBuilder pickerBuilder,
                        com.yalantis.ucrop.callback.onSelected onSelected) {
        this.mediaContents.clear();
        this.mediaContents = mediaContents;
        this.pickerBuilder = pickerBuilder;
        this.onSelected = onSelected;
        this.MultipleSelect = pickerBuilder.isMultiple();
        this.ImageLimit = pickerBuilder.getImageLimit();
        this.VideoLimit = pickerBuilder.getVideoLimit();
        EventBus.getDefault().post(new Events.newLimit(pickerBuilder.getImageLimit(), pickerBuilder.getVideoLimit()));
    }

    /*private PermissionListener CameraPermission;*/

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == NORMAL_TYPE) {
            View v = inflater.inflate(R.layout.ucrop_each_image, parent, false);
            onSelected.onSelect(-1, 0);
            return new NormalViewHolder(v);

        } else {
            View v = inflater.inflate(R.layout.ucrop_camera_row, parent, false);
            return new CameraViewHolder(v);
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final int itemType = getItemViewType(position);

        if (itemType == CAMERA_TYPE) {

            if (mediaContents.get(position).getType() == MediaContent.IS_IMAGE) {

                ((CameraViewHolder)holder).camera_icon.setImageDrawable(ContextCompat.getDrawable(context,
                        R.drawable.ic_camera_alt_white_48dp));

            }

            ((CameraViewHolder)holder).camera_laout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSelected.onSelect(0, 0);
                }
            });


        } else {
            final int p = position;

            final NormalViewHolder nh = (NormalViewHolder) holder;

            nh.bindViews(mediaContents.get(position));

            /*if (LastImageView == nh.imageView) {
                nh.checked.setVisibility(View.VISIBLE);
            }*/

            nh.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!MultipleSelect) {

                        if (LastSelected == -1) {
                            doSelect(nh, p);
                        } else if (LastSelected == p) {
                            mediaContents.get(p).setChecked(false);
                            nh.checked.setChecked(false, true, 1);
                            onSelected.onDeSelect(mediaContents.get(p));
                            LastSelected = -1;
                            LastCheckbox = null;
                        } else {

                            //deselect last
                            mediaContents.get(LastSelected).setChecked(false);
                            LastCheckbox.setChecked(false, false, 1);
                            onSelected.onDeSelect(mediaContents.get(LastSelected));

                            //select new one
                            doSelect(nh, p);

                        }
                    } else {
                        if (mediaContents.get(p).isChecked()) {
                            mediaContents.get(p).setChecked(false);
                            nh.checked.setChecked(false, true, 1);
                            onSelected.onDeSelect(mediaContents.get(p));
                            if (mediaContents.get(p).getType() == MediaContent.IS_IMAGE) {
                                ImageSelected --;
                                EventBus.getDefault().post(new Events.onLimitChange(ImageSelected, -1));
                            } else {
                                VideoSelected --;
                                EventBus.getDefault().post(new Events.onLimitChange(-1, VideoSelected));
                            }
                        } else {

                            if (mediaContents.get(p).getType() == MediaContent.IS_IMAGE) {

                                if (pickerBuilder.getMinimum() != -1 &&
                                        pickerBuilder.getMinimum() >= (VideoSelected+ImageSelected)) {

                                    if (ImageLimit == -1 ) {
                                        ImageSelected++;
                                        doSelect(nh, p);
                                    } else {
                                        if (ImageSelected < ImageLimit) {
                                            ImageSelected ++;
                                            doSelect(nh, p);
                                        }
                                    }
                                } else if (pickerBuilder.getMinimum() == -1) {
                                    if (ImageLimit == -1 ) {
                                        ImageSelected++;
                                        doSelect(nh, p);
                                    } else {
                                        if (ImageSelected < ImageLimit) {
                                            ImageSelected ++;
                                            doSelect(nh, p);
                                        }
                                    }
                                }

                            } else {

                                if (pickerBuilder.getMinimum() != -1 && pickerBuilder.getMinimum() >= (VideoSelected+ImageSelected)) {
                                    if (VideoLimit == -1) {
                                        VideoSelected++;
                                        doSelect(nh, p);
                                    } else {
                                        if (VideoSelected < VideoLimit) {
                                            VideoSelected ++;
                                            doSelect(nh, p);
                                        }
                                    }
                                } else if (pickerBuilder.getMinimum() == -1) {
                                    if (VideoLimit == -1) {
                                        VideoSelected++;
                                        doSelect(nh, p);
                                    } else {
                                        if (VideoSelected < VideoLimit) {
                                            VideoSelected ++;
                                            doSelect(nh, p);
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            });

            nh.checked.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {

                    if (MultipleSelect) {

                        if (mediaContents.get(p).getType() == MediaContent.IS_IMAGE) {
                            if (ImageLimit == -1) {

                                mediaContents.get(p).setChecked(isChecked);
                                if (isChecked) {
                                    onSelected.onSelect(p, mediaContents.get(p).getType());
                                } else {
                                    onSelected.onDeSelect(mediaContents.get(p));
                                }

                            } else {
                                if (ImageSelected < ImageLimit) {
                                    mediaContents.get(p).setChecked(isChecked);
                                    if (isChecked) {
                                        onSelected.onSelect(p, mediaContents.get(p).getType());
                                        ImageSelected ++;
                                        EventBus.getDefault().post(new Events.onLimitChange(ImageSelected, -1));
                                    } else {
                                        onSelected.onDeSelect(mediaContents.get(p));
                                        ImageSelected --;
                                        EventBus.getDefault().post(new Events.onLimitChange(ImageSelected, -1));
                                    }
                                } else {
                                    if (!isChecked) {
                                        mediaContents.get(p).setChecked(false);
                                        onSelected.onDeSelect(mediaContents.get(p));
                                        ImageSelected --;
                                        EventBus.getDefault().post(new Events.onLimitChange(ImageSelected, -1));
                                    } else {
                                        nh.checked.setChecked(false, false, 1);
                                    }
                                }
                            }
                        } else {
                            if (VideoLimit == -1) {

                                mediaContents.get(p).setChecked(isChecked);
                                if (isChecked) {
                                    onSelected.onSelect(p, mediaContents.get(p).getType());
                                } else {
                                    onSelected.onDeSelect(mediaContents.get(p));
                                }

                            } else {
                                if (VideoSelected < VideoLimit) {
                                    mediaContents.get(p).setChecked(isChecked);
                                    if (isChecked) {
                                        onSelected.onSelect(p, mediaContents.get(p).getType());
                                        VideoSelected ++;
                                        EventBus.getDefault().post(new Events.onLimitChange(-1, VideoSelected));
                                    } else {
                                        onSelected.onDeSelect(mediaContents.get(p));
                                        VideoSelected --;
                                        EventBus.getDefault().post(new Events.onLimitChange(-1, VideoSelected));
                                    }
                                } else {
                                    if (!isChecked) {
                                        mediaContents.get(p).setChecked(false);
                                        onSelected.onDeSelect(mediaContents.get(p));
                                        VideoSelected --;
                                        EventBus.getDefault().post(new Events.onLimitChange(-1, VideoSelected));
                                    } else {
                                        nh.checked.setChecked(false, false, 1);
                                    }
                                }
                            }
                        }

                    } else {

                        if (isChecked) {

                            if (LastSelected == -1) {
                                mediaContents.get(p).setChecked(true);
                                LastCheckbox = nh.checked;
                                LastSelected = p;
                                onSelected.onSelect(p, mediaContents.get(p).getType());
                            } else {
                                mediaContents.get(LastSelected).setChecked(false);
                                LastCheckbox.setChecked(false, false, 1);
                                onSelected.onDeSelect(mediaContents.get(LastSelected));
                                LastSelected = p;
                                LastCheckbox = nh.checked;

                                //select new one
                                mediaContents.get(p).setChecked(true);
                                onSelected.onSelect(p, mediaContents.get(p).getType());
                            }

                        } else {

                            if (LastSelected == p) {
                                LastCheckbox.setChecked(false, false, 1);
                                LastCheckbox = null;
                                LastSelected = -1;
                                mediaContents.get(p).setChecked(false);
                                onSelected.onDeSelect(mediaContents.get(p));
                            }
                        }
                    }
                }
            });
        }


    }

    @Override
    public int getItemViewType(int position) {
        if (mediaContents.get(position).isCamera()){
            return CAMERA_TYPE;
        } else {
            return NORMAL_TYPE;
        }
    }

    private void doSelect(NormalViewHolder holder, int p) {
        /*LastImageView = holder.imageView;*/

        mediaContents.get(p).setChecked(true);
        holder.checked.setChecked(true, true, 1);
        onSelected.onSelect(p, mediaContents.get(p).getType());
        LastSelected = p;
        LastCheckbox = holder.checked;
        if (mediaContents.get(p).getType() == MediaContent.IS_IMAGE) {
            EventBus.getDefault().post(new Events.onLimitChange(ImageSelected, -1));
        } else {
            EventBus.getDefault().post(new Events.onLimitChange(-1, VideoSelected));
        }

    }

    @Override
    public int getItemCount() {
        if (mediaContents.isEmpty()) {
            return 0;
        } else {
            return mediaContents.size();
        }
    }

    private void LoadImage(AppCompatImageView imageView, String path) {

        Glide.with(context).load(path).into(imageView);

    }

    private class NormalViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView imageView;
        TextView Video_time;
        SmoothCheckBox checked;

        NormalViewHolder(View itemView) {
            super(itemView);
            imageView = (AppCompatImageView) itemView.findViewById(R.id.each_image_view);
            Video_time = (TextView) itemView.findViewById(R.id.Video_time);
            checked = (SmoothCheckBox) itemView.findViewById(R.id.each_checkbox);
            Video_time.setVisibility(View.GONE);
        }

        void bindViews(MediaContent mediaContent) {

            Video_time.setVisibility(View.GONE);

            LoadImage(imageView, mediaContent.getUri().getPath());

            checked.setChecked(mediaContent.isChecked(), 1);

            if (mediaContent.getType() == MediaContent.IS_VIDEO) {
                String time = Utility.getTime(mediaContent.getTime());
                Video_time.setText(time);
                Video_time.setVisibility(View.VISIBLE);
            }

        }
    }

    private class CameraViewHolder extends RecyclerView.ViewHolder {

        ImageView camera_icon;
        RelativeLayout camera_laout;

        CameraViewHolder(View itemView) {
            super(itemView);

            camera_icon = (ImageView) itemView.findViewById(R.id.camera_icon);
            camera_laout = (RelativeLayout) itemView.findViewById(R.id.camera_relative);
        }
    }


}
