package app.opusreader.engine

import app.opusreader.engine.model.Locator
import org.junit.Assert.assertEquals
import org.junit.Test

class LocatorTest {
    @Test
    fun locatorPropertiesAreRetained() {
        val locator = Locator(spineIndex = 2, charOffset = 150)
        assertEquals(2, locator.spineIndex)
        assertEquals(150, locator.charOffset)
    }
}
