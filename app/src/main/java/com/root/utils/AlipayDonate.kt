package com.root.utils

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import java.net.URLEncoder

class AlipayDonate(private var context: Context) {

    private val ALIPAY_SHOP = "https://qr.alipay.com/fkx10112bomxshw4ximae9a"//商户
    private val ALIPAY_PERSON = "https://qr.alipay.com/fkx10112bomxshw4ximae9a"//个人(支付宝里面我的二维码)
    private val ALIPAY_PERSON_2_PAY = "https://qr.alipay.com/fkx10112bomxshw4ximae9a"//个人(支付宝里面我的二维码,然后提示让用的收款码)

    fun jumpAlipay() {
        openAliPay2Pay(ALIPAY_SHOP)
    }

    /**
     * 支付
     * @param qrCode
     */
    private fun openAliPay2Pay(qrCode: String) {
        openAlipayPayPage(context, qrCode)
    }

    fun openAlipayPayPage(context: Context, qrcode: String): Boolean {
        var encodeedQrcode = qrcode
        try {
            encodeedQrcode = URLEncoder.encode(qrcode, "utf-8")
        } catch (e: Exception) {
        }
        try {
            val alipayqr = "alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=$encodeedQrcode"
            val url = alipayqr + "%3F_s%3Dweb-other&_t=" + System.currentTimeMillis()
            openUri(context, url)
            return true
        } catch (e: Exception) {
        }
        return false
    }

    /**
     * 发送一个intent
     * @param context
     * @param s
     */
    private fun openUri(context: Context, s: String) {
        val intent = Intent(Intent.ACTION_VIEW, s.toUri())
        context.startActivity(intent)
    }
}