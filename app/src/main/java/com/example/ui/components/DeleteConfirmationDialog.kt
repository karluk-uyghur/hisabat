package com.example.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AccountRecordEntity

@Composable
fun DeleteConfirmationDialog(
    record: AccountRecordEntity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      shape = RoundedCornerShape(24.dp),
      icon = {
        Icon(
            imageVector = Icons.Filled.WarningAmber,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
        )
      },
      title = {
        Text(
            text = stringResource(R.string.delete_dialog_title),
            style = MaterialTheme.typography.titleLarge,
        )
      },
      text = {
        Text(
            text = stringResource(R.string.delete_dialog_message, record.name),
            style = MaterialTheme.typography.bodyMedium,
        )
      },
      confirmButton = {
        Button(
            onClick = onConfirm,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            modifier = Modifier.testTag("confirm_delete_button"),
        ) {
          Text(text = stringResource(R.string.btn_delete))
        }
      },
      dismissButton = {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("cancel_delete_button"),
        ) {
          Text(text = stringResource(R.string.btn_cancel))
        }
      },
  )
}
