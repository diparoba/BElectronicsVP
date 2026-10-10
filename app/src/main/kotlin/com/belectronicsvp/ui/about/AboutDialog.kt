package com.belectronicsvp.ui.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belectronicsvp.ui.theme.DarkBackground
import com.belectronicsvp.ui.theme.DarkSurface
import com.belectronicsvp.ui.theme.DarkSurfaceVariant
import com.belectronicsvp.ui.theme.ElectricCyan
import com.belectronicsvp.ui.theme.NeonGreen
import com.belectronicsvp.ui.theme.TextPrimaryDark
import com.belectronicsvp.ui.theme.TextSecondaryDark

private const val OFFICIAL_REPO_URL = "https://github.com/diparoba/BElectronicsVP"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimaryDark,
        textContentColor = TextSecondaryDark,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(DarkBackground)
                        .border(1.5.dp, ElectricCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "BElectronicsVP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "v1.0.0 • Open Source Edition",
                        fontSize = 12.sp,
                        color = ElectricCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "A modernized, open-source telemetry dashboard app cleanly reverse-engineered from the legendary Keuwlsoft Bluetooth Electronics.",
                    fontSize = 13.sp,
                    color = TextPrimaryDark,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Highlights badges
                Text(
                    text = "Key Improvements",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ElectricCyan
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FeatureBadge("Universal BLE (ESP32-S3/C3)")
                    FeatureBadge("MTU 512 Negotiation")
                    FeatureBadge("Classic SPP Ch 1 Fallback")
                    FeatureBadge("Real-time Name/MAC Search")
                    FeatureBadge("1-Tap Quick Reconnect")
                    FeatureBadge("JSON Panel Sharing")
                    FeatureBadge("100% Jetpack Compose")
                    FeatureBadge("OpenSpec Documented")
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Credits & Maintainer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Author & Maintainer",
                                fontSize = 11.sp,
                                color = TextSecondaryDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "Diego Robalino (@diparoba)",
                            fontSize = 13.sp,
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "Licensed under MIT License • Open for contributions",
                            fontSize = 11.sp,
                            color = TextSecondaryDark,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { openGitHubRepository(context) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = DarkBackground
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "View on GitHub", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = TextSecondaryDark)
            }
        }
    )
}

@Composable
private fun FeatureBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = TextPrimaryDark,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun openGitHubRepository(context: Context) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(OFFICIAL_REPO_URL)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, "GitHub URL: $OFFICIAL_REPO_URL", Toast.LENGTH_LONG).show()
    }
}
