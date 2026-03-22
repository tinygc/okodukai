package com.tinygc.okodukai.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tinygc.okodukai.domain.util.DateTimeUtil

/**
 * 日付選択用の共通コンポーネント。
 * タップでMaterial3 DatePickerDialogを開き、選択後にyyyy-MM-dd形式で返す。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    dateValue: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "日付",
    enabled: Boolean = true
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val initialMillis = remember(dateValue) { DateTimeUtil.dateStringToMillis(dateValue) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    // 外部からdateValueが変更された場合にDatePickerStateを同期
    LaunchedEffect(dateValue) {
        val millis = DateTimeUtil.dateStringToMillis(dateValue)
        if (datePickerState.selectedDateMillis != millis) {
            datePickerState.selectedDateMillis = millis
        }
    }

    OutlinedTextField(
        value = dateValue,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = "カレンダーを開く",
                modifier = Modifier.clickable(enabled = enabled) { showDatePicker = true },
                tint = MaterialTheme.colorScheme.primary
            )
        },
        singleLine = true,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { showDatePicker = true }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(DateTimeUtil.millisToDateString(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("キャンセル")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
