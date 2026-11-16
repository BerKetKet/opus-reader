package app.opusreader.design

import org.junit.Assert.assertEquals
import org.junit.Test

class DesignTest {
    @Test
    fun moduleNameMatches() {
        assertEquals("design", DesignSystem.MODULE_NAME)
    }
}
