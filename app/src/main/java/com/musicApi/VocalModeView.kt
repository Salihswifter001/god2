package com.musicApi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Vokal modu görünümü - MusicCreatorScreen'den ayrıştırılmış
 */
@Composable
fun VocalModeView(
    isVocalModeActive: Boolean,
    vocalInput: String,
    onVocalModeToggle: () -> Unit,
    onVocalInputChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Switch ve Başlık
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vokal İçersin",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            
            Switch(
                checked = isVocalModeActive,
                onCheckedChange = { onVocalModeToggle() }
            )
        }
        
        // Vokal modu açıksa içerik göster
        if (isVocalModeActive) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                // Şarkı sözleri giriş alanı
                TextField(
                    value = vocalInput,
                    onValueChange = { onVocalInputChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Şarkı sözlerini buraya yazın") }
                )
                
                // Şarkı yapısı başlığı
                Text(
                    text = "Şarkı Yapısı Ekle:",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                // BUTONLAR - İki satır halinde düzenlenmiş
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // İlk satır - VERSE ve PRE-CHORUS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onVocalInputChange(vocalInput + "\n\n[VERSE]\n") },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Blue
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("VERSE", color = Color.White, fontSize = 12.sp)
                        }
                        
                        Button(
                            onClick = { onVocalInputChange(vocalInput + "\n\n[PRE-CHORUS]\n") },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9C27B0)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("PRE-CHORUS", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    
                    // İkinci satır - CHORUS ve BRIDGE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onVocalInputChange(vocalInput + "\n\n[CHORUS]\n") },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Magenta
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("CHORUS", color = Color.White, fontSize = 12.sp)
                        }
                        
                        Button(
                            onClick = { onVocalInputChange(vocalInput + "\n\n[BRIDGE]\n") },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("BRIDGE", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // Vokal modu kapalı durumda bilgi
            Text(
                text = "Şarkı sözü eklemek için vokal modunu açın",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
} 