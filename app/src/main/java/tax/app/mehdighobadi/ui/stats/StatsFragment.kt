package tax.app.mehdighobadi.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tax.app.mehdighobadi.data.AppRepository
import tax.app.mehdighobadi.data.TaxData
import tax.app.mehdighobadi.databinding.FragmentStatsBinding
import tax.app.mehdighobadi.databinding.ItemChapterProgressBinding
import tax.app.mehdighobadi.theme.ThemeManager
import tax.app.mehdighobadi.ui.common.animateCounter
import tax.app.mehdighobadi.ui.common.enterStaggered
import tax.app.mehdighobadi.ui.common.toFaDigits

/** آمار و پیشرفت: شمارنده‌های متحرک + پیشرفت به تفکیک فصل */
class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val repo by lazy { AppRepository.get(requireContext()) }
    private val themeManager by lazy { ThemeManager(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val motion = themeManager.reduceMotion

        val total = repo.totalArticles()
        val read = repo.readCount()
        val progress = repo.overallProgress()

        binding.tvStatRead.animateCounter(read, motion)
        binding.tvStatDays.animateCounter(repo.studyDays(), motion)

        binding.progressCard.enterStaggered(0, motion)

        binding.rvChapterProgress.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChapterProgress.adapter = ChapterProgressAdapter(repo, themeManager)
        binding.rvChapterProgress.enterStaggered(2, motion)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val total = repo.totalArticles()
        val read = repo.readCount()
        val progress = repo.overallProgress()
        val motion = themeManager.reduceMotion

        binding.tvOverallPercent.text = "${(progress * 100).toInt().toFaDigits()}٪"
        binding.progressOverall.setProgressCompat((progress * 100).toInt(), true)
        binding.tvOverallDetail.text = getString(
            tax.app.mehdighobadi.R.string.chapter_progress,
            read.toFaDigits(), total.toFaDigits()
        )
        (binding.rvChapterProgress.adapter as? ChapterProgressAdapter)?.notifyDataSetChanged()
        binding.tvStatRead.animateCounter(read, motion)
        binding.tvStatDays.animateCounter(repo.studyDays(), motion)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ChapterProgressAdapter(
    private val repo: AppRepository,
    private val themeManager: ThemeManager
) : RecyclerView.Adapter<ChapterProgressAdapter.Holder>() {

    private val chapters = TaxData.content.chapters

    class Holder(val binding: ItemChapterProgressBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemChapterProgressBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val chapter = chapters[position]
        val read = repo.chapterReadCount(chapter.id)
        val size = chapter.articles.size
        holder.binding.tvChapterTitle.text = chapter.title
        holder.binding.tvChapterRatio.text = "${read.toFaDigits()}/${size.toFaDigits()}"
        holder.binding.progressChapter.progress =
            if (size == 0) 0 else read * 100 / size
        holder.binding.root.enterStaggered(position, themeManager.reduceMotion)
    }

    override fun getItemCount(): Int = chapters.size
}
