package tax.app.mehdighobadi.ui.toc

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import tax.app.mehdighobadi.data.TaxData
import tax.app.mehdighobadi.data.Chapter
import tax.app.mehdighobadi.databinding.ActivityTocBinding
import tax.app.mehdighobadi.ui.content.ContentActivity

class TOCActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTocBinding
    private val chapters = TaxData.content.chapters

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTocBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        binding.rvChapters.layoutManager = LinearLayoutManager(this)
        binding.rvChapters.adapter = ChapterAdapter(chapters) { chapter ->
            val intent = Intent(this, ContentActivity::class.java)
            intent.putExtra("chapter_id", chapter.id)
            startActivity(intent)
        }
    }
}

class ChapterAdapter(
    private val chapters: List<Chapter>,
    private val onChapterClick: (Chapter) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<ChapterAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val tvTitle: android.widget.TextView = itemView.findViewById(R.id.tv_chapter_title)
        val tvArticles: android.widget.TextView = itemView.findViewById(R.id.tv_articles)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chapter = chapters[position]
        holder.tvTitle.text = chapter.title
        holder.tvArticles.text = chapter.articles.joinToString(" | ") { it.number }
        holder.itemView.setOnClickListener { onChapterClick(chapter) }
    }

    override fun getItemCount() = chapters.size
}
