package org.proninyaroslav.libretorrent.core.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class TorrentListTest {

    private val hashA = "0123456789abcdef0123456789abcdef01234567"
    private val hashB = "89abcdef0123456789abcdef0123456789abcdef"
    private val a = TorrentList.Entry(hashA, "Field Recordings, Spring", "magnet:?xt=urn:btih:$hashA&dn=Field", "content://downloads/tree/1", paused = false, sequential = true, firstLastPieces = false)
    private val b = TorrentList.Entry(hashB, "Public Domain Films", TorrentList.plainMagnet(hashB, "Public Domain Films"), "file:///storage/emulated/0/Download", paused = true, sequential = false, firstLastPieces = true)

    private fun kind(text: String) = assertThrows(TorrentList.BackupException::class.java) { TorrentList.read(text) }.kind

    @Test
    fun aListComesBackWhole() {
        val text = TorrentList.write(listOf(a, b), Instant.parse("2026-11-03T12:00:00Z"))
        assertEquals(listOf(a, b), TorrentList.read(text))
    }

    @Test
    fun anEmptyListIsStillABackup() {
        assertEquals(emptyList<TorrentList.Entry>(), TorrentList.read(TorrentList.write(emptyList())))
    }

    @Test
    fun theWrongFileIsRefused() {
        assertEquals(TorrentList.BackupException.Kind.NOT_A_BACKUP, kind("not json"))
        assertEquals(TorrentList.BackupException.Kind.NOT_A_BACKUP, kind("""{"format":"something-else","version":1,"torrents":[]}"""))
        assertEquals(TorrentList.BackupException.Kind.NEWER, kind("""{"format":"honryu-torrent-list","version":2,"torrents":[]}"""))
    }

    @Test
    fun oneBadEntrySpoilsTheWholeFile() {
        val good = TorrentList.write(listOf(a, b))
        assertEquals(TorrentList.BackupException.Kind.DAMAGED, kind(good.replace(hashB, "not-a-hash")))
        assertEquals(TorrentList.BackupException.Kind.DAMAGED, kind(good.replace("\"magnet:?xt=urn:btih:$hashA&dn=Field\"", "\"https://tracker.example/x\"")))
        assertEquals(TorrentList.BackupException.Kind.DAMAGED, kind(good.replace(hashB, hashA)))
        assertEquals(TorrentList.BackupException.Kind.DAMAGED, kind("""{"format":"honryu-torrent-list","version":1}"""))
    }

    @Test
    fun onlyTheMissingAreAdded() {
        assertEquals(listOf(b), TorrentList.toAdd(listOf(a, b), setOf(hashA)))
        // Hashes are compared without regard to case.
        assertEquals(emptyList<TorrentList.Entry>(), TorrentList.toAdd(listOf(a), setOf(hashA.uppercase())))
    }

    @Test
    fun aPlainMagnetNamesTheTorrent() {
        assertTrue(b.magnet.startsWith("magnet:?xt=urn:btih:$hashB&dn=Public%20Domain%20Films"))
    }
}
