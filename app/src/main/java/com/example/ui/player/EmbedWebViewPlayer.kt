package com.example.ui.player

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SuccessGreen
import java.io.ByteArrayInputStream

/**
 * Strict Ad-Block & Anti-Telegram Script Injected into Embed Players.
 * Removes popups, click-traps, sticky telegram banners, and ads.
 */
private const val STRICT_ADBLOCK_JS = """
(function() {
    // 1. Prevent popups and new tabs
    window.open = function() { console.log('[AdBlock] Blocked window.open'); return null; };
    window.alert = function() {};
    window.confirm = function() { return false; };
    window.prompt = function() { return null; };

    // 2. CSS to aggressively hide Telegram promo banners, popups, and ad overlays
    var style = document.createElement('style');
    style.type = 'text/css';
    style.innerHTML = `
        a[href*="t.me"], a[href*="telegram.me"], a[href*="telegram.org"], a[href*="joinchat"],
        [class*="telegram"], [id*="telegram"], [class*="tg-"], [id*="tg-"],
        [class*="tele_"], [id*="tele_"], .telegram, #telegram,
        .ad, .ads, [class*="ad-"], [id*="ad-"], [class*="banner"], [id*="banner"],
        [class*="popup"], [id*="popup"], [class*="overlay"]:not(video),
        div[style*="z-index: 2147483647"], div[style*="z-index: 999999"], div[style*="z-index: 100000"],
        div[style*="position: fixed"][style*="bottom: 0"]:not(:has(video)),
        div[style*="position: absolute"][style*="z-index"]:has(a[href*="t.me"]),
        iframe[src*="ad"], iframe[src*="telegram"] {
            display: none !important;
            visibility: hidden !important;
            opacity: 0 !important;
            pointer-events: none !important;
            width: 0 !important;
            height: 0 !important;
            max-height: 0 !important;
            z-index: -999 !important;
        }
        body, html {
            margin: 0 !important;
            padding: 0 !important;
            overflow: hidden !important;
            background: #000 !important;
            width: 100% !important;
            height: 100% !important;
        }
        video {
            width: 100% !important;
            height: 100% !important;
            max-width: 100% !important;
            max-height: 100% !important;
            object-fit: contain !important;
            z-index: 10 !important;
        }
    `;
    if (document.head) {
        document.head.appendChild(style);
    } else {
        document.documentElement.appendChild(style);
    }

    // 3. Remove existing Telegram banners & ad elements
    function purgeAdElements() {
        var telegramLinks = document.querySelectorAll('a[href*="t.me"], a[href*="telegram.me"], a[href*="joinchat"]');
        telegramLinks.forEach(function(el) {
            var parent = el.closest('div, section, aside, p') || el;
            parent.remove();
        });

        // Search for Arabic text inviting to Telegram (e.g., انضم لقناة تليجرام)
        var allDivs = document.querySelectorAll('div, span, p, a, b');
        allDivs.forEach(function(el) {
            var txt = (el.innerText || el.textContent || '').trim();
            if (txt.includes('تليجرام') || txt.includes('انضم لقناة') || txt.includes('telegram') || txt.includes('Telegram')) {
                if (!el.querySelector('video')) {
                    el.remove();
                }
            }
        });

        // Remove click-intercepting transparent overlays on top of video
        var overlays = document.querySelectorAll('div[style*="position: absolute"], div[style*="position: fixed"]');
        overlays.forEach(function(div) {
            var z = parseInt(window.getComputedStyle(div).zIndex, 10);
            if (z > 50 && !div.querySelector('video')) {
                div.remove();
            }
        });
    }

    purgeAdElements();
    setInterval(purgeAdElements, 1000);

    // 4. MutationObserver to catch dynamically injected popups/banners
    var observer = new MutationObserver(function(mutations) {
        purgeAdElements();
    });
    observer.observe(document.documentElement, { childList: true, subtree: true });
})();
"""

/**
 * Extracts embed URL from iframe HTML string or returns URL directly.
 */
fun extractEmbedUrl(embedInput: String): String {
    val trimmed = embedInput.trim()
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        return trimmed
    }
    // Match src="..." or src='...' inside iframe
    val srcRegex = """src=["']([^"']+)["']""".toRegex(RegexOption.IGNORE_CASE)
    val match = srcRegex.find(trimmed)
    if (match != null && match.groupValues.size > 1) {
        val extracted = match.groupValues[1].trim()
        if (extracted.startsWith("//")) {
            return "https:$extracted"
        }
        return extracted
    }
    return trimmed
}

/**
 * Builds responsive clean HTML wrapper for embed video/iframe player.
 */
