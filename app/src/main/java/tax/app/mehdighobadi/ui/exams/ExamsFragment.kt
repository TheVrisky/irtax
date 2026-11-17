package tax.app.mehdighobadi.ui.exams

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import tax.app.mehdighobadi.R

private data class QuizQuestion(val prompt: String, val options: List<String>, val answer: Int, val explanation: String)

class ExamsFragment : Fragment() {
    private lateinit var root: LinearLayout
    private var index = 0
    private var selected = -1
    private val answers = mutableMapOf<Int, Int>()
    private val questions = listOf(
        QuizQuestion("هدف اصلی سامانه مؤدیان چیست؟", listOf("ثبت و مدیریت صورت‌حساب‌های الکترونیکی", "صدور گذرنامه", "ثبت احوال", "محاسبه حقوق کارکنان"), 0, "سامانه مؤدیان بستر ثبت‌نام مؤدیان و صدور و مدیریت صورت‌حساب‌های الکترونیکی است."),
        QuizQuestion("اصل بر صحت کدام اطلاعات است؟", listOf("اطلاعات شفاهی", "اطلاعات ثبت‌شده مؤدی در سامانه", "اطلاعات شبکه‌های اجتماعی", "اطلاعات ثبت‌نشده"), 1, "طبق ماده ۴، اصل بر صحت اطلاعات ثبت‌شده مؤدی در سامانه است، مگر خلاف آن اثبات شود."),
        QuizQuestion("صورت‌حساب فروشنده برای خریدار عضو سامانه چگونه ارسال می‌شود؟", listOf("با پست", "با مراجعه حضوری", "به‌صورت خودکار به کارپوشه", "فقط با پیامک"), 2, "طبق ماده ۵، صورت‌حساب به‌صورت خودکار به کارپوشه خریدار عضو سامانه منتقل می‌شود."),
        QuizQuestion("در صورت نقص فنی مانع صدور صورت‌حساب، مؤدی چه زمانی باید موضوع را اعلام کند؟", listOf("تا پایان روز اداری بعد", "یک ماه بعد", "پایان سال", "نیازی به اعلام نیست"), 0, "ماده ۱۲ اعلام موضوع از طریق کارپوشه تا پایان روز اداری بعد را مقرر کرده است."),
        QuizQuestion("حساب بانکی یا کارت‌خوان جدید شغلی چه زمانی باید اعلام شود؟", listOf("پایان سال", "پس از اخطار", "بلافاصله از طریق کارپوشه", "فقط هنگام اظهارنامه"), 2, "مطابق ماده ۱۰، اطلاعات ابزار پرداخت جدید باید بلافاصله از طریق کارپوشه اعلام شود.")
    )

