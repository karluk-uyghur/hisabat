package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AccountRecordEntity
import com.example.ui.theme.LocalHesabatColors

@Composable
fun AccountRecordCard(
    record: AccountRecordEntity,
    currentMonthIndex: Int,
    onToggleMonth: (Int) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopyValue: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
  val glass = LocalHesabatColors.current
  var isPasswordVisible by rememberSaveable(record.localId) { mutableStateOf(false) }
  val paidCount = record.paidMonthsCount()

  Surface(
      modifier = modifier.fillMaxWidth().testTag("account_card_${record.localId}"),
      shape = MaterialTheme.shapes.large,
      color = glass.glassSurface,
      border = BorderStroke(1.dp, glass.glassBorder),
      shadowElevation = 6.dp,
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      // Header Row: Owner Name + Account Number + Edit/Delete Actions
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
      ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
                text = record.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color =
                    if (paidCount == 12) glass.paidBg
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                border =
                    BorderStroke(
                        1.dp,
                        if (paidCount == 12) glass.paidBorder
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    ),
            ) {
              Text(
                  text = "$paidCount / 12",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color =
                      if (paidCount == 12) glass.paidText else MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              )
            }
          }

          // Tappable Account Number Pill (tap to copy)
          Surface(
              shape = RoundedCornerShape(10.dp),
              color = glass.glassInnerSurface,
              modifier =
                  Modifier.clip(RoundedCornerShape(10.dp))
                      .clickable { onCopyValue(record.accountName) }
                      .testTag("copy_account_btn_${record.localId}"),
          ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                  text = record.accountName,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
              )
              Icon(
                  imageVector = Icons.Filled.ContentCopy,
                  contentDescription = stringResource(R.string.cd_copy),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(14.dp),
              )
            }
          }
        }

        // Action Buttons (Edit & Delete, 48dp touch targets)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Surface(
              shape = RoundedCornerShape(14.dp),
              color = glass.editBg,
              border = BorderStroke(1.dp, glass.editBorder),
          ) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(48.dp).testTag("edit_account_${record.localId}"),
            ) {
              Icon(
                  imageVector = Icons.Filled.Edit,
                  contentDescription = stringResource(R.string.cd_edit_account),
                  tint = glass.editText,
                  modifier = Modifier.size(20.dp),
              )
            }
          }

          Surface(
              shape = RoundedCornerShape(14.dp),
              color = glass.unpaidBg,
              border = BorderStroke(1.dp, glass.unpaidBorder),
          ) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp).testTag("delete_account_${record.localId}"),
            ) {
              Icon(
                  imageVector = Icons.Filled.Delete,
                  contentDescription = stringResource(R.string.cd_delete_account),
                  tint = glass.unpaidText,
                  modifier = Modifier.size(20.dp),
              )
            }
          }
        }
      }

      // Payment Progress Bar
      LinearProgressIndicator(
          progress = { paidCount / 12f },
          modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(999.dp)),
          color = if (paidCount == 12) glass.paidText else MaterialTheme.colorScheme.primary,
          trackColor = glass.glassInnerSurface,
          strokeCap = StrokeCap.Round,
      )

      HorizontalDivider(color = glass.glassBorder)

      // Details Grid (Adaptive columns via BoxWithConstraints)
      BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = if (maxWidth > 520.dp) 3 else 2
        val tiles =
            listOf<@Composable () -> Unit>(
                {
                  PasswordDetailTile(
                      password = record.password,
                      isVisible = isPasswordVisible,
                      onToggleVisibility = { isPasswordVisible = !isPasswordVisible },
                      onCopy = {
                        if (record.password.isNotEmpty()) {
                          onCopyValue(record.password)
                        }
                      },
                  )
                },
                {
                  InfoDetailTile(
                      icon = Icons.Filled.Speed,
                      label = stringResource(R.string.field_flow_label),
                      value = record.flowAmount.ifBlank { "-" },
                  )
                },
                {
                  InfoDetailTile(
                      icon = Icons.Filled.CalendarToday,
                      label = stringResource(R.string.field_open_date_label),
                      value = record.openDate.ifBlank { "-" },
                  )
                },
                {
                  InfoDetailTile(
                      icon = Icons.Filled.Home,
                      label = stringResource(R.string.field_street_label),
                      value = record.streetName.ifBlank { "-" },
                  )
                },
                {
                  InfoDetailTile(
                      icon = Icons.Filled.Home,
                      label = stringResource(R.string.field_house_label),
                      value = record.houseNumber.ifBlank { "-" },
                  )
                },
            )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          tiles.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              rowItems.forEach { tile -> Box(modifier = Modifier.weight(1f)) { tile() } }
              repeat(columns - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
          }
        }
      }

      // Notes Callout
      if (record.notes.isNotBlank()) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
              modifier = Modifier.height(IntrinsicSize.Min),
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(
                modifier =
                    Modifier.width(4.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
            )
            Text(
                text = "${stringResource(R.string.field_notes_label)}: ${record.notes}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            )
          }
        }
      }

      // 12-Month Payment Interactive Grid
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.months_card_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
          val monthCols = if (maxWidth > 540.dp) 6 else 4
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (rowStart in 0 until 12 step monthCols) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                for (offset in 0 until monthCols) {
                  val monthIdx = rowStart + offset
                  if (monthIdx < 12) {
                    val isPaid = record.isMonthPaid(monthIdx)
                    val isCurrent = monthIdx == currentMonthIndex
                    MonthStatusButton(
                        monthNumber = monthIdx + 1,
                        isPaid = isPaid,
                        isCurrentMonth = isCurrent,
                        onClick = { onToggleMonth(monthIdx) },
                        modifier =
                            Modifier.weight(1f)
                                .testTag("month_toggle_${record.localId}_$monthIdx"),
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
private fun InfoDetailTile(
    icon: ImageVector,
    label: String,
    value: String,
) {
  val glass = LocalHesabatColors.current
  Surface(
      shape = RoundedCornerShape(14.dp),
      color = glass.glassInnerSurface,
      modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
          text = value,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun PasswordDetailTile(
    password: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onCopy: () -> Unit,
) {
  val glass = LocalHesabatColors.current
  val displayText =
      when {
        password.isEmpty() -> stringResource(R.string.no_password_placeholder)
        isVisible -> password
        else -> "••••••"
      }

  Surface(
      shape = RoundedCornerShape(14.dp),
      color = glass.glassInnerSurface,
      modifier =
          Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .clickable(onClick = onToggleVisibility),
  ) {
    Row(
        modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
              imageVector = Icons.Filled.Lock,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(12.dp),
          )
          Text(
              text = stringResource(R.string.field_password_label),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
          )
        }
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color =
                if (password.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
      }

      Row {
        IconButton(
            onClick = onToggleVisibility,
            modifier = Modifier.size(36.dp),
        ) {
          Icon(
              imageVector =
                  if (isVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
              contentDescription = stringResource(R.string.cd_toggle_password),
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp),
          )
        }
        if (password.isNotEmpty()) {
          IconButton(
              onClick = onCopy,
              modifier = Modifier.size(36.dp),
          ) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = stringResource(R.string.cd_copy),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MonthStatusButton(
    monthNumber: Int,
    isPaid: Boolean,
    isCurrentMonth: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val glass = LocalHesabatColors.current
  val bgColor by
      animateColorAsState(
          targetValue = if (isPaid) glass.paidBg else glass.unpaidBg,
          animationSpec = tween(durationMillis = 200),
          label = "monthBg",
      )
  val borderColor by
      animateColorAsState(
          targetValue =
              when {
                isCurrentMonth -> MaterialTheme.colorScheme.secondary
                isPaid -> glass.paidBorder
                else -> glass.unpaidBorder
              },
          animationSpec = tween(durationMillis = 200),
          label = "monthBorder",
      )
  val contentColor: Color = if (isPaid) glass.paidText else glass.unpaidText

  Surface(
      modifier =
          modifier
              .minimumInteractiveComponentSize()
              .height(50.dp)
              .clip(RoundedCornerShape(13.dp))
              .clickable(onClick = onClick),
      shape = RoundedCornerShape(13.dp),
      color = bgColor,
      border = BorderStroke(if (isCurrentMonth) 1.5.dp else 1.dp, borderColor),
  ) {
    Column(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
      Text(
          text = stringResource(R.string.month_label_format, monthNumber),
          style = MaterialTheme.typography.labelSmall,
          color = contentColor.copy(alpha = 0.85f),
          maxLines = 1,
      )
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Icon(
            imageVector = if (isPaid) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text =
                stringResource(
                    if (isPaid) R.string.status_paid else R.string.status_unpaid
                ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = contentColor,
            maxLines = 1,
        )
      }
    }
  }
}
