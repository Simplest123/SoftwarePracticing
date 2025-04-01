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

 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 
 // 定义一个广播接收器类，用于处理闹钟事件
 public class AlarmReceiver extends BroadcastReceiver {
     // 当接收到广播时调用的方法
     @Override
     public void onReceive(Context context, Intent intent) {
         // 设置接收到的意图的目标类为 AlarmAlertActivity
         intent.setClass(context, AlarmAlertActivity.class);
         // 添加标志以启动新任务
         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         // 启动 AlarmAlertActivity
         context.startActivity(intent);
     }
 }
 