package tax.app.mehdighobadi.ui.exams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tax.app.mehdighobadi.R
import tax.app.mehdighobadi.databinding.FragmentExamsBinding
import tax.app.mehdighobadi.ui.main.MainActivity

/** صفحه معرفی مرکز آزمون؛ موتور آزمون در مرحله بعدی توسعه متصل می‌شود. */
class ExamsFragment : Fragment() {
    private var _binding: FragmentExamsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnExamsExplore.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_articles)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}