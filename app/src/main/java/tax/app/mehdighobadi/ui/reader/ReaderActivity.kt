package tax.app.mehdighobadi.ui.reader

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import tax.app.mehdighobadi.R
import tax.app.mehdighobadi.data.AppRepository
import tax.app.mehdighobadi.data.TaxData
import tax.app.mehdighobadi.databinding.ActivityReaderBinding
import tax.app.mehdighobadi.theme.ThemeManager
import tax.app.mehdighobadi.ui.common.addPressScale
import tax.app.mehdighobadi.ui.common.toFaDigits

/**
 * صفحه‌ی سوالات: کارت سوال در مرکز، نوار پیشرفت، ناوبری قبلی/بعدی
 * با ترنزیشن slide جهت‌دار (متناسب با RTL) و دکمه‌ی شناور نشان‌گذاری.
 */
class ReaderActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {\n        super.attachBaseContext(ThemeManager.wrapContext(newBase))\n    }\n\n    private lateinit var binding: ActivityReaderBinding
    private lateinit var themeManager: ThemeManager
    private lateinit var repo: AppRepository

    private var chapterId: String = ""
    private var articles: List<tax.app.mehdighobadi.data.Article> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        themeManager = ThemeManager(this)
        themeManager.applyNightMode()
        themeManager.applyAccent(this)
        super.onCreate(savedInstanceState)
        binding = ActivityReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = AppRepository.get(this)

        // آیکون‌های نوار وضعیت با روشن/تاریکی هماهنگ شوند
        val isNight = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = !isNight

        chapterId = intent.getStringExtra(EXTRA_CHAPTER_ID) ?: ""
        val startIndex = intent.getIntExtra(EXTRA_ARTICLE_INDEX, 0)
        val chapter = TaxData.content.chapters.find { it.id == chapterId }
        if (chapter == null) { finish(); return }
        articles = chapter.articles

        setupHeader(chapter.title)
        setupPager(startIndex)
        setupBottomBar()
    }

    private fun setupHeader(chapterTitle: String) {
        binding.tvChapterTitle.text = chapterTitle
        binding.btnBack.addPressScale(themeManager.reduceMotion)
        binding.btnBack.setOnClickListener { finish() }
        binding.btnBookmark.addPressScale(themeManager.reduceMotion)
        binding.btnBookmark.setOnClickListener { toggleBookmark() }
    }

    private fun setupPager(startIndex: Int) {
        binding.viewpager.adapter = ArticlePagerAdapter(this, chapterId, articles)
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                onPageChanged(position)
            }
        })
        binding.viewpager.currentItem = startIndex.coerceIn(articles.indices)
    }

    private fun setupBottomBar() {
        binding.btnPrev.addPressScale(themeManager.reduceMotion)
        binding.btnNext.addPressScale(themeManager.reduceMotion)
        binding.btnPrev.setOnClickListener {
            val current = binding.viewpager.currentItem
            if (current > 0) binding.viewpager.currentItem = current - 1
        }
        binding.btnNext.setOnClickListener {
            val current = binding.viewpager.currentItem
            if (current < articles.size - 1) binding.viewpager.currentItem = current + 1
        }
    }

    private fun onPageChanged(position: Int) {
        val article = articles.getOrNull(position) ?: return

        // ثبت مطالعه برای آمار و پیشنهاد هوشمند
        repo.markRead(chapterId, position)

        // شماره سوال + نوار پیشرفت ظریف
        binding.tvCounter.text = getString(
            R.string.reader_progress,
            article.number,
            articles.size.toFaDigits()
        )
        binding.progressArticles.progress = ((position + 1) * 100 / articles.size)

        // دکمه‌ی نشان‌گذاری (شناور در هدر)
        val isFav = repo.isFavorite(chapterId, position)
        binding.btnBookmark.setImageResource(
            if (isFav) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark
        )
        binding.btnBookmark.contentDescription =
            getString(if (isFav) R.string.reader_bookmarked else R.string.cd_bookmark)

        // وضعیت قبلی/بعدی
        binding.btnPrev.isEnabled = position > 0
        binding.btnNext.isEnabled = position < articles.size - 1
        binding.btnPrev.alpha = if (position > 0) 1f else 0.4f
        binding.btnNext.alpha = if (position < articles.size - 1) 1f else 0.4f
    }

    private fun toggleBookmark() {
        val position = binding.viewpager.currentItem
        val added = repo.toggleFavorite(chapterId, position)
        binding.btnBookmark.setImageResource(
            if (added) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark
        )
    }

    /** ادامه از آخرین ماده‌ی خوانده‌شده یا ماده‌ی مشخص */
    companion object {
        private const val EXTRA_CHAPTER_ID = "chapter_id"
        private const val EXTRA_ARTICLE_INDEX = "article_index"

        fun intent(context: Context, chapterId: String, articleIndex: Int): Intent =
            Intent(context, ReaderActivity::class.java)
                .putExtra(EXTRA_CHAPTER_ID, chapterId)
                .putExtra(EXTRA_ARTICLE_INDEX, articleIndex)
    }
}

/** آداپتر ViewPager2: هر ماده یک صفحه با ترنزیشن slide طبیعی RTL */
class ArticlePagerAdapter(
    activity: FragmentActivity,
    private val chapterId: String,
    private val articles: List<tax.app.mehdighobadi.data.Article>
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = articles.size

    override fun createFragment(position: Int): Fragment =
        ArticlePageFragment.newInstance(chapterId, position)
}
