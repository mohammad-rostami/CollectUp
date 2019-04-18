package com.collect_up.c_up.fragments;/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.model.BaseModel;
import com.collect_up.c_up.model.PostProfileTag;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.view.CustomEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@SuppressLint ("ValidFragment")

public class FragmentTagPeople extends FragmentMentions {

  public static boolean isRunning;
  private final Bitmap mImage;

  private BaseModel postType;
  private Menu mMenu;
  private String finalUserText;
  private View fragmentSView;
  ArrayList<String> mentionsUser = new ArrayList<>();
  ArrayList<String> mentionsEncrypt = new ArrayList<>();
  private String finalHashtagText;
  private List<String> hashTags = new LinkedList<>();
  private boolean isFocused;
  private View view;
  private Toolbar toolbar;
  private ImageView imgTag;
  private ImageView btnRemove;
  private RelativeLayout tagLayout;
  private TextView txtTag;
  private CustomEditText mPostBodyEditText;
  private int _xDelta;
  private int _yDelta;
  private CustomEditText edtSearchPeople;
  private ArrayList<PostProfileTag> tagedProfiles = new ArrayList<>();
  private HashMap<String, RelativeLayout> tags = new HashMap<>();
  private FrameLayout root;

  @Override
  public void onStop() {
    isRunning = false;
    super.onStop();
  }

