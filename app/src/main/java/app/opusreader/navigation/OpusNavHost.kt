package app.opusreader.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import app.opusreader.design.component.OpusResumeCover
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.feature.home.HomeScreen
import app.opusreader.feature.library.LibraryScreen
import app.opusreader.feature.library.LibraryViewModel
import app.opusreader.feature.more.MoreScreen
import coil.compose.AsyncImage

sealed class Screen(val route: String, val label: String, val tag: String, val iconRes: Int, val id: Int) {
    data object Home : Screen("home", "Home", "tab_home", app.opusreader.design.R.drawable.ic_kindle_home, 0)
    data object Library : Screen("library", "Library", "tab_library", app.opusreader.design.R.drawable.ic_kindle_library, 1)
    data object More : Screen("more", "More", "tab_more", app.opusreader.design.R.drawable.ic_kindle_more, 2)
}

@Composable
fun OpusNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route,
    deepLinkUri: String? = null
) {
    LaunchedEffect(deepLinkUri) {
        if (deepLinkUri != null) {
            navController.navigate(Screen.Library.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

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
            icon = ImageVector.vectorResource(screen.iconRes),
            tag = screen.tag
        )
    }

    val context = LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
    val app = context.applicationContext as OpusApplication
    val libraryViewModel: LibraryViewModel = if (activity != null) {
        viewModel(
            viewModelStoreOwner = activity,
            factory = LibraryViewModel.provideFactory(
                app.container.libraryRepository,
                app.container.settingsRepository
            )
        )
    } else {
        viewModel(
            factory = LibraryViewModel.provideFactory(
                app.container.libraryRepository,
                app.container.settingsRepository
            )
        )
    }
    val libraryUiState by libraryViewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_scaffold")
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    modifier = Modifier.testTag("screen_home"),
                    onOpenBook = { _ -> }
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen(
                    uiState = libraryUiState,
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
                    onGroupBySeriesChange = libraryViewModel::setGroupBySeries,
                    onApplyFilterState = libraryViewModel::onApplyFilterState,
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

        // Bottom Navigation Bar (hidden when a window-level bottom sheet is active)
        if (libraryUiState.activeSheet == null) {
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
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Floating resume cover pinned at bottom left over the bottom navigation bar
        val lastBook = libraryUiState.lastReadBook
        if (currentRoute in listOf(Screen.Home.route, Screen.Library.route) &&
            libraryUiState.activeSheet == null &&
            lastBook != null
        ) {
            OpusResumeCover(
                title = lastBook.title,
                onResumeClick = { },
                coverContent = if (lastBook.coverPath != null) {
                    {
                        AsyncImage(
                            model = lastBook.coverPath,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else null,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = OpusDimensions.ResumeCoverMarginStart,
                        bottom = OpusDimensions.ResumeCoverOffsetBottom
                    )
                    .testTag("resume_cover_card")
            )
        }
    }
}
