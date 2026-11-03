package org.proninyaroslav.libretorrent.core.backup

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import org.proninyaroslav.libretorrent.core.RepositoryHelper
import org.proninyaroslav.libretorrent.core.model.AddTorrentParams
import org.proninyaroslav.libretorrent.core.model.TorrentEngine
import org.proninyaroslav.libretorrent.core.model.data.entity.Torrent
import org.proninyaroslav.libretorrent.core.system.SystemFacadeHelper

/**
 * Saving and restoring the torrent list against a file the person picked: the Android half of
 * [TorrentList]. Both run off the main thread.
 */
object TorrentListFiles {

    sealed interface Result {
        data class Done(val added: Int, val alreadyHere: Int) : Result
        data class Refused(val kind: TorrentList.BackupException.Kind) : Result
        data object Unreadable : Result
        /** The torrent engine did not start, so nothing could be added. */
        data object NotRunning : Result
    }

    /** Writes the list to [uri]; false, with the file removed, when that fails. */
    @JvmStatic
    fun save(context: Context, uri: Uri): Boolean {
        val app = context.applicationContext
        val engine = TorrentEngine.getInstance(app)
        val ok = runCatching {
            val entries = RepositoryHelper.getTorrentRepository(app).allTorrents.map { entry(engine, it) }
            val text = TorrentList.write(entries)
            app.contentResolver.openOutputStream(uri, "wt")?.use { it.write(text.toByteArray(Charsets.UTF_8)) } ?: error("no stream")
        }.isSuccess
        if (!ok) runCatching { DocumentsContract.deleteDocument(app.contentResolver, uri) }
        return ok
    }

    private fun entry(engine: TorrentEngine, t: Torrent) = TorrentList.Entry(
        hash = t.id,
        name = t.name,
        // The engine's own link names the trackers too; a torrent it has not loaded gets a bare one.
        magnet = runCatching { engine.makeMagnet(t.id, false) }.getOrNull()?.takeIf { it.startsWith("magnet:?") }
            ?: t.magnet?.takeIf { it.startsWith("magnet:?") }
            ?: TorrentList.plainMagnet(t.id, t.name),
        downloadPath = t.downloadPath.toString(),
        paused = t.manuallyPaused,
        sequential = t.sequentialDownload,
        firstLastPieces = t.firstLastPiecePriority,
    )

    /**
     * Reads the whole file first; only a file that passes adds anything. Torrents already in
     * the list are left as they are; the rest are added by their magnet links, saving where they
     * saved before when that folder can still be reached, or else in the folder Settings names.
     * A file over 5 MB is refused unread: a list of thousands is far smaller.
     */
    @JvmStatic
    fun restore(context: Context, uri: Uri): Result {
        val app = context.applicationContext
        val bytes = runCatching {
            app.contentResolver.openInputStream(uri)?.use { readCapped(it) }
        }.getOrNull() ?: return Result.Unreadable
        if (bytes.size > MAX_BYTES) return Result.Refused(TorrentList.BackupException.Kind.NOT_A_BACKUP)
        val entries = try {
            TorrentList.read(String(bytes, Charsets.UTF_8))
        } catch (e: TorrentList.BackupException) {
            return Result.Refused(e.kind)
        }
        val repo = RepositoryHelper.getTorrentRepository(app)
        val here = repo.allTorrents.map { it.id }.toSet()
        val toAdd = TorrentList.toAdd(entries, here)
        if (toAdd.isEmpty()) return Result.Done(0, entries.size)

        val engine = TorrentEngine.getInstance(app)
        if (!startEngine(engine)) return Result.NotRunning
        val fs = SystemFacadeHelper.getFileSystemFacade(app)
        val fallback = Uri.parse(RepositoryHelper.getSettingsRepository(app).saveTorrentsIn())
        var added = 0
        for (e in toAdd) {
            val path = Uri.parse(e.downloadPath).takeIf { runCatching { fs.fileExists(it) }.getOrDefault(false) } ?: fallback
            val params = AddTorrentParams(e.magnet, true, e.hash.lowercase(), e.name.ifEmpty { e.hash }, null, path, e.sequential, e.paused, ArrayList(), e.firstLastPieces)
            if (runCatching { engine.addTorrentSync(params, false) }.getOrNull() != null) added++
        }
        return Result.Done(added, entries.size - toAdd.size)
    }

    /** Starts the engine if it is not running and waits for it, up to half a minute. */
    private fun startEngine(engine: TorrentEngine): Boolean {
        if (engine.isRunning) return true
        engine.start()
        repeat(30) {
            if (engine.isRunning) return true
            Thread.sleep(1000)
        }
        return engine.isRunning
    }

    /** At most MAX_BYTES + 1 bytes, so a file that is too big can be told from one that just fits. */
    private fun readCapped(input: java.io.InputStream): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buf = ByteArray(8192)
        while (out.size() <= MAX_BYTES) {
            val n = input.read(buf)
            if (n < 0) break
            out.write(buf, 0, n)
        }
        return out.toByteArray()
    }

    private const val MAX_BYTES = 5 * 1024 * 1024
}
