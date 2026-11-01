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

/** Everything the APK contains that is not this fork's own, as taken from the Gradle dependencies. */
val LICENCE_COMPONENTS: List<LicenceComponent> = listOf(
    LicenceComponent("LibreTorrent 4.1.1, the app this is a fork of", "Yaroslav Pronin", "GPL-3.0-or-later", "https://gitlab.com/proninyaroslav/libretorrent"),
    LicenceComponent("libtorrent", "Arvid Norberg", "BSD-3-Clause", "https://www.libtorrent.org"),
    LicenceComponent("libtorrent4j", "Alden Torres", "MIT", "https://github.com/aldenml/libtorrent4j"),
    LicenceComponent("Mudita Mindful Design (MMD)", "Mudita", "Apache-2.0", "https://github.com/mudita/MMD"),
    LicenceComponent("Jetpack Compose and AndroidX (AppCompat, Fragment, Lifecycle, Navigation, Paging, Preference, RecyclerView, Room, WorkManager and others)", "Google", "Apache-2.0", "https://developer.android.com/jetpack"),
    LicenceComponent("Material Components for Android", "Google", "Apache-2.0", "https://github.com/material-components/material-components-android"),
    LicenceComponent("RxJava and RxAndroid", "ReactiveX", "Apache-2.0", "https://github.com/ReactiveX/RxJava"),
    LicenceComponent("Guava", "Google", "Apache-2.0", "https://github.com/google/guava"),
    LicenceComponent("Gson", "Google", "Apache-2.0", "https://github.com/google/gson"),
    LicenceComponent("Moshi", "Square", "Apache-2.0", "https://github.com/square/moshi"),
    LicenceComponent("Apache Commons IO and Commons Text", "The Apache Software Foundation", "Apache-2.0", "https://commons.apache.org"),
    LicenceComponent("JNA", "The JNA authors", "Apache-2.0", "https://github.com/java-native-access/jna"),
    LicenceComponent("ACRA", "The ACRA authors", "Apache-2.0", "https://github.com/ACRA/acra"),
    LicenceComponent("ColorPickerView", "skydoves", "Apache-2.0", "https://github.com/skydoves/ColorPickerView"),
    LicenceComponent("ExpandableLayout", "cachapa", "Apache-2.0", "https://github.com/cachapa/ExpandableLayout"),
    LicenceComponent("jurl", "Anthony Simon", "MIT", "https://github.com/anthonynsimon/jurl"),
    LicenceComponent("Lato typeface", "Lukasz Dziedzic", "OFL-1.1", "https://www.latofonts.com"),
)

/** Each distinct licence text once: family id and the raw resource. */
val LICENCE_RAW: List<Pair<String, Int>> = listOf(
    "gpl" to R.raw.licence_gpl3,
    "apache" to R.raw.licence_apache2,
    "mit" to R.raw.licence_mit,
    "bsd" to R.raw.licence_bsd3,
    "ofl" to R.raw.licence_ofl,
)

@Composable
fun rememberLicencesData(): LicencesData = LicencesData(
    appName = "Torrents",
    sourceUrl = SOURCE_URL,
    licenceLine = stringResource(R.string.honryu_licences_licence),
    components = LICENCE_COMPONENTS,
    texts = LICENCE_RAW.map { (id, raw) ->
        LicenceText(id, stringResource(when (id) {
            "gpl" -> R.string.honryu_licences_full_gpl
            "apache" -> R.string.honryu_licences_full_apache
            "mit" -> R.string.honryu_licences_full_mit
            "bsd" -> R.string.honryu_licences_full_bsd
            "ofl" -> R.string.honryu_licences_full_ofl
            else -> R.string.honryu_licences_full_head
        }), raw)
    },
)

@Composable
fun rememberLicencesLabels(): LicencesLabels {
    val none = stringResource(R.string.honryu_about_no_browser_address, "@@")
    return LicencesLabels(
        title = stringResource(R.string.honryu_about_licences_title),
        back = stringResource(R.string.honryu_licences_back),
        sourceHeading = stringResource(R.string.honryu_licences_source_head),
        licenceHeading = stringResource(R.string.honryu_licences_licence_head),
        builtWithHeading = stringResource(R.string.honryu_licences_built_with),
        fullTextsHeading = stringResource(R.string.honryu_licences_full_head),
        noBrowser = { address -> none.replace("@@", address) },
    )
}
