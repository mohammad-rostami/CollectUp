/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.fragments.pagers;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.collect_up.c_up.R;
import com.collect_up.c_up.adapters.PersonInfoAdapter;
import com.collect_up.c_up.adapters.PersonTimelineAdapter;
import com.collect_up.c_up.fragments.FragmentShare;
import com.collect_up.c_up.helpers.AsyncHttpResponser;
import com.collect_up.c_up.helpers.AsyncTextHttpResponser;
import com.collect_up.c_up.helpers.Constants;
import com.collect_up.c_up.helpers.FragmentHandler;
import com.collect_up.c_up.helpers.GsonParser;
import com.collect_up.c_up.helpers.HttpClient;
import com.collect_up.c_up.helpers.Logged;
import com.collect_up.c_up.helpers.Pagination;
import com.collect_up.c_up.helpers.RToNonR;
import com.collect_up.c_up.helpers.SepehrUtil;
import com.collect_up.c_up.helpers.Utils;
import com.collect_up.c_up.helpers.fragmentType;
import com.collect_up.c_up.listeners.IPaginationCallback;
import com.collect_up.c_up.listeners.ISnackListener;
import com.collect_up.c_up.model.CompactMessage;
import com.collect_up.c_up.model.EnumMessageContentType;
import com.collect_up.c_up.model.Post;
import com.collect_up.c_up.model.Profile;
import com.collect_up.c_up.model.realm.RProfile;
import com.collect_up.c_up.view.PostContextMenu;
import com.collect_up.c_up.view.PostContextMenuManager;
import com.google.gson.Gson;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.rey.material.app.BottomSheetDialog;

import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import cz.msebera.android.httpclient.Header;
import io.realm.Realm;

