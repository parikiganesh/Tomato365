package com.parikiganesh.tomato365.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.parikiganesh.tomato365.repository.SelectionOption

@Composable
fun AppDropdownField(
    label: String,
    selectedId: String,
    options: List<SelectionOption>,
    errorMessage: String?,
    onSelected: (String) -> Unit,
    placeholder: String = "",
    optionLeadingIcon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.firstOrNull { it.id == selectedId }?.name.orEmpty()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            readOnly = true,
            enabled = true,
            label = { Text(text = label) },
            placeholder = {
                if (selectedText.isBlank() && placeholder.isNotBlank()) {
                    Text(text = placeholder)
                }
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = true }
                )
            },
            isError = errorMessage != null,
            supportingText = {
                if (errorMessage != null) {
                    Text(text = errorMessage)
                }
            }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(maxWidth)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option.name) },
                    leadingIcon = optionLeadingIcon?.let { icon ->
                        {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    trailingIcon = if (selectedId == option.id) {
                        {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        onSelected(option.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
