package tax.app.mehdighobadi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import tax.app.mehdighobadi.ui2.components.BottomPillNav
import tax.app.mehdighobadi.ui2.components.Dest
import tax.app.mehdighobadi.ui2.data.AppState
import tax.app.mehdighobadi.ui2.screens.FavoritesScreen
import tax.app.mehdighobadi.ui2.screens.HomeScreen
import tax.app.mehdighobadi.ui2.screens.LibraryScreen
import tax.app.mehdighobadi.ui2.screens.ReaderScreen
import tax.app.mehdighobadi.ui2.screens.SettingsScreen
import tax.app.mehdighobadi.ui2.screens.StatsScreen
import tax.app.mehdighobadi.ui2.theme.LocalMagPalette
import tax.app.mehdighobadi.ui2.theme.MagTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val ctx = LocalContext.current
            LaunchedEffect(Unit) { AppState.load(ctx) }
            MagTheme(dark = AppState.darkMode.value) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
fun AppRoot() {
    val p = LocalMagPalette.current
    val ctx = LocalContext.current
    val current = remember { mutableStateOf(Dest.Home) }
    val openChapter = remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = p.bg) {
        Box(Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = openChapter.value to current.value,
                transitionSpec = {
                    (slideInVertically(tween(300)) { it / 12 } + fadeIn(tween(280))) togetherWith fadeOut(tween(160))
                },
                label = "screen",
                modifier = Modifier.fillMaxSize()
            ) { (chap, dest) ->
                if (chap != null) {
                    ReaderScreen(chapterId = chap, onBack = { openChapter.value = null })
                } else when (dest) {
                    Dest.Home -> HomeScreen(
                        onStart = { openChapter.value = tax.app.mehdighobadi.data.TaxData.content.chapters.firstOrNull()?.id },
                        onOpenChapter = { openChapter.value = it }
                    )
                    Dest.Library -> LibraryScreen(onOpenChapter = { openChapter.value = it })
                    Dest.Favorites -> FavoritesScreen(onOpen = { openChapter.value = it })
                    Dest.Stats -> StatsScreen()
                    Dest.Settings -> SettingsScreen(
                        onToggleDark = { v -> AppState.darkMode.value = v; AppState.persist(ctx) },
                        onToggleMotion = { v -> AppState.reduceMotion.value = v; AppState.persist(ctx) }
                    )
                }
            }

            if (openChapter.value == null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                ) { BottomPillNav(current = current.value, onSelect = { current.value = it }) }
            }
        }
    }
}
