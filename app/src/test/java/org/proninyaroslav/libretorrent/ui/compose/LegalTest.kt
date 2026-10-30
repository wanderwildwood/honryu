package org.proninyaroslav.libretorrent.ui.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LegalTest {
    private fun repoFile(name: String): File {
        val root = if (File("src/main").exists()) File("..") else File(".")
        return File(root, name)
    }

    private fun raw(name: String): File {
        val module = if (File("src/main").exists()) File(".") else File("app")
        return File(module, "src/main/res/raw/$name")
    }

    @Test
    fun theBundledTextsAreTheRepositoryOnes() {
        assertEquals(repoFile("PRIVACY.md").readText(), raw("privacy.txt").readText())
        assertEquals(repoFile("TERMS.md").readText(), raw("terms.txt").readText())
        assertEquals(repoFile("LICENSE.md").readText(), raw("gpl_3_0.txt").readText())
    }

    @Test
    fun everyLicenceInTheListHasItsTextBundled() {
        val files = mapOf(
            "Apache-2.0" to "apache_2_0.txt",
            "MIT" to "mit.txt",
            "BSD-3-Clause" to "bsd_3_clause.txt",
            "OFL-1.1" to "ofl_1_1.txt",
        )
        licenceNames.filter { it != "GPL-3.0-or-later" }.forEach {
            assertTrue("no text for $it", files.containsKey(it) && raw(files.getValue(it)).length() > 500)
        }
    }

    @Test
    fun theBuiltWithListIsNotEmptyAndEveryLinkIsHttps() {
        assertTrue(builtWith.size > 10)
        builtWith.forEach { assertTrue(it.url, it.url.startsWith("https://")) }
    }

    @Test
    fun theHeaderStartsWithTheSourceLink() {
        val rows = licenceHeader("Source", "Licence", "Built with")
        assertEquals(SOURCE_URL, rows.first().link)
        assertEquals("https://github.com/wanderwildwood/honryu", SOURCE_URL)
        assertTrue(rows.size > builtWith.size)
    }

    @Test
    fun paragraphsSplitOnBlankLines() {
        val rows = paragraphs("one\ntwo\n\n  \nthree\r\n\r\nfour")
        assertEquals(listOf("one\ntwo", "three", "four"), rows.map { it.text })
        assertFalse(rows.any { it.link != null })
    }

    @Test
    fun theGplTextIsPagedIntoManyRows() {
        assertTrue(paragraphs(raw("gpl_3_0.txt").readText()).size > 50)
    }

    @Test
    fun markdownShowsNoMarkup() {
        val rows = markdownRows(
            "# Title\n\nSome **bold** and `code` text\ncontinues here.\n\n- one\n- two\n\n| permission | why |\n|---|---|\n| `INTERNET` | to download |\n"
        )
        assertEquals("Title", rows[0].text)
        assertTrue(rows[0].heading)
        assertEquals("Some bold and code text continues here.", rows[1].text)
        assertEquals("\u2022 one", rows[2].text)
        assertEquals("\u2022 two", rows[3].text)
        assertEquals("INTERNET: to download", rows[4].text)
        assertEquals(5, rows.size)
    }

    @Test
    fun thePrivacyAndTermsTextsHaveNoMarkdownLeft() {
        for (name in listOf("privacy.txt", "terms.txt")) {
            val rows = markdownRows(raw(name).readText())
            assertTrue(rows.size > 5)
            rows.forEach {
                assertFalse(it.text, it.text.contains("**") || it.text.startsWith("#") || it.text.contains("`"))
            }
        }
    }
}
