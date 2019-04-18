package com.yalantis.ucrop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yalantis.ucrop.adapter.FilterAdapter;
import com.yalantis.ucrop.callback.BitmapCropCallback;
import com.yalantis.ucrop.callback.BitmapLoadCallback;
import com.yalantis.ucrop.callback.onFilterClickListener;
import com.yalantis.ucrop.callback.onUCropFinished;
import com.yalantis.ucrop.model.AspectRatio;
import com.yalantis.ucrop.model.ExifInfo;
import com.yalantis.ucrop.model.Filters;
import com.yalantis.ucrop.util.RoundBitmap;
import com.yalantis.ucrop.util.SelectedStateListDrawable;
import com.yalantis.ucrop.util.Utility;
import com.yalantis.ucrop.view.CropImageView;
import com.yalantis.ucrop.view.GestureCropImageView;
import com.yalantis.ucrop.view.OverlayView;
import com.yalantis.ucrop.view.TransformImageView;
import com.yalantis.ucrop.view.UCropView;
import com.yalantis.ucrop.view.widget.AspectRatioTextView;
import com.yalantis.ucrop.view.widget.HorizontalProgressWheelView;
import com.zomato.photofilters.SampleFilters;
import com.zomato.photofilters.geometry.Point;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.ToneCurveSubfilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 */

@SuppressWarnings ("ConstantConditions")
public class UCropFragment extends Fragment {

  public static final int DEFAULT_COMPRESS_QUALITY = 90;
  public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
  public static final int NONE = 0;
  public static final int SCALE = 1;
  public static final int ROTATE = 2;
  public static final int ALL = 3;
  private static final String TAG = "UCropFragment";
  private static final int TABS_COUNT = 3;
  private static final int SCALE_WIDGET_SENSITIVITY_COEFFICIENT = 15000;
  private static final int ROTATE_WIDGET_SENSITIVITY_COEFFICIENT = 42;
  private static final String EXTRA_PREFIX = BuildConfig.APPLICATION_ID;
  public static final String EXTRA_INPUT_URI = EXTRA_PREFIX + ".InputUri";
  public static final String EXTRA_OUTPUT_URI = EXTRA_PREFIX + ".OutputUri";

  static
  {
    System.loadLibrary("NativeImageProcessor");
  }

