package tax.app.mehdighobadi.ui.exams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tax.app.mehdighobadi.databinding.FragmentExamsBinding

/** صفحه پایه آزمون‌ها؛ موتور آزمون و بانک سؤال در مرحله بعد اضافه می‌شوند. */
class ExamsFragment : Fragment() {
    private var _binding: FragmentExamsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
