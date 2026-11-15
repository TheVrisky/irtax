package tax.app.mehdighobadi.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import tax.app.mehdighobadi.R
import tax.app.mehdighobadi.databinding.ActivitySettingsBinding
import tax.app.mehdighobadi.theme.AppTheme
import tax.app.mehdighobadi.theme.ThemeManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        themeManager = ThemeManager(this)
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupUI()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupUI() {
        // Font size slider
        binding.sliderFontSize.value = themeManager.fontSize.toFloat()
        updateFontSizeText(themeManager.fontSize)
        
        binding.sliderFontSize.addOnChangeListener { _, value, _ ->
            themeManager.fontSize = value.toInt()
            updateFontSizeText(value.toInt())
        }

        // Switches
        binding.switchCompactMode.isChecked = themeManager.compactMode
        binding.switchCompactMode.setOnCheckedChangeListener { _, isChecked ->
            themeManager.compactMode = isChecked
        }

        binding.switchReduceMotion.isChecked = themeManager.reduceMotion
        binding.switchReduceMotion.setOnCheckedChangeListener { _, isChecked ->
            themeManager.reduceMotion = isChecked
        }

        // Reset button
        binding.btnReset.setOnClickListener {
            themeManager.resetToDefaults()
            binding.sliderFontSize.value = 16f
            binding.switchCompactMode.isChecked = false
            binding.switchReduceMotion.isChecked = false
            updateFontSizeText(16)
        }
    }

    private fun updateFontSizeText(size: Int) {
        binding.tvFontSizeValue.text = "${size}px"
    }
}
