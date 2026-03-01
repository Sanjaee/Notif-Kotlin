package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpInputField(
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    otpCount: Int = 6
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Perhitungan dimensi responsif
    val screenWidth = configuration.screenWidthDp.dp
    val spacing = 8.dp
    val padding = 32.dp
    val availableWidth = screenWidth - padding - (spacing * (otpCount - 1))
    val fieldSize = (availableWidth / otpCount).coerceIn(48.dp, 64.dp)

    val focusRequesters = remember { List(otpCount) { FocusRequester() } }

    // Auto-focus pada kotak yang kosong saat teks berubah
    LaunchedEffect(otpText) {
        val index = otpText.length.coerceAtMost(otpCount - 1)
        if (index >= 0 && index < focusRequesters.size) {
            focusRequesters[index].requestFocus()
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(otpCount) { index ->
            val char = otpText.getOrNull(index)?.toString() ?: ""
            val textFieldValue = remember(char) {
                TextFieldValue(
                    text = char,
                    selection = TextRange(char.length)
                )
            }

            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    val digits = newValue.text.filter { it.isDigit() }

                    when {
                        // ⬅️ BACKSPACE (Jika kotak kosong dan tekan hapus)
                        digits.isEmpty() -> {
                            if (index < otpText.length) {
                                // Menghapus karakter di posisi tertentu
                                val newOtp = otpText.removeRange(index, index + 1)
                                onOtpTextChange(newOtp)
                            } else if (index > 0) {
                                // Pindah ke kotak sebelumnya jika kotak saat ini sudah kosong
                                focusRequesters[index - 1].requestFocus()
                            }
                        }

                        // 📋 PASTE OTP
                        digits.length > 1 -> {
                            val pasted = digits.take(otpCount)
                            onOtpTextChange(pasted)
                        }

                        // ➡️ INPUT NORMAL
                        else -> {
                            val newOtp = buildString {
                                val currentText = otpText.padEnd(otpCount, ' ')
                                append(currentText.take(index))
                                append(digits)
                                append(currentText.substring(index + 1))
                            }.trimEnd().take(otpCount)

                            onOtpTextChange(newOtp)

                            if (index < otpCount - 1) {
                                focusRequesters[index + 1].requestFocus()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .width(fieldSize)
                    .height(fieldSize) // Menggunakan height tetap agar kotak sempurna
                    .padding(horizontal = 2.dp)
                    .focusRequester(focusRequesters[index]),
                textStyle = TextStyle(
                    fontSize = with(density) {
                        // Perbaikan: Lakukan perbandingan pada Float, bukan pada TextUnit (sp)
                        val calculatedPx = fieldSize.toPx() * 0.4f
                        val minPx = 18.sp.toPx()
                        val maxPx = 28.sp.toPx()
                        calculatedPx.coerceIn(minPx, maxPx).toSp()
                    },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    autoCorrect = false
                ),
                keyboardActions = KeyboardActions(
                    onDone = { /* Handle submit action here */ }
                ),
                singleLine = true,
                enabled = enabled,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}