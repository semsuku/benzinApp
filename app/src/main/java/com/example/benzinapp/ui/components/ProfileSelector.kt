package com.example.benzinapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.benzinapp.data.Profile

private val COLOR_OPTIONS = listOf(
    "#1E88E5", // Blue
    "#43A047", // Green
    "#E53935", // Red
    "#FB8C00", // Orange
    "#8E24AA", // Purple
    "#00ACC1", // Cyan
    "#3949AB", // Indigo
    "#D81B60"  // Pink
)

private val ICON_OPTIONS = listOf(
    "car" to Icons.Default.DirectionsCar,
    "moto" to Icons.Default.TwoWheeler,
    "bus" to Icons.Default.DirectionsBus,
    "truck" to Icons.Default.LocalShipping,
    "person" to Icons.Default.Person
)

fun parseHexColor(hex: String, defaultColor: Color = Color(0xFF1E88E5)): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = android.graphics.Color.parseColor("#$cleanHex")
        Color(colorInt)
    } catch (e: Exception) {
        defaultColor
    }
}

fun getProfileIcon(iconName: String): ImageVector {
    return when (iconName) {
        "moto" -> Icons.Default.TwoWheeler
        "bus" -> Icons.Default.DirectionsBus
        "truck" -> Icons.Default.LocalShipping
        "person" -> Icons.Default.Person
        else -> Icons.Default.DirectionsCar
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileSelector(
    profiles: List<Profile>,
    activeProfileId: Long,
    onSelectProfile: (Long) -> Unit,
    onAddProfile: (name: String, vehicle: String, icon: String, color: String) -> Unit,
    onEditProfile: (profile: Profile) -> Unit,
    onDeleteProfile: (profile: Profile) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var profileToEdit by remember { mutableStateOf<Profile?>(null) }
    var profileToDelete by remember { mutableStateOf<Profile?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Profilo Famiglia (${profiles.size}/4)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (profiles.size < 4) {
                TextButton(
                    onClick = { showAddDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aggiungi profilo",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Nuovo Profilo", fontSize = 13.sp)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            profiles.forEach { profile ->
                val isActive = profile.id == activeProfileId
                val color = parseHexColor(profile.colorHex)

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isActive) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        width = if (isActive) 2.dp else 1.dp,
                        color = if (isActive) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                        .combinedClickable(
                            onClick = { onSelectProfile(profile.id) },
                            onLongClick = { profileToEdit = profile }
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getProfileIcon(profile.iconName),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isActive) color else MaterialTheme.colorScheme.onSurface
                            )
                            if (profile.vehicleName.isNotBlank()) {
                                Text(
                                    text = profile.vehicleName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isActive) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifica profilo",
                                tint = color,
                                modifier = Modifier
                                    .size(16.dp)
                                    .combinedClickable(onClick = { profileToEdit = profile })
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ProfileEditDialog(
            title = "Nuovo Profilo Famiglia",
            initialName = "",
            initialVehicle = "",
            initialIcon = "car",
            initialColor = COLOR_OPTIONS.first(),
            onDismiss = { showAddDialog = false },
            onConfirm = { name, vehicle, icon, color ->
                onAddProfile(name, vehicle, icon, color)
                showAddDialog = false
            }
        )
    }

    profileToEdit?.let { profile ->
        ProfileEditDialog(
            title = "Modifica Profilo",
            initialName = profile.name,
            initialVehicle = profile.vehicleName,
            initialIcon = profile.iconName,
            initialColor = profile.colorHex,
            canDelete = profiles.size > 1,
            onDismiss = { profileToEdit = null },
            onConfirm = { name, vehicle, icon, color ->
                onEditProfile(profile.copy(name = name, vehicleName = vehicle, iconName = icon, colorHex = color))
                profileToEdit = null
            },
            onDelete = {
                profileToDelete = profile
                profileToEdit = null
            }
        )
    }

    profileToDelete?.let { profile ->
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = { Text("Eliminare il profilo ${profile.name}?") },
            text = { Text("Attenzione: l'eliminazione del profilo rimuoverà anche tutti i rifornimenti e le scadenze associate a questo profilo.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProfile(profile)
                        profileToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Elimina Definitivamente")
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text("Annulla")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileEditDialog(
    title: String,
    initialName: String,
    initialVehicle: String,
    initialIcon: String,
    initialColor: String,
    canDelete: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (name: String, vehicle: String, icon: String, color: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialName) }
    var vehicle by remember { mutableStateOf(initialVehicle) }
    var selectedIcon by remember { mutableStateOf(initialIcon) }
    var selectedColor by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Profilo (es. Io, Moglie, Marco)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = vehicle,
                    onValueChange = { vehicle = it },
                    label = { Text("Veicolo (es. Golf GTI, Panda 1.2)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Icona Veicolo:", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ICON_OPTIONS.forEach { (iconKey, vector) ->
                        val isSelected = selectedIcon == iconKey
                        IconButton(
                            onClick = { selectedIcon = iconKey },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    CircleShape
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = vector,
                                contentDescription = iconKey,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }

                Text("Colore Identificativo:", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    COLOR_OPTIONS.forEach { colorHex ->
                        val isSelected = selectedColor.equals(colorHex, ignoreCase = true)
                        val color = parseHexColor(colorHex)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(color, CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                                .combinedClickable(onClick = { selectedColor = colorHex }),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), vehicle.trim(), selectedIcon, selectedColor)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Salva")
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canDelete && onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Elimina",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Annulla")
                }
            }
        }
    )
}
