package tax.app.mehdighobadi.ui.favorites

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tax.app.mehdighobadi.databinding.FragmentFavoritesBinding
import tax.app.mehdighobadi.databinding.ItemFavoriteBinding
import tax.app.mehdighobadi.data.AppRepository
import tax.app.mehdighobadi.theme.ThemeManager
import tax.app.mehdighobadi.ui.common.addPressScale
import tax.app.mehdighobadi.ui.common.enterStaggered
import tax.app.mehdighobadi.ui.reader.ReaderActivity

/** نشان‌شده‌ها: مواد مهم ذخیره‌شده با دکمه‌ی نشان‌گذاری در صفحه‌ی سوالات */
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val repo by lazy { AppRepository.get(requireContext()) }
    private val themeManager by lazy { ThemeManager(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onResume() {
        super.onResume()
        binding.rvFavorites.adapter = FavoritesAdapter(repo, themeManager) { ref ->
            startActivity(ReaderActivity.intent(requireContext(), ref.chapterId, ref.articleIndex))
        }
        val empty = repo.favoriteRefs().isEmpty()
        binding.emptyState.visibility = if (empty) View.VISIBLE else View.GONE
        binding.rvFavorites.visibility = if (empty) View.GONE else View.VISIBLE
        if (!empty) {
            binding.rvFavorites.enterStaggered(0, themeManager.reduceMotion)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class FavoritesAdapter(
    private val repo: AppRepository,
    private val themeManager: ThemeManager,
    private val onOpen: (AppRepository.ArticleRef) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.Holder>() {

    private val refs = repo.favoriteRefs()

    class Holder(val binding: ItemFavoriteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemFavoriteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val ref = refs[position]
        val article = ref.article()
        val chapter = ref.chapter()
        holder.binding.tvArticleNumber.text = article?.number ?: ""
        holder.binding.tvChapterName.text = chapter?.title ?: ""
        holder.binding.tvArticleSnippet.text = article?.content ?: ""
        holder.binding.root.addPressScale(themeManager.reduceMotion)
        holder.binding.root.enterStaggered(position, themeManager.reduceMotion)
        holder.binding.root.setOnClickListener { onOpen(ref) }
    }

    override fun getItemCount(): Int = refs.size
}
