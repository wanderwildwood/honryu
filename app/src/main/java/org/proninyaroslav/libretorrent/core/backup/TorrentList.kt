package org.proninyaroslav.libretorrent.core.backup

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.Instant

/**
 * The torrent list as one plain JSON file, written and read through the system's file picker:
 * each torrent's magnet link, name, where it saves and how it was set to run. Not the downloaded
 * files, and not the .torrent files: a restored torrent fetches its details from its peers again
 * and checks whatever is already in its folder. Field names are data, not interface.
 *
 * [read] checks the whole file and throws a [BackupException] for anything it cannot use; only
 * then does the caller add anything.
 */
object TorrentList {

    const val FORMAT = "honryu-torrent-list"
    const val VERSION = 1

    data class Entry(
        val hash: String,
        val name: String,
        val magnet: String,
        val downloadPath: String,
        val paused: Boolean,
        val sequential: Boolean,
        val firstLastPieces: Boolean,
    )

    /** Why a file was refused, as a kind the screen turns into a plain sentence. */
    class BackupException(val kind: Kind) : Exception(kind.name) {
        enum class Kind { NOT_A_BACKUP, NEWER, DAMAGED }
    }

    fun write(entries: List<Entry>, written: Instant = Instant.now()): String {
        val root = JSONObject()
        root.put("format", FORMAT)
        root.put("version", VERSION)
        root.put("written", written.toString())
        root.put("torrents", JSONArray().apply {
            for (e in entries) put(JSONObject().apply {
                put("hash", e.hash)
                put("name", e.name)
                put("magnet", e.magnet)
                put("download_path", e.downloadPath)
                put("paused", e.paused)
                put("sequential", e.sequential)
                put("first_last_pieces", e.firstLastPieces)
            })
        })
        return root.toString(2)
    }

    /** Reads a list whole, or throws a [BackupException]; nothing is half-read. */
    fun read(text: String): List<Entry> {
        val root = try {
            JSONObject(text)
        } catch (e: JSONException) {
            throw BackupException(BackupException.Kind.NOT_A_BACKUP)
        }
        if (root.optString("format") != FORMAT) throw BackupException(BackupException.Kind.NOT_A_BACKUP)
        val version = root.optInt("version", -1)
        if (version > VERSION) throw BackupException(BackupException.Kind.NEWER)
        if (version < 1) throw BackupException(BackupException.Kind.DAMAGED)
        try {
            val a = root.getJSONArray("torrents")
            val entries = (0 until a.length()).map { i ->
                val o = a.getJSONObject(i)
                val hash = o.getString("hash")
                require(HASH.matches(hash))
                val magnet = o.getString("magnet")
                require(magnet.startsWith("magnet:?"))
                Entry(
                    hash = hash,
                    name = o.getString("name"),
                    magnet = magnet,
                    downloadPath = o.getString("download_path"),
                    paused = o.getBoolean("paused"),
                    sequential = o.getBoolean("sequential"),
                    firstLastPieces = o.getBoolean("first_last_pieces"),
                )
            }
            require(entries.map { it.hash.lowercase() }.toSet().size == entries.size)
            return entries
        } catch (e: Exception) {
            throw BackupException(BackupException.Kind.DAMAGED)
        }
    }

    /** The entries of a restore that are not in the list already, by their hash. */
    fun toAdd(entries: List<Entry>, here: Set<String>): List<Entry> {
        val known = here.map { it.lowercase() }.toSet()
        return entries.filter { it.hash.lowercase() !in known }
    }

    /** A v1 info hash in hex, or a v2 one. */
    private val HASH = Regex("[0-9a-fA-F]{40}|[0-9a-fA-F]{64}")

    /** A magnet link for a torrent the engine cannot make one for right now. */
    fun plainMagnet(hash: String, name: String): String =
        "magnet:?xt=urn:btih:$hash" + if (name.isEmpty()) "" else "&dn=" + java.net.URLEncoder.encode(name, "UTF-8").replace("+", "%20")
}
