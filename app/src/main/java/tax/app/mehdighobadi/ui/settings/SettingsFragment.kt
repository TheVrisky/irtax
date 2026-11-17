package tax.app.mehdighobadi.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButtonToggleGroup
import tax.app.mehdighobadi.R
import tax.app.mehdighobadi.databinding.FragmentSettingsBinding
import tax.app.mehdighobadi.theme.Accent
import tax.app.mehdighobadi.theme.DarkMode
import tax.app.mehdighobadi.theme.ThemeManager
import tax.app.mehdighobadi.ui.common.addPressScale
import tax.app.mehdighobadi.ui.common.enterStaggered
import tax.app.mehdighobadi.ui.common.toFaDigits

/** تنظیمات: حالت نمایش، رنگ اصلی، اندازه فونت، کاهش انیمیشن، بازنشانی */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val themeManager by lazy { ThemeManager(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val motion = themeManager.reduceMotion

        binding.progressSection.enterStaggered(0, motion)
        binding.readingSection.enterStaggered(1, motion)

        setupDarkMode()
        setupAccent()
        setupReading()
        setupReset()
    }

    private fun setupDarkMode() {
        val checkedId = when (themeManager.darkMode) {
            DarkMode.LIGHT -> R.id.btn_mode_light
            DarkMode.DARK -> R.id.btn_mode_dark
            DarkMode.SYSTEM -> R.id.btn_mode_system
        }
        binding.toggleDarkMode.check(checkedId)
        binding.toggleDarkMode.addOnButtonCheckedListener { _: MaterialButtonToggleGroup, id: Int, isChecked: Boolean ->
            if (!isChecked) return@addOnButtonCheckedListener
            val mode = when (id) {
                R.id.btn_mode_light -> DarkMode.LIGHT
                R.id.btn_mode_dark -> DarkMode.DARK
                else -> DarkMode.SYSTEM
            }
            if (mode != themeManager.darkMode) {
                themeManager.darkMode = mode
                themeManager.applyNightMode() // Activityها به‌صورت خودکار بازسازی می‌شوند
            }
        }
    }

    private fun setupAccent() {
        updateAccentSelection()
        binding.swatchIndigo.setOnClickListener { changeAccent(Accent.INDIGO) }
        binding.swatchTeal.setOnClickListener { changeAccent(Accent.TEAL) }
        binding.swatchViolet.setOnClickListener { changeAccent(Accent.VIOLET) }
        listOf(binding.swatchIndigo, binding.swatchTeal, binding.swatchViolet)
            .forEach { it.addPressScale(themeManager.reduceMotion) }
    }

    private fun changeAccent(accent: Accent) {
        if (accent == themeManager.accent) return
        themeManager.accent = accent
        // اعمال رنگ اصلی نیاز به بازسازی Activity دارد
        activity?.recreate()
    }

    private fun updateAccentSelection() {
        binding.swatchIndigoInner.setBackgroundResource(
            if (themeManager.accent == Accent.INDIGO) R.drawable.bg_accent_swatch_selected
            else R.drawable.bg_accent_swatch
        )
        binding.swatchTealInner.setBackgroundResource(
            if (themeManager.accent == Accent.TEAL) R.drawable.bg_accent_swatch_selected
            else R.drawable.bg_accent_swatch
        )
        binding.swatchVioletInner.setBackgroundResource(
            if (themeManager.accent == Accent.VIOLET) R.drawable.bg_accent_swatch_selected
            else R.drawable.bg_accent_swatch
        )
    }

    private fun setupReading() {
        binding.sliderFont.value = themeManager.fontSize.toFloat()
        binding.tvFontValue.text = themeManager.fontSize.toFaDigits()
        binding.sliderFont.addOnChangeListener { _, value, fromUser ->
            if (!fromUser) return@addOnChangeListener
            val size = value.toInt()
            themeManager.fontSize = size
            binding.tvFontValue.text = size.toFaDigits()
        }
        binding.sliderFont.addOnSliderTouchListener(object : com.google.android.material.slider.Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: com.google.android.material.slider.Slider) = Unit
            override fun onStopTrackingTouch(slider: com.google.android.material.slider.Slider) {
                activity?.recreate()
            }
        })

        binding.switchReduceMotion.isChecked = themeManager.reduceMotion
        binding.switchReduceMotion.setOnCheckedChangeListener { _, checked ->
            themeManager.reduceMotion = checked
        }
    }

    private fun setupReset() {
        binding.btnReset.addPressScale(themeManager.reduceMotion)
        binding.btnReset.setOnClickListener {
            themeManager.resetToDefaults()
            themeManager.applyNightMode()
            Toast.makeText(requireContext(), R.string.settings_reset_done, Toast.LENGTH_SHORT).show()
            activity?.recreate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