    override fun onCreateView(inflater: android.view.LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val scroll = ScrollView(requireContext())
        root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(20), dp(24), dp(20), dp(120))
        }
        scroll.addView(root)
        renderHome()
        return scroll
    }

    private fun renderHome() {
        root.removeAllViews()
        addText("مرکز آزمون‌ها", 25, true)
        addText("دانسته‌هایت را بسنج و نتیجه را در همین دستگاه ذخیره کن.", 12)
        val stats = card()
        stats.addView(text("آزمون‌های انجام‌شده: " + prefs().getInt("attempts", 0), 14))
        val best = prefs().getInt("best", -1)
        stats.addView(text(if (best < 0) "هنوز آزمونی ثبت نکرده‌ای" else "بهترین نتیجه: " + best + " از ۵", 14))
        root.addView(stats)
        val exam = card()
        exam.addView(text("آزمون آشنایی با قانون پایانه‌های فروشگاهی", 17, true))
        exam.addView(text("۵ پرسش چهارگزینه‌ای • حدود ۵ دقیقه • سطح مقدماتی", 12))
        val start = button("شروع آزمون")
        start.setOnClickListener { startQuiz() }
        exam.addView(start)
        root.addView(exam)
        val last = prefs().getInt("last", -1)
        if (last >= 0) addText("آخرین نتیجه: " + last + " از ۵", 14, true)
    }

    private fun startQuiz() { index = 0; selected = -1; answers.clear(); renderQuestion() }

    private fun renderQuestion() {
        root.removeAllViews()
        val q = questions[index]
        addText("آزمون آشنایی با قانون", 22, true)
        addText("پرسش " + (index + 1) + " از " + questions.size, 13)
        val card = card()
        card.addView(text(q.prompt, 17, true))
        q.options.forEachIndexed { i, option ->
            val b = button((if (selected == i) "●  " else "○  ") + option)
            b.setOnClickListener { selected = i; renderQuestion() }
            card.addView(b)
        }
        root.addView(card)
        val next = button(if (index == questions.lastIndex) "ثبت آزمون و مشاهده نتیجه" else "پرسش بعدی")
        next.isEnabled = selected >= 0
        next.setOnClickListener {
            answers[index] = selected
            if (index < questions.lastIndex) { index++; selected = answers[index] ?: -1; renderQuestion() }
            else {
                val score = questions.indices.count { answers[it] == questions[it].answer }
                val p = prefs()
                p.edit().putInt("attempts", p.getInt("attempts", 0) + 1).putInt("last", score)
                    .putInt("best", maxOf(score, p.getInt("best", -1))).apply()
                renderResult(score)
            }
        }
        root.addView(next)
        val back = button("بازگشت به فهرست")
        back.setOnClickListener { renderHome() }
        root.addView(back)
    }

    private fun renderResult(score: Int) {
        root.removeAllViews()
        addText("نتیجه آزمون", 24, true)
        val summary = card()
        summary.addView(text(score.toString() + " از ۵ پاسخ صحیح", 22, true))
        summary.addView(text("درصد پاسخ صحیح: " + (score * 20) + "٪", 15))
        summary.addView(text(if (score == 5) "عالی! همه پاسخ‌ها درست بود." else if (score >= 3) "خوب است؛ پاسخ‌های نادرست را مرور کن." else "مطالب را دوباره مرور کن و دوباره امتحان بده.", 13))
        root.addView(summary)
        questions.forEachIndexed { i, q ->
            val review = card()
            review.addView(text((i + 1).toString() + ". " + q.prompt, 14, true))
            review.addView(text("پاسخ شما: " + (q.options.getOrNull(answers[i] ?: -1) ?: "بدون پاسخ"), 12))
            review.addView(text("پاسخ صحیح: " + q.options[q.answer], 12, true))
            review.addView(text(q.explanation, 12))
            root.addView(review)
        }
        val again = button("شرکت دوباره")
        again.setOnClickListener { startQuiz() }
        root.addView(again)
        val home = button("بازگشت به مرکز آزمون")
        home.setOnClickListener { renderHome() }
        root.addView(home)
    }

    private fun card() = LinearLayout(requireContext()).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(15), dp(16), dp(15))
        background = GradientDrawable().apply {
            setColor(ContextCompat.getColor(requireContext(), R.color.surface_glass_strong))
            cornerRadius = dp(22).toFloat()
            setStroke(dp(1), ContextCompat.getColor(requireContext(), R.color.outline))
        }
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14); topMargin = dp(8) }
    }
    private fun text(value: String, size: Int, bold: Boolean = false) = TextView(requireContext()).apply {
        text = value; textSize = size.toFloat(); gravity = Gravity.RIGHT
        textDirection = View.TEXT_DIRECTION_RTL
        setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setPadding(0, dp(5), 0, dp(5))
    }
    private fun addText(value: String, size: Int, bold: Boolean = false) { root.addView(text(value, size, bold)) }
    private fun button(value: String) = MaterialButton(requireContext()).apply {
        text = value; isAllCaps = false; textSize = 13f
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6) }
    }
    private fun prefs() = requireContext().getSharedPreferences("irtax_exam_results", 0)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
