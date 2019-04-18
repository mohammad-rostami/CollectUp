package com.collect_up.c_up.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
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
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.model.StickerPackage;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.rey.material.app.BottomSheetDialog;
import com.rey.material.widget.Button;

import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class FragmentAllStickers extends BaseFragment implements StickersAdapter.ItemListener {
  public static boolean isRunning;
  private AppCompatDelegate mDelegate;
  @Bind (R.id.btnAllStickers)
  Button btnAllStickers;
  @Bind (R.id.txtSection)
  TextView txtSection;

  private int mCurrentPage;
  private List<StickerPackage> stickers = new LinkedList<>();
  private StickersAdapter adapter;
  private UltimateRecyclerView list;
  private View view;


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

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view == null)
    {
      view = inflater.inflate(R.layout.fragment_stickers, container, false);
      ButterKnife.bind(this, view);
      ((AppCompatActivity) getActivity()).getSupportActionBar().show();
      setHasOptionsMenu(true);

      btnAllStickers.setVisibility(View.GONE);
      txtSection.setVisibility(View.GONE);

      list = (UltimateRecyclerView) view.findViewById(R.id.recycler_view);
      list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
      loadFirstStickersPage();
      list.setRefreshing(true);
      list.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                         @Override
                                         public void onRefresh() {
                                           loadFirstStickersPage();

                                         }
                                       }
      );
      list.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
        @Override
        public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
          loadMoreStickers();
        }
      });
      return view;
    } else
    {
      return view;
    }
  }


  @SuppressWarnings ("unchecked")
  private void loadFirstStickersPage() {
    mCurrentPage = 1;
    Pagination.getStickers(mCurrentPage, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        // Reset items to make the pull to refresh right
        if (pageList.size() <= 0)
        {
          setEmtyView();
          list.setRefreshing(false);
          return;
        }
        stickers.clear();
        stickers.addAll((List<StickerPackage>) pageList);

        adapter = new StickersAdapter(getContext(), stickers);
        adapter.setListener(FragmentAllStickers.this);

        list.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        mCurrentPage++;

        if (pageList.size() != Pagination.PAGE_IN_REQUEST)
        {
          if (list.isLoadMoreEnabled())
          {
            list.disableLoadmore();
          }
        } else
        {
          if (!list.isLoadMoreEnabled())
          {
            list.enableLoadmore();
          }
        }
      }

      @Override
      public void onFailure() {
        list.setRefreshing(false);
        Utils.showSnack(new ISnackListener() {
          @Override
          public void onClick() {
            loadFirstStickersPage();
          }
        }, getActivity());
      }
    });
  }

  @SuppressWarnings ("unchecked")
  private void loadMoreStickers() {
    Pagination.getStickers(mCurrentPage, getContext(), new IPaginationCallback() {
      @Override
      public <T> void onPageReceived(List<T> pageList) {
        for (StickerPackage sticker : (List<StickerPackage>) pageList)
        {
          adapter.insertInternal(stickers, sticker, stickers.size());
        }

        mCurrentPage++;

        if (pageList.size() != Pagination.PAGE_IN_REQUEST)
        {
          if (list.isLoadMoreEnabled())
          {
            list.disableLoadmore();
          }
        } else
        {
          if (!list.isLoadMoreEnabled())
          {
            list.reenableLoadmore();
          }
        }
      }

      @Override
      public void onFailure() {
        Utils.showSnack(new ISnackListener() {
          @Override
          public void onClick() {
            loadMoreStickers();
          }
        }, getActivity());
      }
    });
  }


  @Override
  public void onStickerClick(final StickerPackage stickerPackage) {
    boolean exist = false;
    final BottomSheetDialog mDialog = new BottomSheetDialog(getContext());
    mDialog.contentView(R.layout.bottom_sheet_sticker)
      .inDuration(300)
      .cancelable(true);

    final com.rey.material.widget.ImageButton btnAdd = (com.rey.material.widget.ImageButton) mDialog.findViewById(R.id.btnAdd);
    if (Logged.Models.getUserStickerPackages(getContext()).size() > 0)
    {
      for (int i = 0; i < Logged.Models.getUserStickerPackages(getContext()).size(); i++)
      {
        if (Logged.Models.getUserStickerPackages(getContext()).get(i).getId().equalsIgnoreCase(stickerPackage.getId()))
        {

          exist = true;
          break;
        }
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
