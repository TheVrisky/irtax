package tax.app.mehdighobadi.ui.reader

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import tax.app.mehdighobadi.R
import tax.app.mehdighobadi.data.LegalAmendment
import tax.app.mehdighobadi.data.LegalDocumentVersion
import tax.app.mehdighobadi.data.LegalVersionStatus
import tax.app.mehdighobadi.data.repository.LegalHistoryRepository
import kotlinx.coroutines.launch

/** Read-only screen for the publication and amendment history of a legal article. */
class LegalHistoryActivity : AppCompatActivity() {
    private lateinit var content: LinearLayout
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getColor(R.color.background))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        val header = TextView(this).apply {
            text = "تاریخچه و اصلاحات قانون"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(getColor(R.color.text_primary))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(18))
        }
        root.addView(header)
        progress = ProgressBar(this).apply { visibility = View.GONE }
        root.addView(progress, LinearLayout.LayoutParams(dp(32), dp(32)).apply {
            gravity = Gravity.CENTER
            topMargin = dp(8)
        })
        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(8), dp(18), dp(28))
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        val documentId = intent.getStringExtra(EXTRA_DOCUMENT_ID).orEmpty()
        val articleId = intent.getStringExtra(EXTRA_ARTICLE_ID).orEmpty()
        val articleTitle = intent.getStringExtra(EXTRA_ARTICLE_TITLE).orEmpty()
        if (documentId.isBlank()) {
            showMessage("شناسه سند قانونی در دسترس نیست.")
            return
        }
        addSectionTitle(articleTitle.ifBlank { "سوابق انتشار سند" })
        loadHistory(documentId, articleId)
    }

    private fun loadHistory(documentId: String, articleId: String) {
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            val repository = LegalHistoryRepository()
            val versions = repository.loadVersions(documentId)
            val amendments = repository.loadAmendments(documentId)
            val articleHistory = if (articleId.isNotBlank()) {
                repository.loadArticleHistory(articleId)
            } else null
            progress.visibility = View.GONE

            addSectionTitle("نسخه‌های منتشرشده")
            when (versions) {
                is tax.app.mehdighobadi.data.repository.LegalHistoryResult.Success ->
                    if (versions.items.isEmpty()) addMessage("هنوز نسخه‌ای از سرور ثبت نشده است.")
                    else versions.items.forEach { addVersion(it) }
                is tax.app.mehdighobadi.data.repository.LegalHistoryResult.Failure ->
                    addMessage(versions.message)
            }

            addSectionTitle("اصلاحیه‌های سند")
            when (amendments) {
                is tax.app.mehdighobadi.data.repository.LegalHistoryResult.Success ->
                    if (amendments.items.isEmpty()) addMessage("اصلاحیه‌ای برای این سند ثبت نشده است.")
                    else amendments.items.forEach { addAmendment(it) }
                is tax.app.mehdighobadi.data.repository.LegalHistoryResult.Failure ->
                    addMessage(amendments.message)
            }

            if (articleHistory != null) {
                addSectionTitle("تاریخچه همین ماده")
                when (articleHistory) {
                    is tax.app.mehdighobadi.data.repository.LegalHistoryResult.Success ->
                        if (articleHistory.items.isEmpty()) addMessage("برای این ماده سابقه تغییری ثبت نشده است.")
                        else articleHistory.items.forEach { addAmendment(it) }
                    is tax.app.mehdighobadi.data.repository.LegalHistoryResult.Failure ->
                        addMessage(articleHistory.message)
                }
            }
        }
    }

    private fun addVersion(version: LegalDocumentVersion) {
        addCard {
            addText("نسخه ${version.versionNumber}", 17f, true)
            addText("وضعیت: ${statusLabel(version.status)}")
            version.publicationDate?.let { addText("تاریخ انتشار: $it") }
            version.effectiveDate?.let { addText("تاریخ اجرا: $it") }
            version.expirationDate?.let { addText("تاریخ پایان اعتبار: $it") }
            version.amendmentDate?.let { addText("آخرین اصلاح: $it") }
            version.checksum?.takeIf { it.isNotBlank() }?.let {
                addText("شناسه صحت نسخه: ${it.take(18)}", 12f)
            }
        }
    }

    private fun addAmendment(item: LegalAmendment) {
        addCard {
            addText("نوع تغییر: ${item.amendmentType}", 16f, true)
            addText("شناسه ماده: ${item.articleId}", 12f)
            item.publicationDate?.let { addText("انتشار: $it") }
            item.effectiveDate?.let { addText("لازم‌الاجرا: $it") }
            item.sourceReference?.takeIf { it.isNotBlank() }?.let { addText("مستند: $it") }
            item.previousText?.takeIf { it.isNotBlank() }?.let {
                addText("متن پیشین", 14f, true, R.color.error)
                addText(it)
            }
            item.newText?.takeIf { it.isNotBlank() }?.let {
                addText("متن اصلاح‌شده", 14f, true, R.color.primary)
                addText(it)
            }
            if (item.previousText.isNullOrBlank() && item.newText.isNullOrBlank()) {
                addText("متن تغییر در پاسخ سرور ارائه نشده است.")
            }
        }
    }

    private fun addSectionTitle(title: String) {
        val view = TextView(this).apply {
            text = title
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(getColor(R.color.primary))
            setPadding(0, dp(18), 0, dp(8))
        }
        content.addView(view)
    }

    private fun addMessage(message: String) {
        addCard { addText(message) }
    }

    private fun addCard(build: LinearLayout.() -> Unit) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            setBackgroundResource(R.drawable.bg_glass_card)
            build()
        }
        content.addView(card, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(10)
        })
    }

    private fun LinearLayout.addText(
        value: String,
        size: Float = 14f,
        bold: Boolean = false,
        colorRes: Int = R.color.text_primary
    ) {
        addView(TextView(this@LegalHistoryActivity).apply {
            text = value
            textSize = size
            setTextColor(getColor(colorRes))
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setLineSpacing(dp(3).toFloat(), 1.08f)
            setPadding(0, dp(3), 0, dp(3))
            textDirection = View.TEXT_DIRECTION_FIRST_STRONG
        })
    }

    private fun statusLabel(status: LegalVersionStatus): String = when (status) {
        LegalVersionStatus.DRAFT -> "پیش‌نویس"
        LegalVersionStatus.REVIEW -> "در حال بررسی"
        LegalVersionStatus.APPROVED -> "تأییدشده"
        LegalVersionStatus.PUBLISHED -> "منتشرشده"
        LegalVersionStatus.ARCHIVED -> "بایگانی‌شده"
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        addMessage(message)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val EXTRA_DOCUMENT_ID = "legal_document_id"
        private const val EXTRA_ARTICLE_ID = "legal_article_id"
        private const val EXTRA_ARTICLE_TITLE = "legal_article_title"

        fun intent(context: android.content.Context, documentId: String, articleId: String?, articleTitle: String) =
            android.content.Intent(context, LegalHistoryActivity::class.java)
                .putExtra(EXTRA_DOCUMENT_ID, documentId)
                .putExtra(EXTRA_ARTICLE_ID, articleId)
                .putExtra(EXTRA_ARTICLE_TITLE, articleTitle)
    }
}
