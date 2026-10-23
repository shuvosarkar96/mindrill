package com.mindrill.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileTab(
    modifier: Modifier = Modifier,
    userName: String,
    onLogout: () -> Unit,
    onOpenMarket: () -> Unit,
    onOpenMaps: () -> Unit
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
                    Text(
                        name.first().uppercase(),
                        color = s.onPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
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
        MenuRow(Icons.Filled.Store, "My listings") { }
        MenuRow(Icons.Filled.Bookmark, "Saved items") { }
        MenuRow(Icons.Filled.Settings, "Settings") { }
        MenuRow(Icons.AutoMirrored.Filled.ExitToApp, "Log out", onClick = onLogout)
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