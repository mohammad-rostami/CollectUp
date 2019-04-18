/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.collect_up.c_up.R;
import com.collect_up.c_up.activities.ActivityHome;
import com.collect_up.c_up.activities.ActivityPickLocation;
import com.collect_up.c_up.adapters.SearchComplexesAdapter;
import com.collect_up.c_up.adapters.SearchProductsAdapter;
import com.collect_up.c_up.adapters.SearchShopsAdapter;
import com.collect_up.c_up.adapters.ShopEventsAdapter;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.PostCacheHandler;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.model.Complex;
import com.collect_up.c_up.model.Event;
import com.collect_up.c_up.model.Filter;
import com.collect_up.c_up.model.Product;
import com.collect_up.c_up.model.Shop;
import com.collect_up.c_up.services.Location;
import com.collect_up.c_up.view.BasicGridLayoutManager;
import com.collect_up.c_up.view.SpacesItemDecoration;
import com.google.android.gms.maps.model.LatLng;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.RadioButton;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ValidFragment")

public class FragmentSearch extends BaseFragment implements SearchView.OnQueryTextListener,
        View.OnClickListener,
        SearchProductsAdapter.ItemListener,
        SearchShopsAdapter.ItemListener,
        SearchComplexesAdapter.ItemListener,
        View.OnFocusChangeListener {
    public static boolean isRunning;


    private final int mColumns = 2;
    UltimateRecyclerView mRecyclerView;
    private List<Complex> mComplexes = new ArrayList<>();
    private List<Event> mEvents = new ArrayList<>();
    private ShopEventsAdapter mEventsAdapter;
    private SearchComplexesAdapter mComplexesAdapter;
    private SearchShopsAdapter mShopsAdapter;
    private SearchProductsAdapter mProductsAdapter;
    private int mCurrentPage = 1;
    private SimpleDialog mDialog;
    private Filter mFilter = new Filter();
    private BasicGridLayoutManager mLayoutManager;
    private List<Product> mProducts = new ArrayList<>();
    private SearchView mSearchView;
    private List<Shop> mShops = new ArrayList<>();
    private AppCompatEditText txtSearch;
    private boolean isfirts = true;
    private static final int OPTION_NONE = 0;
    private static final int OPTION_NEAREST = 1;
    private static final int OPTION_TOP_RATE = 2;
    private static final int OPTION_TOP_VIEW = 3;


    private static final String KEY_POSITION = "position";
    private int position = -1;
    private View view;
    private Menu mMenu;
    private MenuInflater mInflater;
    private RadioButton nearestRadio;
    private RadioButton topRateRadio;
    private RadioButton topviewRadio;
    private RadioButton noneRadio;
    private TextView locationName;

    private Activity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    public static FragmentSearch newInstance(int position) {
        FragmentSearch frag = new FragmentSearch();
        Bundle args = new Bundle();

        args.putInt(KEY_POSITION, position);
        frag.setArguments(args);

        return (frag);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        try {
            if (isVisibleToUser) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().show();

            }
        } catch (Exception ex) {
        }
    }


    @Override
    public void onStart() {
        isRunning = true;
        super.onStart();
    }

    private void getFirstPageByFilterMode() {
        mFilter.setSearchOption(Logged.Search.getFilterBy());
        if (Logged.Search.getSearchIn() == 1) {
            mRecyclerView.setRefreshing(true);
            loadFirstProductPage();
        } else if (Logged.Search.getSearchIn() == 2) {
            loadFirstShopPage();
        } else if (Logged.Search.getSearchIn() == 3) {
            loadFirstComplexPage();
        } else if (Logged.Search.getSearchIn() == 4) {
            loadFirstEventPage();
        }
    }

    private void getLoadMoreByFilterMode() {
        if (Logged.Search.getSearchIn() == 1) {
            loadMoreProducts();
        } else if (Logged.Search.getSearchIn() == 2) {
            loadMoreShops();
        } else if (Logged.Search.getSearchIn() == 3) {
            loadMoreComplexes();
        }
    }


    @SuppressWarnings("unchecked")
    public void loadFirstComplexPage() {
        // reset current page to make the pull to refresh right
        mCurrentPage = 1;
        Pagination.getComplexes(mCurrentPage, mFilter, getContext(), new IPaginationCallback() {
            @Override
            public <T> void onPageReceived(List<T> pageList) {
                // Reset items to make the pull to refresh right
                mComplexes.clear();

                mComplexes.addAll((List<Complex>) pageList);

                mComplexesAdapter = new SearchComplexesAdapter(getContext(), mComplexes);
                mComplexesAdapter.setListener(FragmentSearch.this);
                mLayoutManager = new BasicGridLayoutManager(getContext(), mColumns, mComplexesAdapter);

                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mComplexesAdapter);

                mComplexesAdapter.notifyDataSetChanged();

                mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getFirstPageByFilterMode();
                    }
                });

                mCurrentPage++;

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
                        loadFirstComplexPage();
                    }
                }, getActivity());
            }
        });
    }

    public void loadFirstEventPage() {
        // reset current page to make the pull to refresh right
        mCurrentPage = 1;
        Pagination.getEvents(mCurrentPage, mFilter, getContext(), new IPaginationCallback() {
            @Override
            public <T> void onPageReceived(List<T> pageList) {
                // Reset items to make the pull to refresh right
                mEvents.clear();

                mEvents.addAll((List<Event>) pageList);

                mEventsAdapter = new ShopEventsAdapter(getContext(), mEvents, null);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mRecyclerView.setAdapter(mEventsAdapter);

                mEventsAdapter.notifyDataSetChanged();

                mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getFirstPageByFilterMode();
                    }
                });

                mCurrentPage++;

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

    private void showProductPosts(List<Product> pageList) {
        isfirts = false;
        mProducts.clear();

        mProducts.addAll((List<Product>) pageList);

        mProductsAdapter = new SearchProductsAdapter(getContext(), mProducts);
        mProductsAdapter.setListener(FragmentSearch.this);
        mLayoutManager = new BasicGridLayoutManager(getContext(), mColumns, mProductsAdapter);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mProductsAdapter);
        mProductsAdapter.notifyDataSetChanged();

        mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFirstPageByFilterMode();
            }
        });

        mCurrentPage++;

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

    @SuppressWarnings("unchecked")
    public void loadFirstProductPage() {
        // reset current page to make the pull to refresh right
        if (PostCacheHandler.getProductPostCache() != null && isfirts) {
            showProductPosts(PostCacheHandler.getProductPostCache());

        }
        mCurrentPage = 1;
        Pagination.getProducts(mCurrentPage, mFilter, getContext(), new IPaginationCallback() {
            @Override
            public <T> void onPageReceived(List<T> pageList) {

                PostCacheHandler.setProductPostCache((List<Product>) pageList);
                showProductPosts((List<Product>) pageList);
                // Reset items to make the pull to refresh right

            }

            @Override
            public void onFailure() {
                mRecyclerView.setRefreshing(false);
                Utils.showSnack(new ISnackListener() {
                    @Override
                    public void onClick() {
                        loadFirstProductPage();
                    }
                }, getActivity());
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void loadFirstShopPage() {
        // reset current page to make the pull to refresh right
        mCurrentPage = 1;
        Pagination.getShops(mCurrentPage, mFilter, getContext(), new IPaginationCallback() {
            @Override
            public <T> void onPageReceived(List<T> pageList) {
                // Reset items to make the pull to refresh right
                mShops.clear();

                mShops.addAll((List<Shop>) pageList);

                mShopsAdapter = new SearchShopsAdapter(getContext(), mShops);
                mShopsAdapter.setListener(FragmentSearch.this);
                mLayoutManager = new BasicGridLayoutManager(getContext(), mColumns, mShopsAdapter);

                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mShopsAdapter);

                mShopsAdapter.notifyDataSetChanged();

                mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getFirstPageByFilterMode();
                    }
                });

                mCurrentPage++;

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
                        loadFirstShopPage();
                    }
                }, getActivity());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void loadMoreComplexes() {
        Pagination.getComplexes(mCurrentPage, mFilter, getContext(), new IPaginationCallback() {
            @Override
            public <T> void onPageReceived(List<T> pageList) {
                for (Complex post : (List<Complex>) pageList) {
                    mComplexesAdapter.insertInternal(mComplexes, post, mComplexes.size());
                }

                mCurrentPage++;

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
                        loadMoreComplexes();
                    }
                }, getActivity());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void loadMoreProducts() {
        Pagination.getProducts(mCurrentPage, mFilter, getContext(), new IPaginationCallback() {
            @Override
            public <T> void onPageReceived(List<T> pageList) {
                for (Product post : (List<Product>) pageList) {
                    mProductsAdapter.insertInternal(mProducts, post, mProducts.size());
                }

                mCurrentPage++;

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
                        loadMoreProducts();
                    }
                }, getActivity());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void loadMoreShops() {
        Pagination.getShops(mCurrentPage, mFilter, getContext(), new IPaginationCallback() {
            @Override
            public <T> void onPageReceived(List<T> pageList) {
                for (Shop post : (List<Shop>) pageList) {
                    mShopsAdapter.insertInternal(mShops, post, mShops.size());
                }

                mCurrentPage++;

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
                        loadMoreShops();
                    }
                }, getActivity());
            }
        });
    }

    @Override
    public void onComplexImageClick(Complex complex) {

        FragmentHandler.replaceFragment(getContext(), fragmentType.COMPLEX, complex);

    }

    @Override
    public void onStop() {
        isRunning = false;
        super.onStop();

    }

    @Override
    public void onResume() {

        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        ((ActivityHome) getActivity()).changeButtonBackgroud(1);

        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_search, container, false);
            mRecyclerView = (UltimateRecyclerView) view.findViewById(R.id.recycler_view);
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();

            int spacingInPixels = (int) getResources().getDimension(R.dimen.grid_spacing);
            setHasOptionsMenu(true);

            getFirstPageByFilterMode();

            mRecyclerView.addItemDecoration(new SpacesItemDecoration(Utils.dpToPx(spacingInPixels)));

            mRecyclerView.setPadding(0, 0, 0, 0);

            mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
                @Override
                public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                    getLoadMoreByFilterMode();
                }
            });

            return view;
        } else {
            getActivity().invalidateOptionsMenu();
            return view;
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);

        menu.findItem(R.id.action_search).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false);
        mSearchView.setOnQueryTextFocusChangeListener(this);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getContext(), ActivityHome.class)));
        mSearchView.clearFocus();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_filter:
                mDialog = new SimpleDialog(getContext());
                mDialog
                        .title(R.string.search_in)

                        .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                        .positiveAction(R.string.ok)
                        .negativeAction(R.string.cancel)
                        .actionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                        .setCancelable(true);
                mDialog.layoutParams(Utils.getScreenWidthPX(getContext()) - Utils.dpToPx(40), ViewGroup.LayoutParams.WRAP_CONTENT);
                mDialog.setContentView(R.layout.dialog_search_filter);
                mDialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);


                mDialog.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setSearchOption();
                        mFilter.setSearchText(txtSearch.getText().toString());
                        mSearchView.setQuery(txtSearch.getText().toString(), false);
                        mDialog.dismiss();
                        getFirstPageByFilterMode();
                    }
                });
                mDialog.negativeActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();

                    }
                });
                mDialog.show();
                initDialogViews();
                break;
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
        }
        return false;
    }

    private void setSearchOption() {
        int searchOption = OPTION_NONE;
        if (noneRadio.isChecked()) {
            searchOption = OPTION_NONE;
        } else if (nearestRadio.isChecked()) {
            searchOption = OPTION_NEAREST;
        } else if (topRateRadio.isChecked()) {
            searchOption = OPTION_TOP_RATE;
        } else if (topviewRadio.isChecked()) {
            searchOption = OPTION_TOP_VIEW;
        }
        mFilter.setSearchOption(searchOption);
        Logged.Search.setFilterBy(searchOption);
    }

    private void initDialogViews() {
        ImageButton productBtn = (ImageButton) mDialog.findViewById(R.id.button_product);
        ImageButton shopBtn = (ImageButton) mDialog.findViewById(R.id.button_shop);
        ImageButton complexBtn = (ImageButton) mDialog.findViewById(R.id.button_complex);
        ImageButton eventBtn = (ImageButton) mDialog.findViewById(R.id.button_events);

        nearestRadio = (RadioButton) mDialog.findViewById(R.id.radio_nearest);
        topRateRadio = (RadioButton) mDialog.findViewById(R.id.radio_top_rate);
        topviewRadio = (RadioButton) mDialog.findViewById(R.id.radio_top_view);
        noneRadio = (RadioButton) mDialog.findViewById(R.id.radio_none);
        noneRadio.setChecked(true);
        mFilter.setSearchOption(OPTION_NONE);
        Logged.Search.setFilterBy(OPTION_NONE);
        nearestRadio.setOnClickListener(this);
        topRateRadio.setOnClickListener(this);
        topviewRadio.setOnClickListener(this);
        noneRadio.setOnClickListener(this);
        locationName = (TextView) mDialog.findViewById(R.id.text_view_location_name);
        ImageButton selectLocation = (ImageButton) mDialog.findViewById(R.id.button_select_location);
        txtSearch = (AppCompatEditText) mDialog.findViewById(R.id.txtSearch);
        txtSearch.setTextColor(ContextCompat.getColor(getContext(), R.color.primary_text));

        if (Logged.Search.getLocationLat() == 0 && Logged.Search.getLocationLng() == 0) {
            Logged.Search.setLocationLat(Double.valueOf(Logged.Models.getUserProfile().getLat()));
            Logged.Search.setLocationLng(Double.valueOf(Logged.Models.getUserProfile().getLong()));
        }

        mFilter.setLatitude(Logged.Search.getLocationLat());
        mFilter.setLongitude(Logged.Search.getLocationLng());
        // if (Utils.isNullOrEmpty(Logged.Search.getLocationName()))
        // {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                final String countryAndCity = Location.getCountryAndCity(getContext(), new LatLng(Logged.Search.getLocationLat(), Logged.Search.getLocationLng()));
                Logged.Search.setLocationName(countryAndCity);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        locationName.setText(countryAndCity);
                    }
                });

                return null;
            }
        };
        asyncTask.execute();
    /*} else
    {
      locationName.setText(Logged.Search.getLocationName());
    }
*/
        if (Logged.Search.getSearchIn() == 1) {
            disableOtherButtons(productBtn);
        } else if (Logged.Search.getSearchIn() == 2) {
            disableOtherButtons(shopBtn);
        } else if (Logged.Search.getSearchIn() == 3) {
            disableOtherButtons(complexBtn);
        } else if (Logged.Search.getSearchIn() == 4) {
            disableOtherButtons(eventBtn);
        }

        productBtn.setOnClickListener(this);
        shopBtn.setOnClickListener(this);
        complexBtn.setOnClickListener(this);
        eventBtn.setOnClickListener(this);
        selectLocation.setOnClickListener(this);
        locationName.setOnClickListener(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            mSearchView.clearFocus();
        }
    }

    @Override
    public void onProductPictureClick(Product product) {
        FragmentHandler.replaceFragment(getContext(), fragmentType.PRODUCT, product);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getContext().startService(new Intent(getContext(), Location.class));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            final LatLng location = data.getParcelableExtra("location");

            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    final String countryAndCity = Location.getCountryAndCity(getContext(), new LatLng(location.latitude, location.longitude));
                    Logged.Search.setLocationName(countryAndCity);
                    mFilter.setLatitude(location.latitude);
                    mFilter.setLongitude(location.longitude);

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            locationName.setText(countryAndCity);
                        }
                    });

                    return null;
                }
            };
            asyncTask.execute();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mSearchView.clearFocus();
        mFilter.setSearchText(query.trim());

        getFirstPageByFilterMode();

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText.trim())) {
            onQueryTextSubmit("");
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_complex:
            case R.id.button_shop:
            case R.id.button_product:
            case R.id.button_events:
                disableOtherButtons(v);
                break;
            case R.id.button_select_location:
            case R.id.text_view_location_name:
                Intent intent = new Intent(getContext(), ActivityPickLocation.class);
                intent.putExtra("return", true);
                startActivityForResult(intent, Constants.RequestCodes.PICK_LOCATION.ordinal());
                break;
            case R.id.radio_top_rate:
                nearestRadio.setChecked(false);
                noneRadio.setChecked(false);
                topviewRadio.setChecked(false);
                break;
            case R.id.radio_nearest:
                noneRadio.setChecked(false);
                topRateRadio.setChecked(false);
                topviewRadio.setChecked(false);
                break;
            case R.id.radio_top_view:
                noneRadio.setChecked(false);
                nearestRadio.setChecked(false);
                topRateRadio.setChecked(false);
                break;
            case R.id.radio_none:
                nearestRadio.setChecked(false);
                topRateRadio.setChecked(false);
                topviewRadio.setChecked(false);
                break;

        }
    }

    public void disableOtherButtons(View view) {
        ImageButton complex = (ImageButton) mDialog.findViewById(R.id.button_complex);
        ImageButton shop = (ImageButton) mDialog.findViewById(R.id.button_shop);
        ImageButton product = (ImageButton) mDialog.findViewById(R.id.button_product);
        ImageButton event = (ImageButton) mDialog.findViewById(R.id.button_events);


        if (view.getId() == R.id.button_shop) {
            complex.setEnabled(true);
            complex.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bottom_sheet_menu_add_complex));

            product.setEnabled(true);
            product.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bottom_sheet_menu_add_product));

            event.setEnabled(true);
            event.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bottom_sheet_menu_add_event));

            shop.setEnabled(false);
            shop.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_button));
            shop.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_store));
            topviewRadio.setVisibility(View.GONE);
            Logged.Search.setSearchIn(2);
        } else if (view.getId() == R.id.button_complex) {
            shop.setEnabled(true);
            shop.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bottom_sheet_menu_add_shop));

            product.setEnabled(true);
            product.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bottom_sheet_menu_add_product));

            event.setEnabled(true);
            event.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bottom_sheet_menu_add_event));

            complex.setEnabled(false);
            complex.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_button));
            complex.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_business));

            Logged.Search.setSearchIn(3);
            topviewRadio.setVisibility(View.GONE);

        } else if (view.getId() == R.id.button_product) {
            shop.setEnabled(true);
            shop.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bottom_sheet_menu_add_shop));

            complex.setEnabled(true);
            complex.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bottom_sheet_menu_add_complex));

            event.setEnabled(true);
            event.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bottom_sheet_menu_add_event));

            product.setEnabled(false);
            product.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_button));
            product.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_basket));
            Logged.Search.setSearchIn(1);
            topviewRadio.setVisibility(View.VISIBLE);

        } else if (view.getId() == R.id.button_events) {
            shop.setEnabled(true);
            shop.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bottom_sheet_menu_add_shop));

            complex.setEnabled(true);
            complex.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bottom_sheet_menu_add_complex));

            product.setEnabled(true);
            product.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bottom_sheet_menu_add_product));

            event.setEnabled(false);
            event.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_button));
            event.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_event_white_24dp));
            Logged.Search.setSearchIn(4);
            topviewRadio.setVisibility(View.GONE);

        }
    }

    @Override
    public void onShopImageClick(Shop shop) {
        FragmentHandler.replaceFragment(getContext(), fragmentType.BUSINESS, shop);
    }


}
