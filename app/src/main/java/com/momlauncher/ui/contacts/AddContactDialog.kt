package com.momlauncher.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.momlauncher.ui.theme.*

@Composable
fun AddContactDialog(
    initialNumber: String = "",
    onDismiss: () -> Unit,
    onSave: (name: String, number: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf(initialNumber) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(PureCardWhite)
                .border(2.5.dp, TurkishEmerald, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Başlık
                Text(
                    text = "REHBERE EKLE",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = TurkishEmerald,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "İsim ve telefon numarasını yazın",
                    fontSize = 16.sp,
                    color = WarmMutedText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // İsim Alanı
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Kişinin İsmi", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                    placeholder = { Text("Örn: Ahmet Bey", fontSize = 16.sp, color = WarmMutedText) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TurkishEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoalText
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TurkishEmerald,
                        unfocusedBorderColor = WarmSandBorder,
                        focusedLabelColor = TurkishEmerald,
                        unfocusedLabelColor = WarmMutedText,
                        cursorColor = TurkishEmerald
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Telefon Numarası Alanı
                OutlinedTextField(
                    value = number,
                    onValueChange = {
                        number = it
                        errorMessage = null
                    },
                    label = { Text("Telefon Numarası", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                    placeholder = { Text("05XX XXX XX XX", fontSize = 16.sp, color = WarmMutedText) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = TurkishEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoalText
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TurkishEmerald,
                        unfocusedBorderColor = WarmSandBorder,
                        focusedLabelColor = TurkishEmerald,
                        unfocusedLabelColor = WarmMutedText,
                        cursorColor = TurkishEmerald
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TurkishBordeaux,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Butonlar: Kaydet ve Vazgeç
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Vazgeç Butonu
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SoftCreamSurface)
                            .border(1.5.dp, WarmSandBorder, RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismiss
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VAZGEÇ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoalText
                        )
                    }

                    // Kaydet Butonu
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(TurkishEmerald)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    val cleanName = name.trim()
                                    val cleanNum = number.trim()
                                    if (cleanName.isBlank()) {
                                        errorMessage = "Lütfen kişinin ismini yazın"
                                    } else if (cleanNum.isBlank()) {
                                        errorMessage = "Lütfen telefon numarasını yazın"
                                    } else {
                                        onSave(cleanName, cleanNum)
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "KAYDET",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
