package tax.app.mehdighobadi.data.cache

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Small persistent cache for immutable legal-history responses.
 * Cache keys include the endpoint and entity ID to keep records isolated.
 */
class LegalHistoryCache(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    private val gson = Gson()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_HISTORY (
                cache_key TEXT PRIMARY KEY NOT NULL,
                payload TEXT NOT NULL,
                cached_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Version 1 is the initial, additive history cache schema.
    }

    fun <T> save(key: String, items: List<T>) {
        val values = ContentValues().apply {
            put(COL_KEY, key)
            put(COL_PAYLOAD, gson.toJson(items))
            put(COL_CACHED_AT, System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict(
            TABLE_HISTORY,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun <T> load(key: String, type: Type): List<T>? {
        readableDatabase.query(
            TABLE_HISTORY,
            arrayOf(COL_PAYLOAD),
            "$COL_KEY = ?",
            arrayOf(key),
            null,
            null,
            null,
            "1"
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return runCatching {
                gson.fromJson<List<T>>(cursor.getString(0), type)
            }.getOrNull()
        }
    }

    inline fun <reified T> load(key: String): List<T>? =
        load(key, object : TypeToken<List<T>>() {}.type)

    companion object {
        private const val DATABASE_NAME = "irtax_legal_history_cache.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_HISTORY = "legal_history"
        private const val COL_KEY = "cache_key"
        private const val COL_PAYLOAD = "payload"
        private const val COL_CACHED_AT = "cached_at"

        fun versionsKey(documentId: String) = "versions:$documentId"
        fun amendmentsKey(documentId: String) = "amendments:$documentId"
        fun articleHistoryKey(articleId: String) = "article-history:$articleId"
    }
}
