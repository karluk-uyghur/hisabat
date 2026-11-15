package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalHesabatColors
import com.example.viewmodel.RecordFilterMode

@Composable
fun SearchAndFilterBar(
    searchQuery: String,
    filterMode: RecordFilterMode,
    selectedStreet: String?,
    availableStreets: List<String>,
    resultCount: Int,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onFilterModeChange: (RecordFilterMode) -> Unit,
    onToggleStreet: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
  val glass = LocalHesabatColors.current

  Surface(
      modifier = modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.large,
      color = glass.glassSurface,
      border = BorderStroke(1.dp, glass.glassBorder),
      shadowElevation = 6.dp,
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      OutlinedTextField(
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          modifier = Modifier.fillMaxWidth().testTag("search_input"),
          placeholder = {
            Text(
                text = stringResource(R.string.search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
            )
          },
          leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = stringResource(R.string.cd_search),
                tint = MaterialTheme.colorScheme.primary,
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(
                  onClick = onClearSearch,
                  modifier = Modifier.size(48.dp).testTag("clear_search_button"),
              ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.cd_clear_search),
                    tint = MaterialTheme.colorScheme.error,
                )
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors =
              OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = glass.glassInnerSurface,
                  unfocusedContainerColor = glass.glassInnerSurface,
                  focusedBorderColor = MaterialTheme.colorScheme.primary,
                  unfocusedBorderColor = glass.glassBorder,
              ),
      )

      // Status Filter Chips
      Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        val filters =
            listOf(
                RecordFilterMode.ALL to stringResource(R.string.filter_all),
                RecordFilterMode.FULLY_PAID to stringResource(R.string.filter_fully_paid),
                RecordFilterMode.HAS_UNPAID to stringResource(R.string.filter_has_unpaid),
                RecordFilterMode.CURRENT_MONTH_UNPAID to
                    stringResource(R.string.filter_current_month_unpaid),
            )
        filters.forEach { (mode, label) ->
          FilterChip(
              selected = filterMode == mode,
              onClick = { onFilterModeChange(mode) },
              label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
              modifier = Modifier.testTag("filter_chip_${mode.name.lowercase()}"),
              colors =
                  FilterChipDefaults.filterChipColors(
                      selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                      selectedLabelColor = MaterialTheme.colorScheme.primary,
                  ),
          )
        }
      }

      // Street Filter Chips (when streets exist)
      if (availableStreets.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          availableStreets.forEach { street ->
            FilterChip(
                selected = selectedStreet == street,
                onClick = { onToggleStreet(street) },
                leadingIcon = {
                  Icon(
                      imageVector = Icons.Filled.LocationOn,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp),
                  )
                },
                label = { Text(text = street, style = MaterialTheme.typography.labelSmall) },
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor =
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f),
                        selectedLabelColor = MaterialTheme.colorScheme.secondary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.secondary,
                    ),
            )
          }
        }
      }

      AnimatedVisibility(visible = searchQuery.isNotBlank()) {
        Text(
            text = stringResource(R.string.search_results_count, resultCount),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
      }
    }
  }
}
