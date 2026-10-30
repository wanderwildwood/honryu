/*
 * This file is part of Torrents, a fork of LibreTorrent, and is under the GNU General Public
 * License, version 3 or later.
 */

package org.proninyaroslav.libretorrent.ui.compose

/** Where the source of this app lives. */
const val SOURCE_URL = "https://github.com/wanderwildwood/honryu"

/** A third-party component the app contains: its name, its licence, and where it comes from. */
data class Component(val name: String, val licence: String, val url: String)

/** Everything the APK contains that is not this fork's own, as taken from the Gradle dependencies. */
val builtWith: List<Component> = listOf(
    Component("LibreTorrent 4.1.1, by Yaroslav Pronin, the app this is a fork of", "GPL-3.0-or-later", "https://gitlab.com/proninyaroslav/libretorrent"),
    Component("libtorrent, by Arvid Norberg", "BSD-3-Clause", "https://www.libtorrent.org"),
    Component("libtorrent4j, by Alden Torres", "MIT", "https://github.com/aldenml/libtorrent4j"),
    Component("Mudita Mindful Design (MMD)", "Apache-2.0", "https://github.com/mudita/MMD"),
    Component("Jetpack Compose and AndroidX (AppCompat, Fragment, Lifecycle, Navigation, Paging, Preference, RecyclerView, Room, WorkManager and others)", "Apache-2.0", "https://developer.android.com/jetpack"),
    Component("Material Components for Android", "Apache-2.0", "https://github.com/material-components/material-components-android"),
    Component("RxJava and RxAndroid", "Apache-2.0", "https://github.com/ReactiveX/RxJava"),
    Component("Guava", "Apache-2.0", "https://github.com/google/guava"),
    Component("Gson", "Apache-2.0", "https://github.com/google/gson"),
    Component("Moshi", "Apache-2.0", "https://github.com/square/moshi"),
    Component("Apache Commons IO and Commons Text", "Apache-2.0", "https://commons.apache.org"),
    Component("JNA", "Apache-2.0", "https://github.com/java-native-access/jna"),
    Component("ACRA", "Apache-2.0", "https://github.com/ACRA/acra"),
    Component("ColorPickerView, by skydoves", "Apache-2.0", "https://github.com/skydoves/ColorPickerView"),
    Component("ExpandableLayout, by cachapa", "Apache-2.0", "https://github.com/cachapa/ExpandableLayout"),
    Component("jurl, by Anthony Simon", "MIT", "https://github.com/anthonynsimon/jurl"),
    Component("Lato typeface, by Lukasz Dziedzic", "OFL-1.1", "https://www.latofonts.com"),
)

/** The distinct licences of [builtWith], so each text is shown once. */
val licenceNames: List<String> = builtWith.map { it.licence }.distinct()

/** The rows of the Licences page, before the full texts: source, licence, then the Built-with list. */
fun licenceHeader(sourceLabel: String, licenceLine: String, builtWithLabel: String): List<LegalRow> =
    buildList {
        add(LegalRow(sourceLabel, link = SOURCE_URL))
        add(LegalRow(licenceLine))
        add(LegalRow(builtWithLabel, heading = true))
        builtWith.forEach { add(LegalRow("${it.name}\n${it.licence}", link = it.url)) }
    }
