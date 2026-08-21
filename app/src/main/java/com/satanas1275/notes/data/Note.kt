package com.satanas1275.notes.data

import org.json.JSONArray
import org.json.JSONObject

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val pinned: Boolean = false,
    val colorIndex: Int = 0
) {
    val isEmpty: Boolean
        get() = title.isBlank() && content.isBlank()

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("content", content)
        put("createdAt", createdAt)
        put("modifiedAt", modifiedAt)
        put("pinned", pinned)
        put("colorIndex", colorIndex)
    }

    companion object {
        fun fromJson(o: JSONObject): Note = Note(
            id = o.getString("id"),
            title = o.optString("title"),
            content = o.optString("content"),
            createdAt = o.optLong("createdAt"),
            modifiedAt = o.optLong("modifiedAt"),
            pinned = o.optBoolean("pinned"),
            colorIndex = o.optInt("colorIndex")
        )

        fun listToJson(notes: List<Note>): String =
            JSONArray().apply { notes.forEach { put(it.toJson()) } }.toString()

        fun listFromJson(json: String?): List<Note> {
            if (json.isNullOrBlank()) return emptyList()
            return runCatching {
                val array = JSONArray(json)
                List(array.length()) { index -> fromJson(array.getJSONObject(index)) }
            }.getOrDefault(emptyList())
        }
    }
}
