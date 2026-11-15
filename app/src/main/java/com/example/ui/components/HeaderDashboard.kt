package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalHesabatColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeaderDashboard(
    totalCount: Int,
    paidMonthsCount: Int,
    unpaidMonthsCount: Int,
    fullyPaidAccountsCount: Int,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenBackupSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val glass = LocalHesabatColors.current

  Surface(
      modifier = modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.large,
      color = glass.glassSurfaceElevated,
      border = BorderStroke(1.dp, glass.glassBorder),
      shadowElevation = 8.dp,
  ) {
    Column {
      // Top colorful gradient strip
      Box(
          modifier =
              Modifier.fillMaxWidth()
                  .height(5.dp)
                  .background(
                      Brush.horizontalGradient(
                          colors =
                              listOf(
                                  MaterialTheme.colorScheme.primary,
                                  MaterialTheme.colorScheme.secondary,
                                  glass.paidText,
                              )
                      )
                  )
      )

      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Box(
                modifier =
                    Modifier.size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary,
                                )
                            )
                        ),
                contentAlignment = Alignment.Center,
            ) {
              Icon(
                  imageVector = Icons.Filled.AccountBalanceWallet,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(26.dp),
              )
            }

            Column {
              Text(
                  text = stringResource(R.string.header_title),
                  style = MaterialTheme.typography.titleLarge,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
              )
              Text(
                  text = stringResource(R.string.header_subtitle),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = glass.glassInnerSurface,
                border = BorderStroke(1.dp, glass.glassBorder),
            ) {
              IconButton(
                  onClick = onToggleTheme,
                  modifier = Modifier.size(48.dp).testTag("toggle_theme_button"),
              ) {
                Icon(
                    imageVector =
                        if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = stringResource(R.string.cd_toggle_theme),
                    tint =
                        if (isDarkTheme) glass.editText else MaterialTheme.colorScheme.primary,
                )
              }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = glass.glassInnerSurface,
                border = BorderStroke(1.dp, glass.glassBorder),
            ) {
              IconButton(
                  onClick = onOpenBackupSheet,
                  modifier = Modifier.size(48.dp).testTag("open_backup_button"),
              ) {
                Icon(
                    imageVector = Icons.Filled.Storage,
                    contentDescription = stringResource(R.string.cd_backup_menu),
                    tint = MaterialTheme.colorScheme.secondary,
                )
              }
            }
          }
        }

        // Prominent Counter Badge + Quick Stats Strip
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          // Total Accounts Glowing Gradient Badge
          Box(
              modifier =
                  Modifier.clip(RoundedCornerShape(16.dp))
                      .background(
                          Brush.horizontalGradient(
                              listOf(
                                  glass.badgeGradientStart,
                                  glass.badgeGradientMid,
                                  glass.badgeGradientEnd,
                              )
                          )
                      )
                      .padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                  imageVector = Icons.Filled.People,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp),
              )
              Text(
                  text = stringResource(R.string.total_accounts_label),
                  style = MaterialTheme.typography.labelMedium,
                  color = Color.White,
              )
              Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color.White.copy(alpha = 0.24f),
              ) {
                Text(
                    text = totalCount.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier =
                        Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            .testTag("total_accounts_count"),
                )
              }
            }
          }

          StatMiniPill(
              icon = Icons.Filled.CheckCircle,
              label = stringResource(R.string.paid_months_label),
              value = paidMonthsCount.toString(),
              tint = glass.paidText,
              bgColor = glass.paidBg,
              borderColor = glass.paidBorder,
          )

          StatMiniPill(
              icon = Icons.Filled.WarningAmber,
              label = stringResource(R.string.unpaid_months_label),
              value = unpaidMonthsCount.toString(),
              tint = glass.unpaidText,
              bgColor = glass.unpaidBg,
              borderColor = glass.unpaidBorder,
          )

          StatMiniPill(
              icon = Icons.Filled.CheckCircle,
              label = stringResource(R.string.fully_paid_label),
              value = fullyPaidAccountsCount.toString(),
              tint = MaterialTheme.colorScheme.primary,
              bgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
              borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
          )
        }
      }
    }
  }
}

@Composable
private fun StatMiniPill(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    bgColor: Color,
    borderColor: Color,
) {
  Surface(
      shape = RoundedCornerShape(14.dp),
      color = bgColor,
      border = BorderStroke(1.dp, borderColor),
  ) {
    Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Icon(
          imageVector = icon,
          contentDescription = null,
          tint = tint,
          modifier = Modifier.size(16.dp),
      )
      Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(modifier = Modifier.width(2.dp))
      Text(
          text = value,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = tint,
      )
    }
  }
}
