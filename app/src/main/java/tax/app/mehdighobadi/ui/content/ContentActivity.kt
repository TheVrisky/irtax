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
import tax.app.mehdighobadi.theme.ThemeManager
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
        ThemeManager(this).applyTo(this, binding.root, binding.toolbar)

        chapterId = intent.getStringExtra("chapter_id").orEmpty()
        if (chapterId.isBlank()) { finish(); return }
        
        setupToolbar()
        loadContent()
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) ThemeManager(this).applyTo(this, binding.root, binding.toolbar)
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
        val palette = ThemeManager(this).palette(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(palette.article)
            setPadding(8, 8, 8, 8)
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
            setTextColor(palette.accent)
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(16, 16, 16, 8)
        }
        container.addView(tvNumber)

        // Article content
        val tvContent = TextView(this).apply {
            text = article.content
            setTextColor(palette.text)
            textSize = ThemeManager(this@ContentActivity).fontSize.toFloat()
            setPadding(16, 0, 16, 8)
        }
        container.addView(tvContent)

        // Article sections
        article.sections.forEach { section ->
            val tvSection = TextView(this).apply {
                text = "${section.title}: ${section.content}"
                setTextColor(palette.text)
                textSize = ThemeManager(this@ContentActivity).fontSize.toFloat()
                setPadding(24, 6, 24, 6)
            }
            container.addView(tvSection)
        }

        // Notes
        article.notes.forEach { note ->
            val tvNote = TextView(this).apply {
                text = "${note.label}: ${note.content}"
                setTextColor(palette.accent)
                textSize = 13f
                setPadding(24, 4, 24, 4)
            }
            container.addView(tvNote)
        }

        binding.articlesContainer.addView(container)
    }
}
