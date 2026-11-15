package tax.app.mehdighobadi.ui.main

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import tax.app.mehdighobadi.R
import tax.app.mehdighobadi.databinding.ActivityMainBinding
import tax.app.mehdighobadi.theme.ThemeManager
import tax.app.mehdighobadi.ui.bank.BankFragment
import tax.app.mehdighobadi.ui.favorites.FavoritesFragment
import tax.app.mehdighobadi.ui.home.HomeFragment
import tax.app.mehdighobadi.ui.settings.SettingsFragment
import tax.app.mehdighobadi.ui.stats.StatsFragment

/**
 * میزبان اصلی اپ: پنج تب (خانه، بانک سوالات، نشان‌شده‌ها، آمار، تنظیمات)
 * روی یک نوار پایین شناور و شیشه‌ای.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        themeManager = ThemeManager(this)
        themeManager.applyNightMode()
        themeManager.applyAccent(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyStatusBarContrast()

        if (savedInstanceState == null) {
            switchTo(HomeFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { switchTo(HomeFragment()); true }
                R.id.nav_bank -> { switchTo(BankFragment()); true }
                R.id.nav_favorites -> { switchTo(FavoritesFragment()); true }
                R.id.nav_stats -> { switchTo(StatsFragment()); true }
                R.id.nav_settings -> { switchTo(SettingsFragment()); true }
                else -> false
            }
        }
    }

    fun selectTab(itemId: Int) {
        binding.bottomNav.selectedItemId = itemId
    }

    /** آیکون‌های نوار وضعیت با روشن/تاریکی هماهنگ شوند */
    private fun applyStatusBarContrast() {
        val isNight = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = !isNight
    }

    private fun switchTo(fragment: Fragment) {
        val motion = themeManager.reduceMotion
        supportFragmentManager.beginTransaction().apply {
            if (!motion) setCustomAnimations(R.anim.anim_slide_up_in, R.anim.anim_fade_out)
            replace(R.id.fragment_container, fragment)
        }.commit()
    }
}