fun buildEmbedHtmlWrapper(urlOrCode: String): String {
    val srcUrl = extractEmbedUrl(urlOrCode)
    val finalSrc = if (srcUrl.startsWith("//")) "https:$srcUrl" else srcUrl
    val escapedSrc = finalSrc.replace("\"", "&quot;")

    return """
        <!DOCTYPE html>
        <html lang="ar" dir="rtl">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                html, body {
                    width: 100%;
                    height: 100%;
                    background-color: #000000;
                    overflow: hidden;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
                .player-container {
                    position: relative;
                    width: 100%;
                    height: 100%;
                    background: #000;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
                iframe {
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    border: none;
                    background: #000;
                }
            </style>
        </head>
        <body>
            <div class="player-container">
                <iframe 
                    src="$escapedSrc" 
                    width="100%" 
                    height="100%" 
                    frameborder="0" 
                    allowfullscreen="true" 
                    allow="autoplay; fullscreen; encrypted-media; picture-in-picture" 
                    scrolling="no">
                </iframe>
            </div>
        </body>
        </html>
    """.trimIndent()
}

/**
 * Known ad networks and telemetry domains to block.
 */
private val BLOCKED_AD_DOMAINS = listOf(
    "t.me",
    "telegram.me",
    "telegram.dog",
    "popads",
    "adsterra",
    "propellerads",
    "1xbet",
    "bet365",
    "melbet",
    "mostbet",
    "googleads",
    "doubleclick",
    "onclick",
    "syndication",
    "traffic",
    "adnxs",
    "exoclick",
    "adclick",
    "yadro.ru",
    "an.yandex.ru"
)

/**
 * High-Performance WebView Embed Player with Strict AdBlock & Anti-Telegram Protection.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EmbedWebViewPlayer(
    embedCode: String,
    streamUrl: String = "",
    strictAdBlock: Boolean = true,
    channelName: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val targetUrl = remember(embedCode, streamUrl) {
        val candidate = embedCode.ifBlank { streamUrl }
        extractEmbedUrl(candidate)
    }

    val htmlContent = remember(embedCode, streamUrl, targetUrl) {
        buildEmbedHtmlWrapper(targetUrl)
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.apply {
                stopLoading()
                loadUrl("about:blank")
                clearHistory()
                destroy()
            }
            webViewInstance = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.BLACK)

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = false
                        displayZoomControls = false
                        allowFileAccess = false
                        allowContentAccess = false
                        cacheMode = WebSettings.LOAD_DEFAULT

                        if (strictAdBlock) {
                            // Block popup windows strictly
                            setSupportMultipleWindows(false)
                            javaScriptCanOpenWindowsAutomatically = false
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: android.os.Message?
                        ): Boolean {
                            // Strictly reject any popup window creation
                            return false
                        }

                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            return true
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                            errorMessage = null
                            if (strictAdBlock && view != null) {
                                view.evaluateJavascript(STRICT_ADBLOCK_JS, null)
                            }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            if (strictAdBlock && view != null) {
                                view.evaluateJavascript(STRICT_ADBLOCK_JS, null)
                            }
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val reqUrl = request?.url?.toString() ?: return false

                            // Strictly block Telegram deep links and web links
                            if (reqUrl.startsWith("tg:") ||
                                reqUrl.startsWith("telegram:") ||
                                reqUrl.startsWith("intent:") ||
                                reqUrl.contains("t.me/") ||
                                reqUrl.contains("telegram.me/") ||
                                reqUrl.contains("joinchat")
                            ) {
                                return true // Block completely
                            }

                            if (strictAdBlock) {
                                // Block ad networks and tracking redirects
                                for (domain in BLOCKED_AD_DOMAINS) {
                                    if (reqUrl.contains(domain, ignoreCase = true)) {
                                        return true // Block redirect
                                    }
                                }
                            }

                            // Keep navigation inside iframe
                            return false
                        }

                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            if (!strictAdBlock) return super.shouldInterceptRequest(view, request)

                            val reqUrl = request?.url?.toString()?.lowercase() ?: return null

                            // Intercept Telegram invite requests and ad scripts
                            for (adPattern in BLOCKED_AD_DOMAINS) {
                                if (reqUrl.contains(adPattern)) {
                                    return WebResourceResponse(
                                        "text/plain",
                                        "UTF-8",
                                        ByteArrayInputStream(ByteArray(0))
                                    )
                                }
                            }

                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            if (failingUrl == targetUrl) {
                                errorMessage = "تعذر تحميل بث القناة: $description"
                                isLoading = false
                            }
                        }
                    }

                    webViewInstance = this
                    loadDataWithBaseURL(targetUrl, htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webViewInstance = webView
                if (reloadKey > 0) {
                    webView.loadDataWithBaseURL(targetUrl, htmlContent, "text/html", "UTF-8", null)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading Indicator
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color = NeonPurple,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "جاري تهيئة مشغل الكود وحظر الإعلانات...",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (strictAdBlock) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "مانع الإعلانات الصارم مفعل 🛡️",
                                color = SuccessGreen,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Error State with Retry
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground.copy(alpha = 0.95f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = LiveRed,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = errorMessage ?: "خطأ في تشغيل القناة",
                        color = Color.White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ElectricPurple,
                        modifier = Modifier
                            .clickable {
                                reloadKey++
                                errorMessage = null
                            }
                            .testTag("retry_embed_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إعادة المحاولة",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
