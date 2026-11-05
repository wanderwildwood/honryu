package org.proninyaroslav.libretorrent.ui.detailtorrent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ShowInCabinetTest {
    @Test
    public void theActionIsCabinets() {
        assertEquals("com.wanderwildwood.tana.action.SHOW_FILE", ShowInCabinet.ACTION);
        assertEquals("com.wanderwildwood.tana", ShowInCabinet.PACKAGE);
        assertEquals("path", ShowInCabinet.EXTRA_PATH);
    }

    @Test
    public void internalStorageBecomesAPlainPath() {
        assertEquals("/storage/emulated/0/Download/Torrents",
                ShowInCabinet.fromDocumentId(ShowInCabinet.EXTERNAL_STORAGE, "primary:Download/Torrents"));
        assertEquals("/storage/emulated/0",
                ShowInCabinet.fromDocumentId(ShowInCabinet.EXTERNAL_STORAGE, "primary:"));
    }

    @Test
    public void aCardBecomesAPlainPath() {
        assertEquals("/storage/1A2B-3C4D/Torrents",
                ShowInCabinet.fromDocumentId(ShowInCabinet.EXTERNAL_STORAGE, "1A2B-3C4D:Torrents"));
    }

    @Test
    public void theDownloadsProviderBecomesAPlainPath() {
        assertEquals("/storage/emulated/0/Download",
                ShowInCabinet.fromDocumentId(ShowInCabinet.DOWNLOADS, "downloads"));
        assertEquals("/storage/emulated/0/Download/Films",
                ShowInCabinet.fromDocumentId(ShowInCabinet.DOWNLOADS, "raw:/storage/emulated/0/Download/Films/"));
    }

    @Test
    public void otherDocumentsHaveNoPlainPath() {
        assertNull(ShowInCabinet.fromDocumentId(ShowInCabinet.DOWNLOADS, "msf:42"));
        assertNull(ShowInCabinet.fromDocumentId(ShowInCabinet.EXTERNAL_STORAGE, "home:Torrents"));
        assertNull(ShowInCabinet.fromDocumentId("com.example.cloud.documents", "primary:Torrents"));
        assertNull(ShowInCabinet.fromDocumentId(null, "primary:Torrents"));
    }

    @Test
    public void theTopLevelIsTheFolderOrTheSingleFile() {
        assertEquals("Field Recordings", ShowInCabinet.topLevel("Field Recordings/Disc 1/01.flac"));
        assertEquals("public-domain-film.mkv", ShowInCabinet.topLevel("public-domain-film.mkv"));
        assertEquals("Field Recordings", ShowInCabinet.topLevel("/Field Recordings/01.flac"));
        assertNull(ShowInCabinet.topLevel(""));
        assertNull(ShowInCabinet.topLevel(null));
    }
}
