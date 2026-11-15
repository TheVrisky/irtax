package tax.app.mehdighobadi.ui2.screens

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tax.app.mehdighobadi.data.TaxData
import tax.app.mehdighobadi.ui2.components.AnimatedCount
import tax.app.mehdighobadi.ui2.components.GlassCard
import tax.app.mehdighobadi.ui2.data.AppState
import tax.app.mehdighobadi.ui2.theme.LocalMagPalette

@Composable
fun HomeScreen(
    onStart: () -> Unit,
    onOpenChapter: (String) -> Unit
) {
    val p = LocalMagPalette.current
    val chapters = TaxData.content.chapters
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TopBar() }
        item { HeroCard(onStart = onStart) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Modifier.weight(1f), AppState.totalArticles(), "سوال", Icons.Outlined.MenuBook)
                StatCard(Modifier.weight(1f), AppState.progressPct(), "درصد پیشرفت", Icons.Outlined.Bolt)
                StatCard(Modifier.weight(1f), AppState.studyMinutes.value, "دقیقه مطالعه", Icons.Outlined.Schedule)
            }
        }
        item { SmartSuggestion(chapters.firstOrNull()?.title ?: "", onOpenChapter) }
        item {
            Text(
                "دسته‌بندی‌ها",
                style = MaterialTheme.typography.titleLarge,
                color = p.ink,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }
        itemsIndexed(chapters) { idx, ch ->
            ChapterRow(idx = idx, title = ch.title, count = ch.articles.size) { onOpenChapter(ch.id) }
        }
    }
}

@Composable
private fun TopBar() {
    val p = LocalMagPalette.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("قوانین مالیاتی", style = MaterialTheme.typography.headlineSmall, color = p.ink, fontWeight = FontWeight.Bold)
            Text("دستیار هوشمند مطالعه", style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(p.surfaceTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = p.primary)
        }
    }
}

@Composable
private fun HeroCard(onStart: () -> Unit) {
    val p = LocalMagPalette.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(p.heroGradient)
            .clickable { onStart() }
            .padding(22.dp)
    ) {
        Column {
            Text("شروع مطالعه هوشمند", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "بر اساس پیشرفت شما، ادامه مطالعه پیشنهاد می‌شود",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.88f)
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.20f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ادامه", color = Color.White, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Outlined.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, value: Int, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val p = LocalMagPalette.current
    GlassCard(modifier = modifier.height(110.dp), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, contentDescription = null, tint = p.primary, modifier = Modifier.size(22.dp))
            val animated = AnimatedCount(value)
            Text(animated.toString(), style = MaterialTheme.typography.titleLarge, color = p.ink, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = p.inkSoft)
        }
    }
}

@Composable
private fun SmartSuggestion(chapterTitle: String, onOpenChapter: (String) -> Unit) {
    val p = LocalMagPalette.current
    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), tint = p.surfaceTint) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(p.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = p.primary) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("پیشنهاد هوشمند", style = MaterialTheme.typography.titleMedium, color = p.ink)
                Text("این دسته را مرور کنید: $chapterTitle", style = MaterialTheme.typography.bodySmall, color = p.inkSoft, maxLines = 2)
            }
        }
    }
}

@Composable
private fun ChapterRow(idx: Int, title: String, count: Int, onClick: () -> Unit) {
    val p = LocalMagPalette.current
    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Row(
            Modifier.padding(16.dp).clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(p.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) { Text((idx + 1).toString(), color = p.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = p.ink, maxLines = 2)
                Text("$count مقاله", style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
            }
        }
    }
}