  View v;
  Bundle bundle;
  private String mToolbarTitle;
  // Enables dynamic coloring
  private int mToolbarColor;
  private int mStatusBarColor;
  private int mActiveWidgetColor;
  private int mToolbarWidgetColor;
  private int mLogoColor;
  private boolean mShowBottomControls;
  private boolean mShowLoader = true;
  private UCropView mUCropView;
  private GestureCropImageView mGestureCropImageView;
  private OverlayView mOverlayView;
  // buttons view group
  private ViewGroup mWrapperStateAspectRatio, mWrapperStateRotate,
    mWrapperStateScale, mWrapperStateFilter;
  private ViewGroup mLayoutAspectRatio, mLayoutRotate, mLayoutScale, mLayoutFilter;
  private List<ViewGroup> mCropAspectRatioViews = new ArrayList<>();
  private TextView mTextViewRotateAngle, mTextViewScalePercent;
  private View mBlockingView;
  private RecyclerView mRecyclerFilter;
  private Bitmap.CompressFormat mCompressFormat = DEFAULT_COMPRESS_FORMAT;
  private int mCompressQuality = DEFAULT_COMPRESS_QUALITY;
  private int[] mAllowedGestures = new int[] {SCALE, ROTATE, ALL};
  private final View.OnClickListener mStateClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      if (!v.isSelected())
      {
        setWidgetState(v.getId());
      }
    }
  };
  //basic bitmap for filter
  private Bitmap bitmap;
  private ArrayList<Filters> models;
  private FilterAdapter adapter;
  private Intent intent = null;
  private onUCropFinished cropFinished;
  public static boolean isVisible;

  private TransformImageView.TransformImageListener mImageListener = new TransformImageView.TransformImageListener() {
    @Override
    public void onRotate(float currentAngle) {
      setAngleText(currentAngle);
    }

    @Override
    public void onScale(float currentScale) {
      setScaleText(currentScale);
    }

    @Override
    public void onLoadComplete() {
      mUCropView.animate().alpha(1).setDuration(300).setInterpolator(new AccelerateInterpolator());
      mBlockingView.setClickable(false);
      mShowLoader = false;
            /*supportInvalidateOptionsMenu();*/
    }

    @Override
    public void onLoadFailure(@NonNull Exception e) {
      setResultError(e);
            /*finish();*/
    }

  };


  public UCropFragment() {
    super();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    if (v != null)
    {
      ViewGroup parent = (ViewGroup) v.getRootView();
      if (parent != null)
      {
        parent.removeView(mOverlayView);
        parent.removeView(mBlockingView);
        Toolbar mainToolbar = (Toolbar) getActivity().findViewById(R.id.firstToolbar);
        mainToolbar.setVisibility(View.GONE);
      }
    }

    v = inflater.inflate(R.layout.ucrop_activity_photobox, container, false);

    final Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
    toolbar.inflateMenu(R.menu.ucrop_menu_activity);

    Toolbar mainToolbar = (Toolbar) getActivity().findViewById(R.id.firstToolbar);
    mainToolbar.setVisibility(View.GONE);

    toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        cropAndSaveImage();
        item.setEnabled(false);
        isVisible = false;
        toolbar.setNavigationIcon(null);

        return false;
      }
    });

    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getFragmentManager().popBackStack();
        Toolbar mainToolbar = (Toolbar) getActivity().findViewById(R.id.firstToolbar);
        mainToolbar.setVisibility(View.VISIBLE);
      }
    });


    cropFinished = (onUCropFinished) getActivity();


    bundle = getArguments();
    intent = new Intent();
    intent.putExtra(EXTRA_INPUT_URI, bundle.getParcelable("input"));
    intent.putExtra(EXTRA_OUTPUT_URI, bundle.getParcelable("output"));

    setupViews(intent);
    setImageData(intent);
    setInitialState();
    addBlockingView();

    return v;
  }

  public void getBitmapFromUri(Uri uri) {

    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
    bitmap = BitmapFactory.decodeFile(uri.getPath(), bmOptions);

    if (bitmap == null)
    {
      try
      {
        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
      } catch (IOException e)
      {
        e.printStackTrace();
      }
    }

    if (bitmap != null)
    {
      Log.d(TAG, "getBitmapFromUri: bitmap not null");

      createFilterData();

    } else
    {
      Log.d(TAG, "getBitmapFromUri: bitmap null");
    }

  }

  private void createFilterData() {

    Filter Toaster = new Filter();
    Point[] Toasterred = new Point[6];
    Toasterred[0] = new Point(0, 120);
    Toasterred[1] = new Point(50, 160);
    Toasterred[2] = new Point(105, 198);
    Toasterred[3] = new Point(145, 215);
    Toasterred[4] = new Point(190, 230);
    Toasterred[5] = new Point(255, 255);

    Point[] Toastergreen = new Point[4];
    Toastergreen[0] = new Point(0, 0);
    Toastergreen[1] = new Point(22, 60);
    Toastergreen[2] = new Point(125, 180);
    Toastergreen[3] = new Point(255, 255);

    Point[] Toasterblue = new Point[6];
    Toasterblue[0] = new Point(0, 50);
    Toasterblue[1] = new Point(40, 60);
    Toasterblue[2] = new Point(80, 102);
    Toasterblue[3] = new Point(122, 148);
    Toasterblue[4] = new Point(185, 185);
    Toasterblue[5] = new Point(255, 210);

    Toaster.addSubFilter(new ToneCurveSubfilter(null, Toasterred, Toastergreen, Toasterblue));

    Filter Amaro = new Filter();
    Point[] AmaroRed = new Point[5];
    AmaroRed[0] = new Point(0, 19);
    AmaroRed[1] = new Point(30, 62);
    AmaroRed[2] = new Point(82, 148);
    AmaroRed[3] = new Point(128, 88);
    AmaroRed[4] = new Point(145, 200);

    Point[] AmaroGreen = new Point[6];
    AmaroGreen[0] = new Point(0, 0);
    AmaroGreen[1] = new Point(48, 72);
    AmaroGreen[2] = new Point(115, 188);
    AmaroGreen[3] = new Point(160, 120);
    AmaroGreen[4] = new Point(233, 245);
    AmaroGreen[5] = new Point(255, 255);

    Point[] AmaroBlue = new Point[7];
    AmaroBlue[0] = new Point(0, 25);
    AmaroBlue[1] = new Point(35, 80);
    AmaroBlue[2] = new Point(106, 75);
    AmaroBlue[3] = new Point(151, 181);
    AmaroBlue[4] = new Point(215, 215);
    AmaroBlue[5] = new Point(240, 235);
    AmaroBlue[6] = new Point(255, 245);

    Amaro.addSubFilter(new ToneCurveSubfilter(null, AmaroRed, AmaroGreen, AmaroBlue));

    Filter Nashville = new Filter();
    Point[] NashvilleRed = new Point[7];
    NashvilleRed[0] = new Point(0, 19);
    NashvilleRed[1] = new Point(30, 5);
    NashvilleRed[2] = new Point(58, 25);
    NashvilleRed[3] = new Point(83, 85);
    NashvilleRed[4] = new Point(112, 140);
    NashvilleRed[5] = new Point(190, 120);
    NashvilleRed[6] = new Point(255, 255);

    Point[] NashvilleGreen = new Point[6];
    NashvilleGreen[0] = new Point(0, 0);
    NashvilleGreen[1] = new Point(20, 5);
    NashvilleGreen[2] = new Point(50, 62);
    NashvilleGreen[3] = new Point(132, 150);
    NashvilleGreen[4] = new Point(190, 205);
    NashvilleGreen[5] = new Point(255, 225);

    Point[] NashvilleBlue = new Point[5];
    NashvilleBlue[0] = new Point(0, 65);
    NashvilleBlue[1] = new Point(40, 90);
    NashvilleBlue[2] = new Point(85, 115);
    NashvilleBlue[3] = new Point(212, 185);
    NashvilleBlue[4] = new Point(255, 205);

    Nashville.addSubFilter(new ToneCurveSubfilter(null, NashvilleRed, NashvilleGreen, NashvilleBlue));

    Filter f1977 = new Filter();
    Point[] f1977Red = new Point[5];
    f1977Red[0] = new Point(0, 75);
    f1977Red[1] = new Point(75, 125);
    f1977Red[2] = new Point(145, 200);
    f1977Red[3] = new Point(190, 220);
    f1977Red[4] = new Point(255, 230);

    Point[] f1977Green = new Point[6];
    f1977Green[0] = new Point(0, 52);
    f1977Green[1] = new Point(42, 54);
    f1977Green[2] = new Point(110, 120);
    f1977Green[3] = new Point(154, 168);
    f1977Green[4] = new Point(232, 235);
    f1977Green[5] = new Point(255, 242);

    Point[] f1977Blue = new Point[6];
    f1977Blue[0] = new Point(0, 62);
    f1977Blue[1] = new Point(65, 82);
    f1977Blue[2] = new Point(108, 132);
    f1977Blue[3] = new Point(175, 210);
    f1977Blue[4] = new Point(210, 208);
    f1977Blue[5] = new Point(255, 208);

    f1977.addSubFilter(new ToneCurveSubfilter(null, f1977Red, f1977Green, f1977Blue));

    Filter Hefe = new Filter();
    Point[] HefeRed = new Point[4];
    HefeRed[0] = new Point(0, 0);
    HefeRed[1] = new Point(60, 55);
    HefeRed[2] = new Point(130, 155);
    HefeRed[3] = new Point(255, 255);

    Point[] HefeGreen = new Point[4];
    HefeGreen[0] = new Point(0, 0);
    HefeGreen[1] = new Point(65, 40);
    HefeGreen[2] = new Point(125, 125);
    HefeGreen[3] = new Point(255, 255);

    Point[] HefeBlue = new Point[5];
    HefeBlue[0] = new Point(0, 0);
    HefeBlue[1] = new Point(65, 30);
    HefeBlue[2] = new Point(125, 105);
    HefeBlue[3] = new Point(170, 165);
    HefeBlue[4] = new Point(255, 240);

    Hefe.addSubFilter(new ToneCurveSubfilter(null, HefeRed, HefeGreen, HefeBlue));

    Filter Walden = new Filter();
    Point[] WaldenRed = new Point[6];
    WaldenRed[0] = new Point(0, 12);
    WaldenRed[1] = new Point(40, 44);
    WaldenRed[2] = new Point(85, 125);
    WaldenRed[3] = new Point(122, 180);
    WaldenRed[4] = new Point(170, 220);
    WaldenRed[5] = new Point(255, 255);

    Point[] WaldenGreen = new Point[6];
    WaldenGreen[0] = new Point(0, 35);
    WaldenGreen[1] = new Point(40, 78);
    WaldenGreen[2] = new Point(90, 140);
    WaldenGreen[3] = new Point(130, 188);
    WaldenGreen[4] = new Point(175, 215);
    WaldenGreen[5] = new Point(255, 245);

    Point[] WaldenBlue = new Point[5];
    WaldenBlue[0] = new Point(0, 85);
    WaldenBlue[1] = new Point(85, 150);
    WaldenBlue[2] = new Point(130, 170);
    WaldenBlue[3] = new Point(165, 185);
    WaldenBlue[4] = new Point(255, 220);

    Walden.addSubFilter(new ToneCurveSubfilter(null, WaldenRed, WaldenGreen, WaldenBlue));

    Filter Brannan = new Filter();
    Point[] BrannanRed = new Point[5];
    BrannanRed[0] = new Point(0, 35);
    BrannanRed[1] = new Point(40, 50);
    BrannanRed[2] = new Point(125, 165);
    BrannanRed[3] = new Point(175, 230);
    BrannanRed[4] = new Point(255, 255);

    Point[] BrannanGreen = new Point[5];
    BrannanGreen[0] = new Point(0, 0);
    BrannanGreen[1] = new Point(65, 50);
    BrannanGreen[2] = new Point(92, 102);
    BrannanGreen[3] = new Point(180, 220);
    BrannanGreen[4] = new Point(255, 255);

    Point[] BrannanBlue = new Point[6];
    BrannanBlue[0] = new Point(0, 35);
    BrannanBlue[1] = new Point(62, 62);
    BrannanBlue[2] = new Point(88, 95);
    BrannanBlue[3] = new Point(132, 158);
    BrannanBlue[4] = new Point(225, 230);
    BrannanBlue[5] = new Point(255, 232);

    Brannan.addSubFilter(new ToneCurveSubfilter(null, BrannanRed, BrannanGreen, BrannanBlue));

    Filter Sutro = new Filter();
    Point[] SutroRed = new Point[6];
    SutroRed[0] = new Point(0, 0);
    SutroRed[1] = new Point(40, 35);
    SutroRed[2] = new Point(90, 92);
    SutroRed[3] = new Point(145, 155);
    SutroRed[4] = new Point(235, 230);
    SutroRed[5] = new Point(255, 235);

    Point[] SutroGreen = new Point[5];
    SutroGreen[0] = new Point(0, 0);
    SutroGreen[1] = new Point(62, 50);
    SutroGreen[2] = new Point(155, 140);
    SutroGreen[3] = new Point(210, 188);
    SutroGreen[4] = new Point(255, 225);

    Point[] SutroBlue = new Point[5];
    SutroBlue[0] = new Point(0, 0);
    SutroBlue[1] = new Point(80, 80);
    SutroBlue[2] = new Point(128, 112);
    SutroBlue[3] = new Point(182, 145);
    SutroBlue[4] = new Point(255, 220);

    Sutro.addSubFilter(new ToneCurveSubfilter(null, SutroRed, SutroGreen, SutroBlue));

    Filter Earlybird = new Filter();
    Point[] EarlybirdRed = new Point[6];
    EarlybirdRed[0] = new Point(0, 25);
    EarlybirdRed[1] = new Point(45, 80);
    EarlybirdRed[2] = new Point(85, 135);
    EarlybirdRed[3] = new Point(120, 180);
    EarlybirdRed[4] = new Point(230, 240);
    EarlybirdRed[5] = new Point(255, 255);

    Point[] EarlybirdGreen = new Point[7];
    EarlybirdGreen[0] = new Point(0, 0);
    EarlybirdGreen[1] = new Point(40, 55);
    EarlybirdGreen[2] = new Point(88, 112);
    EarlybirdGreen[3] = new Point(132, 175);
    EarlybirdGreen[4] = new Point(168, 198);
    EarlybirdGreen[5] = new Point(215, 218);
    EarlybirdGreen[6] = new Point(255, 240);

    Point[] EarlybirdBlue = new Point[7];
    EarlybirdBlue[0] = new Point(0, 18);
    EarlybirdBlue[1] = new Point(42, 58);
    EarlybirdBlue[2] = new Point(90, 102);
    EarlybirdBlue[3] = new Point(120, 130);
    EarlybirdBlue[4] = new Point(164, 170);
    EarlybirdBlue[5] = new Point(212, 195);
    EarlybirdBlue[6] = new Point(255, 210);

    Earlybird.addSubFilter(new ToneCurveSubfilter(null, EarlybirdRed, EarlybirdGreen, EarlybirdBlue));

    Filter Lo_Fi = new Filter();
    Point[] Lo_FiRed = new Point[7];
    Lo_FiRed[0] = new Point(0, 0);
    Lo_FiRed[1] = new Point(40, 20);
    Lo_FiRed[2] = new Point(88, 80);
    Lo_FiRed[3] = new Point(125, 150);
    Lo_FiRed[4] = new Point(170, 200);
    Lo_FiRed[5] = new Point(230, 245);
    Lo_FiRed[6] = new Point(255, 255);

    Point[] Lo_FiGreen = new Point[7];
    Lo_FiGreen[0] = new Point(0, 0);
    Lo_FiGreen[1] = new Point(30, 15);
    Lo_FiGreen[2] = new Point(90, 70);
    Lo_FiGreen[3] = new Point(105, 105);
    Lo_FiGreen[4] = new Point(148, 180);
    Lo_FiGreen[5] = new Point(188, 218);
    Lo_FiGreen[6] = new Point(255, 255);

    Point[] Lo_FiBlue = new Point[7];
    Lo_FiBlue[0] = new Point(0, 0);
    Lo_FiBlue[1] = new Point(62, 50);
    Lo_FiBlue[2] = new Point(100, 95);
    Lo_FiBlue[3] = new Point(130, 155);
    Lo_FiBlue[4] = new Point(150, 182);
    Lo_FiBlue[5] = new Point(190, 220);
    Lo_FiBlue[6] = new Point(255, 255);

    Lo_Fi.addSubFilter(new ToneCurveSubfilter(null, Lo_FiRed, Lo_FiGreen, Lo_FiBlue));

    Filter Sierra = new Filter();
    Point[] SierraRed = new Point[7];
    SierraRed[0] = new Point(0, 10);
    SierraRed[1] = new Point(48, 88);
    SierraRed[2] = new Point(105, 155);
    SierraRed[3] = new Point(130, 180);
    SierraRed[4] = new Point(190, 212);
    SierraRed[5] = new Point(232, 234);
    SierraRed[6] = new Point(255, 245);

    Point[] SierraGreen = new Point[7];
    SierraGreen[0] = new Point(0, 0);
    SierraGreen[1] = new Point(38, 72);
    SierraGreen[2] = new Point(85, 124);
    SierraGreen[3] = new Point(124, 160);
    SierraGreen[4] = new Point(172, 186);
    SierraGreen[5] = new Point(218, 210);
    SierraGreen[6] = new Point(255, 230);

    Point[] SierraBlue = new Point[7];
    SierraBlue[0] = new Point(0, 30);
    SierraBlue[1] = new Point(45, 82);
    SierraBlue[2] = new Point(95, 132);
    SierraBlue[3] = new Point(136, 164);
    SierraBlue[4] = new Point(176, 182);
    SierraBlue[5] = new Point(210, 200);
    SierraBlue[6] = new Point(255, 218);

    Sierra.addSubFilter(new ToneCurveSubfilter(null, SierraRed, SierraGreen, SierraBlue));

    Filter InkWell = new Filter();
    InkWell.addSubFilter(new SaturationSubfilter(-50));

    Filter BlueMessFilter = SampleFilters.getBlueMessFilter();
    Filter LimeStutterFilter = SampleFilters.getLimeStutterFilter();
    Filter NightWhisperFilter = SampleFilters.getNightWhisperFilter();
    Filter StarLitFilter = SampleFilters.getStarLitFilter();
    Filter AweStruckVibeFilter = SampleFilters.getAweStruckVibeFilter();

    Bitmap bitmap = RoundBitmap.getRoundedCornerBitmap(Utility.scaleCenterCrop(this.bitmap, 150, 150), 10);

    models.add(new Filters(bitmap, "Normal", null));
    models.add(new Filters(bitmap, "Blue Mess", BlueMessFilter));
    models.add(new Filters(bitmap, "Lime", LimeStutterFilter));
    models.add(new Filters(bitmap, "Whisper", NightWhisperFilter));
    models.add(new Filters(bitmap, "Star Lit", StarLitFilter));
    models.add(new Filters(bitmap, "AweStruck", AweStruckVibeFilter));
    models.add(new Filters(bitmap, "Toaster", Toaster));
    models.add(new Filters(bitmap, "Amaro", Amaro));
    models.add(new Filters(bitmap, "InkWell", InkWell));
    models.add(new Filters(bitmap, "Nashville", Nashville));
    models.add(new Filters(bitmap, "1977", f1977));
    models.add(new Filters(bitmap, "Hefe", Hefe));
    models.add(new Filters(bitmap, "Walden", Walden));
    models.add(new Filters(bitmap, "Brannan", Brannan));
    models.add(new Filters(bitmap, "Sutro", Sutro));
    models.add(new Filters(bitmap, "Earlybird", Earlybird));
    models.add(new Filters(bitmap, "Lo-Fi", Lo_Fi));
    models.add(new Filters(bitmap, "Sierra", Sierra));

  }

  /**
   * This method extracts all data from the incoming intent and setups views properly.
   */
  private void setImageData(@NonNull Intent intent) {
    Uri inputUri = intent.getParcelableExtra(UCrop.EXTRA_INPUT_URI);
    Uri outputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
    processOptions(intent);

    if (inputUri != null && outputUri != null)
    {
      try
      {
        mGestureCropImageView.setImageUri(inputUri, outputUri, new BitmapLoadCallback() {
          @Override
          public void onBitmapLoaded(@NonNull Bitmap bitmap, @NonNull ExifInfo exifInfo, @NonNull String imageInputPath, @Nullable String imageOutputPath) {
            //getBitmapFromUri(inputUri);
            UCropFragment.this.bitmap = bitmap;
            createFilterData();

          }

          @Override
          public void onFailure(@NonNull Exception bitmapWorkerException) {

          }
        });
      } catch (Exception e)
      {
        setResultError(e);
                /*finish();*/
      }
    } else
    {
      setResultError(new NullPointerException(getString(R.string.ucrop_error_input_data_is_absent)));
            /*finish();*/
    }
  }

  /**
   * This method extracts {@link com.yalantis.ucrop.UCrop.Options #optionsBundle} from incoming intent
   * and setups Activity, {@link OverlayView} and {@link CropImageView} properly.
   */
  @SuppressWarnings ("deprecation")
  private void processOptions(@NonNull Intent intent) {
    // Bitmap compression options
    String compressionFormatName = intent.getStringExtra(UCrop.Options.EXTRA_COMPRESSION_FORMAT_NAME);
    Bitmap.CompressFormat compressFormat = null;
    if (!TextUtils.isEmpty(compressionFormatName))
    {
      compressFormat = Bitmap.CompressFormat.valueOf(compressionFormatName);
    }
    mCompressFormat = (compressFormat == null) ? DEFAULT_COMPRESS_FORMAT : compressFormat;

    mCompressQuality = intent.getIntExtra(UCrop.Options.EXTRA_COMPRESSION_QUALITY, UCropFragment.DEFAULT_COMPRESS_QUALITY);

    // Gestures options
    int[] allowedGestures = intent.getIntArrayExtra(UCrop.Options.EXTRA_ALLOWED_GESTURES);
    if (allowedGestures != null && allowedGestures.length == TABS_COUNT)
    {
      mAllowedGestures = allowedGestures;
    }

    // Crop image view options
    mGestureCropImageView.setMaxBitmapSize(intent.getIntExtra(UCrop.Options.EXTRA_MAX_BITMAP_SIZE, CropImageView.DEFAULT_MAX_BITMAP_SIZE));
    mGestureCropImageView.setMaxScaleMultiplier(intent.getFloatExtra(UCrop.Options.EXTRA_MAX_SCALE_MULTIPLIER, CropImageView.DEFAULT_MAX_SCALE_MULTIPLIER));
    mGestureCropImageView.setImageToWrapCropBoundsAnimDuration(intent.getIntExtra(UCrop.Options.EXTRA_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION, CropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION));


    // Overlay view options
    mOverlayView.setFreestyleCropEnabled(bundle.getBoolean(UCrop.Options.EXTRA_FREE_STYLE_CROP, OverlayView.DEFAULT_FREESTYLE_CROP_ENABLED));

    mOverlayView.setDimmedColor(intent.getIntExtra(UCrop.Options.EXTRA_DIMMED_LAYER_COLOR, getResources().getColor(R.color.ucrop_color_default_dimmed)));
    mOverlayView.setCircleDimmedLayer(intent.getBooleanExtra(UCrop.Options.EXTRA_CIRCLE_DIMMED_LAYER, OverlayView.DEFAULT_CIRCLE_DIMMED_LAYER));

    mOverlayView.setShowCropFrame(intent.getBooleanExtra(UCrop.Options.EXTRA_SHOW_CROP_FRAME, OverlayView.DEFAULT_SHOW_CROP_FRAME));
    mOverlayView.setCropFrameColor(intent.getIntExtra(UCrop.Options.EXTRA_CROP_FRAME_COLOR, getResources().getColor(R.color.ucrop_color_default_crop_frame)));
    mOverlayView.setCropFrameStrokeWidth(intent.getIntExtra(UCrop.Options.EXTRA_CROP_FRAME_STROKE_WIDTH, getResources().getDimensionPixelSize(R.dimen.ucrop_default_crop_frame_stoke_width)));

    mOverlayView.setShowCropGrid(intent.getBooleanExtra(UCrop.Options.EXTRA_SHOW_CROP_GRID, OverlayView.DEFAULT_SHOW_CROP_GRID));
    mOverlayView.setCropGridRowCount(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_ROW_COUNT, OverlayView.DEFAULT_CROP_GRID_ROW_COUNT));
    mOverlayView.setCropGridColumnCount(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_COLUMN_COUNT, OverlayView.DEFAULT_CROP_GRID_COLUMN_COUNT));
    mOverlayView.setCropGridColor(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_COLOR, getResources().getColor(R.color.ucrop_color_default_crop_grid)));
    mOverlayView.setCropGridStrokeWidth(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_STROKE_WIDTH, getResources().getDimensionPixelSize(R.dimen.ucrop_default_crop_grid_stoke_width)));

    // Aspect ratio options
    float aspectRatioX = bundle.getFloat(UCrop.EXTRA_ASPECT_RATIO_X, 0);
    float aspectRatioY = bundle.getFloat(UCrop.EXTRA_ASPECT_RATIO_Y, 0);

    int aspectRationSelectedByDefault = intent.getIntExtra(UCrop.Options.EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, 0);
    ArrayList<AspectRatio> aspectRatioList = intent.getParcelableArrayListExtra(UCrop.Options.EXTRA_ASPECT_RATIO_OPTIONS);

    if (aspectRatioX > 0 && aspectRatioY > 0)
    {
      if (mWrapperStateAspectRatio != null)
      {
        mWrapperStateAspectRatio.setVisibility(View.GONE);
      }
      mGestureCropImageView.setTargetAspectRatio(aspectRatioX / aspectRatioY);
    } else if (aspectRatioList != null && aspectRationSelectedByDefault < aspectRatioList.size())
    {
      mGestureCropImageView.setTargetAspectRatio(aspectRatioList.get(aspectRationSelectedByDefault).getAspectRatioX() /
        aspectRatioList.get(aspectRationSelectedByDefault).getAspectRatioY());
    } else
    {
      mGestureCropImageView.setTargetAspectRatio(CropImageView.SOURCE_IMAGE_ASPECT_RATIO);
    }

    // Result bitmap max size options
    int maxSizeX = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_X, 0);
    int maxSizeY = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_Y, 0);

    if (maxSizeX > 0 && maxSizeY > 0)
    {
      mGestureCropImageView.setMaxResultImageSizeX(maxSizeX);
      mGestureCropImageView.setMaxResultImageSizeY(maxSizeY);
    }
  }

  private void setupViews(@NonNull Intent intent) {

    mStatusBarColor = intent.getIntExtra(UCrop.Options.EXTRA_STATUS_BAR_COLOR, ContextCompat.getColor(getContext(), R.color.ucrop_color_statusbar));
    mToolbarColor = intent.getIntExtra(UCrop.Options.EXTRA_TOOL_BAR_COLOR, ContextCompat.getColor(getContext(), R.color.ucrop_color_toolbar));
    mActiveWidgetColor = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_COLOR_WIDGET_ACTIVE, ContextCompat.getColor(getContext(), R.color.ucrop_color_widget_active));
    mToolbarWidgetColor = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_WIDGET_COLOR_TOOLBAR, ContextCompat.getColor(getContext(), R.color.ucrop_color_toolbar_widget));
    mToolbarTitle = intent.getStringExtra(UCrop.Options.EXTRA_UCROP_TITLE_TEXT_TOOLBAR);
    mToolbarTitle = !TextUtils.isEmpty(mToolbarTitle) ? mToolbarTitle : getResources().getString(R.string.ucrop_label_edit_photo);
    mLogoColor = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_LOGO_COLOR, ContextCompat.getColor(getContext(), R.color.ucrop_color_default_logo));
    mShowBottomControls = !intent.getBooleanExtra(UCrop.Options.EXTRA_HIDE_BOTTOM_CONTROLS, false);

    setupAppBar();
    initiateRootViews();

    if (mShowBottomControls)
    {
      ViewGroup photoBox = (ViewGroup) v.findViewById(R.id.ucrop_photobox);
      View.inflate(getContext(), R.layout.ucrop_controls, photoBox);

      mWrapperStateAspectRatio = (ViewGroup) v.findViewById(R.id.state_aspect_ratio);
      mWrapperStateAspectRatio.setOnClickListener(mStateClickListener);
      mWrapperStateRotate = (ViewGroup) v.findViewById(R.id.state_rotate);
      mWrapperStateRotate.setOnClickListener(mStateClickListener);
      mWrapperStateScale = (ViewGroup) v.findViewById(R.id.state_scale);
      mWrapperStateScale.setOnClickListener(mStateClickListener);
      mWrapperStateFilter = (ViewGroup) v.findViewById(R.id.state_filter);
      mWrapperStateFilter.setOnClickListener(mStateClickListener);

      //my code

      mLayoutAspectRatio = (ViewGroup) v.findViewById(R.id.layout_aspect_ratio);
      mLayoutRotate = (ViewGroup) v.findViewById(R.id.layout_rotate_wheel);
      mLayoutScale = (ViewGroup) v.findViewById(R.id.layout_scale_wheel);
      mLayoutFilter = (ViewGroup) v.findViewById(R.id.layout_filter);

      setupAspectRatioWidget(intent);
      setupRotateWidget();
      setupScaleWidget();
      setupFilterWidget();
      setupStatesWrapper();
    }
  }

  /**
   * Configures and styles both status bar and toolbar.
   */
  private void setupAppBar() {
        /*setStatusBarColor(mStatusBarColor);*/

    final Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);

    // Set all of the Toolbar coloring
    toolbar.setBackgroundColor(mToolbarColor);
    toolbar.setTitleTextColor(mToolbarWidgetColor);

    final TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
    toolbarTitle.setTextColor(mToolbarWidgetColor);
    toolbarTitle.setText(mToolbarTitle);

    // Color buttons inside the Toolbar
    Drawable stateButtonDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_arrow_back_white_24dp).mutate();
    stateButtonDrawable.setColorFilter(mToolbarWidgetColor, PorterDuff.Mode.SRC_ATOP);
    toolbar.setNavigationIcon(stateButtonDrawable);

        /*((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        final ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }*/
  }

  private void initiateRootViews() {
    mUCropView = (UCropView) v.findViewById(R.id.ucrop);
    mGestureCropImageView = mUCropView.getCropImageView();
    mOverlayView = mUCropView.getOverlayView();

    mGestureCropImageView.setTransformImageListener(mImageListener);

    ((ImageView) v.findViewById(R.id.image_view_logo)).setColorFilter(mLogoColor, PorterDuff.Mode.SRC_ATOP);
  }

  /**
   * Use {@link #mActiveWidgetColor} for color filter
   */
  private void setupStatesWrapper() {
    ImageView stateScaleImageView = (ImageView) v.findViewById(R.id.image_view_state_scale);
    ImageView stateRotateImageView = (ImageView) v.findViewById(R.id.image_view_state_rotate);
    ImageView stateAspectRatioImageView = (ImageView) v.findViewById(R.id.image_view_state_aspect_ratio);
    ImageView stateFilterImageView = (ImageView) v.findViewById(R.id.image_view_state_filter);
    ImageView rotateByAngle = (ImageView) v.findViewById(R.id.rotate_by_angle);
    ImageView resetRotate = (ImageView) v.findViewById(R.id.idReset);

    Drawable filter = ContextCompat.getDrawable(getContext(), R.drawable.ic_filter_white_24dp);
    filter = DrawableCompat.wrap(filter);
    DrawableCompat.setTint(filter.getCurrent(), ContextCompat.getColor(getContext(), R.color.ucrop_color_widget));
    stateFilterImageView.setImageDrawable(filter);

    Drawable scale = ContextCompat.getDrawable(getContext(), R.drawable.ic_zoom_out_map_white_24dp);
    scale = DrawableCompat.wrap(scale);
    DrawableCompat.setTint(scale.getCurrent(), ContextCompat.getColor(getContext(), R.color.ucrop_color_widget));
    stateScaleImageView.setImageDrawable(scale);

    Drawable rotate = ContextCompat.getDrawable(getContext(), R.drawable.ic_rotate_right_white_24dp);
    rotate = DrawableCompat.wrap(rotate);
    DrawableCompat.setTint(rotate.getCurrent(), ContextCompat.getColor(getContext(), R.color.ucrop_color_widget));
    stateRotateImageView.setImageDrawable(rotate);

    Drawable aspect = ContextCompat.getDrawable(getContext(), R.drawable.ic_crop_white_24dp);
    aspect = DrawableCompat.wrap(aspect);
    DrawableCompat.setTint(aspect.getCurrent(), ContextCompat.getColor(getContext(), R.color.ucrop_color_widget));
    stateAspectRatioImageView.setImageDrawable(aspect);

    Drawable angle = ContextCompat.getDrawable(getContext(), R.drawable.ucrop_ic_angle);
    angle = DrawableCompat.wrap(angle);
    DrawableCompat.setTint(angle.getCurrent(), ContextCompat.getColor(getContext(), R.color.ucrop_color_widget));
    rotateByAngle.setImageDrawable(angle);

    Drawable reset = ContextCompat.getDrawable(getContext(), R.drawable.ucrop_ic_reset);
    reset = DrawableCompat.wrap(reset);
    DrawableCompat.setTint(reset.getCurrent(), ContextCompat.getColor(getContext(), R.color.ucrop_color_widget));
    resetRotate.setImageDrawable(reset);

    stateScaleImageView.setImageDrawable(new SelectedStateListDrawable(stateScaleImageView.getDrawable(), mActiveWidgetColor));
    stateRotateImageView.setImageDrawable(new SelectedStateListDrawable(stateRotateImageView.getDrawable(), mActiveWidgetColor));
    stateAspectRatioImageView.setImageDrawable(new SelectedStateListDrawable(stateAspectRatioImageView.getDrawable(), mActiveWidgetColor));
    stateFilterImageView.setImageDrawable(new SelectedStateListDrawable(stateFilterImageView.getDrawable(), mActiveWidgetColor));
  }


    /*@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBarColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Window window = getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(color);
            }
        }
    }*/

  private void setupAspectRatioWidget(@NonNull Intent intent) {

    int aspectRationSelectedByDefault = intent.getIntExtra(UCrop.Options.EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, 0);
    ArrayList<AspectRatio> aspectRatioList = intent.getParcelableArrayListExtra(UCrop.Options.EXTRA_ASPECT_RATIO_OPTIONS);

    if (aspectRatioList == null || aspectRatioList.isEmpty())
    {
      aspectRationSelectedByDefault = 1;

      aspectRatioList = new ArrayList<>();
      aspectRatioList.add(new AspectRatio(getString(R.string.ucrop_label_square).toUpperCase(), 1, 1));
      // aspectRatioList.add(new AspectRatio(null, 3, 4));
      aspectRatioList.add(new AspectRatio(getString(R.string.ucrop_label_original).toUpperCase(),
        CropImageView.SOURCE_IMAGE_ASPECT_RATIO, CropImageView.SOURCE_IMAGE_ASPECT_RATIO));
      // aspectRatioList.add(new AspectRatio(null, 3, 2));
      // aspectRatioList.add(new AspectRatio(null, 16, 9));
    }

    LinearLayout wrapperAspectRatioList = (LinearLayout) v.findViewById(R.id.layout_aspect_ratio);

    FrameLayout wrapperAspectRatio;
    AspectRatioTextView aspectRatioTextView;
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
    lp.weight = 1;
    for (AspectRatio aspectRatio : aspectRatioList)
    {
      wrapperAspectRatio = (FrameLayout) getLayoutInflater(getArguments()).inflate(R.layout.ucrop_aspect_ratio, null);
      wrapperAspectRatio.setLayoutParams(lp);
      aspectRatioTextView = ((AspectRatioTextView) wrapperAspectRatio.getChildAt(0));
      aspectRatioTextView.setActiveColor(mActiveWidgetColor);
      aspectRatioTextView.setAspectRatio(aspectRatio);

      wrapperAspectRatioList.addView(wrapperAspectRatio);
      mCropAspectRatioViews.add(wrapperAspectRatio);
    }

    mCropAspectRatioViews.get(aspectRationSelectedByDefault).setSelected(true);

    for (ViewGroup cropAspectRatioView : mCropAspectRatioViews)
    {
      cropAspectRatioView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mGestureCropImageView.setTargetAspectRatio(
            ((AspectRatioTextView) ((ViewGroup) v).getChildAt(0)).getAspectRatio(v.isSelected()));
          mGestureCropImageView.setImageToWrapCropBounds();
          if (!v.isSelected())
          {
            for (ViewGroup cropAspectRatioView : mCropAspectRatioViews)
            {
              cropAspectRatioView.setSelected(cropAspectRatioView == v);
            }
          }
        }
      });
    }
  }

  private void setupRotateWidget() {
    mTextViewRotateAngle = ((TextView) v.findViewById(R.id.text_view_rotate));
    ((HorizontalProgressWheelView) v.findViewById(R.id.rotate_scroll_wheel))
      .setScrollingListener(new HorizontalProgressWheelView.ScrollingListener() {
        @Override
        public void onScroll(float delta, float totalDistance) {
          mGestureCropImageView.postRotate(delta / ROTATE_WIDGET_SENSITIVITY_COEFFICIENT);
        }

        @Override
        public void onScrollEnd() {
          mGestureCropImageView.setImageToWrapCropBounds();
        }

        @Override
        public void onScrollStart() {
          mGestureCropImageView.cancelAllAnimations();
        }
      });

    ((HorizontalProgressWheelView) v.findViewById(R.id.rotate_scroll_wheel)).setMiddleLineColor(mActiveWidgetColor);


    v.findViewById(R.id.wrapper_reset_rotate).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        resetRotation();
      }
    });
    v.findViewById(R.id.wrapper_rotate_by_angle).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        rotateByAngle(90);
      }
    });
  }

  private void setupScaleWidget() {
    mTextViewScalePercent = ((TextView) v.findViewById(R.id.text_view_scale));
    ((HorizontalProgressWheelView) v.findViewById(R.id.scale_scroll_wheel))
      .setScrollingListener(new HorizontalProgressWheelView.ScrollingListener() {
        @Override
        public void onScroll(float delta, float totalDistance) {
          if (delta > 0)
          {
            mGestureCropImageView.zoomInImage(mGestureCropImageView.getCurrentScale()
              + delta * ((mGestureCropImageView.getMaxScale() - mGestureCropImageView.getMinScale()) / SCALE_WIDGET_SENSITIVITY_COEFFICIENT));
          } else
          {
            mGestureCropImageView.zoomOutImage(mGestureCropImageView.getCurrentScale()
              + delta * ((mGestureCropImageView.getMaxScale() - mGestureCropImageView.getMinScale()) / SCALE_WIDGET_SENSITIVITY_COEFFICIENT));
          }
        }

        @Override
        public void onScrollEnd() {
          mGestureCropImageView.setImageToWrapCropBounds();
        }

        @Override
        public void onScrollStart() {
          mGestureCropImageView.cancelAllAnimations();
        }
      });
    ((HorizontalProgressWheelView) v.findViewById(R.id.scale_scroll_wheel)).setMiddleLineColor(mActiveWidgetColor);
  }

  private void setupFilterWidget() {

    models = new ArrayList<>();

    mRecyclerFilter = (RecyclerView) v.findViewById(R.id.FilterRecyclerView);
    adapter = new FilterAdapter(models, new onFilterClickListener() {
      @Override
      public void onClick(Filter filter) {

        ProcessImage processImage = new ProcessImage();
        Bitmap b = null;
        try
        {

          b = processImage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filter).get();
          File file = new File(getContext().getCacheDir(), "temp.jpg");
          FileOutputStream outputStream = new FileOutputStream(file);
          b = Bitmap.createScaledBitmap(b, b.getWidth() * 2, b.getHeight() * 2, true);


          b.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
          outputStream.close();
          if (new File(file.getPath()).exists())
          {
            File output = File.createTempFile("output", ".jpg");
            mGestureCropImageView.setImageUri(Uri.fromFile(file), Uri.fromFile(output), null);
            mGestureCropImageView.setImageToWrapCropBounds();

          }

        } catch (Exception e)
        {
          Log.d(TAG, "onClick: ", e);
        }

      }
    });

    mRecyclerFilter.setAdapter(adapter);
    RecyclerView.LayoutManager layoutManager =
      new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
    mRecyclerFilter.setLayoutManager(layoutManager);
    adapter.notifyDataSetChanged();

  }

  private void setAngleText(float angle) {
    if (mTextViewRotateAngle != null)
    {
      mTextViewRotateAngle.setText(String.format(Locale.getDefault(), "%.1f°", angle));
    }
  }

  private void setScaleText(float scale) {
    if (mTextViewScalePercent != null)
    {
      mTextViewScalePercent.setText(String.format(Locale.getDefault(), "%d%%", (int) (scale * 100)));
    }
  }

  private void resetRotation() {
    mGestureCropImageView.postRotate(-mGestureCropImageView.getCurrentAngle());
    mGestureCropImageView.setImageToWrapCropBounds();
  }

  private void rotateByAngle(int angle) {
    mGestureCropImageView.postRotate(angle);
    mGestureCropImageView.setImageToWrapCropBounds();
  }

  private void setInitialState() {
    if (mShowBottomControls)
    {
      if (mWrapperStateAspectRatio.getVisibility() == View.VISIBLE)
      {
        setWidgetState(R.id.state_aspect_ratio);
      } else
      {
        setWidgetState(R.id.state_scale);
      }
    } else
    {
      setAllowedGestures(0);
    }
  }

  private void setWidgetState(@IdRes int stateViewId) {
    if (!mShowBottomControls)
    {
      return;
    }

    mWrapperStateAspectRatio.setSelected(stateViewId == R.id.state_aspect_ratio);
    mWrapperStateRotate.setSelected(stateViewId == R.id.state_rotate);
    mWrapperStateScale.setSelected(stateViewId == R.id.state_scale);
    mWrapperStateFilter.setSelected(stateViewId == R.id.state_filter);

    mLayoutAspectRatio.setVisibility(stateViewId == R.id.state_aspect_ratio ? View.VISIBLE : View.GONE);
    mLayoutRotate.setVisibility(stateViewId == R.id.state_rotate ? View.VISIBLE : View.GONE);
    mLayoutScale.setVisibility(stateViewId == R.id.state_scale ? View.VISIBLE : View.GONE);
    mLayoutFilter.setVisibility(stateViewId == R.id.state_filter ? View.VISIBLE : View.GONE);

    if (stateViewId == R.id.state_scale)
    {
      setAllowedGestures(0);
    } else if (stateViewId == R.id.state_rotate)
    {
      setAllowedGestures(1);
    } else
    {
      setAllowedGestures(2);
    }
  }

  private void setAllowedGestures(int tab) {
    mGestureCropImageView.setScaleEnabled(mAllowedGestures[tab] == ALL || mAllowedGestures[tab] == SCALE);
    mGestureCropImageView.setRotateEnabled(mAllowedGestures[tab] == ALL || mAllowedGestures[tab] == ROTATE);
  }

  /**
   * Adds view that covers everything below the Toolbar.
   * When it's clickable - user won't be able to click/touch anything below the Toolbar.
   * Need to block user input while loading and cropping an image.
   */
  private void addBlockingView() {

    if (mBlockingView == null)
    {
      ((RelativeLayout) v.findViewById(R.id.ucrop_photobox)).removeView(mBlockingView);
      mBlockingView = new View(getContext());
      RelativeLayout.LayoutParams lp = new RelativeLayout
        .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
      lp.addRule(RelativeLayout.BELOW, R.id.toolbar);
      mBlockingView.setLayoutParams(lp);
      mBlockingView.setClickable(true);
    }

    ((RelativeLayout) v.findViewById(R.id.ucrop_photobox)).addView(mBlockingView);
  }

  protected void cropAndSaveImage() {
    mBlockingView.setClickable(true);
    mShowLoader = true;
        /*supportInvalidateOptionsMenu();*/

    mGestureCropImageView.cropAndSaveImage(mCompressFormat, mCompressQuality, new BitmapCropCallback() {

      @Override
      public void onBitmapCropped(@NonNull Uri resultUri, int imageWidth, int imageHeight) {
        setResultUri(resultUri, mGestureCropImageView.getTargetAspectRatio(), imageWidth, imageHeight);
                /*finish();*/
      }

      @Override
      public void onCropFailure(@NonNull Throwable t) {
        setResultError(t);
                /*finish();*/
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    isVisible = true;
  }

  @Override
  public void onStop() {
    super.onStop();
    isVisible = false;
  }

  protected void setResultUri(Uri uri, float resultAspectRatio, int imageWidth, int imageHeight) {

    getFragmentManager().popBackStack("first", 1);
    getFragmentManager().popBackStack("second", 1);

    Intent result = new Intent();
    result
      .putExtra("type", "image")
      .putExtra(UCrop.EXTRA_OUTPUT_URI, uri)
      .putExtra(UCrop.EXTRA_OUTPUT_CROP_ASPECT_RATIO, resultAspectRatio)
      .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_WIDTH, imageWidth)
      .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_HEIGHT, imageHeight)
      .putExtra("step", bundle.getInt("step", 0));
    cropFinished.onResult(result);

  }

  protected void setResultError(Throwable throwable) {

    cropFinished.onError(throwable);

  }

  @IntDef ({NONE, SCALE, ROTATE, ALL})
  @Retention (RetentionPolicy.SOURCE)
  public @interface GestureTypes {

  }

  class ProcessImage extends AsyncTask<Filter, Void, Bitmap> {

    @Override
    protected Bitmap doInBackground(Filter... filters) {

            /*final Bitmap b = bitmap.copy(Bitmap.Config.ARGB_8888, true);*/
      final Bitmap b = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, false);
      if (filters[0] != null)
      {
        return filters[0].processFilter(b);
      } else
      {
        return b;
      }

    }
  }

}
