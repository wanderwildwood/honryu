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

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.proninyaroslav.libretorrent.ui.base.EInkDialogBuilder;

import org.proninyaroslav.libretorrent.R;
import org.proninyaroslav.libretorrent.core.system.SystemFacadeHelper;
import org.proninyaroslav.libretorrent.databinding.DialogAboutBinding;
import org.proninyaroslav.libretorrent.ui.compose.LegalReaderKt;
import org.proninyaroslav.libretorrent.ui.compose.LegalRow;
import org.proninyaroslav.libretorrent.ui.compose.LicenceDataKt;
import org.proninyaroslav.libretorrent.ui.customviews.GuardedLinksKt;

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
        binding.privacy.setOnClickListener((v) -> LegalReaderKt.showLegalReader(
                activity,
                getString(R.string.honryu_about_privacy_title),
                LegalReaderKt.markdownRows(LegalReaderKt.readRaw(activity, R.raw.privacy))));
        binding.terms.setOnClickListener((v) -> LegalReaderKt.showLegalReader(
                activity,
                getString(R.string.honryu_about_terms_title),
                LegalReaderKt.markdownRows(LegalReaderKt.readRaw(activity, R.raw.terms))));
        binding.licences.setOnClickListener((v) -> showLicences(activity));
        binding.site.setOnClickListener((v) -> open("https://wanderthe.dev"));
        // Straight to the checkout: the site's own Donate button only leads there anyway.
        binding.llama.setOnClickListener((v) -> open("https://square.link/u/AGu8oT10"));

        return builder.create();
    }

    private void showLicences(Activity activity) {
        var rows = new ArrayList<>(LicenceDataKt.licenceHeader(
                getString(R.string.honryu_licences_source),
                getString(R.string.honryu_licences_licence),
                getString(R.string.honryu_licences_built_with)));
        // The GPL, then each other licence once.
        rows.add(new LegalRow("GNU General Public License", null, true));
        rows.addAll(LegalReaderKt.paragraphs(LegalReaderKt.readRaw(activity, R.raw.gpl_3_0)));
        var names = LicenceDataKt.getLicenceNames();
        if (names.contains("Apache-2.0")) {
            rows.add(new LegalRow("Apache License 2.0", null, true));
            rows.addAll(LegalReaderKt.paragraphs(LegalReaderKt.readRaw(activity, R.raw.apache_2_0)));
        }
        if (names.contains("MIT")) {
            rows.add(new LegalRow("MIT License", null, true));
            rows.addAll(LegalReaderKt.paragraphs(LegalReaderKt.readRaw(activity, R.raw.mit)));
        }
        if (names.contains("BSD-3-Clause")) {
            rows.add(new LegalRow("BSD 3-Clause License", null, true));
            rows.addAll(LegalReaderKt.paragraphs(LegalReaderKt.readRaw(activity, R.raw.bsd_3_clause)));
        }
        if (names.contains("OFL-1.1")) {
            rows.add(new LegalRow("SIL Open Font License 1.1 (Lato)", null, true));
            rows.addAll(LegalReaderKt.paragraphs(LegalReaderKt.readRaw(activity, R.raw.ofl_1_1)));
        }
        LegalReaderKt.showLegalReader(activity, getString(R.string.honryu_about_licences_title), rows);
    }

    private void open(String url) {
        GuardedLinksKt.openGuarded(requireContext(), url);
    }
}
