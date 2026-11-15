package tax.app.mehdighobadi.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tax.app.mehdighobadi.R
import tax.app.mehdighobadi.data.TaxData
import tax.app.mehdighobadi.theme.ThemeManager
import tax.app.mehdighobadi.databinding.ActivitySearchBinding
import tax.app.mehdighobadi.ui.content.ContentActivity

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ThemeManager(this).applyTo(this, binding.root, binding.toolbar)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        binding.rvResults.layoutManager = LinearLayoutManager(this)
    }

    private fun setupSearch() {
        binding.btnSearch.setOnClickListener {
            performSearch()
        }
        
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            performSearch()
            true
        }
    }

    private fun performSearch() {
        val query = binding.etSearch.text?.toString().orEmpty().trim()
        if (query.length < 2) {
            binding.rvResults.visibility = View.GONE
            binding.tvNoResults.visibility = View.VISIBLE
            binding.tvNoResults.text = "برای جستجو حداقل دو نویسه وارد کنید"
            return
        }

        val results = TaxData.searchInContent(query)
        
        if (results.isEmpty()) {
            binding.rvResults.visibility = View.GONE
            binding.tvNoResults.visibility = View.VISIBLE
            binding.tvNoResults.text = getString(R.string.no_results)
        } else {
            binding.rvResults.visibility = View.VISIBLE
            binding.tvNoResults.visibility = View.GONE
            binding.rvResults.adapter = SearchResultAdapter(results) { chapterId ->
                startActivity(Intent(this, ContentActivity::class.java).apply {
                    putExtra("chapter_id", chapterId)
                })
            }
        }
    }
}

class SearchResultAdapter(
    private val results: List<Pair<String, String>>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvChapterTitle)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (_, title) = results[position]
        holder.tvTitle.text = title
        holder.itemView.setOnClickListener { onClick(results[position].first) }
    }

    override fun getItemCount() = results.size
}
