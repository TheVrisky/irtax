package tax.app.mehdighobadi.ui2.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tax.app.mehdighobadi.data.TaxData
import tax.app.mehdighobadi.ui2.components.GlassCard
import tax.app.mehdighobadi.ui2.data.AppState
import tax.app.mehdighobadi.ui2.theme.LocalMagPalette

@Composable
fun ReaderScreen(chapterId: String, onBack: () -> Unit) {
    val p = LocalMagPalette.current
    val ctx = LocalContext.current
    val chapter = TaxData.content.chapters.find { it.id == chapterId }
    var index by remember { mutableIntStateOf(0) }
    if (chapter == null || chapter.articles.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("محتوایی یافت نشد", color = p.inkSoft) }
        return
    }
    val article = chapter.articles[index]
    val key = "${chapter.id}#$index"
    val fav = AppState.favorites[key] ?: false

    Column(Modifier.fillMaxSize()) {
        // top bar
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "بازگشت", tint = p.ink)
            }
            Text(chapter.title, style = MaterialTheme.typography.titleMedium, color = p.ink, modifier = Modifier.weight(1f), maxLines = 1)
            IconButton(onClick = { AppState.toggleFav(ctx, key) }) {
                Icon(if (fav) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = null, tint = if (fav) p.primary else p.inkSoft)
            }
        }
        LinearProgressIndicator(
            progress = { (index + 1f) / chapter.articles.size },
            modifier = Modifier.fillMaxWidth().height(4.dp).padding(horizontal = 20.dp).clip(RoundedCornerShape(50)),
            color = p.primary,
            trackColor = p.line
        )
        Spacer(Modifier.height(10.dp))

        AnimatedContent(
            targetState = index,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally(tween(320)) { -it / 3 } + fadeIn(tween(320))) togetherWith (slideOutHorizontally(tween(320)) { it / 3 } + fadeOut(tween(200)))
                } else {
                    (slideInHorizontally(tween(320)) { it / 3 } + fadeIn(tween(320))) togetherWith (slideOutHorizontally(tween(320)) { -it / 3 } + fadeOut(tween(200)))
                }
            },
            modifier = Modifier.weight(1f),
            label = "q"
        ) { i ->
            val a = chapter.articles[i]
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.clip(RoundedCornerShape(50)).background(p.primary.copy(alpha = 0.14f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) { Text(a.number, color = p.primary, style = MaterialTheme.typography.labelLarge) }
                            Spacer(Modifier.weight(1f))
                            Text("${i + 1} / ${chapter.articles.size}", style = MaterialTheme.typography.labelSmall, color = p.inkSoft)
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(a.content, style = MaterialTheme.typography.bodyLarge, color = p.ink)
                        a.sections.forEach { s ->
                            Spacer(Modifier.height(14.dp))
                            Text(s.title, style = MaterialTheme.typography.titleMedium, color = p.primary)
                            Spacer(Modifier.height(4.dp))
                            Text(s.content, style = MaterialTheme.typography.bodyMedium, color = p.ink)
                        }
                        a.notes.forEach { n ->
                            Spacer(Modifier.height(14.dp))
                            GlassCard(shape = RoundedCornerShape(16.dp), tint = p.surfaceTint) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(n.label, style = MaterialTheme.typography.labelLarge, color = p.secondary)
                                    Spacer(Modifier.height(4.dp))
                                    Text(n.content, style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Box(
                            Modifier.clip(RoundedCornerShape(14.dp)).background(if (AppState.readArticles[key] == true) p.secondary.copy(alpha = 0.16f) else p.surfaceTint)
                                .clickable { AppState.toggleRead(ctx, key) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(if (AppState.readArticles[key] == true) "✓ مطالعه شد" else "علامت‌گذاری به‌عنوان خوانده‌شده", style = MaterialTheme.typography.labelLarge, color = if (AppState.readArticles[key] == true) p.secondary else p.ink)
                        }
                    }
                }
                Spacer(Modifier.height(90.dp))
            }
        }

        // bottom prev/next bar
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NavButton(
                text = "قبلی",
                icon = Icons.Outlined.ChevronRight,
                enabled = index > 0,
                modifier = Modifier.weight(1f)
            ) { if (index > 0) index-- }
            NavButton(
                text = "بعدی",
                icon = Icons.Outlined.ChevronLeft,
                enabled = index < chapter.articles.size - 1,
                modifier = Modifier.weight(1f)
            ) { if (index < chapter.articles.size - 1) index++ }
        }
    }
}

@Composable
private fun NavButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val p = LocalMagPalette.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) p.surface else p.surface.copy(alpha = 0.5f))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (enabled) p.primary else p.inkSoft.copy(alpha = 0.5f))
        Spacer(Modifier.width(6.dp))
        Text(text, color = if (enabled) p.ink else p.inkSoft.copy(alpha = 0.5f), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}
