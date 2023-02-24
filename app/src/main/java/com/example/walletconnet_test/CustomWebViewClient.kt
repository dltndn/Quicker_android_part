package com.example.walletconnet_test

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat.startActivity


//class CustomWebViewClient(val writeLog: (String) -> Unit) : WebViewClient() {
//    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//        if (url.startsWith("intent://")) {
//            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
//            if (intent != null) {
//                val fallbackUrl = intent.getStringExtra("browser_fallback_url")
//                return if (fallbackUrl != null) {
//                    webView.loadUrl(fallbackUrl)
//                    true
//                } else {
//                    false
//                }
//            }
//        }
//        else if (url.startsWith("tel:")){
//            val intent = Intent(Intent.ACTION_DIAL)
//            intent.data = Uri.parse(url)
//            startActivity(intent)
//            return true
//        }
//        else if (url.startsWith("mailto:")) {
//            val intent = Intent(Intent.ACTION_VIEW)
//            val data = Uri.parse(
//                url + Uri.encode("subject") + "&body=" + Uri.encode(
//                    "body"
//                )
//            )
//            intent.data = data
//            startActivity(intent)
//            return true
//        }
//        return false
//
//    }
//}