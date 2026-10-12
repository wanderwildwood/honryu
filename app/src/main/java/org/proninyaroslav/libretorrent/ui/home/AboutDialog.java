/*
 * Copyright (C) 2025 Yaroslav Pronin <proninyaroslav@mail.ru>
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

package org.proninyaroslav.libretorrent.ui.home;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.proninyaroslav.libretorrent.ui.base.EInkDialogBuilder;

import org.proninyaroslav.libretorrent.R;
import org.proninyaroslav.libretorrent.core.system.SystemFacadeHelper;
import org.proninyaroslav.libretorrent.databinding.DialogAboutBinding;

public class AboutDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        var activity = requireActivity();
        var binding = DialogAboutBinding.inflate(getLayoutInflater(), null, false);

        var builder = new EInkDialogBuilder(activity)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.honryu_about_close, (dialog, which) -> dismiss());

        String versionName = SystemFacadeHelper
                .getSystemFacade(activity.getApplicationContext())
                .getAppVersionName();
        binding.version.setText(getString(R.string.honryu_about_title,
                versionName != null ? versionName : ""));
        binding.site.setOnClickListener((v) -> open("https://wanderthe.dev"));
        // Straight to the checkout: the site's own Donate button only leads there anyway.
        binding.llama.setOnClickListener((v) -> open("https://square.link/u/AGu8oT10"));

        return builder.create();
    }

    private void open(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), R.string.honryu_about_no_browser, Toast.LENGTH_SHORT).show();
        }
    }
}
