/*
 * This file is part of Torrents, a fork of LibreTorrent, and is under the GNU General Public
 * License, version 3 or later.
 */

package org.proninyaroslav.libretorrent.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.proninyaroslav.libretorrent.R

/** Where the source of this app lives. */
const val SOURCE_URL = "https://github.com/wanderwildwood/honryu"

/** Each distinct licence text once: family id and the raw resource. */
val LICENCE_RAW: List<Pair<String, Int>> = listOf(
    "gpl" to R.raw.licence_gpl3,
    "apache" to R.raw.licence_apache2,
    "ofl" to R.raw.licence_ofl,
)

@Composable
fun rememberLicencesData(): LicencesData = LicencesData(
    appName = "Torrents",
    sourceUrl = SOURCE_URL,
    licenceLine = stringResource(R.string.honryu_licences_licence),
    // One row per artifact in the APK, listed by the build (config/libraries adds what Gradle cannot see).
    components = rememberGeneratedComponents(R.raw.aboutlibraries),
    texts = LICENCE_RAW.map { (id, raw) ->
        LicenceText(id, stringResource(when (id) {
            "gpl" -> R.string.honryu_licences_full_gpl
            "apache" -> R.string.honryu_licences_full_apache
            "ofl" -> R.string.honryu_licences_full_ofl
            else -> R.string.honryu_licences_full_head
        }), raw)
    },
)

@Composable
fun rememberLicencesLabels(): LicencesLabels {
    val none = stringResource(R.string.honryu_about_no_browser_address, "@@")
    val noText = stringResource(R.string.honryu_licences_no_text, "@@")
    return LicencesLabels(
        title = stringResource(R.string.honryu_about_licences_title),
        back = stringResource(R.string.honryu_licences_back),
        sourceHeading = stringResource(R.string.honryu_licences_source_head),
        noText = { licence -> noText.replace("@@", licence) },
        noBrowser = { address -> none.replace("@@", address) },
    )
}
