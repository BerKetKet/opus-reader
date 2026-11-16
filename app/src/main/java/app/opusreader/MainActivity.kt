package app.opusreader

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import app.opusreader.design.theme.OpusTheme
import app.opusreader.navigation.OpusNavHost

class MainActivity : ComponentActivity() {

    private val deepLinkUriState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        setContent {
            OpusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    OpusNavHost(deepLinkUri = deepLinkUriState.value)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data
        if (data != null && data.scheme == "opus" && data.host == "fixture") {
            val uriStr = data.toString()
            deepLinkUriState.value = uriStr
            android.util.Log.d("OpusDebug", "MainActivity handleIntent: $uriStr")
            app.opusreader.debug.DebugFixtureController.handleFixtureDeepLink(this, uriStr)
        }
    }
}
