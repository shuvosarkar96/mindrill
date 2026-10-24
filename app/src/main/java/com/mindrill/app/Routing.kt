package com.mindrill.app

import kotlin.math.hypot
import kotlin.math.roundToInt

data class RouteResult(
    val points: List<Pair<Float, Float>>,
    val km: Int,
    val minutes: Int,
    val via: List<String>
)

private class RoadNode(val id: String, val label: String, val x: Float, val y: Float)

// Fixed starting position (Dhaka), as fractions of the map width and height
val startPoint: Pair<Float, Float> = 0.490f to 0.500f

fun durationLabel(minutes: Int): String =
    if (minutes < 60) "$minutes min" else "${minutes / 60} h ${minutes % 60} min"

private fun segmentKm(a: RoadNode, b: RoadNode): Double {
    val dx = (a.x - b.x) * 477.0
    val dy = (a.y - b.y) * 677.0
    return hypot(dx, dy) * 1.3
}

fun findRoute(place: MapPlace): RouteResult? {
    val n = roadNodes.size
    val start = roadNodes.indexOfFirst { it.id == "You" }
    val goal = roadNodes.indexOfFirst { it.id == place.name }
    if (start < 0 || goal < 0) return null

    val dist = DoubleArray(n) { Double.MAX_VALUE }
    val prev = IntArray(n) { -1 }
    val done = BooleanArray(n)
    dist[start] = 0.0

    repeat(n) {
        var u = -1
        for (i in 0 until n) {
            if (!done[i] && (u == -1 || dist[i] < dist[u])) u = i
        }
        if (u == -1 || dist[u] == Double.MAX_VALUE) return@repeat
        done[u] = true
        for ((a, b) in roadEdges) {
            val ia = roadNodes.indexOfFirst { it.id == a }
            val ib = roadNodes.indexOfFirst { it.id == b }
            val v = when (u) {
                ia -> ib
                ib -> ia
                else -> -1
            }
            if (v >= 0 && !done[v]) {
                val d = dist[u] + segmentKm(roadNodes[u], roadNodes[v])
                if (d < dist[v]) {
                    dist[v] = d
                    prev[v] = u
                }
            }
        }
    }
    if (dist[goal] == Double.MAX_VALUE) return null

    val path = ArrayList<Int>()
    var c = goal
    while (c != -1) {
        path.add(0, c)
        c = prev[c]
    }
    val km = dist[goal].roundToInt()
    val minutes = (km / 45.0 * 60.0).roundToInt()
    return RouteResult(
        points = path.map { roadNodes[it].x to roadNodes[it].y },
        km = km,
        minutes = minutes,
        via = path.drop(1).dropLast(1).map { roadNodes[it].label }
    )
}

// ---------- Simplified road network (main cities and highways) ----------

private val roadNodes = listOf(
    RoadNode("You", "Dhaka (you)", 0.490f, 0.500f),
    RoadNode("Jamuna Bridge", "Jamuna Bridge", 0.379f, 0.369f),
    RoadNode("Cumilla", "Cumilla", 0.660f, 0.520f),
    RoadNode("Bhairab", "Bhairab", 0.634f, 0.426f),
    RoadNode("Jessore", "Jessore", 0.257f, 0.570f),
    RoadNode("Feni", "Feni", 0.723f, 0.597f),
    RoadNode("Madhyapara Hard Rock Mine", "Madhyapara", 0.192f, 0.172f),
    RoadNode("Khalashpir Coal Block", "Khalashpir", 0.300f, 0.170f),
    RoadNode("Bholaganj Stone Quarry", "Bholaganj", 0.740f, 0.290f),
    RoadNode("Jaflong Stone Yard", "Jaflong", 0.900f, 0.300f),
    RoadNode("Tamabil Sand Pit", "Tamabil", 0.780f, 0.400f),
    RoadNode("Sitakunda Ridge Quarry", "Sitakunda", 0.783f, 0.661f),
    RoadNode("Tejgaon Equipment Market", "Tejgaon", 0.511f, 0.474f),
    RoadNode("Chattogram Machinery Hub", "Chattogram", 0.819f, 0.707f),
    RoadNode("Khulna Tools Bazar", "Khulna", 0.330f, 0.628f),
    RoadNode("Rajshahi Mining Supply", "Rajshahi", 0.128f, 0.374f),
    RoadNode("Bogura Heavy Parts", "Bogura", 0.300f, 0.300f),
    RoadNode("Sylhet Gear Market", "Sylhet", 0.600f, 0.270f)
)

private val roadEdges = listOf(
    "You" to "Tejgaon Equipment Market",
    "You" to "Jamuna Bridge",
    "You" to "Cumilla",
    "You" to "Bhairab",
    "You" to "Jessore",
    "Jessore" to "Khulna Tools Bazar",
    "Jamuna Bridge" to "Bogura Heavy Parts",
    "Bogura Heavy Parts" to "Rajshahi Mining Supply",
    "Bogura Heavy Parts" to "Khalashpir Coal Block",
    "Bogura Heavy Parts" to "Madhyapara Hard Rock Mine",
    "Cumilla" to "Feni",
    "Feni" to "Sitakunda Ridge Quarry",
    "Sitakunda Ridge Quarry" to "Chattogram Machinery Hub",
    "Bhairab" to "Sylhet Gear Market",
    "Sylhet Gear Market" to "Bholaganj Stone Quarry",
    "Bholaganj Stone Quarry" to "Jaflong Stone Yard",
    "Bholaganj Stone Quarry" to "Tamabil Sand Pit",
    "Bhairab" to "Cumilla"
)