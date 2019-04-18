/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.fragments.pagers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityPickLocation;
import com.collect_up.c_up.adapters.ShopEventsAdapter;
import com.collect_up.c_up.adapters.ShopInfoAdapter;
import com.collect_up.c_up.adapters.ShopProductsAdapter;
import com.collect_up.c_up.adapters.ShopTimelineAdapter;
import com.collect_up.c_up.fragments.FragmentBusiness;
import com.collect_up.c_up.fragments.FragmentShare;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.AsyncTextHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.model.CheckOut;
import com.collect_up.c_up.model.CompactMessage;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.EnumMessageContentType;
import com.collect_up.c_up.model.Event;
import com.collect_up.c_up.model.InternalCategory;
import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.model.Product;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.view.BasicGridLayoutManager;
import com.collect_up.c_up.view.EventContextMenu;
import com.collect_up.c_up.view.EventContextMenuManager;
import com.collect_up.c_up.view.PostContextMenu;
import com.collect_up.c_up.view.PostContextMenuManager;
import com.collect_up.c_up.view.SpacesItemDecoration;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.rey.material.app.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

public class BusinessPagerFragment extends Fragment implements ShopInfoAdapter.ItemListener,
        ShopEventsAdapter.OnFeedItemClickListener,
        EventContextMenu.OnFeedContextMenuItemClickListener,
        PostContextMenu.OnFeedContextMenuItemClickListener,
        ShopTimelineAdapter.OnFeedItemClickListener,
        ShopProductsAdapter.ItemListener {
    public static final String ARG_OBJECT = "object";
    private final int mColumns = 2;
    private final String mCurrentUserId = Logged.Models.getUserProfile().getId();
    private final List<Event> mShopEventList = new ArrayList<>();
    private final List<Post> mShopPostList = new ArrayList<>();
    private final List<Product> mShopProductList = new ArrayList<>();
    public UltimateRecyclerView mRecyclerView;
    public Shop mShop;
    public ShopEventsAdapter mShopEventsAdapter;
    ShopInfoAdapter infoAdapter;
    private int mCurrentEventPage = 1;
    private int mCurrentPostPage = 1;
    private int mCurrentProductPage = 1;
    private BasicGridLayoutManager mShopProductLayoutManager;
    private ShopProductsAdapter mShopProductsAdapter;
    private ShopTimelineAdapter mShopTimelineAdapter;
    private ArrayList<InternalCategory> internalCategories = new ArrayList<>();

    private void notifyEventAdapter() {
        if (mShopEventsAdapter != null) {
            mShopEventsAdapter.notifyDataSetChanged();
        }
    }

    // Suitable for reinitializing from onNewIntent()


    private void notifyTimelineAdapter() {
        if (mShopTimelineAdapter != null) {
            mShopTimelineAdapter.notifyDataSetChanged();

        }
    }

    private void notifyProductAdapter() {
        if (mShopProductsAdapter != null) {
            mShopProductsAdapter.notifyDataSetChanged();
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFirstEventPage() {
        // reset current page to make the pull to refresh right
        mCurrentEventPage = 1;
        Pagination.getShopEvents(mCurrentEventPage, mShop.getId(), getContext(), new IPaginationCallback() {
            @Override
            public <T> void onPageReceived(List<T> pageList) {
                if (pageList.size() == 0) {
                    setEmtyView();
                    mShopEventList.clear();
                    mShopEventsAdapter = new ShopEventsAdapter(getActivity(), mShopEventList, mShop);
                    mRecyclerView.setAdapter(mShopEventsAdapter);
                    notifyEventAdapter();

                    return;
                }
                // Reset items to make the pull to refresh right
                mShopEventList.clear();

                notifyEventAdapter();

                mShopEventList.addAll((List<Event>) pageList);
                mShopEventsAdapter = new ShopEventsAdapter(getActivity(), mShopEventList, mShop);
                mShopEventsAdapter.setOnFeedItemClickListener(BusinessPagerFragment.this);
                mRecyclerView.setAdapter(mShopEventsAdapter);
                mCurrentEventPage++;

                notifyEventAdapter();

                if (pageList.size() != Pagination.PAGE_IN_REQUEST) {
                    if (mRecyclerView.isLoadMoreEnabled()) {
                        mRecyclerView.disableLoadmore();
                    }
                } else {
                    if (!mRecyclerView.isLoadMoreEnabled()) {
                        mRecyclerView.enableLoadmore();
                    }
                }
            }

            @Override
            public void onFailure() {
                mRecyclerView.setRefreshing(false);
                Utils.showSnack(new ISnackListener() {
                    @Override
                    public void onClick() {
                        loadFirstEventPage();
                    }
                }, getActivity());
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void loadFirstPostPage() {
        // reset current page to make the pull to refresh right
        mCurrentPostPage = 1;
        Pagination.getShopPosts(mCurrentPostPage, mShop.getId(), getContext(), new IPaginationCallback() {
            @Override
            public <T> void onPageReceived(List<T> pageList) {
                if (pageList.size() == 0) {
                    setEmtyView();
                    mShopPostList.clear();
                    mShopTimelineAdapter = new ShopTimelineAdapter(getActivity(), mShopPostList);
                    mRecyclerView.setAdapter(mShopTimelineAdapter);
                    notifyTimelineAdapter();

                    return;
                }
                // Reset items to make the pull to refresh right
                mShopPostList.clear();

                notifyTimelineAdapter();

                mShopPostList.addAll((List<Post>) pageList);
                mShopTimelineAdapter = new ShopTimelineAdapter(getActivity(), mShopPostList);
                mShopTimelineAdapter.setOnFeedItemClickListener(BusinessPagerFragment.this);
                mRecyclerView.setAdapter(mShopTimelineAdapter);
                mCurrentPostPage++;

                notifyTimelineAdapter();

                if (pageList.size() != Pagination.PAGE_IN_REQUEST) {
                    if (mRecyclerView.isLoadMoreEnabled()) {
                        mRecyclerView.disableLoadmore();
                    }
                } else {
                    if (!mRecyclerView.isLoadMoreEnabled()) {
                        mRecyclerView.enableLoadmore();
                    }
                }
            }

            @Override
            public void onFailure() {
                mRecyclerView.setRefreshing(false);
                Utils.showSnack(new ISnackListener() {
                    @Override
                    public void onClick() {
                        loadFirstPostPage();
                    }
                }, getActivity());
            }
        });
    }


    @SuppressWarnings("unchecked")
    public void loadFirstProductPage(final String catId) {
        mCurrentProductPage = 1;


        // reset current page to make the pull to refresh right
        Pagination.getShopProducts(mCurrentProductPage, mShop.getId(), getContext(), catId, new IPaginationCallback() {
            @Override
            public <T> void onPageReceived(List<T> pageList) {
                if (pageList.size() == 0) {
                    setEmtyView();
                    mShopProductList.clear();
                    mShopProductsAdapter = new ShopProductsAdapter(getActivity(), mShopProductList);
                    mRecyclerView.setAdapter(mShopProductsAdapter);
                    notifyProductAdapter();

                    return;
                }
                // Reset items to make the pull to refresh right
                mShopProductList.clear();

                notifyProductAdapter();

                mShopProductList.addAll((List<Product>) pageList);

                mShopProductsAdapter = new ShopProductsAdapter(getActivity(), mShopProductList);
                mShopProductsAdapter.setListener(BusinessPagerFragment.this);
                mShopProductLayoutManager = new BasicGridLayoutManager(getActivity(), mColumns, mShopProductsAdapter);

                mRecyclerView.setLayoutManager(mShopProductLayoutManager);
                mRecyclerView.setAdapter(mShopProductsAdapter);
                mCurrentProductPage++;

                notifyProductAdapter();

                if (pageList.size() != Pagination.PAGE_IN_REQUEST) {
                    if (mRecyclerView.isLoadMoreEnabled()) {
                        mRecyclerView.disableLoadmore();
                    }
                } else {
                    if (!mRecyclerView.isLoadMoreEnabled()) {
                        mRecyclerView.enableLoadmore();
                    }
                }
            }

            @Override
            public void onFailure() {
                mRecyclerView.setRefreshing(false);
                Utils.showSnack(new ISnackListener() {
                    @Override
                    public void onClick() {
                        loadFirstProductPage(catId);
                    }
                }, getActivity());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void loadMoreEvents() {
        Pagination.getShopEvents(mCurrentEventPage, mShop.getId(), getContext(), new IPaginationCallback() {
            @Override
            public <T> void onPageReceived(List<T> pageList) {
                for (Event post : (List<Event>) pageList) {
                    mShopEventsAdapter.insertInternal(mShopEventList, post, mShopEventList.size());
                }
                mCurrentEventPage++;

                if (pageList.size() != Pagination.PAGE_IN_REQUEST) {
                    if (mRecyclerView.isLoadMoreEnabled()) {
                        mRecyclerView.disableLoadmore();
                    }
                } else {
                    if (!mRecyclerView.isLoadMoreEnabled()) {
                        mRecyclerView.reenableLoadmore();
                    }
                }
            }

            @Override
            public void onFailure() {
                Utils.showSnack(new ISnackListener() {
                    @Override
                    public void onClick() {
                        loadMoreEvents();
                    }
                }, getActivity());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void loadMorePosts() {
        Pagination.getShopPosts(mCurrentPostPage, mShop.getId(), getContext(), new IPaginationCallback() {
            @Override
            public <T> void onPageReceived(List<T> pageList) {
                for (Post post : (List<Post>) pageList) {
                    mShopTimelineAdapter.insertInternal(mShopPostList, post, mShopPostList.size());
                }
                mCurrentPostPage++;

                if (pageList.size() != Pagination.PAGE_IN_REQUEST) {
                    if (mRecyclerView.isLoadMoreEnabled()) {
                        mRecyclerView.disableLoadmore();
                    }
                } else {
                    if (!mRecyclerView.isLoadMoreEnabled()) {
                        mRecyclerView.reenableLoadmore();
                    }
                }
            }

            @Override
            public void onFailure() {
                Utils.showSnack(new ISnackListener() {
                    @Override
                    public void onClick() {
                        loadMorePosts();
                    }
                }, getActivity());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void loadMoreProducts(final String catId) {
        Pagination.getShopProducts(mCurrentProductPage, mShop.getId(), getContext(), catId, new IPaginationCallback() {
            @Override
            public <T> void onPageReceived(List<T> pageList) {
                for (Product post : (List<Product>) pageList) {
                    mShopProductsAdapter.insertInternal(mShopProductList, post, mShopProductList.size());
                }
                mCurrentProductPage++;

                if (pageList.size() != Pagination.PAGE_IN_REQUEST) {
                    if (mRecyclerView.isLoadMoreEnabled()) {
                        mRecyclerView.disableLoadmore();
                    }
                } else {
                    if (!mRecyclerView.isLoadMoreEnabled()) {
                        mRecyclerView.reenableLoadmore();
                    }
                }
            }

            @Override
            public void onFailure() {
                Utils.showSnack(new ISnackListener() {
                    @Override
                    public void onClick() {
                        loadMoreProducts(catId);
                    }
                }, getActivity());
            }
        });
    }

    @Override
    public void onCommentsClick(View v, Post post) {
        FragmentHandler.replaceFragment(getContext(), fragmentType.DISPLAYPOST, post);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RequestCodes.UPDATE_UI.ordinal()) {
            if (data.getParcelableExtra("post") != null) {
                if (mShopTimelineAdapter != null) {
                    mShopTimelineAdapter.updatePost((Post) data.getParcelableExtra("post"), data.getBooleanExtra("deleted", false));
                }
            } else if (data.getParcelableExtra("profile") != null) {
                if (mShopProductsAdapter != null) {
                    mShopProductsAdapter.updateProduct((Product) data.getParcelableExtra("profile"), data.getBooleanExtra("deleted", false));
                }
            }
        }
    }

    @Override
    public <T> void onTimelineMoreClick(View view, String postId, final T post, final int position) {
        final BottomSheetDialog mDialog = new BottomSheetDialog(getContext());
        mDialog.contentView(R.layout.bottom_sheet_share)
                .heightParam(ViewGroup.LayoutParams.WRAP_CONTENT)
                .inDuration(300)
                .cancelable(true);

        ImageButton btnShareOnProfile = (ImageButton) mDialog.findViewById(R.id.btnProfileShare);
        ImageButton btnShareChat = (ImageButton) mDialog.findViewById(R.id.btnChatShare);
        btnShareOnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShareOnProfile(position, post);
                mDialog.dismiss();

            }
        });
        btnShareChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShareToChats(position, post);
                mDialog.dismiss();

            }
        });
        mDialog.show();

    }

    @Override
    public void onLikesCountClick(String postId) {
        FragmentHandler.replaceFragment(getContext(), fragmentType.LIKES, postId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        int spacingInPixels = (int) getResources().getDimension(R.dimen.grid_spacing);

        View rootView = inflater.inflate(R.layout.fragment_tabs, container, false);

        Bundle args = getArguments();

        mShop = args.getParcelable("shop");

        mRecyclerView = (UltimateRecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setEmptyView(R.layout.empty_list_view);
        mRecyclerView.setHasFixedSize(true);

        // Timeline
        if (args.getInt(ARG_OBJECT) == 0) {
            loadFirstPostPage();

            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(layoutManager);

            mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadFirstPostPage();
                }
            });

            mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
                @Override
                public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                    loadMorePosts();
                }
            });
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    PostContextMenuManager.getInstance().onRecyclerViewScroll(dy);
                }
            });
        }
        // Products
        else if (args.getInt(ARG_OBJECT) == 1) {
            final String[] categoryId = new String[1];

            loadFirstProductPage(categoryId[0]);
            mRecyclerView.addItemDecoration(new SpacesItemDecoration(Utils.dpToPx(spacingInPixels)));
            final Spinner spinner = (Spinner) rootView.findViewById(R.id.spnCategory);
            spinner.setVisibility(View.VISIBLE);
            mShopProductsAdapter = new ShopProductsAdapter(getActivity(), mShopProductList);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    loadFirstProductPage(i == 0 ? null : internalCategories.get(i).getId());
                    categoryId[0] = i == 0 ? null : internalCategories.get(i).getId();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });


            HttpClient.get(String.format(Constants.Server.Shop.GET_INTERNALCATEGORY, mShop.getId()), new AsyncHttpResponser(getContext()) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    internalCategories.clear();
                    super.onSuccess(statusCode, headers, responseBody);
                    ArrayList<String> categoryStrings = new ArrayList<String>();
                    categoryStrings.add("All Categories");
                    InternalCategory[] cats = GsonParser.getArrayFromGson(responseBody, InternalCategory[].class);
                    if (cats != null) {
                        internalCategories.add(null);
                        for (int i = 0; i < cats.length; i++) {
                            internalCategories.add(cats[i]);

                        }
                        for (int i = 0; i < internalCategories.size(); i++) {
                            if (internalCategories.get(i) != null) {
                                categoryStrings.add(internalCategories.get(i).getName());
                            }
                        }
                        Activity activity = getActivity();
                        if (activity != null) {
                            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categoryStrings); //selected item will look like a spinner set from XML
                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(spinnerArrayAdapter);
                            spinner.setSelection(0);
                        }
                    } else {
                        Toast.makeText(getContext(), "Internal Error", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    super.onFailure(statusCode, headers, responseBody, error);
                }
            });
            BasicGridLayoutManager layoutManager = new BasicGridLayoutManager(getActivity(), mColumns, mShopProductsAdapter);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setPadding(0, 0, 0, 0);
            mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadFirstProductPage(categoryId[0]);
                }
            });

            mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
                @Override
                public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                    loadMoreProducts(categoryId[0]);
                }
            });
        }
        // Events
        else if (args.getInt(ARG_OBJECT) == 2) {
            loadFirstEventPage();

            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(layoutManager);

            mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadFirstEventPage();
                }
            });

            mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
                @Override
                public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                    loadMoreEvents();
                }
            });
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    EventContextMenuManager.getInstance().onRecyclerViewScroll(dy);
                }
            });
        }
        // Info
        else if (args.getInt(ARG_OBJECT) == 3) {

            infoAdapter = new ShopInfoAdapter(getActivity(), mShop);
            infoAdapter.setClickListener(this);

            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setAdapter(infoAdapter);

            getUpdatedShopAsync();
        }

        return rootView;
    }

    private void setEmtyView()

    {
        mRecyclerView.showEmptyView();
    }

    private void getUpdatedShopAsync() {
        HttpClient.get(String.format(Constants.Server.Shop.GET_SHOP, mShop.getId()), new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (isVisible()) {
                    final Shop shop = GsonParser.getObjectFromGson(responseBody, Shop.class);
                    if (infoAdapter != null) {
                        infoAdapter.updateDataSet(shop);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

            }
        });
    }

    @Override
    public void onDeleteClick(final int itemPosition, final String eventId) {
        final com.rey.material.app.SimpleDialog dialog = new com.rey.material.app.SimpleDialog(getActivity());
        dialog.message(getString(R.string.want_to_delete_event))
                .messageTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text))
                .title(R.string.are_you_sure)
                .titleColor(ContextCompat.getColor(getActivity(), R.color.colorAccent))
                .positiveAction(R.string.im_sure)
                .negativeAction(R.string.cancel)
                .actionTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent))
                .setCancelable(true);

        dialog.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });
        dialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpClient.delete(String.format(Constants.Server.Event.DELETE_BY_ID, eventId), new AsyncHttpResponser(getContext()) {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        mShopEventsAdapter.removeInternal(mShopEventList, itemPosition);
                        mShopEventsAdapter.notifyItemRemoved(itemPosition);
                        dialog.dismiss();

                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers,
                                          byte[] responseBody,
                                          Throwable error) {
                        super.onFailure(statusCode, headers, responseBody, error);

                        dialog.dismiss();


                    }
                });
            }
        });
        dialog.show();
    }

    @Override
    public void onEventCancelClick() {
        EventContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onEventMoreClick(View view, String eventId, int position) {
        onDeleteClick(position, eventId);

    }

    @Override
    public void onBusinessImageClick(FragmentBusiness view) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        Intent intent = new Intent(getActivity(), ActivityPickLocation.class);
        intent.putExtra("location", latLng);
        startActivityForResult(intent, Constants.RequestCodes.PICK_LOCATION.ordinal());
    }

    @Override
    public void onComplexClick(Complex complex) {
        FragmentHandler.replaceFragment(getContext(), fragmentType.COMPLEX, complex);
    }

    @Override
    public void onFollowersClick(String shopId) {

        Bundle bundle = new Bundle();
        bundle.putString("id", shopId);
        bundle.putInt("flag", 2);
        FragmentHandler.replaceFragment(getContext(), fragmentType.FOLLOWNFOLLOWING, bundle);

    }

    @Override
    public void onRatingBarChanged(final RatingBar[] ratingBar,
                                   float rating,
                                   boolean fromUser,
                                   final Shop shop,
                                   final TextView totalVotesView,
                                   final TextView txtAverage) {
        String url = String.format(Constants.Server.Shop.GET_RATE, shop.getId(), Float.toString(rating));
        HttpClient.get(url, new AsyncHttpResponser(getContext()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                CheckOut checkOut = GsonParser.getObjectFromGson(responseBody, CheckOut.class);
                totalVotesView.setText(Integer.toString(checkOut.getId()));
                String averageVotes = checkOut.getValue();
                float value = Float.valueOf(averageVotes);
                ratingBar[1].setRating(value);
                shop.setRatesAverage(checkOut.getValue());
                shop.setRatesCount(checkOut.getId());
                txtAverage.setText(averageVotes.matches("[0-9]+\\.[0-9]*") ? averageVotes : averageVotes + ".0");
                infoAdapter.setRatingColor(ratingBar[0], R.color.green_opacity, R.color.green);

            }

            @Override
            public void onFailure(int statusCode,
                                  Header[] headers,
                                  byte[] responseBody,
                                  Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                Toast.makeText(getActivity(), getString(R.string.toast_error_updating_rate), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }


    @Override
    public void onFollowClick(final Button view, final TextView totalFollowers, final Shop shop) {
        final Hashtable<ShopInfoAdapter.ShopInfoViewHolder, String> tags = (Hashtable<ShopInfoAdapter.ShopInfoViewHolder, String>) view
                .getTag();

        final ShopInfoAdapter.ShopInfoViewHolder holder = tags.keys().nextElement();

        //Prevent from multiple clicking on the view
        view.setEnabled(false);

        if (tags.values().iterator().next().equals("false")) {
            HttpClient.get(String.format(Constants.Server.Shop.GET_FOLLOW, shop.getId()), new AsyncHttpResponser(getContext()) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    tags.put(holder, "true");
                    view.setTag(tags);
                    view.setText(R.string.unfollow);
                    totalFollowers.setText(Integer.toString(Integer.parseInt(totalFollowers.getText()
                            .toString()) + 1));
                    view.setEnabled(true);
                    shop.getFollowers().add(Logged.Models.getUserProfile().getId());
                }

                @Override
                public void onFailure(int statusCode,
                                      Header[] headers,
                                      byte[] responseBody,
                                      Throwable error) {
                    super.onFailure(statusCode, headers, responseBody, error);

                    Toast.makeText(getActivity(), getActivity().getString(R.string.toast_error_following), Toast.LENGTH_SHORT)
                            .show();
                    view.setEnabled(true);
                }
            });
        } else {
            HttpClient.get(String.format(Constants.Server.Shop.GET_UNFOLLOW, shop.getId()), new AsyncHttpResponser(getContext()) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    tags.put(holder, "false");
                    view.setTag(tags);
                    view.setText(R.string.follow);
                    totalFollowers.setText(Integer.toString(Integer.parseInt(totalFollowers.getText()
                            .toString()) - 1));
                    view.setEnabled(true);
                    shop.getFollowers().remove(Logged.Models.getUserProfile().getId());
                }

                @Override
                public void onFailure(int statusCode,
                                      Header[] headers,
                                      byte[] responseBody,
                                      Throwable error) {
                    super.onFailure(statusCode, headers, responseBody, error);

                    Toast.makeText(getActivity(), getActivity().getString(R.string.toast_error_unfollowing), Toast.LENGTH_SHORT)
                            .show();
                    view.setEnabled(true);
                }
            });
        }
    }

    @Override
    public void onPostCancelClick() {
        PostContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onReportClick(final String postId, int feedItem) {
        final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getActivity());
        builder.message(R.string.sure_to_report_post)
                .messageTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text))
                .title(R.string.report_post)
                .titleColor(ContextCompat.getColor(getActivity(), R.color.colorAccent))
                .positiveAction(R.string.action_report)
                .negativeAction(R.string.cancel)
                .actionTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent))
                .setCancelable(true);

        builder.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();

            }
        });
        builder.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
                String f = String.format(Constants.Server.Post.GET_REPORT, postId);
                HttpClient.get(f, new AsyncHttpResponser(getContext()) {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.toast_successful_report), Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers,
                                          byte[] responseBody,
                                          Throwable error) {
                        super.onFailure(statusCode, headers, responseBody, error);

                        Toast.makeText(getActivity(), getActivity().getString(R.string.toast_error_report), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            }
        });
        builder.show();
    }

    @Override
    public <T> void onShareOnProfile(int feedItem, final T post) {
        final com.rey.material.app.SimpleDialog builder = new com.rey.material.app.SimpleDialog(getActivity());
        builder.message(R.string.sure_to_share_post_on_profile)
                .messageTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text))
                .title(R.string.share_post)
                .titleColor(ContextCompat.getColor(getActivity(), R.color.colorAccent))
                .positiveAction(R.string.share)
                .negativeAction(R.string.cancel)
                .actionTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent))
                .setCancelable(true);

        builder.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();

            }
        });
        builder.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
                Post oldPost = (Post) post;
                Post newPost = new Post();

                newPost.setSenderProfile(Logged.Models.getUserProfile());
                newPost.setImageAddress(oldPost.getImageAddress());
                newPost.setText(oldPost.getText());
                newPost.setVideoAddress(oldPost.getVideoAddress());
                newPost.setSize(oldPost.getSize());
                HttpClient.post(getActivity(), Constants.Server.Post.POST, new Gson().toJson(newPost, Post.class), "application/json", new AsyncTextHttpResponser(getContext()) {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Toast.makeText(getActivity(), R.string.toast_has_been_shared_on_profile, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers,
                                          byte[] responseBody,
                                          Throwable error) {
                        super.onFailure(statusCode, headers, responseBody, error);

                        Toast.makeText(getActivity(), getString(R.string.toast_error_sharing_post), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            }
        });
        builder.show();
    }

    @Override
    public <T> void onShareToChats(int feedItem, T post) {
        Intent intent = new Intent(getActivity(), FragmentShare.class);
        CompactMessage message = new CompactMessage();
        message.setId(UUID.randomUUID().toString());
        message.setSender(Logged.Models.getUserProfile());
        message.setContentType(EnumMessageContentType.SharedPost);
        message.setSendDateTime(Long.toString(System.currentTimeMillis()));
        message.setText(((Post) post).getSender().getName());
        message.setContentSize(new Gson().toJson(post, Post.class));
        intent.putExtra("message", message);
        FragmentHandler.replaceFragment(getContext(), fragmentType.SHARE, intent);
    }

    @Override
    public void onProductPictureClick(Product product) {

        FragmentHandler.replaceFragment(getActivity(), fragmentType.PRODUCT, product);

    }
}
