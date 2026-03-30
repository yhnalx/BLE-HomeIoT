package com.example.bluetooth_iot.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.outlined.Curtains
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HeaderSection(isConnected: Boolean, onBluetoothClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Home IOT", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF444444))
            Text(
                if (isConnected) "● Connected" else "○ Not Connected",
                color = if (isConnected) Color(0xFF2E7D32) else Color(0xFF777777),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        NeomorphicIconButton(
            icon = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
            onClick = onBluetoothClick,
            isPressed = isConnected
        )
    }
}

@Composable
fun NeomorphicIconCard(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(if (LocalConfiguration.current.screenWidthDp >= 600) 1.5f else 1f)
            .neomorphicShadow(elevation = 8.dp, cornerRadius = 24.dp)
            .background(Color(0xFFE0E5EC), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF444444))
        }
    }
}

@Composable
fun NeomorphicCurtainCard(
    isConnected: Boolean,
    onOpen: (String) -> Unit,
    onClose: (String) -> Unit
) {
    var seconds by remember { mutableStateOf("5") }
    var isExpanded by remember { mutableStateOf(false) }
    
    val statusColor by animateColorAsState(
        if (isConnected) Color(0xFF2E7D32) else Color(0xFF777777)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicShadow(elevation = if (isExpanded && isConnected) 4.dp else 10.dp, cornerRadius = 32.dp)
            .background(Color(0xFFE0E5EC), RoundedCornerShape(32.dp))
            .clickable { if (isConnected) isExpanded = !isExpanded }
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .neomorphicShadow(elevation = 2.dp, cornerRadius = 28.dp, isPressed = true)
                    .background(Color(0xFFE0E5EC), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Curtains, 
                    null, 
                    tint = if (isConnected) Color(0xFF1A73E8) else Color(0xFF777777),
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Column(modifier = Modifier.padding(start = 20.dp).weight(1f)) {
                Text("Bedroom Curtain", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF444444))
                Text(
                    if (isConnected) (if (isExpanded) "Active" else "Connected") else "Not Connected",
                    fontSize = 14.sp,
                    color = statusColor
                )
            }
            
            Box(
                modifier = Modifier
                    .size(width = 50.dp, height = 28.dp)
                    .neomorphicShadow(elevation = 2.dp, cornerRadius = 14.dp, isPressed = true)
                    .background(
                        if (isExpanded && isConnected) Color(0xFF1A73E8).copy(alpha = 0.2f) 
                        else Color(0xFFD1D9E6).copy(alpha = 0.5f), 
                        RoundedCornerShape(14.dp)
                    )
                    .clickable(enabled = isConnected) { isExpanded = !isExpanded },
                contentAlignment = if (isExpanded && isConnected) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(24.dp)
                        .neomorphicShadow(elevation = 3.dp, cornerRadius = 12.dp)
                        .background(
                            if (isExpanded && isConnected) Color(0xFF1A73E8) else Color(0xFFF1F3F4), 
                            CircleShape
                        )
                )
            }
        }

        AnimatedVisibility(visible = isExpanded && isConnected) {
            Column(modifier = Modifier.padding(top = 32.dp)) {
                Text("Run Duration (seconds)", fontWeight = FontWeight.Bold, color = Color(0xFF444444), fontSize = 14.sp)
                
                TextField(
                    value = seconds,
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 2) seconds = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .neomorphicShadow(elevation = 2.dp, cornerRadius = 12.dp, isPressed = true)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE0E5EC),
                        unfocusedContainerColor = Color(0xFFE0E5EC),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("sec") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.height(64.dp)) {
                    NeomorphicActionButton("OPEN", Modifier.weight(1f), Color(0xFF2E7D32)) { onOpen(seconds) }
                    NeomorphicActionButton("CLOSE", Modifier.weight(1f), Color(0xFFC62828)) { onClose(seconds) }
                }
            }
        }
    }
}

@Composable
fun NeomorphicActionButton(text: String, modifier: Modifier = Modifier, color: Color, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .neomorphicShadow(elevation = if (isPressed) 2.dp else 6.dp, cornerRadius = 16.dp, isPressed = isPressed)
            .background(Color(0xFFE0E5EC), RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontWeight = FontWeight.Black, color = color, fontSize = 14.sp, letterSpacing = 1.sp)
    }
}

@Composable
fun NeomorphicIconButton(icon: ImageVector, isPressed: Boolean = false, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f)
    Box(
        modifier = Modifier
            .size(52.dp)
            .scale(scale)
            .neomorphicShadow(elevation = if (isPressed) 2.dp else 8.dp, cornerRadius = 26.dp, isPressed = isPressed)
            .background(Color(0xFFE0E5EC), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = if (isPressed) Color(0xFF1A73E8) else Color(0xFF444444))
    }
}

@SuppressLint("ModifierFactoryUnreferencedReceiver")
fun Modifier.neomorphicShadow(
    elevation: Dp = 8.dp,
    cornerRadius: Dp = 24.dp,
    isPressed: Boolean = false
): Modifier = this.drawBehind {
    val shadowColor = Color.Black.copy(alpha = 0.15f).toArgb()
    val lightColor = Color.White.toArgb()
    val offset = elevation.toPx()
    
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint()
        
        if (!isPressed) {
            paint.color = android.graphics.Color.TRANSPARENT
            paint.setShadowLayer(offset, offset, offset, shadowColor)
            canvas.nativeCanvas.drawRoundRect(
                0f, 0f, size.width, size.height,
                cornerRadius.toPx(), cornerRadius.toPx(), paint
            )
            
            paint.setShadowLayer(offset, -offset / 2, -offset / 2, lightColor)
            canvas.nativeCanvas.drawRoundRect(
                0f, 0f, size.width, size.height,
                cornerRadius.toPx(), cornerRadius.toPx(), paint
            )
        } else {
            paint.color = android.graphics.Color.TRANSPARENT
            paint.setShadowLayer(offset / 2, offset / 4, offset / 4, shadowColor)
            canvas.nativeCanvas.drawRoundRect(
                0f, 0f, size.width, size.height,
                cornerRadius.toPx(), cornerRadius.toPx(), paint
            )
        }
    }
}
