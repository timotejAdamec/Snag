package cz.adamec.timotej.snag.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.NavDisplay
import cz.adamec.timotej.snag.lib.design.SnagTheme
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRoute
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.KoinApplication
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.dsl.koinConfiguration
import snag.composeapp.generated.resources.Res
import snag.composeapp.generated.resources.ic_add
import snag.composeapp.generated.resources.ic_home_filled

@Composable
@Preview
fun App() {
    KoinApplication(
        configuration = koinConfiguration(declaration = { modules(appModule) }),
    ) {
        SnagTheme {
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
                state = rememberTopAppBarState()
            )
            NavigationSuiteScaffold(
                navigationItems = {
                    NavigationSuiteItem(
                        selected = true,
                        onClick = {},
                        icon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_home_filled),
                                contentDescription = "Home",
                            )
                        },
                        label = {
                            Text(
                                text = "Home",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    )
                },
                navigationItemVerticalArrangement = Arrangement.Center,
                primaryActionContent = {
                    FloatingActionButton(
                        modifier = Modifier.padding(start = 20.dp),
                        onClick = {},
                        content = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_add),
                                contentDescription = "Add",
                            )
                        }
                    )
                }
            ) {
                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        MediumFlexibleTopAppBar(
                            title = { Text(text = "Projects") },
                            scrollBehavior = scrollBehavior,
                        )
                    }
                ) { paddingValues ->
                    val backStack = remember { mutableStateListOf<Any>(ProjectsRoute) }
                    val entryProvider = koinEntryProvider()
                    NavDisplay(
                        modifier = Modifier.padding(paddingValues),
                        backStack = backStack,
                        entryProvider = entryProvider,
                    )
                }
            }
        }
    }
}