  @Override
  public void onStart() {
    super.onStart();
    isRunning = true;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  public FragmentTagPeople(Intent intent) {
    mImage = intent.getParcelableExtra("image");
    ArrayList<PostProfileTag> peoples = intent.getParcelableArrayListExtra("peoples");
    if (peoples != null)
    {
      for (int i = 0; i < peoples.size(); i++)
      {
        tagedProfiles.add(peoples.get(i));
      }
    }
  }


  public String getRealPathFromURI(Context context, Uri contentUri) {
    Cursor cursor = null;
    try
    {
      String[] proj = {MediaStore.Images.Media.DATA};
      cursor = context.getContentResolver().query(contentUri, proj, null,
        null, null);
      int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
      cursor.moveToFirst();
      return cursor.getString(column_index);
    } catch (Exception ex)
    {
      return null;
    } finally
    {
      if (cursor != null)
      {
        cursor.close();
      }
    }
  }


  @Override
  public void onResume() {
    super.onResume();

    ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_tag_people);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);
    ((ActivityHome) getActivity()).hideButtonBar(true);
    if (tagedProfiles != null)
    {
      for (int i = 0; i < tagedProfiles.size(); i++)
      {
        addTag(null, tagedProfiles.get(i));
      }
    }
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {

      ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

      view = inflater.inflate(R.layout.fragment_tag_people, container, false);

      getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

      toolbar = (Toolbar) view.findViewById(R.id.toolbar);
      toolbar.setTitle(R.string.title_tag_people);
      toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
      toolbar.setNavigationOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          FragmentHandler.onBackPressed(getContext());
          hideKeyboard(true);

        }
      });
      toolbar.inflateMenu(R.menu.menu_done_discard);
      toolbar.getMenu().findItem(R.id.action_search).setVisible(false);

      setHasOptionsMenu(true);

      toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          onOptionsItemSelected(item);
          return false;
        }
      });

      edtSearchPeople = (CustomEditText) view.findViewById(R.id.edtSearchPeople);
      imgTag = (ImageView) view.findViewById(R.id.imgTag);
      imgTag.setImageBitmap(mImage);

      fragmentSView = view.findViewById(R.id.fragmentSuggestionlist);
      edtSearchPeople.setOnFocusChangeListener(new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
          if (hasFocus)
          {
            isFocused = true;
          } else
          {
            isFocused = false;
          }
        }
      });
      fragmentSView.setVisibility(View.GONE);
      edtSearchPeople.setHandleDismissingKeyboard(new CustomEditText.handleDismissingKeyboard() {
        @Override
        public void dismissKeyboard() {
          if (fragmentSView.getVisibility() == View.VISIBLE)
          {
            fragmentSView.setVisibility(View.GONE);
          } else if (isFocused)
          {
            hideKeyboard(true);
            edtSearchPeople.clearFocus();
          } else
          {
          }
        }
      });
      final FragmentMention fragmentMention = new FragmentMention();
      FragmentManager manager = getChildFragmentManager();
      final FragmentTransaction transaction = manager.beginTransaction();
      transaction.replace(R.id.fragmentSuggestionlist, fragmentMention).commit();
      edtSearchPeople.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          fragmentSView.setVisibility(View.GONE);

          return false;
        }
      });

      edtSearchPeople.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable inputText) {
          int cursorPosition = edtSearchPeople.getSelectionStart();
          int finalPos = SepehrUtil.getLastCharPos(Constants.General.MENTION_USER_SIGN, inputText.toString(), cursorPosition);
          String atUserText = inputText.toString().substring(finalPos, cursorPosition);


          finalUserText = getUserSplite(atUserText);
          if (!Utils.isNullOrEmpty(finalUserText) && !finalUserText.contains(" "))
          {
            fragmentMention.loadFirstMentionPage(finalUserText.trim(), "0", Pagination.MentionMode.USER);
          } else
          {
            fragmentSView.setVisibility(View.GONE);
          }

        }

        private String getBusinessSplite(String business) {
          String[] splitedText = business.split("\\" + Constants.General.MENTION_BUSINESS_SIGN);
          String result = "";
          if (splitedText.length > 0)
          {
            result = splitedText[splitedText.length - 1];
          }
          return result;
        }

        private String getUserSplite(String user) {
          String[] splitedText = user.split(Constants.General.MENTION_USER_SIGN);
          String result = "";
          if (splitedText.length > 0)
          {
            result = splitedText[splitedText.length - 1];
          }
          return result;

        }

        private String getHashtagSplite(String hashtag) {
          String[] splitedText = hashtag.split(Constants.General.HASHTAG_SIGN);
          String result = "";
          if (splitedText.length > 0)
          {
            result = splitedText[splitedText.length - 1];
          }
          return result;

        }
      });

      root = (FrameLayout) view.findViewById(R.id.frameHolder);
      imgTag.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View view, MotionEvent event) {
          addTag(event, null);
          return false;
        }
      });
      return view;
    } else

    {
      return view;
    }

  }

  private boolean addTag(MotionEvent event, final PostProfileTag postProfileTag) {
    LayoutInflater inflater = LayoutInflater.from(getContext());
    tagLayout = (RelativeLayout) inflater.inflate(R.layout.tagable_textview, root, false);

    txtTag = (TextView) tagLayout.findViewById(R.id.txtTag);
    if (postProfileTag != null)
    {
      txtTag.setText(postProfileTag.getProfile().getUsername());
    }
    btnRemove = (ImageView) tagLayout.findViewById(R.id.btnRemove);
    btnRemove.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        RelativeLayout layout = tags.get(((RelativeLayout) view.getParent()).getTag());
        ViewGroup parentView = (ViewGroup) view.getParent().getParent();
        parentView.removeView(layout);
        tags.remove(((RelativeLayout) view.getParent()).getTag());
        if (postProfileTag != null)
        {
          tagedProfiles.remove(postProfileTag);
        }
      }
    });
    if (postProfileTag != null)
    {
      tagLayout.setTag(postProfileTag.getProfile().getId());
    } else
    {
      tagLayout.setTag("");
    }
    tagLayout.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent event) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
          case MotionEvent.ACTION_DOWN:
            FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) view.getLayoutParams();
            _xDelta = X - lParams.leftMargin;
            _yDelta = Y - lParams.topMargin;
            break;
          case MotionEvent.ACTION_UP:


            break;
          case MotionEvent.ACTION_POINTER_DOWN:
            break;
          case MotionEvent.ACTION_POINTER_UP:
            break;
          case MotionEvent.ACTION_MOVE:
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
            layoutParams.leftMargin = X - _xDelta;
            layoutParams.topMargin = Y - _yDelta;

            if (layoutParams.leftMargin <= 0 || layoutParams.leftMargin >= root.getWidth() - view.getWidth()
              || layoutParams.topMargin <= 0 || layoutParams.topMargin >= root.getHeight() - view.getHeight())
            {
              break;
            }
            view.setLayoutParams(layoutParams);
            tags.put((String) view.getTag(), (RelativeLayout) view);
            break;
        }
        root.invalidate();
        return true;
      }
    });
    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tagLayout.getLayoutParams();

    float screenX = event == null ? Utils.dpToPxiel(getContext(), postProfileTag.getX()) : event.getX();
    float screenY = event == null ? Utils.dpToPxiel(getContext(), postProfileTag.getY()) : event.getY();
    final float viewX = screenX - imgTag.getLeft();
    final float viewY = screenY - imgTag.getTop();

    params.leftMargin = (int) viewX;
    params.topMargin = (int) viewY;
    for (int i = 0; i < root.getChildCount(); i++)
    {
      if (event != null && root.getChildAt(i) != null && root.getChildAt(i) instanceof RelativeLayout
        && root.getChildAt(i).getTag().equals(""))
      {
        root.removeView(root.getChildAt(i));
        hideKeyboard(true);
        edtSearchPeople.setVisibility(View.GONE);

        return false;
      }
    }
    if (event != null)
    {
      edtSearchPeople.setVisibility(View.VISIBLE);
      edtSearchPeople.requestFocus();
    }
    if (event != null && tagLayout.getTag().equals(""))
    {
      btnRemove.setVisibility(View.GONE);
    }
    hideKeyboard(false);
    if (postProfileTag != null)
    {
      tags.put(postProfileTag.getProfile().getId(), tagLayout);
    }
    root.addView(tagLayout, params);
    tagLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        tagLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        tagLayout.getWidth();
        if (viewX >= imgTag.getWidth() - tagLayout.getWidth())
        {
          FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) tagLayout.getLayoutParams();
          param.leftMargin = imgTag.getWidth() - tagLayout.getWidth();
          tagLayout.setLayoutParams(param);
        }

        if (viewY >= imgTag.getHeight() - tagLayout.getHeight())
        {
          FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) tagLayout.getLayoutParams();
          param.topMargin = imgTag.getHeight() - tagLayout.getHeight();
          tagLayout.setLayoutParams(param);
        }
      }
    });
    return false;
  }

  @Override
  public void setVisibilityMentionFragment(boolean visible) {
    if (visible)
    {
      fragmentSView.setVisibility(View.VISIBLE);
    } else
    {
      fragmentSView.setVisibility(View.GONE);
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_done_discard, menu);
    MenuItem menuItemLoader = menu.findItem(R.id.menu_loader);
    Drawable menuItemLoaderIcon = menuItemLoader.getIcon();
    if (menuItemLoaderIcon != null)
    {
      try
      {
        menuItemLoaderIcon.mutate();
        menuItemLoaderIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        menuItemLoader.setIcon(menuItemLoaderIcon);
      } catch (IllegalStateException e)
      {
        Log.i("sepehr", String.format("%s - %s", e.getMessage(), getString(R.string.ucrop_mutate_exception_hint)));
      }
      ((Animatable) menuItemLoader.getIcon()).start();
      menuItemLoader.setVisible(false);
    }
    menu.findItem(R.id.action_search).setVisible(false);

  }


  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    mMenu = menu;
  }

  @Override
  public void setProfileToMention(Profile profile) {
    if (tags.containsKey(profile.getId()))
    {
      Toast.makeText(getContext(), R.string.toast_error_tag_people, Toast.LENGTH_SHORT).show();
      return;
    }
    ArrayList<Object> result = setMentioned(profile, edtSearchPeople.getText().toString(), edtSearchPeople.getSelectionStart());
    mentionsUser.add((String) result.get(0));
    mentionsEncrypt.add((String) result.get(1));
    String name = result.get(2).toString().split(Constants.General.MENTION_USER_SIGN)[1];
    edtSearchPeople.setText("");

    tags.put(profile.getId(), tagLayout);
    txtTag.setText(name);
    btnRemove.setVisibility(View.VISIBLE);
    tagLayout.setTag(profile.getId());
    PostProfileTag postProfileTag = new PostProfileTag();
    postProfileTag.setProfile(profile);
    postProfileTag.setX(tagLayout.getLeft());
    postProfileTag.setY(tagLayout.getTop());
    tagedProfiles.add(postProfileTag);
    hideKeyboard(true);
  }

  private void hideKeyboard(boolean doIt) {
    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    if (doIt)
    {
      imm.hideSoftInputFromWindow(edtSearchPeople.getWindowToken(), 0);

    } else
    {
      imm.showSoftInput(edtSearchPeople, InputMethodManager.SHOW_IMPLICIT);
    }
  }

  @Override
  public void setShopToMention(Shop shop) {
    ArrayList<Object> result = setMentioned(shop, edtSearchPeople.getText().toString(), edtSearchPeople.getSelectionStart());
    mentionsUser.add((String) result.get(0));
    mentionsEncrypt.add((String) result.get(1));
    edtSearchPeople.setText((String) result.get(2));
    edtSearchPeople.setSelection((Integer) result.get(3));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId())
    {
      case R.id.home:

        break;
      case R.id.action_done:

        hideKeyboard(true);
        for (int i = 0; i < tagedProfiles.size(); i++)
        {
          tagedProfiles.get(i).setX(Utils.pxToDp(getContext(), tags.get(tagedProfiles.get(i).getProfile().getId()).getLeft()));
          tagedProfiles.get(i).setY(Utils.pxToDp(getContext(), tags.get(tagedProfiles.get(i).getProfile().getId()).getTop()));

        }
        for (int i = 0; i < getActivity().getSupportFragmentManager().getFragments().size(); i++)
        {
          if (getActivity().getSupportFragmentManager().getFragments().get(i) instanceof FragmentPostNew ||
            getActivity().getSupportFragmentManager().getFragments().get(i) instanceof FragmentPostEdit)
          {
            ((FragmentMentions) getActivity().getSupportFragmentManager().getFragments().get(i)).setPeople(tagedProfiles);
            break;
          }

        }
        FragmentHandler.onBackPressed(getContext());

        break;
      case android.R.id.home:
        break;
    }
    return false;
  }

}
