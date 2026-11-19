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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import tax.app.mehdighobadi.R
import tax.app.mehdighobadi.data.remote.ExamDto
import tax.app.mehdighobadi.data.remote.ExamQuestionDto
import tax.app.mehdighobadi.data.repository.ExamDetailResult
import tax.app.mehdighobadi.data.repository.ExamLoadResult
import tax.app.mehdighobadi.data.repository.ExamRepository
import tax.app.mehdighobadi.data.repository.ExamSubmitResult

private data class LocalQuizQuestion(val prompt: String, val options: List<String>, val answer: Int, val explanation: String)

class ExamsFragment : Fragment() {
    private lateinit var root: LinearLayout
    private val repository by lazy { ExamRepository() }
    private var publishedExams: List<ExamDto> = emptyList()
    private var activeExam: ExamDto? = null
    private var questionIndex = 0
    private val serverAnswers = mutableMapOf<String, String>()
    private var localIndex = 0
    private var localSelected = -1
    private val localAnswers = mutableMapOf<Int, Int>()
    private val localQuestions = listOf(
        LocalQuizQuestion("هدف اصلی سامانه مؤدیان چیست؟", listOf("ثبت و مدیریت صورت‌حساب‌های الکترونیکی", "صدور گذرنامه", "ثبت احوال", "محاسبه حقوق کارکنان"), 0, "سامانه مؤدیان بستر ثبت‌نام مؤدیان و صدور و مدیریت صورت‌حساب‌های الکترونیکی است."),
        LocalQuizQuestion("اصل بر صحت کدام اطلاعات است؟", listOf("اطلاعات شفاهی", "اطلاعات ثبت‌شده مؤدی در سامانه", "اطلاعات شبکه‌های اجتماعی", "اطلاعات ثبت‌نشده"), 1, "طبق ماده ۴، اصل بر صحت اطلاعات ثبت‌شده مؤدی در سامانه است، مگر خلاف آن اثبات شود."),
        LocalQuizQuestion("صورت‌حساب فروشنده برای خریدار عضو سامانه چگونه ارسال می‌شود؟", listOf("با پست", "با مراجعه حضوری", "به‌صورت خودکار به کارپوشه", "فقط با پیامک"), 2, "طبق ماده ۵، صورت‌حساب به‌صورت خودکار به کارپوشه خریدار عضو سامانه منتقل می‌شود."),
        LocalQuizQuestion("در صورت نقص فنی مانع صدور صورت‌حساب، مؤدی چه زمانی باید موضوع را اعلام کند؟", listOf("تا پایان روز اداری بعد", "یک ماه بعد", "پایان سال", "نیازی به اعلام نیست"), 0, "ماده ۱۲ اعلام موضوع از طریق کارپوشه تا پایان روز اداری بعد را مقرر کرده است."),
        LocalQuizQuestion("حساب بانکی یا کارت‌خوان جدید شغلی چه زمانی باید اعلام شود؟", listOf("پایان سال", "پس از اخطار", "بلافاصله از طریق کارپوشه", "فقط هنگام اظهارنامه"), 2, "مطابق ماده ۱۰، اطلاعات ابزار پرداخت جدید باید بلافاصله از طریق کارپوشه اعلام شود.")
    )

