package org.proninyaroslav.libretorrent.ui.base

import org.junit.Assert.assertEquals
import org.junit.Test

class HouseMenuTest {

    @Test
    fun dropsUnderTheButtonWhenItFits() {
        assertEquals(60 to 200, HouseMenu.place(height = 200, anchorTop = 12, anchorBottom = 60, top = 8, bottom = 792))
    }

    @Test
    fun goesAboveTheButtonAtTheFootOfThePage() {
        // The "+" button sits at the foot: nothing fits under it.
        assertEquals(500 to 200, HouseMenu.place(height = 200, anchorTop = 700, anchorBottom = 760, top = 8, bottom = 792))
    }

    @Test
    fun aListTooLongForEitherSideTakesTheRoomierOneAndNoMore() {
        assertEquals(60 to 732, HouseMenu.place(height = 900, anchorTop = 12, anchorBottom = 60, top = 8, bottom = 792))
        assertEquals(8 to 692, HouseMenu.place(height = 900, anchorTop = 700, anchorBottom = 760, top = 8, bottom = 792))
    }

    @Test
    fun neverANegativeHeight() {
        assertEquals(0, HouseMenu.place(height = 50, anchorTop = 0, anchorBottom = 900, top = 8, bottom = 792).second)
    }
}
