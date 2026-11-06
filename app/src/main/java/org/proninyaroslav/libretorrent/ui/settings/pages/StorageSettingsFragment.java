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

package org.proninyaroslav.libretorrent.ui.settings.pages;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.snackbar.Snackbar;

import org.proninyaroslav.libretorrent.MainActivity;
import org.proninyaroslav.libretorrent.R;
import org.proninyaroslav.libretorrent.core.RepositoryHelper;
import org.proninyaroslav.libretorrent.core.backup.TorrentList;
import org.proninyaroslav.libretorrent.core.backup.TorrentListFiles;
import org.proninyaroslav.libretorrent.core.exception.UnknownUriException;
import org.proninyaroslav.libretorrent.core.settings.SettingsRepository;
import org.proninyaroslav.libretorrent.core.system.FileSystemFacade;
import org.proninyaroslav.libretorrent.core.system.SystemFacadeHelper;
import org.proninyaroslav.libretorrent.core.utils.Utils;
import org.proninyaroslav.libretorrent.ui.NavBarFragment;
import org.proninyaroslav.libretorrent.ui.NavBarFragmentDirections;
import org.proninyaroslav.libretorrent.ui.filemanager.FileManagerConfig;
import org.proninyaroslav.libretorrent.ui.filemanager.FileManagerFragment;
import org.proninyaroslav.libretorrent.ui.compose.ArmedWatch;
import org.proninyaroslav.libretorrent.ui.compose.FilesCreateDocument;
import org.proninyaroslav.libretorrent.ui.compose.FilesOpenDocument;
import org.proninyaroslav.libretorrent.ui.settings.CustomPreferenceFragment;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class StorageSettingsFragment extends CustomPreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = StorageSettingsFragment.class.getSimpleName();

    private static final String KEY_CHOOSE_FOLDER_DIALOG_REQUEST = TAG + "_choose_folder_dialog";
    private static final String TAG_DIR_CHOOSER_BIND_PREF = "dir_chooser_bind_pref";

    private MainActivity activity;
    private SettingsRepository pref;
    private FileSystemFacade fs;
    /* Preference that is associated with the current dir selection dialog */
    private String dirChooserBindPref;

    private static final String KEY_SAVE_TORRENT_LIST = "pref_key_save_torrent_list";
    private static final String KEY_RESTORE_TORRENT_LIST = "pref_key_restore_torrent_list";
    private final Handler handler = new Handler(Looper.getMainLooper());
    /* The restore row stays armed, with no timer, until the person does something else:
       a tap anywhere else, Back (which only disarms) or leaving the screen. */
    private ArmedWatch armedWatch;
    private final Function0<Unit> disarmRestore = () -> {
        showRestoreArmed(false);
        return Unit.INSTANCE;
    };
    /* Added with this page's view, so it comes before the settings panes' own Back */
    private final OnBackPressedCallback backDisarms = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            armedWatch.disarm();
        }
    };

    private final ActivityResultLauncher<String> saveTorrentList = registerForActivityResult(
            new FilesCreateDocument("application/json"),
            (uri) -> {
                if (uri == null) return;
                var app = requireContext().getApplicationContext();
                inBackground(() -> {
                    boolean ok = TorrentListFiles.save(app, uri);
                    return app.getString(ok ? R.string.torrent_list_saved : R.string.torrent_list_not_saved);
                });
            });

    private final ActivityResultLauncher<String[]> restoreTorrentList = registerForActivityResult(
            new FilesOpenDocument(false),
            (uri) -> {
                if (uri == null) return;
                var app = requireContext().getApplicationContext();
                inBackground(() -> restoreMessage(app, TorrentListFiles.restore(app, uri)));
            });

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof MainActivity a) {
            activity = a;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.appBar.setTitle(R.string.pref_header_storage);
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), backDisarms);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (activity == null) {
            activity = (MainActivity) requireActivity();
        }
        armedWatch = ArmedWatch.Companion.of(activity);

        if (savedInstanceState != null) {
            dirChooserBindPref = savedInstanceState.getString(TAG_DIR_CHOOSER_BIND_PREF);
        }

        var context = activity.getApplicationContext();
        fs = SystemFacadeHelper.getFileSystemFacade(context);
        pref = RepositoryHelper.getSettingsRepository(context);

        String keySaveTorrentsIn = getString(R.string.pref_key_save_torrents_in);
        Preference saveTorrentsIn = findPreference(keySaveTorrentsIn);
        if (saveTorrentsIn != null) {
            String path = pref.saveTorrentsIn();
            if (path != null) {
                Uri uri = Uri.parse(path);
                try {
                    saveTorrentsIn.setSummary(fs.getDirPath(uri));
                } catch (UnknownUriException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                saveTorrentsIn.setOnPreferenceClickListener((preference) -> {
                    dirChooserBindPref = getString(R.string.pref_key_save_torrents_in);
                    dirChooseDialog(uri);

                    return true;
                });
            }
        }

        String keyMoveAfterDownload = getString(R.string.pref_key_move_after_download);
        SwitchPreferenceCompat moveAfterDownload = findPreference(keyMoveAfterDownload);
        if (moveAfterDownload != null) {
            moveAfterDownload.setChecked(pref.moveAfterDownload());
            bindOnPreferenceChangeListener(moveAfterDownload);
        }

        String keyMoveAfterDownloadIn = getString(R.string.pref_key_move_after_download_in);
        Preference moveAfterDownloadIn = findPreference(keyMoveAfterDownloadIn);
        if (moveAfterDownloadIn != null) {
            String path = pref.moveAfterDownloadIn();
            if (path != null) {
                Uri uri = Uri.parse(path);
                try {
                    moveAfterDownloadIn.setSummary(fs.getDirPath(uri));
                } catch (UnknownUriException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                moveAfterDownloadIn.setOnPreferenceClickListener((preference) -> {
                    dirChooserBindPref = getString(R.string.pref_key_move_after_download_in);
                    dirChooseDialog(uri);

                    return true;
                });
            }
        }

        String keySaveTorrentFiles = getString(R.string.pref_key_save_torrent_files);
        SwitchPreferenceCompat saveTorrentFiles = findPreference(keySaveTorrentFiles);
        if (saveTorrentFiles != null) {
            saveTorrentFiles.setChecked(pref.saveTorrentFiles());
            bindOnPreferenceChangeListener(saveTorrentFiles);
        }

        String keySaveTorrentFilesIn = getString(R.string.pref_key_save_torrent_files_in);
        Preference saveTorrentFilesIn = findPreference(keySaveTorrentFilesIn);
        if (saveTorrentFilesIn != null) {
            String path = pref.saveTorrentFilesIn();
            if (path != null) {
                Uri uri = Uri.parse(path);
                try {
                    saveTorrentFilesIn.setSummary(fs.getDirPath(uri));
                } catch (UnknownUriException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                saveTorrentFilesIn.setOnPreferenceClickListener((preference) -> {
                    dirChooserBindPref = getString(R.string.pref_key_save_torrent_files_in);
                    dirChooseDialog(uri);

                    return true;
                });
            }
        }

        String keyWatchDir = getString(R.string.pref_key_watch_dir);
        SwitchPreferenceCompat watchDir = findPreference(keyWatchDir);
        if (watchDir != null) {
            watchDir.setChecked(pref.watchDir());
            bindOnPreferenceChangeListener(watchDir);
        }

        String keyDirToWatch = getString(R.string.pref_key_dir_to_watch);
        Preference dirToWatch = findPreference(keyDirToWatch);
        if (dirToWatch != null) {
            String path = pref.dirToWatch();
            if (path != null) {
                Uri uri = Uri.parse(path);
                try {
                    dirToWatch.setSummary(fs.getDirPath(uri));
                } catch (UnknownUriException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                dirToWatch.setOnPreferenceClickListener((preference) -> {
                    dirChooserBindPref = getString(R.string.pref_key_dir_to_watch);
                    dirChooseDialog(uri);

                    return true;
                });
            }
        }

        String keyWatchDirDeleteFile = getString(R.string.pref_key_watch_dir_delete_file);
        SwitchPreferenceCompat watchDirDeleteFile = findPreference(keyWatchDirDeleteFile);
        if (watchDirDeleteFile != null) {
            watchDirDeleteFile.setChecked(pref.watchDirDeleteFile());
            bindOnPreferenceChangeListener(watchDirDeleteFile);
        }

        String keyPosixDiskIo = getString(R.string.pref_key_posix_disk_io);
        SwitchPreferenceCompat posixDiskIo = findPreference(keyPosixDiskIo);
        if (posixDiskIo != null) {
            posixDiskIo.setChecked(pref.posixDiskIo());
            bindOnPreferenceChangeListener(posixDiskIo);
        }

        Preference saveList = findPreference(KEY_SAVE_TORRENT_LIST);
        if (saveList != null) {
            saveList.setOnPreferenceClickListener((preference) -> {
                try {
                    saveTorrentList.launch("torrents-" + java.time.LocalDate.now() + ".json");
                } catch (Exception e) {
                    Log.w(TAG, "no file picker", e);
                }
                return true;
            });
        }
        Preference restoreList = findPreference(KEY_RESTORE_TORRENT_LIST);
        if (restoreList != null) {
            restoreList.setOnPreferenceClickListener((preference) -> {
                if (!armedWatch.isArmed(disarmRestore)) {
                    showRestoreArmed(true);
                    armedWatch.arm(disarmRestore);
                    return true;
                }
                armedWatch.done(disarmRestore);
                showRestoreArmed(false);
                try {
                    restoreTorrentList.launch(new String[]{"application/json", "text/plain", "application/octet-stream"});
                } catch (Exception e) {
                    Log.w(TAG, "no file picker", e);
                }
                return true;
            });
        }

        var navBarFragment = activity.findNavBarFragment(this);
        if (navBarFragment != null) {
            setChooseFolderDialogListener(navBarFragment);
        }
    }

    /* The first tap arms the row, which then says what a second tap does */
    private void showRestoreArmed(boolean armed) {
        Preference p = findPreference(KEY_RESTORE_TORRENT_LIST);
        if (p != null) {
            p.setTitle(armed ? R.string.restore_torrent_list_armed : R.string.restore_torrent_list);
        }
        backDisarms.setEnabled(armed);
    }

    @Override
    public void onDestroyView() {
        /* Leaving the screen disarms the row; nothing is left armed behind it */
        if (armedWatch != null) armedWatch.done(disarmRestore);
        super.onDestroyView();
    }

    private interface Work {
        String run();
    }

    /* Saving and restoring read and write files and the database: off the main thread, the outcome said in a toast */
    private void inBackground(Work work) {
        var app = requireContext().getApplicationContext();
        new Thread(() -> {
            String message = work.run();
            handler.post(() -> Toast.makeText(app, message, Toast.LENGTH_LONG).show());
        }, "torrent-list").start();
    }

    private static String restoreMessage(Context context, TorrentListFiles.Result r) {
        if (r instanceof TorrentListFiles.Result.Done done) {
            return context.getResources().getQuantityString(R.plurals.torrent_list_restored, done.getAdded(), done.getAdded(), done.getAlreadyHere());
        } else if (r instanceof TorrentListFiles.Result.Refused refused) {
            TorrentList.BackupException.Kind kind = refused.getKind();
            if (kind == TorrentList.BackupException.Kind.NEWER) {
                return context.getString(R.string.torrent_list_newer);
            } else if (kind == TorrentList.BackupException.Kind.DAMAGED) {
                return context.getString(R.string.torrent_list_damaged);
            }
            return context.getString(R.string.torrent_list_not_ours);
        } else if (r instanceof TorrentListFiles.Result.NotRunning) {
            return context.getString(R.string.torrent_list_not_running);
        }
        return context.getString(R.string.torrent_list_unreadable);
    }

    private void setChooseFolderDialogListener(@NonNull NavBarFragment navBarFragment) {
        navBarFragment.getParentFragmentManager().setFragmentResultListener(
                KEY_CHOOSE_FOLDER_DIALOG_REQUEST,
                this,
                (requestKey, result) -> {
                    FileManagerFragment.Result resultValue = result.getParcelable(FileManagerFragment.KEY_RESULT);
                    if (resultValue == null || resultValue.config().showMode != FileManagerConfig.Mode.DIR_CHOOSER) {
                        return;
                    }
                    var uri = resultValue.uri();
                    if (uri == null) {
                        Snackbar.make(
                                activity,
                                binding.coordinatorLayout,
                                getString(R.string.error_select_folder),
                                Snackbar.LENGTH_SHORT
                        ).show();
                    } else {
                        Preference p = findPreference(dirChooserBindPref);
                        if (p == null) {
                            return;
                        }
                        if (dirChooserBindPref.equals(getString(R.string.pref_key_dir_to_watch))) {
                            pref.dirToWatch(uri.toString());
                        } else if (dirChooserBindPref.equals(getString(R.string.pref_key_move_after_download_in))) {
                            pref.moveAfterDownloadIn(uri.toString());
                        } else if (dirChooserBindPref.equals(getString(R.string.pref_key_save_torrent_files_in))) {
                            pref.saveTorrentFilesIn(uri.toString());
                        } else if (dirChooserBindPref.equals(getString(R.string.pref_key_save_torrents_in))) {
                            pref.saveTorrentsIn(uri.toString());
                        }
                        try {
                            p.setSummary(fs.getDirPath(uri));
                        } catch (UnknownUriException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                }
        );
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(TAG_DIR_CHOOSER_BIND_PREF, dirChooserBindPref);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_storage, rootKey);
    }

    private void bindOnPreferenceChangeListener(Preference preference) {
        preference.setOnPreferenceChangeListener(this);
    }

    private void dirChooseDialog(Uri path) {
        String dirPath = null;
        if (path != null && Utils.isFileSystemPath(path)) {
            dirPath = path.getPath();
        }
        var config = new FileManagerConfig(
                dirPath,
                null,
                FileManagerConfig.Mode.DIR_CHOOSER
        );
        var action = NavBarFragmentDirections.actionOpenDirectoryDialog(
                config,
                KEY_CHOOSE_FOLDER_DIALOG_REQUEST
        );
        activity.getRootNavController().navigate(action);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(getString(R.string.pref_key_watch_dir))) {
            pref.watchDir((boolean) newValue);
        } else if (preference.getKey().equals(getString(R.string.pref_key_move_after_download))) {
            pref.moveAfterDownload((boolean) newValue);
        } else if (preference.getKey().equals(getString(R.string.pref_key_save_torrent_files))) {
            pref.saveTorrentFiles((boolean) newValue);
        } else if (preference.getKey().equals(getString(R.string.pref_key_watch_dir_delete_file))) {
            pref.watchDirDeleteFile((boolean) newValue);
        } else if (preference.getKey().equals(getString(R.string.pref_key_posix_disk_io))) {
            pref.posixDiskIo((boolean) newValue);
            Snackbar.make(
                    binding.coordinatorLayout,
                    R.string.apply_settings_after_reboot,
                    Snackbar.LENGTH_LONG
            ).show();
        }

        return true;
    }
}
