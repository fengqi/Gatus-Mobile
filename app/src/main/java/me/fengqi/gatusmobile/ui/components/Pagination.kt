package me.fengqi.gatusmobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.fengqi.gatusmobile.ui.theme.GatusCardBorder
import me.fengqi.gatusmobile.ui.theme.GatusPrimary

@Composable
fun Pagination(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalPages <= 1) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = { if (currentPage > 1) onPageChange(currentPage - 1) },
            enabled = currentPage > 1,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            border = BorderStroke(1.dp, GatusCardBorder)
        ) {
            Text("<")
        }

        val visiblePages = getVisiblePages(currentPage, totalPages, maxVisible = 5)
        for (page in visiblePages) {
            if (page == -1) {
                Text(
                    text = "...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            } else if (page == currentPage) {
                Button(
                    onClick = { onPageChange(page) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GatusPrimary),
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Text(page.toString())
                }
            } else {
                OutlinedButton(
                    onClick = { onPageChange(page) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    border = BorderStroke(1.dp, GatusCardBorder),
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Text(page.toString())
                }
            }
        }

        OutlinedButton(
            onClick = { if (currentPage < totalPages) onPageChange(currentPage + 1) },
            enabled = currentPage < totalPages,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            border = BorderStroke(1.dp, GatusCardBorder)
        ) {
            Text(">")
        }
    }
}

private fun getVisiblePages(current: Int, total: Int, maxVisible: Int): List<Int> {
    val pages = mutableListOf<Int>()
    val half = maxVisible / 2
    var start = (current - half).coerceAtLeast(1)
    var end = (start + maxVisible - 1).coerceAtMost(total)

    if (end - start < maxVisible - 1) {
        start = (end - maxVisible + 1).coerceAtLeast(1)
    }

    if (start > 1) {
        pages.add(1)
        if (start > 2) pages.add(-1)
    }

    for (i in start..end) pages.add(i)

    if (end < total) {
        if (end < total - 1) pages.add(-1)
        pages.add(total)
    }

    return pages
}
