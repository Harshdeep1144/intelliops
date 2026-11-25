package com.harshdeep.intelliops.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harshdeep.intelliops.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Cohesive Enterprise Workspace Palette matching TasksScreen
private object ProfilePalette {
    val Background = Color(0xFFF4F5F7)      // Neutral Canvas
    val Surface = Color(0xFFFFFFFF)         // Card Surface
    val PrimaryBlue = Color(0xFF0052CC)     // Brand Blue
    val SoftBlueBg = Color(0xFFDEEBFF)      // Pill Background
    val SoftBlueText = Color(0xFF0747A6)    // Pill Ink

    val TextPrimary = Color(0xFF172B4D)     // Dark Navy Ink
    val TextSecondary = Color(0xFF5E6C84)   // Muted Slate
    val TextSubtle = Color(0xFF7A869A)      // Subtle Gray
    val BorderLight = Color(0xFFDFE1E6)     // Hairline Border

    val DoneBg = Color(0xFFE3FCEF)
    val DoneText = Color(0xFF006644)

    val DangerRed = Color(0xFFFF5630)
    val DangerBg = Color(0xFFFFEBE6)
    val DangerBorder = Color(0xFFFFBDAD)
}

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val viewModel: ProfileViewModel = viewModel { ProfileViewModel() }
    val user by viewModel.user.collectAsState()
    val company by viewModel.company.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var copiedField by remember { mutableStateOf<String?>(null) }

    fun copyWithFeedback(label: String, value: String) {
        clipboardManager.setText(AnnotatedString(value))
        copiedField = label
        coroutineScope.launch {
            delay(2000)
            if (copiedField == label) copiedField = null
        }
    }

    Scaffold(
        containerColor = ProfilePalette.Background,
        topBar = {
            Surface(
                color = ProfilePalette.Surface,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Account & Workspace",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ProfilePalette.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage your personal identity and organization details",
                        style = MaterialTheme.typography.bodySmall,
                        color = ProfilePalette.TextSecondary
                    )
                }
            }
        }
    ) { innerPadding ->
        if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ProfilePalette.PrimaryBlue)
            }
        } else {
            val currentUser = user!!
            val resolvedCompanyName = currentUser.companyName.ifEmpty { company?.name ?: "N/A" }
            val resolvedInviteCode = company?.inviteCode ?: currentUser.companyId.take(6).uppercase()

            val initials = remember(currentUser.name) {
                val parts = currentUser.name.trim().split(" ").filter { it.isNotEmpty() }
                when {
                    parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
                    parts.isNotEmpty() -> parts[0].take(2).uppercase()
                    else -> "U"
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. User Identity Header Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ProfilePalette.Surface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ProfilePalette.BorderLight),
                    shadowElevation = 0.5.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Initials Avatar with subtle ring
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(ProfilePalette.PrimaryBlue)
                                    .border(2.dp, ProfilePalette.SoftBlueBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = ProfilePalette.TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = ProfilePalette.TextSubtle,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = currentUser.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ProfilePalette.TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))

                                // Role & Active Status Lozenges
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = ProfilePalette.SoftBlueBg,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VerifiedUser,
                                                contentDescription = null,
                                                tint = ProfilePalette.SoftBlueText,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = currentUser.role.name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = ProfilePalette.SoftBlueText,
                                                letterSpacing = 0.4.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        color = ProfilePalette.DoneBg,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ProfilePalette.DoneText,
                                            letterSpacing = 0.4.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Organization & Workspace Details Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ProfilePalette.Surface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ProfilePalette.BorderLight),
                    shadowElevation = 0.5.dp
                ) {
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        Text(
                            text = "ORGANIZATION & ACCESS",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProfilePalette.TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        WorkspaceDetailRow(
                            icon = Icons.Default.Business,
                            label = "Workspace Name",
                            value = resolvedCompanyName,
                            isMonospace = false
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            color = ProfilePalette.BorderLight.copy(alpha = 0.6f)
                        )

                        WorkspaceDetailRow(
                            icon = Icons.Default.Key,
                            label = "Workspace Invite Code",
                            value = resolvedInviteCode,
                            isMonospace = true,
                            isCopied = copiedField == "invite",
                            onCopy = { copyWithFeedback("invite", resolvedInviteCode) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            color = ProfilePalette.BorderLight.copy(alpha = 0.6f)
                        )

                        WorkspaceDetailRow(
                            icon = Icons.Default.Badge,
                            label = "Organization ID",
                            value = currentUser.companyId,
                            isMonospace = true,
                            isCopied = copiedField == "companyId",
                            onCopy = { copyWithFeedback("companyId", currentUser.companyId) }
                        )
                    }
                }

                // 3. Session / Sign Out Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ProfilePalette.Surface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ProfilePalette.BorderLight),
                    shadowElevation = 0.5.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "SESSION",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProfilePalette.TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sign out of your IntelliOps workspace on this device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfilePalette.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.clearData()
                                viewModel.logout {
                                    onLogout()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, ProfilePalette.DangerBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = ProfilePalette.DangerBg.copy(alpha = 0.55f),
                                contentColor = ProfilePalette.DangerRed
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign Out",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun WorkspaceDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    isMonospace: Boolean = false,
    isCopied: Boolean = false,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(ProfilePalette.SoftBlueBg.copy(alpha = 0.65f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ProfilePalette.PrimaryBlue,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ProfilePalette.TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
                ),
                color = ProfilePalette.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (onCopy != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onCopy() },
                color = if (isCopied) ProfilePalette.DoneBg else ProfilePalette.Background,
                border = BorderStroke(
                    1.dp,
                    if (isCopied) ProfilePalette.DoneText.copy(alpha = 0.3f) else ProfilePalette.BorderLight
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy $label",
                        tint = if (isCopied) ProfilePalette.DoneText else ProfilePalette.TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCopied) "Copied" else "Copy",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCopied) ProfilePalette.DoneText else ProfilePalette.TextSecondary
                    )
                }
            }
        }
    }
}