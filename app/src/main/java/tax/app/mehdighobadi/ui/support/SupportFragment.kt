package tax.app.mehdighobadi.ui.support

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts as ComposeContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

class SupportFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { SupportChatScreen() }
        }
}

@Composable
private fun SupportChatScreen() {
    val scheme = if (isSystemInDarkTheme()) IrtaxDarkColors else IrtaxLightColors
    MaterialTheme(colorScheme = scheme) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = { ChatComposer() }
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(76.dp).clip(RoundedCornerShape(25.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("…", color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("در دست توسعه", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}

@Composable
private fun ChatComposer() {
    var message by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ComposeContracts.GetContent()) { uri ->
        selectedImage = uri
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
            selectedImage?.let { uri ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AndroidView(
                        factory = { ImageView(it).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
                        update = { it.setImageURI(uri) },
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
                    )
                    Text("تصویر انتخاب شد", modifier = Modifier.padding(start = 10.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.weight(1f))
                    Text("حذف", color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp))
                }
                Spacer(Modifier.height(8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { imagePicker.launch("image/*") }) {
                    Text("＋", color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge)
                }
                TextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("پیام بنویسید…") },
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                IconButton(onClick = {}, enabled = false) {
                    Text("➤", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private val IrtaxLightColors = lightColorScheme(
    primary = Color(0xFF5968D8), onPrimary = Color.White,
    primaryContainer = Color(0xFFE4E8FF), onPrimaryContainer = Color(0xFF202B72),
    secondary = Color(0xFF168F89), background = Color(0xFFF5F6FC),
    surface = Color(0xFFFFFFFF), surfaceVariant = Color(0xFFEAECF5),
    onSurface = Color(0xFF1C2340), onSurfaceVariant = Color(0xFF68708A)
)
private val IrtaxDarkColors = darkColorScheme(
    primary = Color(0xFFA5AEFF), onPrimary = Color(0xFF172052),
    primaryContainer = Color(0xFF303B75), onPrimaryContainer = Color(0xFFE0E4FF),
    secondary = Color(0xFF76D5C9), background = Color(0xFF0D1427),
    surface = Color(0xFF151F35), surfaceVariant = Color(0xFF222D47),
    onSurface = Color(0xFFE8ECFA), onSurfaceVariant = Color(0xFFA8B2CC)
)
