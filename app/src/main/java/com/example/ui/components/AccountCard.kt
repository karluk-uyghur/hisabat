package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AccountRecord
import com.example.ui.theme.AmberEdit
import com.example.ui.theme.AmberEditBg
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.EmeraldPaidBg
import com.example.ui.theme.EmeraldPaidDark
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.RoseUnpaid
import com.example.ui.theme.RoseUnpaidBg
import com.example.ui.theme.RoseUnpaidDark
import com.example.ui.theme.TurquoiseAccent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountCard(
    account: AccountRecord,
    isDarkGlass: Boolean,
    isCompactWidth: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleMonth: (Int) -> Unit,
    onCopyText: (label: String, value: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPassword by remember(account.id) { mutableStateOf(false) }
    val months = account.normalizedMonths()
    val paidCount = months.count { it }
    val progress = paidCount / 12f
    val paidColor = if (isDarkGlass) EmeraldPaidDark else EmeraldPaid
    val unpaidColor = if (isDarkGlass) RoseUnpaidDark else RoseUnpaid
    val borderColor = if (isDarkGlass) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.72f)
    val detailTileColor = if (isDarkGlass) Color(0x990B1222) else Color(0xCCF8FAFC)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .testTag("account_card_${account.id}"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Header: Name + Badge + Edit/Delete Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            color = IndigoAccent.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.account_badge),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${stringResource(R.string.account_number_prefix)} ${account.accountName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TurquoiseAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        IconButton(
                            onClick = { onCopyText(account.accountName, account.accountName) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = stringResource(R.string.copy_account_cd),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onEditClick)
                            .testTag("edit_account_${account.id}"),
                        color = AmberEditBg,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.edit_action),
                                tint = AmberEdit,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.edit_action),
                                style = MaterialTheme.typography.labelMedium,
                                color = AmberEdit,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onDeleteClick)
                            .testTag("delete_account_${account.id}"),
                        color = RoseUnpaidBg,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = stringResource(R.string.delete_action),
                                tint = unpaidColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.delete_action),
                                style = MaterialTheme.typography.labelMedium,
                                color = unpaidColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Progress Bar Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(7.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = paidColor,
                    trackColor = unpaidColor.copy(alpha = 0.22f)
                )
                Surface(
                    color = paidColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = stringResource(R.string.months_paid_ratio, paidCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = paidColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(color = borderColor.copy(alpha = 0.35f))

            // Details Grid (Password, Flow, Open Date, Street, House)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = if (isCompactWidth) 2 else 3
            ) {
                // Password tile with reveal & copy
                Surface(
                    color = detailTileColor,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.field_password_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = when {
                                    account.password.isBlank() -> stringResource(R.string.no_password_label)
                                    showPassword -> account.password
                                    else -> "••••••"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (account.password.isNotBlank()) {
                            Row {
                                IconButton(
                                    onClick = { showPassword = !showPassword },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = stringResource(
                                            if (showPassword) R.string.hide_password else R.string.show_password
                                        ),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onCopyText(account.password, account.password) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = stringResource(R.string.copy_password_cd),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                DetailTile(
                    label = stringResource(R.string.field_flow_label),
                    value = account.flowAmount.ifBlank { "-" },
                    tileColor = detailTileColor,
                    modifier = Modifier.weight(1f)
                )

                DetailTile(
                    label = stringResource(R.string.field_open_date_label),
                    value = account.openDate.ifBlank { "-" },
                    tileColor = detailTileColor,
                    modifier = Modifier.weight(1f)
                )

                DetailTile(
                    label = stringResource(R.string.field_street_label),
                    value = account.streetName.ifBlank { "-" },
                    tileColor = detailTileColor,
                    modifier = Modifier.weight(1f)
                )

                DetailTile(
                    label = stringResource(R.string.field_house_label),
                    value = account.houseNumber.ifBlank { "-" },
                    tileColor = detailTileColor,
                    modifier = Modifier.weight(1f)
                )
            }

            // Notes Callout
            if (account.notes.isNotBlank()) {
                Surface(
                    color = IndigoAccent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = IndigoAccent.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(14.dp)
                        )
                ) {
                    Text(
                        text = "${stringResource(R.string.field_notes_label)}: ${account.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // 12-Month Payment Interactive Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.months_grid_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                val columns = if (isCompactWidth) 4 else 6
                val rows = (0 until 12).chunked(columns)
                rows.forEach { rowIndices ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowIndices.forEach { monthIdx ->
                            val isPaid = months[monthIdx]
                            MonthStatusPill(
                                monthNumber = monthIdx + 1,
                                isPaid = isPaid,
                                paidColor = paidColor,
                                unpaidColor = unpaidColor,
                                onClick = { onToggleMonth(monthIdx) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("month_toggle_${account.id}_$monthIdx")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTile(
    label: String,
    value: String,
    tileColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = tileColor,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MonthStatusPill(
    monthNumber: Int,
    isPaid: Boolean,
    paidColor: Color,
    unpaidColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isPaid) EmeraldPaidBg else RoseUnpaidBg,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "monthBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isPaid) paidColor else unpaidColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "monthContent"
    )

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = contentColor.copy(alpha = 0.42f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.month_label_format, monthNumber),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = if (isPaid) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = stringResource(if (isPaid) R.string.paid_label else R.string.unpaid_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
