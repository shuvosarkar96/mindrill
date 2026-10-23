package com.mindrill.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

enum class Tab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Maps("Maps", Icons.Filled.Map),
    Chats("Chats", Icons.Filled.ChatBubble),
    Profile("Profile", Icons.Filled.Person)
}

@Composable
fun MainScaffold(userName: String = "", onLogout: () -> Unit = {}) {
    var selected by rememberSaveable { mutableStateOf(Tab.Home) }
    var openChatId by rememberSaveable { mutableStateOf<String?>(null) }
    val conversations = remember {
        mutableStateListOf<Conversation>().apply { addAll(sampleConversations()) }
    }

    fun openChat(id: String?) {
        openChatId = id
        if (id != null) {
            val i = conversations.indexOfFirst { it.id == id }
            if (i >= 0 && conversations[i].unread > 0) {
                conversations[i] = conversations[i].copy(unread = 0)
            }
        }
    }

    fun sendMessage(id: String, text: String) {
        val i = conversations.indexOfFirst { it.id == id }
        if (i >= 0) {
            conversations[i] = conversations[i].copy(
                messages = conversations[i].messages + ChatMessage(text, true),
                lastTime = "Now"
            )
        }
    }

    fun interested(product: Product) {
        val text = "Hello, I am interested in ${product.name} (${product.priceLabel()})."
        val i = conversations.indexOfFirst { it.id == product.seller }
        if (i >= 0) {
            conversations[i] = conversations[i].copy(
                productName = product.name,
                messages = conversations[i].messages + ChatMessage(text, true),
                lastTime = "Now"
            )
        } else {
            conversations.add(
                0,
                Conversation(product.seller, product.seller, product.name, listOf(ChatMessage(text, true)), "Now", 0)
            )
        }
        selected = Tab.Chats
        openChat(product.seller)
    }

    val inConversation = selected == Tab.Chats && openChatId != null

    Scaffold(
        bottomBar = {
            if (!inConversation) {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selected == tab,
                            onClick = { selected = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        when (selected) {
            Tab.Home -> MarketplaceTab(
                modifier = Modifier.padding(padding),
                onInterested = { interested(it) }
            )
            Tab.Maps -> MapsTab(modifier = Modifier.padding(padding))
            Tab.Chats -> ChatsTab(
                modifier = Modifier.padding(padding),
                conversations = conversations,
                openId = openChatId,
                onOpen = { openChat(it) },
                onSend = { id, text -> sendMessage(id, text) }
            )
            Tab.Profile -> ProfileTab(
                modifier = Modifier.padding(padding),
                userName = userName,
                onLogout = onLogout,
                onOpenMarket = { selected = Tab.Home },
                onOpenMaps = { selected = Tab.Maps }
            )
        }
    }
}