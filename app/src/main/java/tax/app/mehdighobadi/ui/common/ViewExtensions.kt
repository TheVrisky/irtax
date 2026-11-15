package tax.app.mehdighobadi.ui.common

import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import tax.app.mehdighobadi.R

/** منحنی easing طبیعی (fast-out / slow-in) مطابق جهت‌گیری طراحی */
fun naturalInterpolator(view: View): Interpolator =
    AnimationUtils.loadInterpolator(view.context, android.R.interpolator.fast_out_slow_in)

/**
 * ورود نرم fade + slide-up با تاخیر پلکانی (stagger).
 * با «کاهش انیمیشن‌ها» غیرفعال می‌شود.
 */
fun View.enterStaggered(position: Int, motionEnabled: Boolean, baseDelay: Long = 0L) {
    if (!motionEnabled) {
        alpha = 1f
        translationY = 0f
        return
    }
    val delay = (position * 55L).coerceAtMost(300L) + baseDelay
    alpha = 0f
    translationY = 36f
    animate()
        .alpha(1f)
        .translationY(0f)
        .setDuration(340L)
        .setStartDelay(delay)
        .setInterpolator(naturalInterpolator(this))
        .start()
}

/** بازخورد لمسی ملایم: فشرده‌شدن (scale 0.97) هنگام press */
fun View.addPressScale(motionEnabled: Boolean = true, pressedScale: Float = 0.97f) {
    if (!motionEnabled) return
    setOnTouchListener { v, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> v.animate().scaleX(pressedScale).scaleY(pressedScale)
                .setDuration(110L).setInterpolator(naturalInterpolator(v)).start()
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f)
                .setDuration(170L).setInterpolator(naturalInterpolator(v)).start()
        }
        false // کلیک همچنان پردازش می‌شود
    }
}

/** شمارنده‌ی متحرک برای اعداد آماری */
fun TextView.animateCounter(target: Int, motionEnabled: Boolean, formatter: (Int) -> String = { it.toFaDigits() }) {
    if (!motionEnabled) {
        text = formatter(target)
        return
    }
    val animator = android.animation.ValueAnimator.ofInt(0, target).apply {
        duration = 750L
        interpolator = naturalInterpolator(this@animateCounter)
        addUpdateListener { text = formatter(it.animatedValue as Int) }
    }
    animator.start()
}

/** تبدیل ارقام لاتین به فارسی */
fun Int.toFaDigits(): String = toString().toFaDigits()

fun String.toFaDigits(): String {
    val sb = StringBuilder(length)
    for (c in this) {
        sb.append(if (c in '0'..'9') ('۰' + (c - '0')) else c)
    }
    return sb.toString()
}

/** رنگ با تنظیم ساده */
fun View.tint(res: Int) = ContextCompat.getColor(context, res)
