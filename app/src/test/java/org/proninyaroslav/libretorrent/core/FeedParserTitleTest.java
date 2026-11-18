package org.proninyaroslav.libretorrent.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class FeedParserTitleTest {

    @Test
    public void aTitleIsKeptAsItIs() {
        assertEquals(" Field Notes 12 ", FeedParser.itemTitle(" Field Notes 12 ",
                "https://example.org/a", "magnet:?xt=urn:btih:0"));
        assertEquals("", FeedParser.itemTitle("", "https://example.org/a", null));
    }

    @Test
    public void noTitleFallsBackToTheArticleLink() {
        assertEquals("https://example.org/a",
                FeedParser.itemTitle(null, "https://example.org/a", "magnet:?xt=urn:btih:0"));
    }

    @Test
    public void noTitleOrArticleFallsBackToTheTorrentLink() {
        assertEquals("magnet:?xt=urn:btih:0",
                FeedParser.itemTitle(null, " ", "magnet:?xt=urn:btih:0"));
    }

    @Test
    public void nothingToNameTheItemByIsNull() {
        assertNull(FeedParser.itemTitle(null, null, ""));
    }
}
