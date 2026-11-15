package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalHesabatColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupAndStatsSheet(
    totalAccounts: Int,
    paidMonthsCount: Int,
    unpaidMonthsCount: Int,
    streetBreakdown: List<Pair<String, Int>>,
    onExportJson: () -> Unit,
    onImportJson: () -> Unit,
    onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val glass = LocalHesabatColors.current
  val totalMonths = (totalAccounts * 12).coerceAtLeast(1)
  val paidRatio = if (totalAccounts == 0) 0f else (paidMonthsCount.toFloat() / totalMonths)
  val paidPercent = (paidRatio * 100).toInt()

  ModalBottomSheet(
      onDismissRequest = onDismiss,
      sheetState = sheetState,
      containerColor = MaterialTheme.colorScheme.surface,
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
  ) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
          Icon(
              imageVector = Icons.Filled.Storage,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(10.dp).size(22.dp),
          )
        }
        Text(
            text = stringResource(R.string.backup_sheet_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
      }

      // Payment Progress Summary Card
      Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
          modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
                text = stringResource(R.string.paid_months_label),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = "$paidPercent% ($paidMonthsCount / ${totalAccounts * 12})",
                style = MaterialTheme.typography.labelLarge,
                color = glass.paidText,
                fontWeight = FontWeight.ExtraBold,
            )
          }

          LinearProgressIndicator(
              progress = { paidRatio },
              modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(999.dp)),
              color = glass.paidText,
              trackColor = glass.unpaidBg,
              strokeCap = StrokeCap.Round,
          )

          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
                text = "${stringResource(R.string.total_accounts_label)} $totalAccounts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${stringResource(R.string.unpaid_months_label)} $unpaidMonthsCount",
                style = MaterialTheme.typography.bodySmall,
                color = glass.unpaidText,
                fontWeight = FontWeight.Bold,
            )
          }
        }
      }

      // Street Breakdown
      if (streetBreakdown.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
              text = stringResource(R.string.field_street_label),
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          streetBreakdown.take(6).forEach { (street, count) ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                      imageVector = Icons.Filled.LocationOn,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.secondary,
                      modifier = Modifier.size(16.dp),
                  )
                  Text(text = street, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
              }
            }
          }
        }
      }

      HorizontalDivider()

      // Export & Import JSON buttons (SAF)
      Button(
          onClick = onExportJson,
          modifier = Modifier.fillMaxWidth().height(52.dp).testTag("export_json_button"),
          shape = RoundedCornerShape(16.dp),
      ) {
        Icon(
            imageVector = Icons.Filled.Download,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.backup_export_btn),
            style = MaterialTheme.typography.labelLarge,
        )
      }

      OutlinedButton(
          onClick = onImportJson,
          modifier = Modifier.fillMaxWidth().height(52.dp).testTag("import_json_button"),
          shape = RoundedCornerShape(16.dp),
      ) {
        Icon(
            imageVector = Icons.Filled.Upload,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.backup_import_btn),
            style = MaterialTheme.typography.labelLarge,
        )
      }
    }
  }
}
