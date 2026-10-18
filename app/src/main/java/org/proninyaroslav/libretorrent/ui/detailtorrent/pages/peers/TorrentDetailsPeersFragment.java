/*
 * Copyright (C) 2016-2025 Yaroslav Pronin <proninyaroslav@mail.ru>
 *
 * This file is part of LibreTorrent.
 *
 * LibreTorrent is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LibreTorrent is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LibreTorrent.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.proninyaroslav.libretorrent.ui.detailtorrent.pages.peers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.proninyaroslav.libretorrent.R;
import org.proninyaroslav.libretorrent.core.utils.Utils;
import org.proninyaroslav.libretorrent.core.utils.WindowInsetsSide;
import org.proninyaroslav.libretorrent.databinding.FragmentTorrentDetailsPeersBinding;
import org.proninyaroslav.libretorrent.ui.compose.ComposeList;
import org.proninyaroslav.libretorrent.ui.compose.Lists;
import org.proninyaroslav.libretorrent.ui.DebugSeed;
import org.proninyaroslav.libretorrent.ui.detailtorrent.TorrentDetailsViewModel;

import java.util.Collections;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/*
 * The fragment for displaying bittorrent peer list. Part of DetailTorrentFragment.
 */

public class TorrentDetailsPeersFragment extends Fragment {
    private AppCompatActivity activity;
    private FragmentTorrentDetailsPeersBinding binding;
    private TorrentDetailsViewModel viewModel;
    private final ComposeList<PeerItem> peers = new ComposeList<>();
    private final CompositeDisposable disposables = new CompositeDisposable();

    public static TorrentDetailsPeersFragment newInstance() {
        var fragment = new TorrentDetailsPeersFragment();
        fragment.setArguments(new Bundle());

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTorrentDetailsPeersBinding.inflate(inflater, container, false);

        if (Utils.isLargeScreenDevice(activity)) {
            Utils.applyWindowInsets(binding.peerList,
                    WindowInsetsSide.BOTTOM | WindowInsetsSide.LEFT | WindowInsetsSide.RIGHT);
        } else {
            Utils.applyWindowInsets(binding.peerList,
                    WindowInsetsSide.LEFT | WindowInsetsSide.RIGHT);
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof AppCompatActivity a) {
            activity = a;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        disposables.clear();
    }

    @Override
    public void onStart() {
        super.onStart();

        subscribeAdapter();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (activity == null) {
            activity = (AppCompatActivity) getActivity();
        }

        viewModel = new ViewModelProvider(requireParentFragment())
                .get(TorrentDetailsViewModel.class);

        peers.setEmptyView(binding.peerList, binding.emptyViewPeerList);
        Lists.peers(binding.peerList, peers, (item) -> sharePeerIp(item.ip));
    }

    private void subscribeAdapter() {
        disposables.add(viewModel.observePeers()
                .subscribeOn(Schedulers.io())
                .flatMapSingle((children) ->
                        Flowable.fromIterable(children)
                                .map(PeerItem::new)
                                .toList()
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((children) -> {
                    Collections.sort(children);
                    peers.submitList(DebugSeed.peers(activity, children));
                }));
    }

    private void sharePeerIp(String ip) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "ip");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, ip);

        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
    }
}
