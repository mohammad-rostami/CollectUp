package com.collect_up.c_up.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.adapters.StickersAdapter;
import com.collect_up.c_up.adapters.StickersBottomSheetAdapter;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.model.StickerPackage;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.rey.material.app.BottomSheetDialog;
import com.rey.material.widget.Button;

import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

@SuppressLint ("ValidFragment")

public class FragmentManageStickers extends BaseFragment implements StickersAdapter.ItemListener,
  View.OnClickListener {
  public static boolean isRunning;
  @Bind (R.id.btnAllStickers)
  Button btnAllStickers;

  private List<StickerPackage> stickers = new LinkedList<>();
  private StickersAdapter adapter;
  private UltimateRecyclerView list;
  private View view;


  private void initBottomMenus() {
    btnAllStickers.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId())
    {
      case R.id.btnAllStickers:
        FragmentHandler.replaceFragment(getContext(), fragmentType.PREF_ALLSTICKERS, null);

    }
  }


  private void showLimitedMessageDialog(@StringRes int res) {
    final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getContext());
    builder.message(res)
      .messageTextColor(ContextCompat.getColor(getContext(), R.color.primary_text))
      .setCancelable(true);

    builder.show();
  }

  @Override
  public void onResume() {
    super.onResume();

    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.stickers);
    ((ActivityHome) getActivity()).changeButtonBackgroud(-1);
    ((ActivityHome) getActivity()).hideButtonBar(true);

  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_stickers, container, false);
      ButterKnife.bind(this, view);
      ((AppCompatActivity) getActivity()).getSupportActionBar().show();
      setHasOptionsMenu(true);
      list = (UltimateRecyclerView) view.findViewById(R.id.recycler_view);
      list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
      if (Logged.Models.getUserStickerPackages(getContext()).size() > 0)
      {
        loadFirstStickersPage();
      } else
      {
        setEmtyView();
      }

      initBottomMenus();

      return view;
    } else
    {
      return view;
    }
  }


  private void setEmtyView()

  {
    View view = list.getEmptyView();
    if (view != null)
    {
      ViewGroup viewGroup = ((ViewGroup) view.getParent());
      if (viewGroup != null)
      {
        viewGroup.removeView(view);
        viewGroup.addView(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.empty_list_view, null));
      }
    } else
    {
      list.setEmptyView(R.layout.empty_list_view);
    }
  }

  @SuppressWarnings ("unchecked")
  private void loadFirstStickersPage() {
    List<StickerPackage> stickerList = Logged.Models.getUserStickerPackages(getContext());
    stickers.addAll(Logged.Models.getUserStickerPackages(getContext()));

    adapter = new StickersAdapter(getContext(), stickerList);
    adapter.setListener(FragmentManageStickers.this);

    list.setAdapter(adapter);

    adapter.notifyDataSetChanged();


  }


  @Override
  public void onStickerClick(final StickerPackage stickerPackage) {
    boolean exist = false;
    final BottomSheetDialog mDialog = new BottomSheetDialog(getContext());
    mDialog.contentView(R.layout.bottom_sheet_sticker)
      .inDuration(300)
      .cancelable(true);

    final com.rey.material.widget.ImageButton btnAdd = (com.rey.material.widget.ImageButton) mDialog.findViewById(R.id.btnAdd);
    for (int i = 0; i < Logged.Models.getUserStickerPackages(getContext()).size(); i++)
    {
      if (Logged.Models.getUserStickerPackages(getContext()).get(i).getId().equalsIgnoreCase(stickerPackage.getId()))
      {

        exist = true;
        break;
      }
    }
    if (exist)
    {
      btnAdd.setImageResource(R.drawable.ic_remove_circle_black_48dp);
      btnAdd.setTag(R.drawable.ic_remove_circle_black_48dp);
    } else
    {
      btnAdd.setImageResource(R.drawable.ic_add_circle_black_48dp);
      btnAdd.setTag(R.drawable.ic_add_circle_black_48dp);
    }

    TextView txtTitle = (TextView) mDialog.findViewById(R.id.txtTitle);
    txtTitle.setText(stickerPackage.getPackageName() + " (" + stickerPackage.getCount() + ")");
    RecyclerView recyclerView = (RecyclerView) mDialog.findViewById(R.id.recycler_view);
    StickersBottomSheetAdapter adapter = new StickersBottomSheetAdapter(getContext(), stickerPackage.getStickers());
    GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 5);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);


    btnAdd.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if ((Integer) v.getTag() == R.drawable.ic_remove_circle_black_48dp)
        {
          HttpClient.get(String.format(Constants.Server.Stickers.REMOVE_PACKGE_PROFILE,
            stickerPackage.getId()), new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              super.onSuccess(statusCode, headers, responseBody);
              btnAdd.setImageResource(R.drawable.ic_add_circle_black_48dp);
              btnAdd.setTag(R.drawable.ic_add_circle_black_48dp);
              Toast.makeText(getContext(), stickerPackage.getPackageName() + " removed", Toast.LENGTH_SHORT).show();
              List<StickerPackage> packages = Logged.Models.getUserStickerPackages(getContext());
              for (int i = 0; i < packages.size(); i++)
              {
                if (packages.get(i).getId().equalsIgnoreCase(stickerPackage.getId()))
                {
                  Logged.Models.removeStickerPackage(getContext(), packages.get(i));
                  packages.remove(i);
                  break;
                }
              }
              for (int i = 0; i < FragmentManageStickers.this.adapter.getStickerPackages().size(); i++)
              {
                if (stickerPackage.getId().equals(FragmentManageStickers.this.adapter.getStickerPackages().get(i).getId()))
                {
                  FragmentManageStickers.this.adapter.getStickerPackages().remove(i);
                  break;
                }
              }
              list.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
              super.onFailure(statusCode, headers, responseBody, error);
            }
          });
        } else
        {
          HttpClient.get(String.format(Constants.Server.Stickers.ADD_TO_PROFILE_PACKAGES,
            stickerPackage.getId()), new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              super.onSuccess(statusCode, headers, responseBody);
              btnAdd.setImageResource(R.drawable.ic_remove_circle_black_48dp);
              btnAdd.setTag(R.drawable.ic_remove_circle_black_48dp);
              Toast.makeText(getContext(), stickerPackage.getPackageName() + " added", Toast.LENGTH_SHORT).show();
              List<StickerPackage> packages = Logged.Models.getUserStickerPackages(getContext());
              packages.add(stickerPackage);
              Logged.Models.setUserStickerPackages(getContext(), packages);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
              super.onFailure(statusCode, headers, responseBody, error);
            }
          });
        }

        mDialog.dismiss();
      }
    });
    mDialog.show();

  }

  @Override
  public void onStop() {
    super.onStop();
    isRunning = false;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId())
    {
      case android.R.id.home:
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onStart() {
    super.onStart();
    isRunning = true;
  }


}
