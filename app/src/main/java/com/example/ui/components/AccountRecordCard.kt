package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AccountRecord
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.RoseUnpaid
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
    onCopyText: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val months = account.normalizedMonths()
    val paidCount = months.count { it }
    val borderColor = if (isDarkGlass) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.75f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. ئىسىم، ھېسابات نومۇرى ۋە تەھرىرلەش/ئۆچۈرۈش قۇرى
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                Brush.linearGradient(listOf(IndigoAccent, TurquoiseAccent))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (paidCount == 12) EmeraldPaid.copy(alpha = 0.2f) else IndigoAccent.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = stringResource(R.string.months_paid_ratio, paidCount),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (paidCount == 12) EmeraldPaid else TurquoiseAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEditClick) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(IndigoAccent.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.edit_action),
                                tint = IndigoAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(onClick = onDeleteClick) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(RoseUnpaid.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete_action),
                                tint = RoseUnpaid,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 2. ھېسابات نومۇرى ۋە مەخپىي نومۇر تاختىسى (مەخسۇس سىنبەلگىلەر بىلەن)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            GlowingTermIcon(icon = Icons.Filled.Badge, color = TurquoiseAccent)
                            Column {
                                Text(
                                    text = stringResource(R.string.account_number_prefix),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = account.accountName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        IconButton(
                            onClick = { onCopyText(account.accountName, account.accountName) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = stringResource(R.string.copy_account_cd),
                                tint = TurquoiseAccent,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    if (account.password.isNotBlank()) {
                        HorizontalDivider(color = borderColor)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                GlowingTermIcon(icon = Icons.Filled.Key, color = Color(0xFFF59E0B))
                                Column {
                                    Text(
                                        text = stringResource(R.string.field_password_label),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (passwordVisible) account.password else "••••••••",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Row {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onCopyText(account.password, account.password) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = stringResource(R.string.copy_password_cd),
                                        tint = IndigoAccent,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. ئاتالغۇلار بەلگىلىرى (ئېقىم مىقدارى، ئاچقان ۋاقتى، كوچا، ئۆي نومۇرى، ئىزاھات)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (account.flowAmount.isNotBlank()) {
                    TermMetadataBadge(
                        icon = Icons.Filled.Speed,
                        label = stringResource(R.string.field_flow_label),
                        value = account.flowAmount,
                        accent = EmeraldPaid
                    )
                }
                if (account.openDate.isNotBlank()) {
                    TermMetadataBadge(
                        icon = Icons.Filled.DateRange,
                        label = stringResource(R.string.field_open_date_label),
                        value = account.openDate,
                        accent = IndigoAccent
                    )
                }
                if (account.streetName.isNotBlank()) {
                    TermMetadataBadge(
                        icon = Icons.Filled.LocationOn,
                        label = stringResource(R.string.field_street_label),
                        value = account.streetName,
                        accent = RoseUnpaid
                    )
                }
                if (account.houseNumber.isNotBlank()) {
                    TermMetadataBadge(
                        icon = Icons.Filled.Home,
                        label = stringResource(R.string.field_house_label),
                        value = account.houseNumber,
                        accent = TurquoiseAccent
                    )
                }
            }

            if (account.notes.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = IndigoAccent.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, IndigoAccent.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlowingTermIcon(icon = Icons.Filled.Description, color = IndigoAccent)
                        Text(
                            text = "${stringResource(R.string.field_notes_label)}: ${account.notes}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // 4. 12 ئايلىق پۇل تۆلەش رېشاتكىسى
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = TurquoiseAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.months_card_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                val columns = if (isCompactWidth) 3 else 4
                val rows = (0 until 12).chunked(columns)
                rows.forEach { rowIndices ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowIndices.forEach { mIdx ->
                            val isPaid = months[mIdx]
                            val bg = if (isPaid) EmeraldPaid.copy(alpha = 0.22f) else RoseUnpaid.copy(alpha = 0.15f)
                            val stroke = if (isPaid) EmeraldPaid.copy(alpha = 0.6f) else RoseUnpaid.copy(alpha = 0.45f)
                            val textCol = if (isPaid) EmeraldPaid else RoseUnpaid

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = bg,
                                border = BorderStroke(1.dp, stroke),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onToggleMonth(mIdx) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stringResource(R.string.month_label_format, mIdx + 1),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        if (isPaid) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = null,
                                                tint = textCol,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                        Text(
                                            text = if (isPaid) stringResource(R.string.paid_label) else stringResource(R.string.unpaid_label),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = textCol,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlowingTermIcon(
    icon: ImageVector,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun TermMetadataBadge(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
