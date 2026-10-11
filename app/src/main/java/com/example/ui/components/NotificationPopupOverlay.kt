package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.NotificationPopup

@Composable
fun NotificationPopupOverlay(
    popup: NotificationPopup,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 18.dp, start = 14.dp, end = 14.dp)
            .testTag("notification_popup_container"),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            when (popup) {
                is NotificationPopup.Achievement -> {
                    AchievementPopupCard(
                        popup = popup,
                        onDismiss = onDismiss
                    )
                }
                is NotificationPopup.Fusion -> {
                    FusionPopupCard(
                        popup = popup,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementPopupCard(
    popup: NotificationPopup.Achievement,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onDismiss() }
            .border(
                2.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFFFBBF24), Color(0xFFF59E0B), Color(0xFFD97706))
                ),
                RoundedCornerShape(16.dp)
            )
            .testTag("achievement_popup"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A),
        shadowElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0x33F59E0B),
                            Color(0x110F172A)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Trophy badge
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color(0x33F59E0B),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFBBF24))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(popup.icon, fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFD97706)
                        ) {
                            Text(
                                "CONQUISTA DESBLOQUEADA!",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = popup.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFDE047),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = popup.description,
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar notificação",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FusionPopupCard(
    popup: NotificationPopup.Fusion,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onDismiss() }
            .border(
                2.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFFC084FC), Color(0xFF818CF8), Color(0xFF38BDF8))
                ),
                RoundedCornerShape(16.dp)
            )
            .testTag("fusion_popup"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A),
        shadowElevation = 14.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0x446366F1),
                            Color(0x220284C7),
                            Color(0x110F172A)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fusion icon badge
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x444338CA),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFA78BFA))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(popup.icon, fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF7C3AED)
                        ) {
                            Text(
                                "⚡ FUSÃO LENDÁRIA REALIZADA!",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = popup.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFDE047),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Receita: ${popup.recipe}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFBAE6FD),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = popup.description,
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar notificação",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
