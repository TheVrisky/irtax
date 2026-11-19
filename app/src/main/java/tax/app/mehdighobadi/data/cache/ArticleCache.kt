package tax.app.mehdighobadi.data.cache

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import tax.app.mehdighobadi.data.remote.ArticleDto

/**
 * Small offline-first cache for published server articles.
 * Existing static TaxData and reading-progress storage are left untouched.
 */
class ArticleCache(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_ARTICLES (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                slug TEXT,
                summary TEXT,
                content TEXT NOT NULL,
                category_id TEXT,
                updated_at TEXT,
                cached_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX index_articles_updated_at ON $TABLE_ARTICLES(updated_at)")
        createLegalDocumentsTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createLegalDocumentsTable(db)
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_LEGAL_DOCUMENTS RENAME TO legal_documents_legacy")
            createLegalDocumentsTable(db)
            db.execSQL("INSERT OR IGNORE INTO $TABLE_LEGAL_DOCUMENTS (id, version, payload, cached_at) SELECT id, COALESCE(version, 'legacy'), payload, cached_at FROM legal_documents_legacy")
            db.execSQL("DROP TABLE legal_documents_legacy")
        }
    }

    private fun createLegalDocumentsTable(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_LEGAL_DOCUMENTS (id TEXT NOT NULL, version TEXT NOT NULL, payload TEXT NOT NULL, cached_at INTEGER NOT NULL, PRIMARY KEY(id, version))")
    }

    fun saveLegalDocument(id: String, version: String?, payload: String) {
        val values = ContentValues().apply {
            put("id", id)
            put("version", version ?: "unknown")
            put("payload", payload)
            put("cached_at", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict(TABLE_LEGAL_DOCUMENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getLegalDocumentVersion(id: String, version: String): String? {
        readableDatabase.query(TABLE_LEGAL_DOCUMENTS, arrayOf("payload"), "id = ? AND version = ?", arrayOf(id, version), null, null, null, "1").use { cursor ->
            return if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }

    fun getCachedLegalVersions(id: String): List<String> {
        val versions = mutableListOf<String>()
        readableDatabase.query(TABLE_LEGAL_DOCUMENTS, arrayOf("version"), "id = ?", arrayOf(id), null, null, "cached_at DESC").use { cursor ->
            while (cursor.moveToNext()) versions += cursor.getString(0)
        }
        return versions
    }

    fun getLatestLegalDocument(): String? {
        readableDatabase.query(
            TABLE_LEGAL_DOCUMENTS, arrayOf("payload"), null, null, null, null, "cached_at DESC", "1"
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }

    fun upsertAll(articles: List<ArticleDto>) {
        if (articles.isEmpty()) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            val now = System.currentTimeMillis()
            articles.forEach { article ->
                val values = ContentValues().apply {
                    put(COL_ID, article.id)
                    put(COL_TITLE, article.title)
                    put(COL_SLUG, article.slug)
                    put(COL_SUMMARY, article.summary)
                    put(COL_CONTENT, article.content)
                    put(COL_CATEGORY_ID, article.categoryId)
                    put(COL_UPDATED_AT, article.updatedAt)
                    put(COL_CACHED_AT, now)
                }
                db.insertWithOnConflict(TABLE_ARTICLES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getAll(): List<ArticleDto> {
        val result = mutableListOf<ArticleDto>()
        readableDatabase.query(
            TABLE_ARTICLES,
            arrayOf(COL_ID, COL_TITLE, COL_SLUG, COL_SUMMARY, COL_CONTENT, COL_CATEGORY_ID, COL_UPDATED_AT),
            null, null, null, null, "$COL_UPDATED_AT DESC, $COL_TITLE COLLATE NOCASE ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += ArticleDto(
                    id = cursor.getString(0),
                    title = cursor.getString(1),
                    slug = cursor.getString(2),
                    summary = cursor.getString(3),
                    content = cursor.getString(4),
                    categoryId = cursor.getString(5),
                    updatedAt = cursor.getString(6)
                )
            }
        }
        return result
    }

    fun getById(id: String): ArticleDto? {
        readableDatabase.query(
            TABLE_ARTICLES,
            arrayOf(COL_ID, COL_TITLE, COL_SLUG, COL_SUMMARY, COL_CONTENT, COL_CATEGORY_ID, COL_UPDATED_AT),
            "$COL_ID = ?", arrayOf(id), null, null, null, "1"
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return ArticleDto(
                id = cursor.getString(0),
                title = cursor.getString(1),
                slug = cursor.getString(2),
                summary = cursor.getString(3),
                content = cursor.getString(4),
                categoryId = cursor.getString(5),
                updatedAt = cursor.getString(6)
            )
        }
    }

    fun count(): Int {
        readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_ARTICLES", null).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    companion object {
        private const val DATABASE_NAME = "irtax_content_cache.db"
        private const val DATABASE_VERSION = 3
        private const val TABLE_ARTICLES = "articles"
        private const val TABLE_LEGAL_DOCUMENTS = "legal_documents"
        private const val COL_ID = "id"
        private const val COL_TITLE = "title"
        private const val COL_SLUG = "slug"
        private const val COL_SUMMARY = "summary"
        private const val COL_CONTENT = "content"
        private const val COL_CATEGORY_ID = "category_id"
        private const val COL_UPDATED_AT = "updated_at"
        private const val COL_CACHED_AT = "cached_at"
    }
}
