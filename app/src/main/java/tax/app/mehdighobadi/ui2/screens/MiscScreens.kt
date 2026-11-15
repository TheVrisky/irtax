package tax.app.mehdighobadi.ui2.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.MotionPhotosOff
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tax.app.mehdighobadi.data.TaxData
import tax.app.mehdighobadi.ui2.components.AnimatedCount
import tax.app.mehdighobadi.ui2.components.GlassCard
import tax.app.mehdighobadi.ui2.data.AppState
import tax.app.mehdighobadi.ui2.theme.LocalMagPalette

@Composable
fun FavoritesScreen(onOpen: (String) -> Unit) {
    val p = LocalMagPalette.current
    val items = TaxData.content.chapters.flatMap { ch ->
        ch.articles.mapIndexed { i, a -> Triple("${ch.id}#$i", ch.id, a.number) }
    }.filter { AppState.favorites[it.first] == true }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text("نشان‌شده‌ها", style = MaterialTheme.typography.headlineSmall, color = p.ink, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 12.dp))
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Bookmark, contentDescription = null, tint = p.inkSoft, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("هنوز موردی نشان نکرده‌اید", color = p.inkSoft, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 120.dp)) {
                items(items) { (_, chId, num) ->
                    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(p.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Bookmark, contentDescription = null, tint = p.primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(num, style = MaterialTheme.typography.titleMedium, color = p.ink)
                                Text("نشان‌شده", style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
                            }
                            Box(Modifier.clip(RoundedCornerShape(10.dp)).background(p.surfaceTint).clickable { onOpen(chId) }.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text("باز کردن", color = p.primary, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsScreen() {
    val p = LocalMagPalette.current
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text("آمار و پیشرفت", style = MaterialTheme.typography.headlineSmall, color = p.ink, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BigStat(Modifier.weight(1f), AppState.progressPct(), "درصد پیشرفت", p.primary)
            BigStat(Modifier.weight(1f), AppState.readCount(), "خوانده‌شده", p.secondary)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BigStat(Modifier.weight(1f), AppState.totalArticles(), "کل مقالات", p.primary)
            BigStat(Modifier.weight(1f), AppState.studyMinutes.value, "دقیقه مطالعه", p.secondary)
        }
    }
}

@Composable
private fun BigStat(modifier: Modifier, value: Int, label: String, color: androidx.compose.ui.graphics.Color) {
    val p = LocalMagPalette.current
    GlassCard(modifier = modifier.height(130.dp), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(color))
            Text(AnimatedCount(value).toString(), style = MaterialTheme.typography.displaySmall, color = p.ink, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
        }
    }
}

@Composable
fun SettingsScreen(onToggleDark: (Boolean) -> Unit, onToggleMotion: (Boolean) -> Unit) {
    val p = LocalMagPalette.current
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text("تنظیمات", style = MaterialTheme.typography.headlineSmall, color = p.ink, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 16.dp))
        GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(6.dp)) {
                SettingsRow(
                    icon = if (AppState.darkMode.value) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                    title = "حالت تاریک",
                    trailing = {
                        Switch(
                            checked = AppState.darkMode.value,
                            onCheckedChange = onToggleDark,
                            colors = SwitchDefaults.colors(checkedTrackColor = p.primary)
                        )
                    }
                )
                SettingsRow(
                    icon = Icons.Outlined.MotionPhotosOff,
                    title = "کاهش انیمیشن",
                    trailing = {
                        Switch(
                            checked = AppState.reduceMotion.value,
                            onCheckedChange = onToggleMotion,
                            colors = SwitchDefaults.colors(checkedTrackColor = p.primary)
                        )
                    }
                )
                SettingsRow(
                    icon = Icons.Outlined.Speed,
                    title = "نسخه اپلیکیشن",
                    trailing = { Text("2.0.0", style = MaterialTheme.typography.bodyMedium, color = p.inkSoft) }
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, trailing: @Composable () -> Unit) {
    val p = LocalMagPalette.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(p.surfaceTint), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = p.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = p.ink, modifier = Modifier.weight(1f))
        trailing()
    }
}
