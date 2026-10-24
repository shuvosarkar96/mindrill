package com.mindrill.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.padding

enum class Tab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Maps("Maps", Icons.Filled.Map),
    Chats("Chats", Icons.Filled.ChatBubble),
    Profile("Profile", Icons.Filled.Person)
}

@Composable
fun MainScaffold(
    userName: String = "",
    onLogout: () -> Unit = {},
    darkMode: Boolean = false,
    onDarkMode: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    var selected by rememberSaveable { mutableStateOf(Tab.Home) }
    var openChatId by rememberSaveable { mutableStateOf<String?>(null) }
    val conversations = remember {
        mutableStateListOf<Conversation>().apply { addAll(sampleConversations()) }
    }
    val myListings = remember { mutableStateListOf<Product>().apply { addAll(prefs.loadListings()) } }
    val savedIds = remember { mutableStateListOf<Int>().apply { addAll(prefs.loadSaved()) } }

    val ownName = userName.ifBlank { "You" }
    val products = myListings.toList() + sampleProducts

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
        if (product.seller == ownName) return
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

    fun toggleSave(id: Int) {
        if (id in savedIds) savedIds.remove(id) else savedIds.add(id)
        prefs.saveSaved(savedIds)
    }

    fun addListing(name: String, price: Int, category: Category, details: String, imagePath: String?) {
        val id = (products.maxOfOrNull { it.id } ?: 0) + 1
        myListings.add(
            0,
            Product(
                id, name, price, category, ownName, "Listed by you",
                details.ifBlank { "No extra details." }, imagePath
            )
        )
        prefs.saveListings(myListings)
    }

    fun updateListing(
        id: Int, name: String, price: Int, category: Category, details: String, imagePath: String?
    ) {
        val i = myListings.indexOfFirst { it.id == id }
        if (i >= 0) {
            val old = myListings[i]
            if (old.imagePath != null && old.imagePath != imagePath) deleteImageFile(old.imagePath)
            myListings[i] = old.copy(
                name = name, priceUsd = price, category = category,
                details = details.ifBlank { "No extra details." }, imagePath = imagePath
            )
            prefs.saveListings(myListings)
        }
    }

    fun deleteListing(id: Int) {
        val i = myListings.indexOfFirst { it.id == id }
        if (i >= 0) {
            deleteImageFile(myListings[i].imagePath)
            myListings.removeAt(i)
            prefs.saveListings(myListings)
        }
        if (id in savedIds) {
            savedIds.remove(id)
            prefs.saveSaved(savedIds)
        }
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
                products = products,
                savedIds = savedIds.toList(),
                onToggleSave = { toggleSave(it) },
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
                myListings = myListings.toList(),
                savedProducts = products.filter { it.id in savedIds },
                darkMode = darkMode,
                onDarkMode = onDarkMode,
                onToggleSave = { toggleSave(it) },
                onAddListing = { n, p, c, d, img -> addListing(n, p, c, d, img) },
                onUpdateListing = { id, n, p, c, d, img -> updateListing(id, n, p, c, d, img) },
                onDeleteListing = { deleteListing(it) },
                onLogout = onLogout,
                onOpenMarket = { selected = Tab.Home },
                onOpenMaps = { selected = Tab.Maps }
            )
        }
    }
}