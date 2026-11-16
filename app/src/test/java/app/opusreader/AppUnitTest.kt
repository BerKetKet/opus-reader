package app.opusreader

import app.opusreader.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Test

class AppUnitTest {
    @Test
    fun routesAreCorrect() {
        assertEquals("home", Screen.Home.route)
        assertEquals("library", Screen.Library.route)
        assertEquals("more", Screen.More.route)
    }
}
