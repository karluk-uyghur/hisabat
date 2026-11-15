package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalHesabatColors
import com.example.viewmodel.AccountEditorState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountEditorSheet(
    state: AccountEditorState,
    onFieldsChange: ((AccountEditorState) -> AccountEditorState) -> Unit,
    onToggleMonth: (Int) -> Unit,
    onSetAllMonths: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val glass = LocalHesabatColors.current
  var showPassword by remember { mutableStateOf(false) }
  var showDatePicker by remember { mutableStateOf(false) }

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
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Sheet Header
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.primaryContainer,
          ) {
            Icon(
                imageVector = if (state.isEditMode) Icons.Filled.Edit else Icons.Filled.PersonAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(10.dp).size(22.dp),
            )
          }
          Text(
              text =
                  stringResource(
                      if (state.isEditMode) R.string.form_title_edit else R.string.form_title_new
                  ),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
          )
        }

        if (state.isEditMode) {
          Surface(
              shape = RoundedCornerShape(999.dp),
              color = glass.editBg,
              border = BorderStroke(1.dp, glass.editBorder),
          ) {
            Text(
                text = stringResource(R.string.form_badge_editing),
                style = MaterialTheme.typography.labelSmall,
                color = glass.editText,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
          }
        }
      }

      if (state.errorMessage != null) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = glass.unpaidBg,
            border = BorderStroke(1.dp, glass.unpaidBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
              text = state.errorMessage,
              style = MaterialTheme.typography.bodySmall,
              color = glass.unpaidText,
              modifier = Modifier.padding(12.dp),
          )
        }
      }

      // Form Fields
      OutlinedTextField(
          value = state.name,
          onValueChange = { value -> onFieldsChange { it.copy(name = value) } },
          label = { Text(stringResource(R.string.field_name_label)) },
          placeholder = { Text(stringResource(R.string.field_name_hint)) },
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().testTag("editor_name_input"),
      )

      OutlinedTextField(
          value = state.accountName,
          onValueChange = { value -> onFieldsChange { it.copy(accountName = value) } },
          label = { Text(stringResource(R.string.field_account_label)) },
          placeholder = { Text(stringResource(R.string.field_account_hint)) },
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().testTag("editor_account_input"),
      )

      OutlinedTextField(
          value = state.password,
          onValueChange = { value -> onFieldsChange { it.copy(password = value) } },
          label = { Text(stringResource(R.string.field_password_label)) },
          placeholder = { Text(stringResource(R.string.field_password_hint)) },
          singleLine = true,
          visualTransformation =
              if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
          trailingIcon = {
            IconButton(
                onClick = { showPassword = !showPassword },
                modifier = Modifier.size(48.dp),
            ) {
              Icon(
                  imageVector =
                      if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                  contentDescription = stringResource(R.string.cd_toggle_password),
              )
            }
          },
          keyboardOptions =
              KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().testTag("editor_password_input"),
      )

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = state.flowAmount,
            onValueChange = { value -> onFieldsChange { it.copy(flowAmount = value) } },
            label = { Text(stringResource(R.string.field_flow_label)) },
            placeholder = { Text(stringResource(R.string.field_flow_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f).testTag("editor_flow_input"),
        )

        // Open Date Picker Trigger Field
        Box(modifier = Modifier.weight(1f)) {
          OutlinedTextField(
              value = state.openDate,
              onValueChange = {},
              readOnly = true,
              label = { Text(stringResource(R.string.field_open_date_label)) },
              placeholder = { Text(stringResource(R.string.field_open_date_hint)) },
              trailingIcon = {
                if (state.openDate.isNotEmpty()) {
                  IconButton(
                      onClick = { onFieldsChange { it.copy(openDate = "") } },
                      modifier = Modifier.size(40.dp),
                  ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(R.string.clear_all_months),
                        modifier = Modifier.size(18.dp),
                    )
                  }
                } else {
                  Icon(
                      imageVector = Icons.Filled.CalendarMonth,
                      contentDescription = stringResource(R.string.cd_pick_date),
                  )
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.fillMaxWidth(),
          )
          Box(
              modifier =
                  Modifier.matchParentSize()
                      .padding(end = if (state.openDate.isNotEmpty()) 44.dp else 0.dp)
                      .clickable { showDatePicker = true }
                      .testTag("editor_date_picker_button")
          )
        }
      }

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = state.streetName,
            onValueChange = { value -> onFieldsChange { it.copy(streetName = value) } },
            label = { Text(stringResource(R.string.field_street_label)) },
            placeholder = { Text(stringResource(R.string.field_street_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f).testTag("editor_street_input"),
        )

        OutlinedTextField(
            value = state.houseNumber,
            onValueChange = { value -> onFieldsChange { it.copy(houseNumber = value) } },
            label = { Text(stringResource(R.string.field_house_label)) },
            placeholder = { Text(stringResource(R.string.field_house_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f).testTag("editor_house_input"),
        )
      }

      OutlinedTextField(
          value = state.notes,
          onValueChange = { value -> onFieldsChange { it.copy(notes = value) } },
          label = { Text(stringResource(R.string.field_notes_label)) },
          placeholder = { Text(stringResource(R.string.field_notes_hint)) },
          maxLines = 3,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().testTag("editor_notes_input"),
      )

      // 12-Month Checkboxes with Select All / Clear All
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
              text = stringResource(R.string.months_grid_prompt),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.weight(1f),
          )
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = { onSetAllMonths(true) }) {
              Text(
                  text = stringResource(R.string.select_all_months),
                  style = MaterialTheme.typography.labelSmall,
              )
            }
            TextButton(onClick = { onSetAllMonths(false) }) {
              Text(
                  text = stringResource(R.string.clear_all_months),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.error,
              )
            }
          }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
          val cols = if (maxWidth > 480.dp) 4 else 3
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (rowStart in 0 until 12 step cols) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                for (offset in 0 until cols) {
                  val idx = rowStart + offset
                  if (idx < 12) {
                    val checked = state.months.getOrElse(idx) { false }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color =
                            if (checked) glass.paidBg
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border =
                            BorderStroke(
                                1.dp,
                                if (checked) glass.paidBorder
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            ),
                        modifier =
                            Modifier.weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onToggleMonth(idx) }
                                .testTag("editor_month_$idx"),
                    ) {
                      Row(
                          modifier = Modifier.padding(horizontal = 8.dp),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.SpaceBetween,
                      ) {
                        Text(
                            text = stringResource(R.string.month_label_format, idx + 1),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color =
                                if (checked) glass.paidText
                                else MaterialTheme.colorScheme.onSurface,
                        )
                        Checkbox(
                            checked = checked,
                            onCheckedChange = null,
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

      Spacer(modifier = Modifier.height(4.dp))

      // Action Buttons
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f).height(52.dp).testTag("editor_save_button"),
            shape = RoundedCornerShape(16.dp),
        ) {
          Icon(
              imageVector = Icons.Filled.Check,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
              text =
                  stringResource(
                      if (state.isEditMode) R.string.btn_save_edit else R.string.btn_save_new
                  ),
              style = MaterialTheme.typography.labelLarge,
          )
        }

        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.height(52.dp).testTag("editor_cancel_button"),
            shape = RoundedCornerShape(16.dp),
            colors =
                ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
        ) {
          Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = stringResource(R.string.btn_cancel))
        }
      }
    }
  }

  if (showDatePicker) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        confirmButton = {
          TextButton(
              onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                  val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                  formatter.timeZone = TimeZone.getTimeZone("UTC")
                  val formatted = formatter.format(Date(millis))
                  onFieldsChange { it.copy(openDate = formatted) }
                }
                showDatePicker = false
              }
          ) {
            Text(stringResource(R.string.btn_confirm))
          }
        },
        dismissButton = {
          TextButton(onClick = { showDatePicker = false }) {
            Text(stringResource(R.string.btn_cancel))
          }
        },
    ) {
      DatePicker(state = datePickerState)
    }
  }
}
