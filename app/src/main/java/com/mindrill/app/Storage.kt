package com.mindrill.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

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
                o.getString("cond"), o.getString("details")
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
            )
        }
        sp.edit().putString("listings", arr.toString()).apply()
    }
}