package tax.app.mehdighobadi.ui2.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tax.app.mehdighobadi.data.TaxData
import tax.app.mehdighobadi.ui2.components.GlassCard
import tax.app.mehdighobadi.ui2.theme.LocalMagPalette

@Composable
fun LibraryScreen(onOpenChapter: (String) -> Unit) {
    val p = LocalMagPalette.current
    var query by remember { mutableStateOf("") }
    val chapters = TaxData.content.chapters.filter {
        query.isBlank() || it.title.contains(query, true) || it.articles.any { a -> a.content.contains(query, true) }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("بانک سوال و دسته‌بندی", style = MaterialTheme.typography.headlineSmall, color = p.ink, fontWeight = FontWeight.Bold)
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("جستجو در محتوا…", color = p.inkSoft) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = p.inkSoft) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = p.primary,
                    unfocusedBorderColor = p.line,
                    focusedContainerColor = p.surface.copy(alpha = 0.6f),
                    unfocusedContainerColor = p.surface.copy(alpha = 0.6f)
                )
            )
        }
        itemsIndexed(chapters) { idx, ch ->
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                Row(
                    Modifier.padding(18.dp).clickable { onOpenChapter(ch.id) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(p.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) { Text("${idx + 1}", color = p.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(ch.title, style = MaterialTheme.typography.titleMedium, color = p.ink, maxLines = 2)
                        Spacer(Modifier.height(2.dp))
                        Text("${ch.articles.size} مقاله", style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
                    }
                }
            }
        }
    }
}