public class PersonPagerFragment extends Fragment
        implements PersonTimelineAdapter.OnFeedItemClickListener,
        PostContextMenu.OnFeedContextMenuItemClickListener,
        PersonInfoAdapter.OnItemClick {
    public static final String ARG_OBJECT = "object";
    private final CopyOnWriteArrayList<Post> postList = new CopyOnWriteArrayList<>();
    private final String mCurrentUserId = Logged.Models.getUserProfile().getId();
    private PersonTimelineAdapter mAdapter;
    private int mCurrentPage = 1;
    private Profile mProfile;

    private UltimateRecyclerView mRecyclerView;
    private PersonInfoAdapter infoAdapter;

    private void notifyAdapter() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFirstPage() {
        // reset current page to make the pull to refresh right
        mCurrentPage = 1;
        Pagination.getProfilePosts(mCurrentPage, mProfile.getId(), getContext(), new IPaginationCallback() {
            @Override
            public <T> void onPageReceived(List<T> pageList) {
                // Reset items to make the pull to refresh right
                if (pageList.size() == 0) {
                    setEmtyView();

                    mAdapter = new PersonTimelineAdapter(getActivity(), PersonPagerFragment.this.postList);
                    mRecyclerView.setAdapter(mAdapter);
                    notifyAdapter();

                    return;
                }
                PersonPagerFragment.this.postList.clear();

                notifyAdapter();

                PersonPagerFragment.this.postList.addAll((List<Post>) pageList);
                mAdapter = new PersonTimelineAdapter(getActivity(), PersonPagerFragment.this.postList);
                mAdapter.setOnFeedItemClickListener(PersonPagerFragment.this);
                mRecyclerView.setAdapter(mAdapter);
                mCurrentPage++;

                notifyAdapter();

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
                        loadFirstPage();
                    }
                }, getActivity());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void loadMoreData() {
        Pagination.getProfilePosts(mCurrentPage, mProfile.getId(), getContext(), new IPaginationCallback() {
            @Override
            public <T> void onPageReceived(List<T> pageList) {
                for (Post post : (List<Post>) pageList) {
                    mAdapter.insertInternal(postList, post, postList.size());
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
                        loadMoreData();
                    }
                }, getActivity());
            }
        });
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();

        mProfile = args.getParcelable("profile");

        View rootView;

        if (args.getInt(ARG_OBJECT) == 0) {
            if (mProfile.getIsPrivate() && !mProfile.getId().equals(Logged.Models.getUserProfile().getId()) && !Logged.Models.getUserProfile().getFollowing().contains(mProfile.getId())) {
                // I must create a different layout because of UltimateRecyclerView which doesn't allow to change the empty view dynamically
                rootView = inflater.inflate(R.layout.fragment_tabs_account_is_private, container, false);

                mRecyclerView = (UltimateRecyclerView) rootView.findViewById(R.id.recycler_view);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(layoutManager);
                View view = mRecyclerView.getEmptyView();
                if (view != null) {
                    ViewGroup viewGroup = ((ViewGroup) view.getParent());
                    viewGroup.removeView(view);
                    viewGroup.addView(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_account_is_private, null));
                } else {
                    mRecyclerView.setEmptyView(R.layout.item_account_is_private);
                }
                mRecyclerView.setAdapter(null);
            } else {

                rootView = inflater.inflate(R.layout.fragment_tabs, container, false);

                mRecyclerView = (UltimateRecyclerView) rootView.findViewById(R.id.recycler_view);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(layoutManager);

                loadFirstPage();

                mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        loadFirstPage();
                    }
                });

                mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
                    @Override
                    public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                        loadMoreData();
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
        } else {
            rootView = inflater.inflate(R.layout.fragment_tabs, container, false);

            mRecyclerView = (UltimateRecyclerView) rootView.findViewById(R.id.recycler_view);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(layoutManager);

            infoAdapter = new PersonInfoAdapter(getActivity(), mProfile);
            infoAdapter.setListener(this);
            mRecyclerView.setAdapter(infoAdapter);

            getUpdatedProfileAsync();
        }

        return rootView;
    }

    private void getUpdatedProfileAsync() {
        if (getActivity() != null) {
            HttpClient.get(String.format(Constants.Server.Profile.GET_PROFILE, mProfile.getId()), new AsyncHttpResponser(getContext(), Looper.getMainLooper()) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    if (isVisible()) {
                        final Profile profile = GsonParser.getObjectFromGson(responseBody, Profile.class);
                        if (infoAdapter != null) {
                            infoAdapter.updateDataSet(profile);
                        }
                        Realm realm = Realm.getInstance(SepehrUtil.getRealmConfiguration(getContext()));
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                RProfile rProfile = realm.where(RProfile.class).equalTo("Id", profile.getId()).findFirst();
                                if (rProfile != null) {
                                    realm.copyToRealmOrUpdate(RToNonR.profileToRProfile(profile));
                                }
                            }
                        });

                        if (profile.getId().equals(Logged.Models.getUserProfile().getId())) {
                            Logged.Models.setUserProfile(profile);
                        }

                        Utils.addContact(getActivity(), profile);


                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    super.onFailure(statusCode, headers, responseBody, error);

                }
            });
        }

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
                HttpClient.post(getContext(), Constants.Server.Post.POST, new Gson().toJson(newPost, Post.class), "application/json", new AsyncTextHttpResponser(getContext()) {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Toast.makeText(getContext(), getContext().getString(R.string.toast_has_been_shared_on_profile), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers,
                                          byte[] responseBody,
                                          Throwable error) {
                        super.onFailure(statusCode, headers, responseBody, error);

                        Toast.makeText(getContext(), getString(R.string.toast_error_sharing_post), Toast.LENGTH_SHORT)
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
        message.setText(((Post) post).getSender().getName());
        message.setSendDateTime(Long.toString(System.currentTimeMillis()));
        message.setContentSize(new Gson().toJson(post, Post.class));
        intent.putExtra("message", message);
        FragmentHandler.replaceFragment(getContext(), fragmentType.SHARE, intent);
    }

    @Override
    public void onPostCancelClick() {
        PostContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onCommentsClick(View v, Post post) {
        FragmentHandler.replaceFragment(getContext(), fragmentType.DISPLAYPOST, post);
    }

    @Override
    public <T> void onMoreClick(View v, final String postId, final T post, final int position) {
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
    public void OnFollowersClick(String profileId) {
        Bundle bundle = new Bundle();
        bundle.putString("id", profileId);
        bundle.putInt("flag", 0);
        FragmentHandler.replaceFragment(getContext(), fragmentType.FOLLOWNFOLLOWING, bundle);

    }

    @Override
    public void OnFollowingClick(String profileId) {
        Bundle bundle = new Bundle();
        bundle.putString("id", profileId);
        bundle.putInt("flag", 1);
        FragmentHandler.replaceFragment(getContext(), fragmentType.FOLLOWNFOLLOWING, bundle);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RequestCodes.UPDATE_UI.ordinal()) {
            if (data.getParcelableExtra("post") != null) {
                if (mAdapter != null) {
                    mAdapter.updatePost((Post) data.getParcelableExtra("post"), data.getBooleanExtra("deleted", false));
                }
            }
        }
    }

    private void setEmtyView()

    {
        View view = mRecyclerView.getEmptyView();
        if (view != null) {
            ViewGroup viewGroup = ((ViewGroup) view.getParent());
            if (viewGroup != null) {
                viewGroup.removeView(view);
                viewGroup.addView(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.empty_list_view, null));
            }
        } else {
            mRecyclerView.setEmptyView(R.layout.empty_list_view);
        }
    }

    @Override
    public void onFollowClick(final Button view, final TextView totalFollowers, final String profileId) {
        final Hashtable<PersonInfoAdapter.ViewHolder, String> tag = (Hashtable<PersonInfoAdapter.ViewHolder, String>) view
                .getTag();
        final PersonInfoAdapter.ViewHolder holder = tag.keys().nextElement();

        //Prevent from multiple clicking on the view
        view.setEnabled(false);

        if (tag.values().iterator().next().equals("false")) {


            HttpClient.get(String.format(Constants.Server.Profile.GET_FOLLOW, profileId), new AsyncHttpResponser(getContext()) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    if (mProfile.getRequested() != null && mProfile.getRequested().contains(Logged.Models.getUserProfile().getId()) && mProfile.getIsPrivate()) {

                        tag.put(holder, "requested");
                        view.setTag(tag);
                        view.setText(R.string.requested);
                        view.setEnabled(true);
                        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.divider));
                    } else {

                        if (mProfile.getIsPrivate()) {
                            if (!mProfile.getRequested().contains(Logged.Models.getUserProfile().getId())) {
                                tag.put(holder, "requested");
                                view.setTag(tag);
                                view.setText(R.string.requested);
                                view.setEnabled(true);
                                view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.divider));
                            }
                        } else {

                            tag.put(holder, "true");
                            view.setTag(tag);
                            totalFollowers.setText(Integer.toString(Integer.parseInt(totalFollowers.getText()
                                    .toString()) + 1));
                            view.setText(R.string.unfollow);

                            Profile myProfile = Logged.Models.getUserProfile();
                            if (myProfile != null) {
                                myProfile.getFollowing().add(profileId);
                                Logged.Models.setUserProfile(myProfile);
                            }

                            view.setEnabled(true);
                        }
                    }
                }

                @Override
                public void onFailure(int statusCode,
                                      Header[] headers,
                                      byte[] responseBody,
                                      Throwable error) {
                    super.onFailure(statusCode, headers, responseBody, error);

                    Toast.makeText(getActivity(), getResources().getString(R.string.toast_error_following), Toast.LENGTH_SHORT)
                            .show();
                    view.setEnabled(true);
                }
            });
        } else if (tag.values().iterator().next().equals("true")) {
            HttpClient.get(String.format(Constants.Server.Profile.GET_UNFOLLOW, profileId), new AsyncHttpResponser(getContext()) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    tag.put(holder, "false");
                    view.setTag(tag);
                    totalFollowers.setText(Integer.toString(Integer.parseInt(totalFollowers.getText()
                            .toString()) - 1));
                    view.setText(R.string.follow);

                    Profile myProfile = Logged.Models.getUserProfile();
                    if (myProfile != null) {
                        myProfile.getFollowing().remove(profileId);
                        Logged.Models.setUserProfile(myProfile);
                    }

                    view.setEnabled(true);
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
        } else {
            HttpClient.get(String.format(Constants.Server.Request.CANCEL_REQUEST, Logged.Models.getUserProfile().getId(), mProfile.getId()), new AsyncHttpResponser(getContext()) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    tag.put(holder, "false");
                    view.setTag(tag);
                    if (Integer.parseInt(totalFollowers.getText().toString()) > 0) {
                        totalFollowers.setText(Integer.toString(Integer.parseInt(totalFollowers.getText()
                                .toString()) - 1));
                    }
                    view.setText(R.string.follow);
                    Profile myProfile = Logged.Models.getUserProfile();
                    if (myProfile != null) {
                        myProfile.getFollowing().remove(profileId);
                        Logged.Models.setUserProfile(myProfile);
                    }
                    myProfile.getRequested().remove(Logged.Models.getUserProfile().getId());
                    view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                    view.setEnabled(true);
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
}
