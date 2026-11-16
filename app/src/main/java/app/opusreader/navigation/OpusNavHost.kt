package app.opusreader.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.opusreader.feature.home.HomeScreen
import app.opusreader.feature.library.LibraryScreen
import app.opusreader.feature.more.MoreScreen

sealed class Screen(val route: String, val label: String, val tag: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", "tab_home", Icons.Default.Home)
    data object Library : Screen("library", "Library", "tab_library", Icons.Default.LocalLibrary)
    data object More : Screen("more", "More", "tab_more", Icons.Default.MoreHoriz)
}

@Composable
fun OpusNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    val items = listOf(
        Screen.Home,
        Screen.Library,
        Screen.More
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier.testTag("main_scaffold"),
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                items.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        modifier = Modifier.testTag(screen.tag),
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    modifier = Modifier.testTag("screen_home"),
                    onOpenBook = { bookId ->
                        // Will be wired in future slices
                    }
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen(
                    modifier = Modifier.testTag("screen_library"),
                    onOpenBook = { bookId ->
                        // Will be wired in future slices
                    }
                )
            }
            composable(Screen.More.route) {
                MoreScreen(
                    modifier = Modifier.testTag("screen_more"),
                    onNavigateToSettings = {
                        // Will be wired in future slices
                    }
                )
            }
        }
    }
}
