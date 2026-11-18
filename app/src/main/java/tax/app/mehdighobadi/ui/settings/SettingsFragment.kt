package tax.app.mehdighobadi.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import tax.app.mehdighobadi.data.auth.AuthRepository
import tax.app.mehdighobadi.data.auth.AuthResult
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
    private val authRepository by lazy { AuthRepository(requireContext()) }

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

        binding.accountSection.enterStaggered(0, motion)
        binding.progressSection.enterStaggered(1, motion)
        binding.readingSection.enterStaggered(2, motion)

        setupAccount()
        setupDarkMode()
        setupAccent()
        setupReading()
        setupReset()
    }

    private fun setupAccount() {
        renderAccountState()
        binding.btnAccountAction.addPressScale(themeManager.reduceMotion)
        binding.btnAccountAction.setOnClickListener {
            if (authRepository.isSignedIn()) confirmLogout() else showLoginDialog()
        }
    }

    private fun renderAccountState() {
        val signedIn = runCatching { authRepository.isSignedIn() }.getOrDefault(false)
        binding.tvAccountStatus.setText(
            if (signedIn) R.string.account_signed_in
            else R.string.account_signed_out
        )
        binding.btnAccountAction.setText(
            if (signedIn) R.string.account_logout
            else R.string.account_login
        )
    }

    private fun showLoginDialog() {
        val context = requireContext()
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(8), dp(24), 0)
        }

        val emailLayout = TextInputLayout(context).apply {
            hint = getString(R.string.account_email)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val emailInput = TextInputEditText(emailLayout.context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            isSingleLine = true
        }
        emailLayout.addView(emailInput)
        content.addView(emailLayout)

        val passwordLayout = TextInputLayout(context).apply {
            hint = getString(R.string.account_password)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(12) }
        }
        val passwordInput = TextInputEditText(passwordLayout.context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            isSingleLine = true
        }
        passwordLayout.addView(passwordInput)
        content.addView(passwordLayout)

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.account_login_title)
            .setView(content)
            .setNegativeButton(R.string.back, null)
            .setPositiveButton(R.string.account_login, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val email = emailInput.text?.toString().orEmpty().trim()
                    val password = passwordInput.text?.toString().orEmpty()
                    if (email.isBlank() || password.isBlank()) {
                        emailLayout.error = if (email.isBlank()) getString(R.string.account_required) else null
                        passwordLayout.error = if (password.isBlank()) getString(R.string.account_required) else null
                        return@setOnClickListener
                    }
                    emailLayout.error = null
                    passwordLayout.error = null
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    viewLifecycleOwner.lifecycleScope.launch {
                        when (val result = authRepository.login(email, password)) {
                            is AuthResult.Success -> {
                                if (_binding != null) {
                                    renderAccountState()
                                    Toast.makeText(
                                        requireContext(),
                                        R.string.account_login_success,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                dialog.dismiss()
                            }
                            is AuthResult.Failure -> {
                                if (_binding != null) {
                                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                                }
                                if (dialog.isShowing) {
                                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                                        .isEnabled = true
                                }
                            }
                        }
                    }
                }
        }
        dialog.show()
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.account_logout_title)
            .setMessage(R.string.account_logout_confirm)
            .setNegativeButton(R.string.back, null)
            .setPositiveButton(R.string.account_logout) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    authRepository.logout()
                    if (_binding != null) {
                        renderAccountState()
                        Toast.makeText(
                            requireContext(),
                            R.string.account_logout_success,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .show()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

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
