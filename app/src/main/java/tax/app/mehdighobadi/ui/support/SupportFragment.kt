package tax.app.mehdighobadi.ui.support

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
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
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val scheme = if (isDark) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = scheme) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("پرسش و پاسخ", fontWeight = FontWeight.SemiBold) }
                )
            },
            bottomBar = { ChatInputField(enabled = false) }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { SupportWelcomeCard() }
            }
        }
    }
}

@Composable
private fun SupportWelcomeCard() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(58.dp).clip(CircleShape).background(Color(0xFFE0F2F1)),
            contentAlignment = Alignment.Center
        ) {
            Text("پ", color = Color(0xFF087F75), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        Text("ارتباط مستقیم با پشتیبانی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "پرسش‌های خود را اینجا ثبت کن و پاسخ کارشناسان را در همین گفت‌وگوی خصوصی دریافت کن.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(18.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("برای شروع، وارد حساب کاربری شو", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "پس از راه‌اندازی سرویس حساب کاربری، تاریخچه پیام‌ها و پاسخ‌ها فقط برای صاحب حساب و تیم مجاز پشتیبانی قابل مشاهده خواهد بود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChatInputField(enabled: Boolean) {
    var message by remember { mutableStateOf("") }
    Surface(shadowElevation = 6.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = message,
                onValueChange = { message = it },
                enabled = enabled,
                modifier = Modifier.weight(1f),
                placeholder = { Text("پیام خود را بنویسید...") },
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            IconButton(onClick = {}, enabled = false) {
                Text("➤", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** حباب پیام کاربر؛ برای اتصال به تاریخچه واقعی گفتگو در مرحله API آماده است. */
@Composable
private fun UserMessageItem(message: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(color = Color(0xFFEDE7F6), shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)) {
            Text(message, modifier = Modifier.padding(14.dp), color = Color(0xFF35245B))
        }
    }
}

/** حباب پاسخ پشتیبانی؛ پیام‌های واقعی در مرحله اتصال API از سرور دریافت می‌شوند. */
@Composable
private fun AIMessageItem(message: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)) {
            Text(message, modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
