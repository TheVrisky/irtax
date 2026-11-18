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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Schema migrations will be added here as the API content model evolves.
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
        private const val DATABASE_VERSION = 1
        private const val TABLE_ARTICLES = "articles"
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
