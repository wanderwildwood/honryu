package org.proninyaroslav.libretorrent.ui.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.DialogFragment
import com.mudita.mmd.ThemeMMD
import org.proninyaroslav.libretorrent.R
import org.proninyaroslav.libretorrent.core.system.SystemFacadeHelper
import org.proninyaroslav.libretorrent.ui.compose.AboutData
import org.proninyaroslav.libretorrent.ui.compose.AboutLabels
import org.proninyaroslav.libretorrent.ui.compose.AboutSection
import org.proninyaroslav.libretorrent.ui.compose.HouseAboutDialog
import org.proninyaroslav.libretorrent.ui.compose.LicencesScreen
import org.proninyaroslav.libretorrent.ui.compose.SOURCE_URL
import org.proninyaroslav.libretorrent.ui.compose.houseTypography
import org.proninyaroslav.libretorrent.ui.compose.monochrome
import org.proninyaroslav.libretorrent.ui.compose.rememberLicencesData
import org.proninyaroslav.libretorrent.ui.compose.rememberLicencesLabels

/**
 * This app's About: only the words and the address. The dialog, its one Close button, the
 * Licences row, the links and the llama are the shared HouseAbout.kt, the same in every app.
 * The fragment is an empty, transparent shell that the navigation graph can open; the dialog
 * itself is the Compose one.
 */
class AboutDialog : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ThemeMMD(colorScheme = monochrome, typography = houseTypography) { AboutContent(onDismiss = { dismissAllowingStateLoss() }) }
            }
        }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setLayout(1, 1)
        }
    }
}

@Composable
private fun AboutContent(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val version = SystemFacadeHelper.getSystemFacade(context.applicationContext).appVersionName ?: ""
    val data = AboutData(
        title = stringResource(R.string.honryu_about_title, version),
        sections = listOf(
            AboutSection(stringResource(R.string.honryu_about_privacy_title), stringResource(R.string.honryu_about_privacy)),
            AboutSection(stringResource(R.string.privacy_who_title), stringResource(R.string.privacy_who)),
            AboutSection(stringResource(R.string.privacy_contact_title), stringResource(R.string.privacy_contact)),
            AboutSection(stringResource(R.string.privacy_rights_title), stringResource(R.string.privacy_rights)),
            AboutSection(stringResource(R.string.honryu_about_terms_title), stringResource(R.string.honryu_about_terms)),
            AboutSection(null, stringResource(R.string.honryu_about_licence)),
            AboutSection(null, stringResource(R.string.honryu_about_after)),
        ),
        sourceUrl = SOURCE_URL,
    )
    val labels = AboutLabels(
        close = stringResource(R.string.honryu_about_close),
        licences = stringResource(R.string.honryu_about_licences_title),
        feedTheLlamas = stringResource(R.string.honryu_about_feed_the_llamas),
        noBrowser = { address -> context.getString(R.string.honryu_about_no_browser_address, address) },
        writeTo = { address -> context.getString(R.string.about_write_to, address) },
        noMailApp = { address -> context.getString(R.string.about_no_mail_app, address) },
    )
    HouseAboutDialog(data, labels, R.drawable.llama, onDismiss) { modifier, back ->
        LicencesScreen(rememberLicencesData(), rememberLicencesLabels(), onBack = back, modifier = modifier)
    }
}
