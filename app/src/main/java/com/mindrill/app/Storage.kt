package com.mindrill.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("mindrill", Context.MODE_PRIVATE)

    var loggedIn: Boolean
        get() = sp.getBoolean("loggedIn", false)
        set(v) { sp.edit().putBoolean("loggedIn", v).apply() }

    var userName: String
        get() = sp.getString("userName", "") ?: ""
        set(v) { sp.edit().putString("userName", v).apply() }

    fun darkOrNull(): Boolean? = if (sp.contains("dark")) sp.getBoolean("dark", false) else null
    fun setDark(v: Boolean) { sp.edit().putBoolean("dark", v).apply() }

    fun loadSaved(): List<Int> =
        (sp.getString("saved", "") ?: "").split(",").mapNotNull { it.toIntOrNull() }

    fun saveSaved(list: List<Int>) {
        sp.edit().putString("saved", list.joinToString(",")).apply()
    }

    fun loadListings(): List<Product> = try {
        val arr = JSONArray(sp.getString("listings", "[]") ?: "[]")
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Product(
                o.getInt("id"), o.getString("name"), o.getInt("price"),
                Category.valueOf(o.getString("cat")), o.getString("seller"),
                o.getString("cond"), o.getString("details"),
                o.optString("img", "").ifBlank { null }
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    fun saveListings(list: List<Product>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id).put("name", it.name).put("price", it.priceUsd)
                    .put("cat", it.category.name).put("seller", it.seller)
                    .put("cond", it.condition).put("details", it.details)
                    .put("img", it.imagePath ?: "")
            )
        }
        sp.edit().putString("listings", arr.toString()).apply()
    }
}

/** Copies the picked photo into the app's private storage (shrunk) and returns its path. */
fun saveImageFromUri(context: Context, uri: Uri): String? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (bounds.outWidth / sample > 1200 || bounds.outHeight / sample > 1200) sample *= 2
    val bmp = context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
    }
    if (bmp == null) {
        null
    } else {
        val file = File(context.filesDir, "listing_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        file.absolutePath
    }
} catch (e: Exception) {
    null
}

fun deleteImageFile(path: String?) {
    if (path != null) {
        try {
            File(path).delete()
        } catch (e: Exception) {
            // ignore
        }
    }
}