package tax.app.mehdighobadi.ui.bank

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import android.widget.Toast
import kotlinx.coroutines.launch
import tax.app.mehdighobadi.data.repository.LegalDocumentRepository
import tax.app.mehdighobadi.data.repository.LegalContentSource
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tax.app.mehdighobadi.R
import tax.app.mehdighobadi.data.AppRepository
import tax.app.mehdighobadi.data.Chapter
import tax.app.mehdighobadi.data.TaxData
import tax.app.mehdighobadi.databinding.FragmentBankBinding
import tax.app.mehdighobadi.databinding.ItemChapterBinding
import tax.app.mehdighobadi.theme.ThemeManager
import tax.app.mehdighobadi.ui.common.addPressScale
import tax.app.mehdighobadi.ui.common.enterStaggered
import tax.app.mehdighobadi.ui.common.toFaDigits
import tax.app.mehdighobadi.ui.reader.ReaderActivity
import tax.app.mehdighobadi.ui.main.MainActivity

/** بانک سوالات: فصل‌ها با پیشرفت هر فصل + جستجوی زنده در متن سوالات */
class BankFragment : Fragment() {

    private var _binding: FragmentBankBinding? = null
    private val binding get() = _binding!!
    private val repo by lazy { AppRepository.get(requireContext()) }
    private val themeManager by lazy { ThemeManager(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnFavorites.setOnClickListener {
            (activity as? MainActivity)?.showFavorites()
        }

        binding.rvBank.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBank.adapter = ChapterAdapter(repo, themeManager) { chapterId, index ->
            startActivity(ReaderActivity.intent(requireContext(), chapterId, index))
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.btnClear.visibility =
                    if (s.isNullOrBlank()) View.GONE else View.VISIBLE
                applyFilter(s?.toString() ?: "")
            }
        })

        binding.btnClear.setOnClickListener {
            binding.etSearch.setText("")
        }

        applyFilter("")
        loadLegalDocument()
    }

    override fun onResume() {
        super.onResume()
        // پیشرفت ممکن است پس از مطالعه تغییر کرده باشد
        if (binding.etSearch.text.isNullOrBlank()) applyFilter("")
    }


    private fun loadLegalDocument() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = LegalDocumentRepository(requireContext()).loadActiveDocument()
            if (!isAdded || _binding == null) return@launch
            if (result.content.chapters.isNotEmpty()) {
                TaxData.content = result.content
                val adapter = binding.rvBank.adapter as? ChapterAdapter
                val query = binding.etSearch.text?.toString().orEmpty()
                if (query.isBlank()) adapter?.showChapters() else applyFilter(query)
            }
            if (result.source != LegalContentSource.REMOTE && !result.message.isNullOrBlank()) {
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun applyFilter(query: String) {
        val trimmed = query.trim()
        val adapter = binding.rvBank.adapter as ChapterAdapter
        if (trimmed.length >= 2) {
            val results = TaxData.searchInContent(trimmed)
            adapter.showResults(results)
            binding.emptyState.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
            binding.tvEmpty.text = getString(R.string.no_results)
        } else {
            adapter.showChapters()
            binding.emptyState.visibility = View.GONE
            if (trimmed.length == 1) {
                binding.emptyState.visibility = View.VISIBLE
                binding.tvEmpty.text = getString(R.string.search_min_hint)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ChapterAdapter(
    private val repo: AppRepository,
    private val themeManager: ThemeManager,
    private val onOpen: (chapterId: String, articleIndex: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private sealed interface Row
    private data class ChapterRow(val chapter: Chapter) : Row
    private data class ResultRow(val chapterId: String, val title: String) : Row

    private var rows: List<Row> = emptyList()

    fun showChapters() {
        rows = TaxData.content.chapters.map { ChapterRow(it) }
        notifyDataSetChanged()
    }

    fun showResults(results: List<Pair<String, String>>) {
        rows = results.map { ResultRow(it.first, it.second) }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int =
        if (rows[position] is ChapterRow) TYPE_CHAPTER else TYPE_RESULT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemChapterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as Holder
        val b = vh.binding
        val motion = themeManager.reduceMotion
        when (val row = rows[position]) {
            is ChapterRow -> {
                val chapter = row.chapter
                b.tvChapterTitle.text = chapter.title
                b.tvChapterCount.text =
                    getStringFor(b, R.string.articles_count, chapter.articles.size.toFaDigits())
                val read = repo.chapterReadCount(chapter.id)
                b.tvChapterProgress.text =
                    getStringFor(b, R.string.chapter_progress, read.toFaDigits(), chapter.articles.size.toFaDigits())
                b.progressChapter.progress =
                    if (chapter.articles.isEmpty()) 0
                    else (read * 100 / chapter.articles.size)
                b.root.setOnClickListener { onOpen(chapter.id, 0) }
            }
            is ResultRow -> {
                b.tvChapterTitle.text = row.title
                b.tvChapterCount.text = ""
                b.tvChapterProgress.text = ""
                b.progressChapter.progress = 0
                b.root.setOnClickListener {
                    val chapter = TaxData.content.chapters.find { it.id == row.chapterId }
                    if (chapter != null) onOpen(chapter.id, 0)
                }
            }
        }
        b.root.addPressScale(motion)
        b.root.enterStaggered(position, motion)
    }

    private fun getStringFor(b: ItemChapterBinding, res: Int, vararg args: String): String =
        b.root.context.getString(res, *args)

    override fun getItemCount(): Int = rows.size

    class Holder(val binding: ItemChapterBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        const val TYPE_CHAPTER = 0
        const val TYPE_RESULT = 1
    }
}
