package com.light.encode.demo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.architecture.utils.LogUtils
import com.hjq.toast.ToastUtils

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        LogUtils.e("BootReceiver", "接收到开机广播, action:${intent?.action}")
        ToastUtils.show("接收到开机广播, action:${intent?.action}")
    }

}