package tax.app.mehdighobadi.ui.content

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import tax.app.mehdighobadi.R
import tax.app.mehdighobadi.data.Article
import tax.app.mehdighobadi.data.TaxData
import tax.app.mehdighobadi.databinding.ActivityContentBinding
import tax.app.mehdighobadi.ui.search.SearchActivity
import tax.app.mehdighobadi.ui.settings.SettingsActivity
import tax.app.mehdighobadi.ui.cover.CoverActivity

class ContentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContentBinding
    private var chapterId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chapterId = intent.getStringExtra("chapter_id") ?: return
        
        setupToolbar()
        loadContent()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_search -> {
                    startActivity(android.content.Intent(this, SearchActivity::class.java))
                    true
                }
                R.id.action_home -> {
                    startActivity(android.content.Intent(this, CoverActivity::class.java).apply {
                        flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                    })
                    true
                }
                R.id.action_settings -> {
                    startActivity(android.content.Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadContent() {
        val chapter = TaxData.content.chapters.find { it.id == chapterId } ?: return
        
        binding.toolbar.title = chapter.title
        binding.tvChapterTitle.text = chapter.title

        binding.articlesContainer.removeAllViews()
        
        chapter.articles.forEach { article ->
            addArticleView(article)
        }
    }

    private fun addArticleView(article: Article) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
                bottomMargin = 8
            }
        }

        // Article number
        val tvNumber = TextView(this).apply {
            text = article.number
            setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(16, 16, 16, 8)
        }
        container.addView(tvNumber)

        // Article content
        val tvContent = TextView(this).apply {
            text = article.content
            setTextColor(ContextCompat.getColor(context, R.color.colorInk))
            textSize = 14f
            setPadding(16, 0, 16, 8)
        }
        container.addView(tvContent)

        // Notes
        article.notes.forEach { note ->
            val tvNote = TextView(this).apply {
                text = "${note.label}: ${note.content}"
                setTextColor(ContextCompat.getColor(context, R.color.colorAccent2))
                textSize = 13f
                setPadding(24, 4, 24, 4)
            }
            container.addView(tvNote)
        }

        binding.articlesContainer.addView(container)
    }
}
