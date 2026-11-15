package tax.app.mehdighobadi.ui.settings

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        themeManager.applyTo(this, binding.root, binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
        setupUI()
    }
    private fun setupUI() {
        binding.rvThemes.layoutManager = LinearLayoutManager(this)
        binding.rvThemes.adapter = themeAdapter()
        binding.sliderFontSize.value = themeManager.fontSize.toFloat()
        updateFontSizeText(themeManager.fontSize)
        binding.sliderFontSize.addOnChangeListener { _, value, _ ->
            themeManager.fontSize = value.toInt(); updateFontSizeText(value.toInt())
        }
        binding.switchCompactMode.isChecked = themeManager.compactMode
        binding.switchCompactMode.setOnCheckedChangeListener { _, checked -> themeManager.compactMode = checked }
        binding.switchReduceMotion.isChecked = themeManager.reduceMotion
        binding.switchReduceMotion.setOnCheckedChangeListener { _, checked -> themeManager.reduceMotion = checked }
        binding.btnReset.setOnClickListener {
            themeManager.resetToDefaults()
            binding.sliderFontSize.value = 16f
            binding.switchCompactMode.isChecked = false
            binding.switchReduceMotion.isChecked = false
            updateFontSizeText(16)
            binding.rvThemes.adapter = themeAdapter()
            themeManager.applyTo(this, binding.root, binding.toolbar)
        }
    }
    private fun themeAdapter() = ThemeAdapter(AppTheme.values().toList(), themeManager.currentTheme) { theme ->
        themeManager.currentTheme = theme
        themeManager.applyTo(this, binding.root, binding.toolbar)
        binding.rvThemes.adapter?.notifyDataSetChanged()
    }
    private fun updateFontSizeText(size: Int) { binding.tvFontSizeValue.text = "${size}sp" }
}

private class ThemeAdapter(private val themes: List<AppTheme>, private var selected: AppTheme, private val onSelect: (AppTheme) -> Unit) : RecyclerView.Adapter<ThemeAdapter.Holder>() {
    class Holder(val label: TextView) : RecyclerView.ViewHolder(label)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val label = TextView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(18, 14, 18, 14); textSize = 15f; gravity = Gravity.CENTER_VERTICAL; isClickable = true; isFocusable = true
        }
        return Holder(label)
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val theme = themes[position]
        holder.label.text = if (theme == selected) "✓  ${theme.displayName}" else theme.displayName
        holder.label.setTextColor(ContextCompat.getColor(holder.label.context, R.color.colorInk))
        holder.label.setBackgroundColor(ContextCompat.getColor(holder.label.context, if (theme == selected) R.color.colorSoft else R.color.colorSurface))
        holder.label.setOnClickListener { selected = theme; onSelect(theme); notifyDataSetChanged() }
    }
    override fun getItemCount(): Int = themes.size
}
