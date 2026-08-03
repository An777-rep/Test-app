package com.example.MainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Mouse
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.screens.RemoteControlScreen
import com.example.ui.screens.SavedHostsScreen
import com.example.ui.screens.WakeOnLanScreen
import com.example.ui.screens.WebHubScreen
import com.example.ui.theme.WinBorderColor
import com.example.ui.theme.WinCardBg
import com.example.ui.theme.WinCyanPrimary
import com.example.ui.theme.WinCyanSecondary
import com.example.ui.theme.WinLinkTheme
import com.example.ui.theme.WinTextPrimary
import com.example.ui.theme.WinTextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WinLinkTheme {
                var selectedTab by remember { mutableIntStateOf(0) }
                val snackbarHostState = remember { SnackbarHostState() }
                val statusMessage by viewModel.statusMessage.collectAsState()

                LaunchedEffect(statusMessage) {
                    statusMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearStatusMessage()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "WinLink LAN",
                                    color = WinTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = WinCardBg)
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = WinCardBg,
                            contentColor = WinCyanPrimary,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 0) Icons.Filled.Language else Icons.Outlined.Language,
                                        contentDescription = "Веб-Хаб"
                                    )
                                },
                                label = { Text("Веб-Хаб") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = WinTextPrimary,
                                    selectedTextColor = WinCyanPrimary,
                                    indicatorColor = WinCyanSecondary,
                                    unselectedIconColor = WinTextSecondary,
                                    unselectedTextColor = WinTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_web_hub")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 1) Icons.Filled.Mouse else Icons.Outlined.Mouse,
                                        contentDescription = "Пульт ДУ"
                                    )
                                },
                                label = { Text("Пульт ДУ") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = WinTextPrimary,
                                    selectedTextColor = WinCyanPrimary,
                                    indicatorColor = WinCyanSecondary,
                                    unselectedIconColor = WinTextSecondary,
                                    unselectedTextColor = WinTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_remote")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 2) Icons.Filled.PowerSettingsNew else Icons.Outlined.PowerSettingsNew,
                                        contentDescription = "WoL и Сеть"
                                    )
                                },
                                label = { Text("WoL и Сеть") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = WinTextPrimary,
                                    selectedTextColor = WinCyanPrimary,
                                    indicatorColor = WinCyanSecondary,
                                    unselectedIconColor = WinTextSecondary,
                                    unselectedTextColor = WinTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_wol")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 3) Icons.Filled.Computer else Icons.Outlined.Computer,
                                        contentDescription = "Сохраненные ПК"
                                    )
                                },
                                label = { Text("Мои ПК") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = WinTextPrimary,
                                    selectedTextColor = WinCyanPrimary,
                                    indicatorColor = WinCyanSecondary,
                                    unselectedIconColor = WinTextSecondary,
                                    unselectedTextColor = WinTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_hosts")
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    when (selectedTab) {
                        0 -> WebHubScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                        1 -> RemoteControlScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                        2 -> WakeOnLanScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                        3 -> SavedHostsScreen(
                            viewModel = viewModel,
                            onNavigateToRemote = { targetIp ->
                                selectedTab = 1
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
