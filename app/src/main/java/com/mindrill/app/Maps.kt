package com.mindrill.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


enum class PlaceType(val label: String, val icon: ImageVector) {
    Mine("Mine", Icons.Filled.Landscape),
    Market("Market", Icons.Filled.Store)
}

data class MapPlace(
    val name: String,
    val type: PlaceType,
    val note: String,
    val x: Float,
    val y: Float,
    val lat: Double,
    val lon: Double
)

private enum class MapFilter(val label: String) { All("All"), Mines("Mines"), Markets("Markets") }

private fun placesFor(f: MapFilter): List<MapPlace> = when (f) {
    MapFilter.All -> mapPlaces
    MapFilter.Mines -> mapPlaces.filter { it.type == PlaceType.Mine }
    MapFilter.Markets -> mapPlaces.filter { it.type == PlaceType.Market }
}

private fun openInMaps(context: Context, p: MapPlace) {
    val uri = Uri.parse("geo:${p.lat},${p.lon}?q=${p.lat},${p.lon}(${Uri.encode(p.name)})")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: Exception) {
        // No maps app available on this device
    }
}

@Composable
fun MapsTab(modifier: Modifier = Modifier) {
    var filter by rememberSaveable { mutableStateOf(MapFilter.All) }
    var selectedName by rememberSaveable { mutableStateOf<String?>(null) }
    var routeName by rememberSaveable { mutableStateOf<String?>(null) }

    val visible = placesFor(filter)
    val selected = visible.firstOrNull { it.name == selectedName }
    val routePlace = mapPlaces.firstOrNull { it.name == routeName }
    val route = routePlace?.let { findRoute(it) }

    val land = MaterialTheme.colorScheme.surfaceVariant
    val landLine = MaterialTheme.colorScheme.outline
    val water = MaterialTheme.colorScheme.secondaryContainer
    val mineColor = MaterialTheme.colorScheme.primary
    val marketColor = MaterialTheme.colorScheme.tertiary
    val routeColor = MaterialTheme.colorScheme.secondary
    val youColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            "Maps",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
        )
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MapFilter.entries.forEach { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { filter = f; selectedName = null; routeName = null },
                    label = { Text("${f.label} (${placesFor(f).size})") }
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(water),
            contentAlignment = Alignment.Center
        ) {
            val w = minOf(maxWidth - 24.dp, (maxHeight - 24.dp) * MAP_ASPECT)
            val h = w / MAP_ASPECT
            Canvas(
                modifier = Modifier
                    .size(w, h)
                    .pointerInput(filter) {
                        detectTapGestures { tap ->
                            val limit = 36.dp.toPx()
                            val hit = visible.minByOrNull {
                                (Offset(it.x * size.width, it.y * size.height) - tap).getDistance()
                            }
                            if (hit != null &&
                                (Offset(hit.x * size.width, hit.y * size.height) - tap).getDistance() <= limit
                            ) {
                                selectedName = hit.name
                                if (routeName != hit.name) routeName = null
                            } else {
                                selectedName = null
                                routeName = null
                            }
                        }
                    }
            ) {
                val path = Path().apply {
                    outline.forEachIndexed { i, (px, py) ->
                        val o = Offset(px * size.width, py * size.height)
                        if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                    }
                    close()
                }
                drawPath(path, land)
                drawPath(path, landLine, style = Stroke(width = 2.dp.toPx()))

                if (route != null) {
                    val rp = Path().apply {
                        route.points.forEachIndexed { i, (px, py) ->
                            val o = Offset(px * size.width, py * size.height)
                            if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                        }
                    }
                    drawPath(
                        rp, Color.White,
                        style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    drawPath(
                        rp, routeColor,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                visible.forEach { p ->
                    val c = Offset(p.x * size.width, p.y * size.height)
                    val col = if (p.type == PlaceType.Mine) mineColor else marketColor
                    val r = (if (p.name == selectedName) 13.dp else 8.dp).toPx()
                    drawCircle(Color.White, r + 2.dp.toPx(), c)
                    drawCircle(col, r, c)
                }

                val you = Offset(startPoint.first * size.width, startPoint.second * size.height)
                drawCircle(Color.White, 9.dp.toPx(), you)
                drawCircle(youColor, 6.dp.toPx(), you)
            }
        }

        if (selected != null) {
            PlaceCard(
                place = selected,
                route = if (routeName == selected.name) route else null,
                onClose = { selectedName = null; routeName = null },
                onDirections = { routeName = selected.name },
                onEnd = { routeName = null }
            )
        } else {
            Text(
                "Tap a pin to see details. The dark dot is your fixed start (Dhaka). Locations are sample data.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun PlaceCard(
    place: MapPlace,
    route: RouteResult?,
    onClose: () -> Unit,
    onDirections: () -> Unit,
    onEnd: () -> Unit
) {
    val tint = if (place.type == PlaceType.Mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    val bg = if (place.type == PlaceType.Mine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer
    OutlinedCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(bg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(place.type.icon, contentDescription = null, tint = tint)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(place.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "${place.type.label}  |  ${place.note}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
            Spacer(Modifier.height(12.dp))
            if (route != null) {
                Text(
                    "${route.km} km  |  about ${durationLabel(route.minutes)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                val viaText = if (route.via.isEmpty()) "Direct road" else "Via " + route.via.joinToString(", ")
                Text(
                    "From Dhaka (fixed start). $viaText.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Estimated along main roads.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onEnd,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("End navigation", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onDirections,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Get directions", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------- Map data: Bangladesh outline (traced) and 12 sample places ----------

private val outline: List<Pair<Float, Float>> = listOf(
    0.087f to 0.000f, 0.071f to 0.022f, 0.071f to 0.041f, 0.082f to 0.049f, 0.082f to 0.056f, 0.066f to 0.075f,
    0.036f to 0.086f, 0.036f to 0.105f, 0.020f to 0.124f, 0.020f to 0.150f, 0.031f to 0.157f, 0.056f to 0.157f,
    0.071f to 0.165f, 0.117f to 0.206f, 0.168f to 0.210f, 0.184f to 0.240f, 0.173f to 0.247f, 0.158f to 0.243f,
    0.138f to 0.247f, 0.102f to 0.243f, 0.092f to 0.247f, 0.092f to 0.277f, 0.076f to 0.288f, 0.025f to 0.292f,
    0.025f to 0.311f, 0.000f to 0.337f, 0.000f to 0.345f, 0.041f to 0.382f, 0.051f to 0.382f, 0.076f to 0.397f,
    0.097f to 0.397f, 0.117f to 0.408f, 0.143f to 0.408f, 0.148f to 0.412f, 0.153f to 0.461f, 0.143f to 0.468f,
    0.122f to 0.472f, 0.117f to 0.517f, 0.158f to 0.547f, 0.148f to 0.566f, 0.153f to 0.584f, 0.184f to 0.592f,
    0.179f to 0.625f, 0.194f to 0.648f, 0.209f to 0.745f, 0.219f to 0.753f, 0.219f to 0.775f, 0.225f to 0.779f,
    0.225f to 0.790f, 0.214f to 0.798f, 0.219f to 0.828f, 0.235f to 0.839f, 0.250f to 0.839f, 0.255f to 0.850f,
    0.270f to 0.850f, 0.291f to 0.835f, 0.347f to 0.839f, 0.357f to 0.832f, 0.367f to 0.832f, 0.372f to 0.820f,
    0.398f to 0.817f, 0.408f to 0.805f, 0.418f to 0.805f, 0.449f to 0.824f, 0.474f to 0.824f, 0.490f to 0.817f,
    0.536f to 0.824f, 0.556f to 0.809f, 0.597f to 0.813f, 0.602f to 0.798f, 0.587f to 0.790f, 0.587f to 0.783f,
    0.592f to 0.779f, 0.617f to 0.779f, 0.622f to 0.786f, 0.653f to 0.786f, 0.679f to 0.764f, 0.684f to 0.727f,
    0.719f to 0.715f, 0.730f to 0.727f, 0.760f to 0.727f, 0.760f to 0.715f, 0.775f to 0.712f, 0.786f to 0.723f,
    0.801f to 0.768f, 0.811f to 0.775f, 0.811f to 0.794f, 0.801f to 0.820f, 0.801f to 0.832f, 0.811f to 0.846f,
    0.806f to 0.869f, 0.847f to 0.899f, 0.847f to 0.925f, 0.878f to 0.951f, 0.878f to 0.959f, 0.903f to 0.993f,
    0.923f to 0.996f, 0.923f to 0.978f, 0.908f to 0.959f, 0.908f to 0.936f, 0.898f to 0.925f, 0.898f to 0.903f,
    0.913f to 0.888f, 0.923f to 0.888f, 0.934f to 0.899f, 0.954f to 0.899f, 0.964f to 0.914f, 0.980f to 0.918f,
    0.995f to 0.906f, 0.995f to 0.895f, 0.985f to 0.884f, 0.985f to 0.865f, 0.980f to 0.861f, 0.980f to 0.817f,
    0.985f to 0.813f, 0.980f to 0.742f, 0.969f to 0.719f, 0.964f to 0.670f, 0.949f to 0.648f, 0.949f to 0.637f,
    0.934f to 0.625f, 0.929f to 0.588f, 0.939f to 0.584f, 0.939f to 0.573f, 0.918f to 0.532f, 0.913f to 0.498f,
    0.888f to 0.491f, 0.872f to 0.491f, 0.862f to 0.498f, 0.847f to 0.494f, 0.827f to 0.498f, 0.827f to 0.536f,
    0.796f to 0.558f, 0.791f to 0.569f, 0.796f to 0.599f, 0.786f to 0.614f, 0.765f to 0.614f, 0.755f to 0.607f,
    0.740f to 0.577f, 0.709f to 0.569f, 0.709f to 0.554f, 0.694f to 0.543f, 0.684f to 0.524f, 0.684f to 0.502f,
    0.694f to 0.487f, 0.694f to 0.472f, 0.704f to 0.464f, 0.719f to 0.464f, 0.730f to 0.442f, 0.760f to 0.446f,
    0.775f to 0.434f, 0.832f to 0.434f, 0.837f to 0.408f, 0.857f to 0.397f, 0.878f to 0.393f, 0.888f to 0.386f,
    0.888f to 0.378f, 0.898f to 0.374f, 0.903f to 0.348f, 0.913f to 0.337f, 0.913f to 0.318f, 0.918f to 0.315f,
    0.949f to 0.315f, 0.959f to 0.307f, 0.954f to 0.288f, 0.923f to 0.266f, 0.908f to 0.266f, 0.867f to 0.247f,
    0.750f to 0.251f, 0.745f to 0.255f, 0.704f to 0.247f, 0.510f to 0.251f, 0.459f to 0.243f, 0.423f to 0.229f,
    0.403f to 0.229f, 0.398f to 0.217f, 0.408f to 0.195f, 0.408f to 0.172f, 0.398f to 0.157f, 0.398f to 0.142f,
    0.408f to 0.135f, 0.408f to 0.120f, 0.383f to 0.086f, 0.367f to 0.079f, 0.367f to 0.071f, 0.342f to 0.071f,
    0.327f to 0.105f, 0.291f to 0.105f, 0.255f to 0.082f, 0.255f to 0.056f, 0.245f to 0.052f, 0.240f to 0.041f,
    0.204f to 0.030f, 0.189f to 0.041f, 0.189f to 0.049f, 0.173f to 0.052f, 0.143f to 0.030f, 0.112f to 0.019f,
    0.097f to 0.000f
)

private const val MAP_ASPECT = 0.7341f

val mapPlaces: List<MapPlace> = listOf(
    MapPlace("Madhyapara Hard Rock Mine", PlaceType.Mine, "Hard rock mine, Dinajpur region", 0.192f, 0.172f, 25.6, 88.9),
    MapPlace("Khalashpir Coal Block", PlaceType.Mine, "Coal block, Rangpur region", 0.300f, 0.170f, 25.7, 89.3),
    MapPlace("Bholaganj Stone Quarry", PlaceType.Mine, "Stone quarry, Sylhet region", 0.740f, 0.290f, 25.15, 91.75),
    MapPlace("Jaflong Stone Yard", PlaceType.Mine, "Stone yard, Sylhet region", 0.900f, 0.300f, 25.1, 92.05),
    MapPlace("Tamabil Sand Pit", PlaceType.Mine, "Sand pit, Sylhet region", 0.780f, 0.400f, 24.6, 91.6),
    MapPlace("Sitakunda Ridge Quarry", PlaceType.Mine, "Ridge quarry, Chattogram region", 0.783f, 0.661f, 22.62, 91.68),
    MapPlace("Tejgaon Equipment Market", PlaceType.Market, "Equipment market, Dhaka", 0.511f, 0.474f, 23.76, 90.4),
    MapPlace("Chattogram Machinery Hub", PlaceType.Market, "Machinery hub, Chattogram", 0.819f, 0.707f, 22.34, 91.85),
    MapPlace("Khulna Tools Bazar", PlaceType.Market, "Tools bazar, Khulna", 0.330f, 0.628f, 22.82, 89.55),
    MapPlace("Rajshahi Mining Supply", PlaceType.Market, "Mining supply, Rajshahi", 0.128f, 0.374f, 24.37, 88.6),
    MapPlace("Bogura Heavy Parts", PlaceType.Market, "Heavy parts market, Bogura", 0.300f, 0.300f, 24.85, 89.37),
    MapPlace("Sylhet Gear Market", PlaceType.Market, "Gear market, Sylhet", 0.600f, 0.270f, 24.9, 91.87)
)