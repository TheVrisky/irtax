package tax.app.mehdighobadi.ui.support

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment

class SupportFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { SupportChatScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupportChatScreen() {
    val isDark = isSystemInDarkTheme()
    val scheme = if (isDark) {
        darkColorScheme(
            primary = Color(0xFF9AA3FF),
            onPrimary = Color(0xFF17192B),
            primaryContainer = Color(0xFF30365F),
            onPrimaryContainer = Color(0xFFE0E3FF),
            secondary = Color(0xFF52D7C5),
            surface = Color(0xFF171D2B),
            surfaceVariant = Color(0xFF252D40),
            onSurface = Color(0xFFE9ECF5),
            onSurfaceVariant = Color(0xFFB3BBD0),
            background = Color(0xFF101521),
            onBackground = Color(0xFFE9ECF5)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF5B67F1),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE7E9FF),
            onPrimaryContainer = Color(0xFF303AAE),
            secondary = Color(0xFF119D91),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF0F2FA),
            onSurface = Color(0xFF1B1F2E),
            onSurfaceVariant = Color(0xFF5B6273),
            background = Color(0xFFF2F5FA),
            onBackground = Color(0xFF1B1F2E)
        )
    }

    MaterialTheme(colorScheme = scheme) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("پرسش و پاسخ", fontWeight = FontWeight.Bold)
                            Text(
                                "پشتیبانی خصوصی ایرتکس",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            },
            bottomBar = {
                ChatInputField(
                    enabled = false,
                    modifier = Modifier.padding(bottom = 92.dp)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item { SupportWelcomeCard() }
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔒", style = MaterialTheme.typography.titleMedium)
                            Column(Modifier.padding(start = 10.dp)) {
                                Text(
                                    "گفت‌وگوی محرمانه",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "پیام‌ها پس از راه‌اندازی حساب کاربری فقط برای صاحب حساب و پشتیبان مجاز نمایش داده می‌شوند.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportWelcomeCard() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "؟",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            "چطور می‌توانیم کمکت کنیم؟",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "سؤال خود را برای تیم پشتیبانی بفرست. پاسخ‌ها در همین صفحه و به‌صورت خصوصی در دسترس خواهند بود.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    "برای شروع، وارد حساب کاربری شو",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    "ورود و ارسال پیام پس از اتصال سرویس حساب کاربری فعال می‌شود. در حال حاضر این بخش فقط پیش‌نمایش رابط کاربری است.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChatInputField(enabled: Boolean, modifier: Modifier = Modifier) {
    var message by remember { mutableStateOf("") }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = message,
                onValueChange = { message = it },
                enabled = enabled,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        if (enabled) "پیام خود را بنویسید..." else "پس از ورود فعال می‌شود"
                    )
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            IconButton(onClick = {}, enabled = enabled) {
                Text("➤", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/** حباب پیام کاربر برای اتصال به تاریخچه واقعی گفتگو. */
@Composable
private fun UserMessageItem(message: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
        ) {
            Text(
                message,
                modifier = Modifier.padding(14.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/** حباب پاسخ پشتیبانی؛ پیام واقعی در مرحله اتصال API دریافت می‌شود. */
@Composable
private fun AIMessageItem(message: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
        ) {
            Text(
                message,
                modifier = Modifier.padding(14.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
