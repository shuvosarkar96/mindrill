package com.mindrill.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class MarketScreen { Home, Categories, Detail }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceTab(modifier: Modifier = Modifier, onInterested: (Product) -> Unit = {}) {
    var screen by rememberSaveable { mutableStateOf(MarketScreen.Home) }
    var selectedId by rememberSaveable { mutableStateOf(-1) }
    var category by rememberSaveable { mutableStateOf<Category?>(null) }
    var query by rememberSaveable { mutableStateOf("") }

    BackHandler(enabled = screen != MarketScreen.Home) { screen = MarketScreen.Home }

    Box(modifier = modifier.fillMaxSize()) {
        when (screen) {
            MarketScreen.Home -> HomeScreen(
                category = category,
                query = query,
                onQuery = { query = it },
                onCategory = { category = it },
                onOpenCategories = { screen = MarketScreen.Categories },
                onOpen = { selectedId = it.id; screen = MarketScreen.Detail }
            )
            MarketScreen.Categories -> CategoriesScreen(
                onBack = { screen = MarketScreen.Home },
                onPick = { category = it; screen = MarketScreen.Home }
            )
            MarketScreen.Detail -> {
                val product = sampleProducts.firstOrNull { it.id == selectedId }
                if (product == null) {
                    screen = MarketScreen.Home
                } else {
                    ProductDetail(
                        product = product,
                        onBack = { screen = MarketScreen.Home },
                        onInterested = onInterested
                    )
                }
            }
        }
    }
}

@Composable
private fun categoryColors(c: Category): Pair<Color, Color> {
    val s = MaterialTheme.colorScheme
    return when (c) {
        Category.Electronics, Category.Wanted -> s.primaryContainer to s.primary
        Category.Electric, Category.Safety -> s.secondaryContainer to s.secondary
        Category.Drilling, Category.Tools -> s.tertiaryContainer to s.tertiary
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    category: Category?,
    query: String,
    onQuery: (String) -> Unit,
    onCategory: (Category?) -> Unit,
    onOpenCategories: () -> Unit,
    onOpen: (Product) -> Unit
) {
    val items = sampleProducts.filter {
        (category == null || it.category == category) && it.name.contains(query, ignoreCase = true)
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "mindrill",
                modifier = Modifier.weight(1f),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onOpenCategories) {
                Icon(Icons.Filled.GridView, contentDescription = "Categories")
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            placeholder = { Text("Search equipment, parts...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(selected = category == null, onClick = { onCategory(null) }, label = { Text("All") })
            Category.entries.forEach { c ->
                FilterChip(selected = category == c, onClick = { onCategory(c) }, label = { Text(c.label) })
            }
        }
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No products found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { product ->
                    ProductCard(product) { onOpen(product) }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    val (container, content) = categoryColors(product.category)
    OutlinedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(container),
                contentAlignment = Alignment.Center
            ) {
                Icon(product.category.icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = content)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                product.priceLabel(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CategoriesScreen(onBack: () -> Unit, onPick: (Category) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Marketplace", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            "Categories",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(Category.entries.toList()) { c ->
                val (container, content) = categoryColors(c)
                Card(
                    colors = CardDefaults.cardColors(containerColor = container),
                    modifier = Modifier.fillMaxWidth().height(130.dp).clickable { onPick(c) }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(c.icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = content)
                        Spacer(Modifier.height(8.dp))
                        Text(c.label, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductDetail(product: Product, onBack: () -> Unit, onInterested: (Product) -> Unit) {
    val (container, content) = categoryColors(product.category)
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Box(
                modifier = Modifier.fillMaxWidth().height(240.dp).background(container)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Icon(
                    product.category.icon,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(110.dp),
                    tint = content
                )
            }
            Column(modifier = Modifier.padding(24.dp)) {
                Text(product.priceLabel(), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text(product.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                product.seller.first().toString(),
                                color = MaterialTheme.colorScheme.onSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.seller, fontWeight = FontWeight.Bold)
                            Text(
                                "Verified seller",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("Specifications", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(product.details, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text("Condition: ${product.condition}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Category: ${product.category.label}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(Icons.Filled.BookmarkBorder, contentDescription = "Save")
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { onInterested(product) },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Filled.ChatBubble, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("I'm interested", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}