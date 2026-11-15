package tax.app.mehdighobadi.ui.cover

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import tax.app.mehdighobadi.R
import tax.app.mehdighobadi.data.TaxData
import tax.app.mehdighobadi.databinding.ActivityCoverBinding
import tax.app.mehdighobadi.theme.AppTheme
import tax.app.mehdighobadi.theme.ThemeManager
import tax.app.mehdighobadi.ui.settings.SettingsActivity
import tax.app.mehdighobadi.ui.toc.TOCActivity

class CoverActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCoverBinding
    private lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        themeManager = ThemeManager(this)
        applyTheme()
        super.onCreate(savedInstanceState)
        
        binding = ActivityCoverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        applyAnimations()
    }

    private fun applyTheme() {
        val colorRes = when (themeManager.currentTheme) {
            AppTheme.CLASSIC -> R.color.classic_accent
            AppTheme.MIDNIGHT -> R.color.midnight_accent
            AppTheme.OCEAN -> R.color.ocean_accent
            AppTheme.FOREST -> R.color.forest_accent
            AppTheme.ROYAL -> R.color.royal_accent
            AppTheme.SUNSET -> R.color.sunset_accent
            AppTheme.EMERALD -> R.color.emerald_accent
            AppTheme.LAVENDER -> R.color.lavender_accent
        }
        
        val bgRes = when (themeManager.currentTheme) {
            AppTheme.MIDNIGHT -> R.color.midnight_bg
            else -> R.color.classic_bg
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        
        binding.root.setBackgroundColor(ContextCompat.getColor(this, bgRes))
    }

    private fun setupUI() {
        val data = TaxData.content

        binding.apply {
            tvTitle.text = "قوانین جامع مالیاتی"
            tvSubtitle.text = "قانون پایانه فروشگاهی و سامانه مؤدیان"
            tvDescription.text = "نسخه کامل قانون با آخرین اصلاحات تا تاریخ ۱۴۰۴.۰۴.۰۸"
            
            tvPreparerName.text = data.preparerName
            tvPreparerOffice.text = data.preparerOffice
            tvPhone.text = data.phone
            tvVersion.text = "نسخه ۱۵ - ۱۴۰۴"

            btnStart.setOnClickListener {
                startActivity(Intent(this@CoverActivity, TOCActivity::class.java))
            }

            btnSettings.setOnClickListener {
                startActivity(Intent(this@CoverActivity, SettingsActivity::class.java))
            }
        }
    }

    private fun applyAnimations() {
        if (!themeManager.reduceMotion) {
            val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
            binding.cardContent.startAnimation(fadeIn)
        }
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }
}
