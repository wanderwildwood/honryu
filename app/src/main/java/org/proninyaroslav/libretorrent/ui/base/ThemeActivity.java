package org.proninyaroslav.libretorrent.ui.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.proninyaroslav.libretorrent.R;
import org.proninyaroslav.libretorrent.core.RepositoryHelper;
import org.proninyaroslav.libretorrent.core.settings.SettingsRepository;
import org.proninyaroslav.libretorrent.core.utils.LocaleHelper;


public abstract class ThemeActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SettingsRepository pref;

    @Override
    protected void attachBaseContext(Context base) {
        SettingsRepository pref = RepositoryHelper.getSettingsRepository(base);
        String locale = pref.locale();
        Context context = LocaleHelper.wrapContext(base, locale);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        pref = RepositoryHelper.getSettingsRepository(getApplicationContext());
        enableEdgeToEdge();
        setupTheme();
        super.onCreate(savedInstanceState);
        pref.registerOnSettingsChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pref.unregisterOnSettingsChangeListener(this);
    }

    private void enableEdgeToEdge() {
        SystemBarStyle systemBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT);
        EdgeToEdge.enable(this, systemBarStyle, systemBarStyle);
    }

    // One theme, black on white; no dynamic colours or dark backgrounds on an E Ink panel.
    private void setupTheme() {
        setTheme(R.style.AppTheme);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key == null) {
            return;
        }
        if (key.equals(getString(R.string.pref_key_locale))) {
            onLocaleChange();
        }
    }

    private void onLocaleChange() {
        LocaleHelper.setLocale(pref.locale());
        recreate();
    }
}