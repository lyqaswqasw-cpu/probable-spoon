package com.example.ui.publisher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.model.Channel
import com.example.data.model.ChannelBranch
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelServer
import com.example.data.security.AdminAuthManager
import com.example.ui.player.EmbedWebViewPlayer
import com.example.ui.player.extractEmbedUrl
import com.example.ui.theme.BrightLavender
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkPurpleContainer
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividViolet
import java.util.UUID

private val COLOR_PALETTE = listOf(
    "#FFFFFF" to "أبيض",
    "#FFD700" to "ذهبي",
    "#00E676" to "أخضر",
    "#00E5FF" to "سماوي",
    "#BB86FC" to "بنفسجي",
    "#FF5252" to "أحمر"
)

private const val SAMPLE_IFRAME_CODE =
    """<iframe src="https://912acsss8af382.yasirtv.com/playervv5.php?match=4828481&key=9f39972b67d6ce22189507d008acwc26" width="640" height="360" frameborder="0" allowfullscreen scrolling="no"></iframe>"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeChannelPublisherTab(
    editingChannel: Channel? = null,
    categories: List<ChannelCategory> = emptyList(),
    branches: List<ChannelBranch> = emptyList(),
    developerPassword: String = "2812246216200810",
    onSaveChannel: (Channel, Boolean) -> Unit,
    onCancelEdit: () -> Unit = {}
) {
    var channelName by remember { mutableStateOf(editingChannel?.name ?: "") }
    var channelImageUrl by remember { mutableStateOf(editingChannel?.imageUrl ?: "") }
    var category by remember { mutableStateOf(editingChannel?.category ?: if (categories.isNotEmpty()) categories.first().name else "بث مباشر") }
    var branchId by remember { mutableStateOf(editingChannel?.branchId ?: "") }
    var branchName by remember { mutableStateOf(editingChannel?.branchName ?: "") }

    var embedCode by remember {
        mutableStateOf(
            editingChannel?.embedCode?.ifBlank {
                editingChannel.servers.firstOrNull { it.streamType == "embed" }?.streamUrl ?: ""
            } ?: ""
        )
    }

    var strictAdBlock by remember { mutableStateOf(editingChannel?.strictAdBlock ?: true) }

    // Text & Image editor options
    var topText by remember { mutableStateOf(editingChannel?.topText ?: "") }
    var topTextSize by remember { mutableIntStateOf(editingChannel?.topTextSize ?: 16) }
    var topTextColor by remember { mutableStateOf(editingChannel?.topTextColor ?: "#FFFFFF") }
    var topImageUrl by remember { mutableStateOf(editingChannel?.topImageUrl ?: "") }

    var bottomText by remember { mutableStateOf(editingChannel?.bottomText ?: "") }
    var bottomTextSize by remember { mutableIntStateOf(editingChannel?.bottomTextSize ?: 14) }
    var bottomTextColor by remember { mutableStateOf(editingChannel?.bottomTextColor ?: "#CCCCCC") }
    var bottomImageUrl by remember { mutableStateOf(editingChannel?.bottomImageUrl ?: "") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var branchDropdownExpanded by remember { mutableStateOf(false) }
    var showLivePreviewPlayer by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Firebase Auth admin check
    val currentAdminUser by AdminAuthManager.currentAdmin.collectAsState()
    val isAdminAuth = (currentAdminUser != null)

    LaunchedEffect(editingChannel) {
        if (editingChannel != null) {
            channelName = editingChannel.name
            channelImageUrl = editingChannel.imageUrl
            category = editingChannel.category
            branchId = editingChannel.branchId
            branchName = editingChannel.branchName
            embedCode = editingChannel.embedCode.ifBlank {
                editingChannel.servers.firstOrNull()?.streamUrl ?: ""
            }
            strictAdBlock = editingChannel.strictAdBlock
            topText = editingChannel.topText
            topTextSize = editingChannel.topTextSize
            topTextColor = editingChannel.topTextColor
            topImageUrl = editingChannel.topImageUrl
            bottomText = editingChannel.bottomText
            bottomTextSize = editingChannel.bottomTextSize
            bottomTextColor = editingChannel.bottomTextColor
            bottomImageUrl = editingChannel.bottomImageUrl
        }
    }

    val extractedUrl = remember(embedCode) { extractEmbedUrl(embedCode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card with glowing badge
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(ElectricPurple.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (editingChannel != null) "تعديل قناة كود / محرر المشغل" else "نشر قناة كود ومحرر المشغل الاحترافي 🎬",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "إضافة قنوات بث عبر كود التضمين Iframe مع تخصيص نصوص وصور المشغل ومانع إعلانات تليجرام الصارم",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Section 1: Basic Channel Info
        Text(
            text = "1. بيانات القناة الأساسية",
            color = BrightLavender,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = channelName,
            onValueChange = { channelName = it; validationError = null },
            label = { Text("اسم القناة (مثال: beIN Sports 1 HD كود)") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonPurple,
                unfocusedBorderColor = DarkSurfaceBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("code_channel_name_input")
        )

        OutlinedTextField(
            value = channelImageUrl,
            onValueChange = { channelImageUrl = it },
            label = { Text("رابط شعار / صورة القناة (اختياري)") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonPurple,
                unfocusedBorderColor = DarkSurfaceBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("code_channel_image_input")
        )

        // Category & Branch Dropdowns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    readOnly = false,
                    label = { Text("التصنيف") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name, color = TextPrimary) },
                            onClick = {
                                category = cat.name
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Branch Dropdown
            if (branches.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = branchDropdownExpanded,
                    onExpandedChange = { branchDropdownExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = branchName.ifBlank { "الفرع (اختياري)" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الفرع") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = branchDropdownExpanded,
                        onDismissRequest = { branchDropdownExpanded = false },
                        modifier = Modifier.background(DarkSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("بدون فرع", color = TextSecondary) },
                            onClick = {
                                branchId = ""
                                branchName = ""
                                branchDropdownExpanded = false
                            }
                        )
                        branches.forEach { br ->
                            DropdownMenuItem(
                                text = { Text(br.name, color = TextPrimary) },
                                onClick = {
                                    branchId = br.id
                                    branchName = br.name
                                    branchDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Embed Code / Iframe Input
        Text(
            text = "2. كود التضمين أو رابط البث (Iframe Embed Code)",
            color = BrightLavender,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = embedCode,
                    onValueChange = { embedCode = it; validationError = null },
                    label = { Text("الصق كود الـ iframe أو رابط البث المباشر هنا") },
                    placeholder = { Text("<iframe src=\"https://...\" width=\"640\" height=\"360\" ...></iframe>") },
                    minLines = 3,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("embed_code_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            embedCode = SAMPLE_IFRAME_CODE
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple),
                        border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("📋 وضع كود تجريبي (YasirTV Iframe)", fontSize = 11.sp)
                    }

                    if (embedCode.isNotBlank()) {
                        IconButton(onClick = { embedCode = "" }) {
                            Text("مسح", color = LiveRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (extractedUrl.isNotBlank() && extractedUrl.startsWith("http")) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElectricPurple.copy(alpha = 0.15f),
                        border = BorderStroke(0.7.dp, ElectricPurple.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "الرابط المستخرج: $extractedUrl",
                                color = TextPrimary,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Strict AdBlock Toggle
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            border = BorderStroke(1.dp, if (strictAdBlock) SuccessGreen.copy(alpha = 0.6f) else DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = if (strictAdBlock) SuccessGreen else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "مانع الإعلانات الصارم وحظر إعلانات التليجرام 🛡️",
                            color = TextPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "يحظر النوافذ المنبثقة، الإعلانات المزعجة، وبانرات الانضمام لقنوات تليجرام تلقائياً داخل المشغل",
                            color = TextSecondary,
                            fontSize = 10.5.sp
                        )
                    }
                }

                Switch(
                    checked = strictAdBlock,
                    onCheckedChange = { strictAdBlock = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SuccessGreen,
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = DarkSurfaceBorder
                    ),
                    modifier = Modifier.testTag("strict_adblock_switch")
                )
            }
        }

        // Section 4: Interactive Player Customizer / Editor
        Text(
            text = "3. محرر المشغل الاحترافي (إضافة نصوص وصور وتعديل الحجم)",
            color = BrightLavender,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Text & Size
                Text(
                    text = "نص أعلى المشغل (Top Text):",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = topText,
                    onValueChange = { topText = it },
                    label = { Text("اكتب نص أعلى المشغل (مثال: بث مباشر بجودة عالية 🔴)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_top_text_input")
                )

                // Top Text Size Control & Color selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("حجم الخط:", color = TextSecondary, fontSize = 11.5.sp)
                        IconButton(
                            onClick = { if (topTextSize > 10) topTextSize -= 2 },
                            modifier = Modifier
                                .size(32.dp)
                                .background(DarkSurface, CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "تصغير", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "${topTextSize}sp",
                            color = BrightLavender,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { if (topTextSize < 32) topTextSize += 2 },
                            modifier = Modifier
                                .size(32.dp)
                                .background(DarkSurface, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "تكبير", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Color Picker
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        COLOR_PALETTE.forEach { (hex, _) ->
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (topTextColor == hex) 2.dp else 0.dp,
                                        color = if (topTextColor == hex) NeonPurple else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { topTextColor = hex }
                            )
                        }
                    }
                }

                // Top Image / Banner URL
                OutlinedTextField(
                    value = topImageUrl,
                    onValueChange = { topImageUrl = it },
                    label = { Text("رابط صورة/بانر أعلى المشغل (اختياري)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Text & Size
                Text(
                    text = "نص أسفل المشغل (Bottom Text):",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = bottomText,
                    onValueChange = { bottomText = it },
                    label = { Text("اكتب نص أسفل المشغل (مثال: نتمنى لكم مشاهدة ممتعة ⚡)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_bottom_text_input")
                )

                // Bottom Text Size Control & Color selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("حجم الخط:", color = TextSecondary, fontSize = 11.5.sp)
                        IconButton(
                            onClick = { if (bottomTextSize > 10) bottomTextSize -= 2 },
                            modifier = Modifier
                                .size(32.dp)
                                .background(DarkSurface, CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "تصغير", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "${bottomTextSize}sp",
                            color = BrightLavender,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { if (bottomTextSize < 32) bottomTextSize += 2 },
                            modifier = Modifier
                                .size(32.dp)
                                .background(DarkSurface, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "تكبير", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Color Picker
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        COLOR_PALETTE.forEach { (hex, _) ->
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (bottomTextColor == hex) 2.dp else 0.dp,
                                        color = if (bottomTextColor == hex) NeonPurple else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { bottomTextColor = hex }
                            )
                        }
                    }
                }

                // Bottom Image / Banner URL
                OutlinedTextField(
                    value = bottomImageUrl,
                    onValueChange = { bottomImageUrl = it },
                    label = { Text("رابط صورة/بانر أسفل المشغل (اختياري)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Section 5: Interactive Live Player Preview Box (معاينة حية للمشغل في المحرر)
        Text(
            text = "4. المعاينة الحية للمشغل والتخصيص",
            color = BrightLavender,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            border = BorderStroke(1.5.dp, ElectricPurple.copy(alpha = 0.8f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Banner preview if present
                if (topImageUrl.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = topImageUrl,
                        contentDescription = "بانر أعلى المشغل",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                // Top Text preview if present
                if (topText.isNotBlank()) {
                    val parsedTopColor = try {
                        Color(android.graphics.Color.parseColor(topTextColor))
                    } catch (_: Exception) {
                        Color.White
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurface.copy(alpha = 0.85f),
                        border = BorderStroke(0.8.dp, NeonPurple.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = topText,
                                color = parsedTopColor,
                                fontSize = topTextSize.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(
                                    onClick = { if (topTextSize > 10) topTextSize -= 2 },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                                IconButton(
                                    onClick = { if (topTextSize < 32) topTextSize += 2 },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                // Player Box Screen Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (showLivePreviewPlayer && extractedUrl.isNotBlank()) {
                        EmbedWebViewPlayer(
                            embedCode = embedCode,
                            streamUrl = extractedUrl,
                            strictAdBlock = strictAdBlock,
                            channelName = channelName,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(ElectricPurple, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = channelName.ifBlank { "شاشة المشغل المباشر" },
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (extractedUrl.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { showLivePreviewPlayer = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple),
                                    border = BorderStroke(1.dp, NeonPurple),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("تشغيل المعاينة الحية الآن ▶", fontSize = 11.sp)
                                }
                            } else {
                                Text(
                                    text = "يرجى إدخال كود التضمين لمشاهدة البث المباشر",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Bottom Text preview if present
                if (bottomText.isNotBlank()) {
                    val parsedBottomColor = try {
                        Color(android.graphics.Color.parseColor(bottomTextColor))
                    } catch (_: Exception) {
                        Color.LightGray
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurface.copy(alpha = 0.85f),
                        border = BorderStroke(0.8.dp, DarkSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bottomText,
                                color = parsedBottomColor,
                                fontSize = bottomTextSize.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(
                                    onClick = { if (bottomTextSize > 10) bottomTextSize -= 2 },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                                IconButton(
                                    onClick = { if (bottomTextSize < 32) bottomTextSize += 2 },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                // Bottom Banner preview if present
                if (bottomImageUrl.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = bottomImageUrl,
                        contentDescription = "بانر أسفل المشغل",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }

        // Error message
        if (validationError != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = LiveRed.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, LiveRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = validationError ?: "",
                    color = LiveRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Success message
        if (successMessage != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SuccessGreen.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, SuccessGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = successMessage ?: "",
                    color = SuccessGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Publish Action Button
        Button(
            onClick = {
                val trimmedName = channelName.trim()
                if (trimmedName.isBlank()) {
                    validationError = "يرجى كتابة اسم القناة"
                    return@Button
                }
                val trimmedEmbed = embedCode.trim()
                if (trimmedEmbed.isBlank()) {
                    validationError = "يرجى إدخال كود الـ iframe أو رابط البث"
                    return@Button
                }

                val finalUrl = extractEmbedUrl(trimmedEmbed)
                val server = ChannelServer(
                    id = editingChannel?.servers?.firstOrNull()?.id ?: UUID.randomUUID().toString(),
                    name = "سيرفر البث كود",
                    streamUrl = finalUrl,
                    streamType = "embed"
                )

                val newChannel = Channel(
                    id = editingChannel?.id ?: UUID.randomUUID().toString(),
                    name = trimmedName,
                    imageUrl = channelImageUrl.trim(),
                    category = category.trim().ifBlank { "بث مباشر" },
                    branchId = branchId.trim(),
                    branchName = branchName.trim(),
                    servers = listOf(server),
                    createdAt = editingChannel?.createdAt ?: System.currentTimeMillis(),
                    views = editingChannel?.views ?: 0L,
                    isEmbedCode = true,
                    embedCode = trimmedEmbed,
                    strictAdBlock = strictAdBlock,
                    topText = topText.trim(),
                    topTextSize = topTextSize,
                    topTextColor = topTextColor,
                    topImageUrl = topImageUrl.trim(),
                    bottomText = bottomText.trim(),
                    bottomTextSize = bottomTextSize,
                    bottomTextColor = bottomTextColor,
                    bottomImageUrl = bottomImageUrl.trim()
                )

                onSaveChannel(newChannel, editingChannel != null)
                successMessage = if (editingChannel != null) "تم تحديث قناة الكود وتخصيص المشغل بنجاح! 🚀" else "تم نشر قناة الكود بنجاح في Firebase! 🚀"
                validationError = null
                if (editingChannel == null) {
                    channelName = ""
                    channelImageUrl = ""
                    embedCode = ""
                    topText = ""
                    bottomText = ""
                    topImageUrl = ""
                    bottomImageUrl = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = VividViolet),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("publish_code_channel_button")
        ) {
            Icon(
                imageVector = Icons.Default.LiveTv,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (editingChannel != null) "حفظ التعديلات على القناة 💾" else "نشر قناة الكود وحفظ التخصيص في Firebase 🚀",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (editingChannel != null) {
            OutlinedButton(
                onClick = onCancelEdit,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = BorderStroke(1.dp, DarkSurfaceBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إلغاء التعديل")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
