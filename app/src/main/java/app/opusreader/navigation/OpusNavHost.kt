package app.opusreader.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.opusreader.OpusApplication
import app.opusreader.design.component.OpusBottomNavBar
import app.opusreader.design.component.OpusNavItem
import app.opusreader.feature.home.HomeScreen
import app.opusreader.feature.library.LibraryScreen
import app.opusreader.feature.library.LibraryViewModel
import app.opusreader.feature.more.MoreScreen

sealed class Screen(val route: String, val label: String, val tag: String, val icon: ImageVector, val id: Int) {
    data object Home : Screen("home", "Home", "tab_home", Icons.Default.Home, 0)
    data object Library : Screen("library", "Library", "tab_library", Icons.Default.LocalLibrary, 1)
    data object More : Screen("more", "More", "tab_more", Icons.Default.MoreHoriz, 2)
}

@Composable
fun OpusNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    val screens = listOf(
        Screen.Home,
        Screen.Library,
        Screen.More
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedId = screens.firstOrNull { it.route == currentRoute }?.id ?: 0

    val navItems = screens.map { screen ->
        OpusNavItem(
            id = screen.id,
            label = screen.label,
            icon = screen.icon,
            tag = screen.tag
        )
    }

    Scaffold(
        modifier = modifier.testTag("main_scaffold"),
        bottomBar = {
            OpusBottomNavBar(
                items = navItems,
                selectedId = selectedId,
                onItemSelected = { id ->
                    val target = screens.firstOrNull { it.id == id }
                    if (target != null && target.route != currentRoute) {
                        navController.navigate(target.route) {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    modifier = Modifier.testTag("screen_home"),
                    onOpenBook = { _ -> }
                )
            }
            composable(Screen.Library.route) {
                val context = LocalContext.current
                val app = context.applicationContext as OpusApplication
                val libraryViewModel: LibraryViewModel = viewModel(
                    factory = LibraryViewModel.provideFactory(
                        app.container.libraryRepository,
                        app.container.settingsRepository
                    )
                )
                val uiState by libraryViewModel.uiState.collectAsStateWithLifecycle()

                LibraryScreen(
                    uiState = uiState,
                    onOpenBook = { },
                    onSearchQueryChange = libraryViewModel::onSearchQueryChange,
                    onFilterChange = libraryViewModel::onFilterChange,
                    onViewModeChange = libraryViewModel::onViewModeChange,
                    onSortModeChange = libraryViewModel::onSortModeChange,
                    onSelectCollection = libraryViewModel::onSelectCollection,
                    onOpenSheet = libraryViewModel::onOpenSheet,
                    onDismissSheet = libraryViewModel::onDismissSheet,
                    onToggleReadStatus = libraryViewModel::onToggleReadStatus,
                    onToggleDownload = libraryViewModel::onToggleDownload,
                    onDeleteBook = libraryViewModel::onDeleteBook,
                    onAssignCollection = libraryViewModel::onAssignCollection,
                    onNewCollectionNameChanged = libraryViewModel::onNewCollectionNameChanged,
                    onCreateCollection = libraryViewModel::onCreateCollection,
                    modifier = Modifier.testTag("screen_library")
                )
            }
            composable(Screen.More.route) {
                MoreScreen(
                    modifier = Modifier.testTag("screen_more"),
                    onNavigateToSettings = { }
                )
            }
        }
    }
}
