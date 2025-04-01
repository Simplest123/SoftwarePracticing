/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * AlarmInitReceiver 是一个广播接收器，在设备启动时（BOOT_COMPLETED 事件）
 * 重新初始化闹钟，以确保应用内的定时事件不会因重启而丢失。
 */
public class AlarmInitReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmInitReceiver";

    /**
     * 当系统广播到来时，此方法会被调用。
     * @param context 应用的上下文，用于访问系统服务。
     * @param intent  触发此广播的意图。
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 记录日志，表明广播已被接收到
        Log.d(TAG, "Received broadcast: " + intent.getAction());

        // 如果广播的 action 是 "android.intent.action.BOOT_COMPLETED"，表示设备刚启动
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // 重新初始化闹钟，确保应用中的定时任务不会因设备重启而丢失
            NotesAlarmManager.initAlarms(context);
        }
    }
}
