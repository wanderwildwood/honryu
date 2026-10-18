/*
 * Copyright (C) 2020-2025 Yaroslav Pronin <proninyaroslav@mail.ru>
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

package org.proninyaroslav.libretorrent.ui.log;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.library.baseAdapters.BR;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import org.proninyaroslav.libretorrent.MainActivity;
import org.proninyaroslav.libretorrent.R;
import org.proninyaroslav.libretorrent.core.logger.LogEntry;
import org.proninyaroslav.libretorrent.core.utils.Utils;
import org.proninyaroslav.libretorrent.core.utils.WindowInsetsSide;
import org.proninyaroslav.libretorrent.databinding.FragmentLogBinding;
import org.proninyaroslav.libretorrent.ui.NavBarFragment;
import org.proninyaroslav.libretorrent.ui.NavBarFragmentDirections;
import org.proninyaroslav.libretorrent.ui.filemanager.FileManagerConfig;
import org.proninyaroslav.libretorrent.ui.filemanager.FileManagerFragment;

import org.proninyaroslav.libretorrent.ui.compose.ComposeList;
import org.proninyaroslav.libretorrent.ui.compose.Lists;
import org.proninyaroslav.libretorrent.ui.compose.OnRow;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LogFragment extends Fragment {
    private static final String TAG = LogFragment.class.getSimpleName();

    private static final String TAG_AUTO_SCROLL = "auto_scroll";
    private static final long LOG_REFRESH_MS = 500;

    private static final String KEY_SAVE_LOG_CHOOSE_DIALOG_REQUEST = TAG + "_save_log_choose_dialog";

    private MainActivity activity;
    private FragmentLogBinding binding;
    private LogViewModel viewModel;
    private final ComposeList<LogEntry> entries = new ComposeList<>();
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof MainActivity a) {
            activity = a;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        var navBarFragment = activity.findNavBarFragment(this);
        if (navBarFragment != null) {
            setSaveLogChooseDialogListener(navBarFragment);
        }
    }

    private void setSaveLogChooseDialogListener(NavBarFragment navBarFragment) {
        navBarFragment.getParentFragmentManager().setFragmentResultListener(
                KEY_SAVE_LOG_CHOOSE_DIALOG_REQUEST,
                this,
                (requestKey, result) -> {
                    FileManagerFragment.Result resultValue = result.getParcelable(FileManagerFragment.KEY_RESULT);
                    if (resultValue == null || resultValue.config().showMode != FileManagerConfig.Mode.SAVE_FILE) {
                        return;
                    }

                    var uri = resultValue.uri();
                    if (uri == null) {
                        viewModel.stopLogRecording();
                        resumeLog();
                    } else {
                        viewModel.saveLog(uri);
                    }
                    invalidateMenu();
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_log, container, false);

        if (Utils.isLargeScreenDevice(activity)) {
            Utils.applyWindowInsets(binding.logList, WindowInsetsSide.BOTTOM | WindowInsetsSide.LEFT | WindowInsetsSide.RIGHT);
        } else {
            Utils.applyWindowInsets(binding.logList, WindowInsetsSide.LEFT | WindowInsetsSide.RIGHT);
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (activity == null) {
            activity = (MainActivity) requireActivity();
        }

        entries.setFollow(savedInstanceState == null
                || savedInstanceState.getBoolean(TAG_AUTO_SCROLL, true));

        viewModel = new ViewModelProvider(this).get(LogViewModel.class);
        binding.setViewModel(viewModel);

        updateToolbarSubtitle();

        /* The newest entries are at the foot, and the list keeps to its last page until paged back */
        entries.setEmptyView(binding.logList, binding.emptyViewLog);
        Lists.log(binding.logList, entries, clickListener);

        binding.appBar.setNavigationOnClickListener((v) ->
                activity.getOnBackPressedDispatcher().onBackPressed());
        binding.appBar.setOnMenuItemClickListener(this::onMenuItemSelected);
        invalidateMenu();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        resumeLog();
    }

    @Override
    public void onStop() {
        super.onStop();

        disposables.clear();
        viewModel.mutableParams.removeOnPropertyChangedCallback(paramsCallback);
    }

    @Override
    public void onStart() {
        super.onStart();

        subscribeLogList();
        viewModel.mutableParams.addOnPropertyChangedCallback(paramsCallback);
    }

    private void subscribeLogList() {
        disposables.add(viewModel.observeDataSetChanged()
                .map((change) -> true)
                .startWithItem(true)
                /* A busy session writes many entries a second; the panel takes a few redraws */
                .throttleLatest(LOG_REFRESH_MS, TimeUnit.MILLISECONDS, true)
                .observeOn(Schedulers.io())
                .map((v) -> viewModel.getLogEntries())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((list) -> {
                    entries.submitList(list);
                    updateToolbarSubtitle();
                })
        );
    }

    private final androidx.databinding.Observable.OnPropertyChangedCallback paramsCallback =
            new androidx.databinding.Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(androidx.databinding.Observable sender, int propertyId) {
                    if (propertyId == BR.logging) {
                        invalidateMenu();
                    }
                }
            };

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(TAG_AUTO_SCROLL, entries.getFollow());

        super.onSaveInstanceState(outState);
    }

    private void invalidateMenu() {
        var menu = binding.appBar.getMenu();

        menu.setGroupVisible(R.id.log_menu, viewModel.mutableParams.isLogging());

        MenuItem pauseResume = menu.findItem(R.id.pause_resume_log_menu);
        if (viewModel.logPausedManually()) {
            pauseResume.setIcon(R.drawable.ic_play_arrow_24px);
            pauseResume.setTitle(R.string.resume_torrent);
        } else {
            pauseResume.setIcon(R.drawable.ic_pause_24px);
            pauseResume.setTitle(R.string.pause_torrent);
        }

        MenuItem record = menu.findItem(R.id.record_log_menu);
        if (viewModel.logRecording()) {
            record.setIcon(R.drawable.ic_stop_circle_24px);
            record.setTitle(R.string.journal_stop_recording);
        } else {
            record.setIcon(R.drawable.ic_screen_record_24px);
            record.setTitle(R.string.journal_start_recording);
        }

        binding.appBar.invalidateMenu();
    }

    private boolean onMenuItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.pause_resume_log_menu) {
            pauseResumeLog();
        } else if (itemId == R.id.record_log_menu) {
            toggleRecord();
        } else if (itemId == R.id.save_log_menu) {
            saveLogPathChooseDialog();
        } else if (itemId == R.id.filter_log_menu) {
            showFilterDialog();
        } else if (itemId == R.id.log_settings_menu) {
            showLogSettings();
        }

        return true;
    }

    private void showFilterDialog() {
        var action = LogFragmentDirections.actionLogFilterDialog();
        NavHostFragment.findNavController(this).navigate(action);
    }

    private void showLogSettings() {
        var action = LogFragmentDirections.actionOpenLogSettings();
        NavHostFragment.findNavController(this).navigate(action);
    }

    private void pauseResumeLog() {
        boolean newPausedState = !viewModel.logPausedManually();
        if (newPausedState) {
            viewModel.pauseLogManually();
        } else {
            viewModel.resumeLogManually();
        }

        invalidateMenu();
    }

    private void resumeLog() {
        if (!viewModel.logPausedManually()) {
            viewModel.resumeLog();
        }
    }

    private void toggleRecord() {
        boolean recording = !viewModel.logRecording();

        if (recording && viewModel.logPausedManually()) {
            return;
        }

        if (recording) {
            Snackbar.make(binding.coordinatorLayout,
                            getString(R.string.journal_started_recording),
                            Snackbar.LENGTH_SHORT)
                    .show();

            viewModel.startLogRecording();

            invalidateMenu();

        } else {
            viewModel.pauseLog();
            saveLogPathChooseDialog();
        }
    }

    private void saveLogPathChooseDialog() {
        var config = new FileManagerConfig(
                null,
                getString(R.string.pref_journal_save_log_to),
                FileManagerConfig.Mode.SAVE_FILE
        );
        config.fileName = viewModel.getSaveLogFileName();
        config.mimeType = Utils.MIME_TEXT_PLAIN;
        var action = NavBarFragmentDirections.actionSaveFileChooseDialog(
                config,
                KEY_SAVE_LOG_CHOOSE_DIALOG_REQUEST
        );
        activity.getRootNavController().navigate(action);
    }

    private void updateToolbarSubtitle() {
        int numEntries = viewModel.getLogEntriesCount();
        if (numEntries > 1) {
            binding.appBar.setSubtitle(Integer.toString(numEntries));
        } else {
            binding.appBar.setSubtitle(null);
        }
    }

    private final OnRow<LogEntry> clickListener = (entry) -> {
        if (viewModel.copyLogEntryToClipboard(entry)) {
            Snackbar.make(binding.coordinatorLayout,
                            R.string.text_copied_to_clipboard,
                            Snackbar.LENGTH_SHORT)
                    .show();
        }
    };
}
