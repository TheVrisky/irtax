package tax.app.mehdighobadi.ui.support

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import tax.app.mehdighobadi.R
import tax.app.mehdighobadi.data.auth.AuthRepository
import tax.app.mehdighobadi.data.remote.ConversationDto
import tax.app.mehdighobadi.data.remote.MessageDto
import tax.app.mehdighobadi.data.repository.SupportRepository
import tax.app.mehdighobadi.data.repository.SupportResult
import tax.app.mehdighobadi.ui.main.MainActivity

class SupportFragment : Fragment() {
    private lateinit var root: LinearLayout
    private val repository by lazy { SupportRepository() }
    private val auth by lazy { AuthRepository(requireContext()) }
    private var activeConversation: ConversationDto? = null
    private var loading = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        val scroll = ScrollView(requireContext()).apply { isFillViewport = true; clipToPadding = false }
        root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(20), dp(24), dp(20), dp(120))
        }
        scroll.addView(root)
        renderHome()
        return scroll
    }

    override fun onResume() {
        super.onResume()
        if (view != null) {
            if (!auth.isSignedIn()) renderSignedOut()
            else if (activeConversation == null) loadConversations()
        }
    }

    private fun renderSignedOut() {
        root.removeAllViews()
        addText("پرسش و پاسخ", 25, true)
        val panel = card()
        panel.addView(text("گفت‌وگوی خصوصی با پشتیبانی", 18, true))
        panel.addView(text("برای ثبت پرسش و مشاهده پاسخ‌ها، ابتدا وارد حساب کاربری شو.", 13))
        val login = button("رفتن به بخش ورود")
        login.setOnClickListener { (activity as? MainActivity)?.selectTab(R.id.nav_settings) }
        panel.addView(login)
        root.addView(panel)
    }

    private fun renderHome() {
        if (!isAdded || !::root.isInitialized) return
        if (!auth.isSignedIn()) { renderSignedOut(); return }
        root.removeAllViews()
        addText("پشتیبانی و گفت‌وگو", 24, true)
        addText("پیام‌های خصوصی خود را مدیریت کن.", 13)
        addHomeActions()
        addText("گفت‌وگوهای من", 17, true)
    }

    private fun addHomeActions() {
        val row = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
        val create = button("گفت‌وگوی جدید")
        create.layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginEnd = dp(6) }
        create.setOnClickListener { showCreateDialog() }
        val refresh = button("بروزرسانی")
        refresh.layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginStart = dp(6) }
        refresh.setOnClickListener { loadConversations() }
        row.addView(create); row.addView(refresh); root.addView(row)
    }

    private fun loadConversations() {
        if (loading || !isAdded || !::root.isInitialized) return
        loading = true
        if (root.childCount == 0) renderHome()
        lifecycleScope.launch {
            when (val result = repository.conversations()) {
                is SupportResult.Success -> {
                    renderHome()
                    if (result.value.isEmpty()) {
                        val empty = card()
                        empty.addView(text("هنوز گفت‌وگویی ثبت نکرده‌ای. برای شروع، گفت‌وگوی جدید بساز.", 14))
                        root.addView(empty)
                    } else result.value.forEach { conversation ->
                        val item = card()
                        item.addView(text(conversation.subject?.takeIf { it.isNotBlank() } ?: "گفت‌وگوی بدون عنوان", 15, true))
                        item.addView(text("وضعیت: " + (conversation.status ?: "نامشخص"), 12))
                        val open = button("مشاهده پیام‌ها")
                        open.setOnClickListener { openConversation(conversation) }
                        item.addView(open); root.addView(item)
                    }
                }
                is SupportResult.Failure -> {
                    renderHome()
                    addText(result.message, 13)
                    val retry = button("تلاش دوباره")
                    retry.setOnClickListener { loadConversations() }
                    root.addView(retry)
                }
            }
            loading = false
        }
    }

    private fun openConversation(conversation: ConversationDto) {
        activeConversation = conversation
        root.removeAllViews()
        addText(conversation.subject ?: "گفت‌وگو با پشتیبانی", 21, true)
        val back = button("بازگشت به فهرست")
        back.setOnClickListener { activeConversation = null; renderHome(); loadConversations() }
        root.addView(back)
        addText("در حال دریافت پیام‌ها…", 12)
        lifecycleScope.launch {
            when (val result = repository.messages(conversation.id)) {
                is SupportResult.Success -> renderMessages(conversation, result.value)
                is SupportResult.Failure -> {
                    if (activeConversation?.id != conversation.id) return@launch
                    root.removeAllViews()
                    addText(conversation.subject ?: "گفت‌وگو", 21, true)
                    addText(result.message, 13)
                    val retry = button("تلاش دوباره")
                    retry.setOnClickListener { openConversation(conversation) }
                    root.addView(retry)
                }
            }
        }
    }

    private fun renderMessages(conversation: ConversationDto, messages: List<MessageDto>) {
        if (activeConversation?.id != conversation.id) return
        root.removeAllViews()
        addText(conversation.subject ?: "گفت‌وگو با پشتیبانی", 21, true)
        val back = button("بازگشت به فهرست")
        back.setOnClickListener { activeConversation = null; renderHome(); loadConversations() }
        root.addView(back)
        if (messages.isEmpty()) addText("هنوز پیامی در این گفت‌وگو وجود ندارد.", 13)
        messages.forEach { message ->
            val bubble = card()
            bubble.addView(text(message.body, 14))
            bubble.addView(text((message.senderType ?: "کاربر") + " • " + (message.createdAt ?: ""), 10))
            root.addView(bubble)
        }
        val composer = TextInputLayout(requireContext()).apply {
            hint = "پیام خود را بنویسید"
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) }
        }
        val input = TextInputEditText(composer.context).apply {
            minLines = 2; maxLines = 4; gravity = Gravity.TOP or Gravity.RIGHT
        }
        composer.addView(input); root.addView(composer)
        val send = button("ارسال پیام")
        send.setOnClickListener {
            val body = input.text?.toString()?.trim().orEmpty()
            if (body.isBlank()) { composer.error = "متن پیام را وارد کن."; return@setOnClickListener }
            composer.error = null; send.isEnabled = false
            lifecycleScope.launch {
                when (val result = repository.send(conversation.id, body)) {
                    is SupportResult.Success -> openConversation(conversation)
                    is SupportResult.Failure -> {
                        send.isEnabled = true
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        root.addView(send)
    }

    private fun showCreateDialog() {
        val context = requireContext()
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(8), dp(24), 0)
        }
        val subjectLayout = TextInputLayout(context).apply {
            hint = "موضوع گفت‌وگو"; boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        }
        val subject = TextInputEditText(subjectLayout.context).apply { isSingleLine = true }
        subjectLayout.addView(subject); content.addView(subjectLayout)
        val bodyLayout = TextInputLayout(context).apply {
            hint = "شرح پرسش یا مشکل"; boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) }
        }
        val body = TextInputEditText(bodyLayout.context).apply {
            minLines = 3; maxLines = 5; gravity = Gravity.TOP or Gravity.RIGHT
        }
        bodyLayout.addView(body); content.addView(bodyLayout)
        val dialog = MaterialAlertDialogBuilder(context).setTitle("ایجاد گفت‌وگوی جدید")
            .setView(content).setNegativeButton("انصراف", null).setPositiveButton("ثبت", null).create()
        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = subject.text?.toString()?.trim().orEmpty()
                val message = body.text?.toString()?.trim().orEmpty()
                subjectLayout.error = if (title.isBlank()) "موضوع را وارد کن." else null
                bodyLayout.error = if (message.isBlank()) "شرح پرسش را وارد کن." else null
                if (title.isBlank() || message.isBlank()) return@setOnClickListener
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).isEnabled = false
                lifecycleScope.launch {
                    when (val result = repository.create(title, message)) {
                        is SupportResult.Success -> { dialog.dismiss(); openConversation(result.value) }
                        is SupportResult.Failure -> {
                            Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                            if (dialog.isShowing) dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        }
                    }
                }
            }
        }
        dialog.show()
    }

    private fun card() = LinearLayout(requireContext()).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(14), dp(16), dp(14))
        background = android.graphics.drawable.GradientDrawable().apply {
            setColor(ContextCompat.getColor(requireContext(), R.color.surface_glass_strong))
            cornerRadius = dp(20).toFloat()
            setStroke(dp(1), ContextCompat.getColor(requireContext(), R.color.outline))
        }
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8); bottomMargin = dp(8) }
    }

    private fun text(value: String, size: Int, bold: Boolean = false) = TextView(requireContext()).apply {
        text = value; textSize = size.toFloat(); gravity = Gravity.RIGHT
        textDirection = View.TEXT_DIRECTION_RTL
        setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setPadding(0, dp(4), 0, dp(4))
    }
    private fun addText(value: String, size: Int, bold: Boolean = false) { root.addView(text(value, size, bold)) }
    private fun button(value: String) = MaterialButton(requireContext()).apply {
        text = value; isAllCaps = false; textSize = 13f
        layoutParams = LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6); bottomMargin = dp(6) }
    }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
