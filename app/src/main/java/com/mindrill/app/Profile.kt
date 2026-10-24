package com.mindrill.app

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class ProfileScreen { Main, Listings, Saved, Settings }

@Composable
fun ProfileTab(
    modifier: Modifier = Modifier,
    userName: String,
    myListings: List<Product>,
    savedProducts: List<Product>,
    darkMode: Boolean,
    onDarkMode: (Boolean) -> Unit,
    onToggleSave: (Int) -> Unit,
    onAddListing: (String, Int, Category, String, String?) -> Unit,
    onUpdateListing: (Int, String, Int, Category, String, String?) -> Unit,
    onDeleteListing: (Int) -> Unit,
    onLogout: () -> Unit,
    onOpenMarket: () -> Unit,
    onOpenMaps: () -> Unit
) {
    var screen by rememberSaveable { mutableStateOf(ProfileScreen.Main) }
    BackHandler(enabled = screen != ProfileScreen.Main) { screen = ProfileScreen.Main }

    when (screen) {
        ProfileScreen.Main -> ProfileMain(
            modifier, userName, onOpenMarket, onOpenMaps,
            onListings = { screen = ProfileScreen.Listings },
            onSaved = { screen = ProfileScreen.Saved },
            onSettings = { screen = ProfileScreen.Settings },
            onLogout = onLogout
        )
        ProfileScreen.Listings -> ListingsScreen(
            modifier, myListings, onAddListing, onUpdateListing, onDeleteListing
        ) { screen = ProfileScreen.Main }
        ProfileScreen.Saved -> SavedScreen(modifier, savedProducts, onToggleSave) { screen = ProfileScreen.Main }
        ProfileScreen.Settings -> SettingsScreen(modifier, darkMode, onDarkMode) { screen = ProfileScreen.Main }
    }
}

@Composable
private fun ProfileMain(
    modifier: Modifier,
    userName: String,
    onOpenMarket: () -> Unit,
    onOpenMaps: () -> Unit,
    onListings: () -> Unit,
    onSaved: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val name = userName.ifBlank { "Guest" }
    val s = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "My Profile",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 16.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = s.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(s.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(name.first().uppercase(), color = s.onPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = s.onPrimaryContainer)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Verified, contentDescription = null, modifier = Modifier.size(18.dp), tint = s.tertiary)
                        Spacer(Modifier.width(6.dp))
                        Text("Verified miner", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = s.tertiary)
                    }
                }
            }
        }

        Text(
            "Services",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 12.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ServiceTile(Modifier.weight(1f), Icons.Filled.Store, "Marketplace", s.primaryContainer, s.primary, onOpenMarket)
            ServiceTile(Modifier.weight(1f), Icons.Filled.Map, "Maps", s.tertiaryContainer, s.tertiary, onOpenMaps)
        }

        Spacer(Modifier.height(24.dp))
        MenuRow(Icons.Filled.Store, "My listings", onListings)
        MenuRow(Icons.Filled.Bookmark, "Saved items", onSaved)
        MenuRow(Icons.Filled.Settings, "Settings", onSettings)
        MenuRow(Icons.AutoMirrored.Filled.ExitToApp, "Log out", onLogout)
    }
}

@Composable
private fun SubHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProductRow(product: Product, trailing: @Composable () -> Unit = {}) {
    OutlinedCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductPhoto(
                product,
                Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)),
                24.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text(
                    "${product.category.label}  |  ${product.priceLabel()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            trailing()
        }
    }
}

