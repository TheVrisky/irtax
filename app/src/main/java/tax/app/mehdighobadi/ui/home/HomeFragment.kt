package tax.app.mehdighobadi.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import tax.app.mehdighobadi.R
import tax.app.mehdighobadi.data.AppRepository
import tax.app.mehdighobadi.data.Chapter
import tax.app.mehdighobadi.data.TaxData
import tax.app.mehdighobadi.databinding.FragmentHomeBinding
import tax.app.mehdighobadi.theme.ThemeManager
import tax.app.mehdighobadi.ui.common.addPressScale
import tax.app.mehdighobadi.ui.common.animateCounter
import tax.app.mehdighobadi.ui.common.enterStaggered
import tax.app.mehdighobadi.ui.common.toFaDigits
import tax.app.mehdighobadi.ui.main.MainActivity
import tax.app.mehdighobadi.ui.reader.ReaderActivity

/** صفحه‌ی اصلی: هدر مینیمال + کارت Hero + آمار شیشه‌ای + پیشنهاد هوشمند + دسترسی سریع */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val repo by lazy { AppRepository.get(requireContext()) }
    private val themeManager by lazy { ThemeManager(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val motion = themeManager.reduceMotion

        // هدر مینیمال: فقط یک آیکون تنظیمات، بدون هیچ اطلاعات هویتی سازنده
        binding.btnSettings.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_settings)
        }
        binding.btnSettings.addPressScale(motion)

        binding.btnHeroCta.setOnClickListener { openReader() }
        binding.btnPractice.setOnClickListener { openSuggestion() }
        binding.heroCard.addPressScale(motion)
        binding.suggestionCard.addPressScale(motion)

        binding.header.enterStaggered(0, motion)
        binding.heroCard.enterStaggered(1, motion)
        binding.suggestionCard.enterStaggered(2, motion)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val chapters = TaxData.content.chapters
        val total = repo.totalArticles()
        val read = repo.readCount()
        val progress = repo.overallProgress()
        val motion = themeManager.reduceMotion

        // آمار هوشمند با شمارنده‌ی متحرک
        binding.statArticles.animateCounter(total, motion)
        binding.statRead.animateCounter(read, motion)

        // Hero: ادامه مطالعه یا شروع آزمون
        val lastRef = repo.lastReadRef()
        if (lastRef != null) {
            binding.tvHeroTitle.text = lastRef.chapter()?.title
            binding.tvHeroSubtitle.text =
                "${lastRef.article()?.number ?: ""} • ${getString(R.string.last_read)}"
            binding.btnHeroCta.text = getString(R.string.hero_cta)
        } else {
            binding.tvHeroTitle.text = getString(R.string.hero_cta_start)
            binding.tvHeroSubtitle.text = getString(R.string.bank_subtitle)
            binding.btnHeroCta.text = getString(R.string.hero_cta_start)
        }
        binding.tvHeroPercent.text = "${(progress * 100).toInt().toFaDigits()}٪"
        binding.heroProgress.setProgressCompat((progress * 100).toInt(), true)

        // پیشنهاد هوشمند (لایه داده‌ی محلی، بدون سرویس آنلاین)
        val suggestion = repo.smartSuggestion()
        if (suggestion != null) {
            val p = repo.chapterProgress(suggestion.id)
            binding.tvSuggestionText.text =
                getString(R.string.smart_suggestion_practice) +
                    "\n${suggestion.title} (${(p * 100).toInt().toFaDigits()}٪)"
            binding.btnPractice.visibility = View.VISIBLE
        } else {
            binding.tvSuggestionText.text = getString(R.string.smart_suggestion_done)
            binding.btnPractice.visibility = View.GONE
        }

        buildChips(chapters)
    }

    private fun buildChips(chapters: List<Chapter>) {
        binding.chipGroupChapters.removeAllViews()
        chapters.forEachIndexed { i, chapter ->
            val chip = Chip(requireContext()).apply {
                text = "${chapter.title.substringBefore(" - ")} • " +
                    chapter.articles.size.toFaDigits() + " " + getString(R.string.stat_articles)
                isCheckable = false
                setOnClickListener { openReader(chapter.id, 0) }
            }
            binding.chipGroupChapters.addView(chip)
            chip.enterStaggered(i, themeManager.reduceMotion, 120L)
        }
    }

    private fun openReader(chapterId: String? = null, index: Int = 0) {
        val ref = repo.lastReadRef()
        val targetChapter = chapterId ?: ref?.chapterId ?: TaxData.content.chapters.first().id
        val targetIndex = if (chapterId == null) ref?.articleIndex ?: 0 else index
        startActivity(ReaderActivity.intent(requireContext(), targetChapter, targetIndex))
    }

    private fun openSuggestion() {
        val suggestion = repo.smartSuggestion() ?: TaxData.content.chapters.first()
        startActivity(ReaderActivity.intent(requireContext(), suggestion.id, 0))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
