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
import tax.app.mehdighobadi.ui.exams.ExamsFragment
import tax.app.mehdighobadi.ui.favorites.FavoritesFragment
import tax.app.mehdighobadi.ui.home.HomeFragment
import tax.app.mehdighobadi.ui.settings.SettingsFragment
import tax.app.mehdighobadi.ui.support.SupportFragment

/** ناوبری اصلی نسخه ۲.۰.۲ */
class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: android.content.Context) {\n        super.attachBaseContext(ThemeManager.wrapContext(newBase))\n    }\n\n    private lateinit var binding: ActivityMainBinding
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
            binding.bottomNav.selectedItemId = R.id.nav_home
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { switchTo(HomeFragment()); true }
                R.id.nav_exams -> { switchTo(ExamsFragment()); true }
                R.id.nav_articles -> { switchTo(BankFragment()); true }
                R.id.nav_support -> { switchTo(SupportFragment()); true }
                R.id.nav_settings -> { switchTo(SettingsFragment()); true }
                else -> false
            }
        }
    }

    fun selectTab(itemId: Int) {
        binding.bottomNav.selectedItemId = itemId
    }

    fun showFavorites() {
        switchTo(FavoritesFragment())
    }

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