@Composable
private fun ListingsScreen(
    modifier: Modifier,
    listings: List<Product>,
    onAdd: (String, Int, Category, String, String?) -> Unit,
    onUpdate: (Int, String, Int, Category, String, String?) -> Unit,
    onDelete: (Int) -> Unit,
    onBack: () -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Product?>(null) }
    var deleting by remember { mutableStateOf<Product?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        SubHeader("My listings", onBack)
        Button(
            onClick = { showAdd = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add listing", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        if (listings.isEmpty()) {
            Text(
                "You have not listed anything yet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(24.dp)
            )
        } else {
            LazyColumn {
                items(listings, key = { it.id }) { p ->
                    ProductRow(p) {
                        IconButton(onClick = { editing = p }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { deleting = p }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        ListingDialog(initial = null, onDismiss = { showAdd = false }, onSave = onAdd)
    }
    editing?.let { p ->
        ListingDialog(
            initial = p,
            onDismiss = { editing = null },
            onSave = { n, pr, c, d, img -> onUpdate(p.id, n, pr, c, d, img) }
        )
    }
    deleting?.let { p ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete listing?") },
            text = { Text("\"${p.name}\" will be removed from your listings.") },
            confirmButton = {
                TextButton(onClick = { onDelete(p.id); deleting = null }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ListingDialog(
    initial: Product?,
    onDismiss: () -> Unit,
    onSave: (String, Int, Category, String, String?) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var price by remember { mutableStateOf(initial?.priceUsd?.toString() ?: "") }
    var details by remember {
        mutableStateOf(initial?.details?.takeIf { it != "No extra details." } ?: "")
    }
    var category by remember { mutableStateOf(initial?.category ?: Category.Tools) }
    var imagePath by remember { mutableStateOf(initial?.imagePath) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val saved = saveImageFromUri(context, uri)
            if (saved != null) {
                if (imagePath != null && imagePath != initial?.imagePath) deleteImageFile(imagePath)
                imagePath = saved
            }
        }
    }

    fun cancel() {
        if (imagePath != initial?.imagePath) deleteImageFile(imagePath)
        onDismiss()
    }

    val priceInt = price.toIntOrNull()
    val preview = remember(imagePath) { imagePath?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() } }

    AlertDialog(
        onDismissRequest = { cancel() },
        title = { Text(if (initial == null) "Add listing" else "Edit listing") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (preview != null) {
                    Image(
                        bitmap = preview,
                        contentDescription = "Listing photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    ) { Text(if (imagePath == null) "Choose photo" else "Change photo") }
                    if (imagePath != null) {
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                if (imagePath != initial?.imagePath) deleteImageFile(imagePath)
                                imagePath = null
                            }
                        ) { Text("Remove") }
                    }
                }
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Product name") }, singleLine = true
                )
                OutlinedTextField(
                    value = price, onValueChange = { price = it.filter { c -> c.isDigit() } },
                    label = { Text("Price (USD)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Category.entries.forEach { c ->
                        FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c.label) })
                    }
                }
                OutlinedTextField(
                    value = details, onValueChange = { details = it },
                    label = { Text("Details") }, minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(name.trim(), priceInt ?: 0, category, details.trim(), imagePath)
                    onDismiss()
                },
                enabled = name.isNotBlank() && priceInt != null
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = { cancel() }) { Text("Cancel") } }
    )
}

@Composable
private fun SavedScreen(
    modifier: Modifier,
    saved: List<Product>,
    onToggleSave: (Int) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        SubHeader("Saved items", onBack)
        if (saved.isEmpty()) {
            Text(
                "Nothing saved yet. Tap the bookmark on a product to save it.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(24.dp)
            )
        } else {
            LazyColumn {
                items(saved, key = { it.id }) { p ->
                    ProductRow(p) {
                        IconButton(onClick = { onToggleSave(p.id) }) {
                            Icon(Icons.Filled.Bookmark, contentDescription = "Remove", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    modifier: Modifier,
    darkMode: Boolean,
    onDarkMode: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        SubHeader("Settings", onBack)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dark mode", fontSize = 16.sp, modifier = Modifier.weight(1f))
            Switch(checked = darkMode, onCheckedChange = onDarkMode)
        }
        Text(
            "mindrill v1.0",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun ServiceTile(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    container: Color,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = container),
        modifier = modifier.height(110.dp).clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(36.dp), tint = tint)
            Spacer(Modifier.height(8.dp))
            Text(label, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(16.dp))
        Text(label, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}