    override fun onCreateView(inflater: android.view.LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val scroll = ScrollView(requireContext())
        root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(20), dp(24), dp(20), dp(120))
        }
        scroll.addView(root)
        renderHome("در حال دریافت آزمون‌های منتشرشده…")
        loadPublishedExams()
        return scroll
    }

    private fun loadPublishedExams() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = repository.loadPublishedExams()) {
                is ExamLoadResult.Success -> {
                    publishedExams = result.exams
                    renderHome(if (publishedExams.isEmpty()) "در حال حاضر آزمون منتشرشده‌ای وجود ندارد." else null)
                }
                is ExamLoadResult.Failure -> renderHome(result.message)
            }
        }
    }

    private fun renderHome(message: String? = null) {
        root.removeAllViews()
        addText("مرکز آزمون‌ها", 25, true)
        addText("آزمون‌های آنلاین از سرور دریافت و در سرور تصحیح می‌شوند.", 13)
        if (!message.isNullOrBlank()) {
            val status = card()
            status.addView(text(message, 13))
            root.addView(status)
        }
        if (publishedExams.isNotEmpty()) {
            addText("آزمون‌های منتشرشده", 18, true)
            publishedExams.forEach { exam ->
                val item = card()
                item.addView(text(exam.title, 17, true))
                item.addView(text(exam.description ?: "آزمون آنلاین", 13))
                item.addView(text("تعداد سؤال: ${exam.questionCount ?: "نامشخص"}  •  زمان: ${exam.durationMinutes?.let { "$it دقیقه" } ?: "بدون محدودیت اعلام‌شده"}", 12))
                val start = button("دریافت سؤال‌ها و شروع")
                start.setOnClickListener { loadServerExam(exam) }
                item.addView(start)
                root.addView(item)
            }
        }
        addText("آزمون آفلاین", 18, true)
        val offline = card()
        offline.addView(text("آزمون آشنایی با قانون پایانه‌های فروشگاهی", 17, true))
        offline.addView(text("۵ پرسش چهارگزینه‌ای • حدود ۵ دقیقه • بدون نیاز به اینترنت", 12))
        offline.addView(text("این آزمون محلی برای زمان قطعی اینترنت در دسترس می‌ماند.", 12))
        val local = button("شروع آزمون آفلاین")
        local.setOnClickListener { startLocalQuiz() }
        offline.addView(local)
        root.addView(offline)
    }

    private fun loadServerExam(exam: ExamDto) {
        root.removeAllViews()
        addText("دریافت آزمون", 22, true)
        addText("در حال دریافت سؤال‌ها از سرور…", 13)
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = repository.loadExam(exam.id)) {
                is ExamDetailResult.Success -> {
                    activeExam = result.exam
                    questionIndex = 0
                    serverAnswers.clear()
                    renderServerQuestion()
                }
                is ExamDetailResult.Failure -> renderHome(result.message)
            }
        }
    }

    private fun renderServerQuestion() {
        val exam = activeExam ?: return
        val q = exam.questions.getOrNull(questionIndex) ?: return
        root.removeAllViews()
        addText(exam.title, 21, true)
        addText("پرسش ${questionIndex + 1} از ${exam.questions.size}", 13)
        val panel = card()
        panel.addView(text(q.text, 17, true))
        q.options.forEach { option ->
            val selected = serverAnswers[q.id] == option.id
            val choice = button((if (selected) "●  " else "○  ") + option.text)
            choice.setOnClickListener { serverAnswers[q.id] = option.id; renderServerQuestion() }
            panel.addView(choice)
        }
        root.addView(panel)
        val next = button(if (questionIndex == exam.questions.lastIndex) "ارسال پاسخ‌ها و دریافت نتیجه" else "پرسش بعدی")
        next.isEnabled = serverAnswers.containsKey(q.id)
        next.setOnClickListener {
            if (questionIndex < exam.questions.lastIndex) {
                questionIndex++
                renderServerQuestion()
            } else submitServerExam(exam)
        }
        root.addView(next)
        if (questionIndex > 0) {
            val previous = button("پرسش قبلی")
            previous.setOnClickListener { questionIndex--; renderServerQuestion() }
            root.addView(previous)
        }
        val cancel = button("انصراف و بازگشت")
        cancel.setOnClickListener { activeExam = null; renderHome() }
        root.addView(cancel)
    }

    private fun submitServerExam(exam: ExamDto) {
        root.removeAllViews()
        addText("در حال ثبت پاسخ‌ها…", 20, true)
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = repository.submit(exam.id, serverAnswers.toMap())) {
                is ExamSubmitResult.Success -> {
                    val data = result.result
                    val total = data.totalQuestions ?: exam.questions.size
                    val correct = data.correctCount
                    val panel = card()
                    panel.addView(text("نتیجه آزمون", 21, true))
                    if (correct != null) panel.addView(text("$correct از $total پاسخ صحیح", 18, true))
                    data.score?.let { panel.addView(text("امتیاز: ${formatScore(it)}", 16)) }
                    panel.addView(text(data.message ?: "نتیجه توسط سرور ثبت شد.", 13))
                    root.addView(panel)
                    val home = button("بازگشت به مرکز آزمون")
                    home.setOnClickListener { activeExam = null; loadPublishedExams() }
                    root.addView(home)
                }
                is ExamSubmitResult.Failure -> {
                    addText(result.message, 14)
                    val retry = button("تلاش دوباره برای ارسال")
                    retry.setOnClickListener { submitServerExam(exam) }
                    root.addView(retry)
                    val back = button("بازگشت به آزمون")
                    back.setOnClickListener { renderServerQuestion() }
                    root.addView(back)
                }
            }
        }
    }

    private fun formatScore(score: Double): String =
        if (score % 1.0 == 0.0) score.toInt().toString() else String.format(java.util.Locale.US, "%.1f", score)

    private fun startLocalQuiz() { localIndex = 0; localSelected = -1; localAnswers.clear(); renderLocalQuestion() }

    private fun renderLocalQuestion() {
        root.removeAllViews()
        val q = localQuestions[localIndex]
        addText("آزمون آفلاین", 22, true)
        addText("پرسش ${localIndex + 1} از ${localQuestions.size}", 13)
        val panel = card()
        panel.addView(text(q.prompt, 17, true))
        q.options.forEachIndexed { i, option ->
            val choice = button((if (localSelected == i) "●  " else "○  ") + option)
            choice.setOnClickListener { localSelected = i; renderLocalQuestion() }
            panel.addView(choice)
        }
        root.addView(panel)
        val next = button(if (localIndex == localQuestions.lastIndex) "ثبت آزمون و مشاهده نتیجه" else "پرسش بعدی")
        next.isEnabled = localSelected >= 0
        next.setOnClickListener {
            localAnswers[localIndex] = localSelected
            if (localIndex < localQuestions.lastIndex) {
                localIndex++
                localSelected = localAnswers[localIndex] ?: -1
                renderLocalQuestion()
            } else renderLocalResult()
        }
        root.addView(next)
        val back = button("بازگشت به مرکز آزمون")
        back.setOnClickListener { renderHome() }
        root.addView(back)
    }

    private fun renderLocalResult() {
        val score = localQuestions.indices.count { localAnswers[it] == localQuestions[it].answer }
        val p = requireContext().getSharedPreferences("irtax_exam_results", 0)
        p.edit().putInt("attempts", p.getInt("attempts", 0) + 1).putInt("last", score)
            .putInt("best", maxOf(score, p.getInt("best", -1))).apply()
        root.removeAllViews()
        addText("نتیجه آزمون آفلاین", 24, true)
        val summary = card()
        summary.addView(text("$score از ۵ پاسخ صحیح", 22, true))
        summary.addView(text("درصد پاسخ صحیح: ${score * 20}٪", 15))
        root.addView(summary)
        localQuestions.forEachIndexed { i, q ->
            val review = card()
            review.addView(text("${i + 1}. ${q.prompt}", 14, true))
            review.addView(text("پاسخ شما: ${q.options.getOrNull(localAnswers[i] ?: -1) ?: "بدون پاسخ"}", 12))
            review.addView(text("پاسخ صحیح: ${q.options[q.answer]}", 12, true))
            review.addView(text(q.explanation, 12))
            root.addView(review)
        }
        val again = button("شرکت دوباره")
        again.setOnClickListener { startLocalQuiz() }
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
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
