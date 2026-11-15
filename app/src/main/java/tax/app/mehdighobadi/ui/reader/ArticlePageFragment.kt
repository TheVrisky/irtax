package tax.app.mehdighobadi.ui.reader

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import tax.app.mehdighobadi.R
import tax.app.mehdighobadi.data.Article
import tax.app.mehdighobadi.data.TaxData
import tax.app.mehdighobadi.databinding.FragmentArticlePageBinding
import tax.app.mehdighobadi.theme.ThemeManager
import tax.app.mehdighobadi.ui.common.enterStaggered

/** هر صفحه: کارت سوال اصلی (متن، بندها) + کارت‌های تبصره، با انیمیشن ورود */
class ArticlePageFragment : Fragment() {

    private var _binding: FragmentArticlePageBinding? = null
    private val binding get() = _binding!!

    private var chapterId: String = ""
    private var articleIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chapterId = arguments?.getString(ARG_CHAPTER_ID) ?: ""
        articleIndex = arguments?.getInt(ARG_ARTICLE_INDEX, 0) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticlePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val chapter = TaxData.content.chapters.find { it.id == chapterId } ?: return
        val article = chapter.articles.getOrNull(articleIndex) ?: return
        val themeManager = ThemeManager(requireContext())
        val motion = themeManager.reduceMotion

        // کارت سوال با انیمیشن ورود slide + fade
        binding.questionCard.enterStaggered(0, motion)

        binding.tvArticleNumber.text = article.number
        binding.tvArticleContent.text = article.content
        // اندازه فونت تنظیمات کاربر
        binding.tvArticleContent.textSize = (themeManager.fontSize + 1).toFloat()

        renderSections(article, themeManager.fontSize)
        renderNotes(article, themeManager.fontSize)
    }

    private fun renderSections(article: Article, fontSize: Int) {
        if (article.sections.isEmpty()) return
        binding.sectionsContainer.visibility = View.VISIBLE
        binding.sectionsContainer.removeAllViews()
        article.sections.forEachIndexed { i, section ->
            val card = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_section_card)
                setPadding(dp(16), dp(14), dp(16), dp(14))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(10) }
            }

            val title = TextView(requireContext()).apply {
                text = section.title
                setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                textSize = 14f
                setTypeface(typeface, Typeface.BOLD)
                letterSpacing = 0.01f
            }
            card.addView(title)

            val content = TextView(requireContext()).apply {
                text = section.content
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                textSize = fontSize.toFloat()
                setLineSpacing(dp(3).toFloat(), 1.1f)
            }
            card.addView(content)

            binding.sectionsContainer.addView(card)
            card.enterStaggered(i + 1, ThemeManager(requireContext()).reduceMotion, 80L)
        }
    }

    private fun renderNotes(article: Article, fontSize: Int) {
        if (article.notes.isEmpty()) return
        binding.notesContainer.visibility = View.VISIBLE
        binding.notesContainer.removeAllViews()

        val label = TextView(requireContext()).apply {
            text = getString(R.string.reader_notes_title)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(4), dp(8), dp(4), dp(4))
        }
        binding.notesContainer.addView(label)

        article.notes.forEachIndexed { i, note ->
            val callout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_note_callout)
                setPadding(dp(16), dp(14), dp(16), dp(14))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(8) }
            }

            val title = TextView(requireContext()).apply {
                text = note.label
                setTextColor(ContextCompat.getColor(requireContext(), R.color.on_secondary_container))
                textSize = 13f
                setTypeface(typeface, Typeface.BOLD)
            }
            callout.addView(title)

            val content = TextView(requireContext()).apply {
                text = note.content
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                textSize = (fontSize - 1).coerceAtLeast(12).toFloat()
                setLineSpacing(dp(2).toFloat(), 1f)
            }
            callout.addView(content)

            binding.notesContainer.addView(callout)
            callout.enterStaggered(i + 1, ThemeManager(requireContext()).reduceMotion, 120L)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CHAPTER_ID = "chapter_id"
        private const val ARG_ARTICLE_INDEX = "article_index"

        fun newInstance(chapterId: String, articleIndex: Int): ArticlePageFragment =
            ArticlePageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CHAPTER_ID, chapterId)
                    putInt(ARG_ARTICLE_INDEX, articleIndex)
                }
            }
    }
}
