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

 package net.micode.notes.widget;

 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 import net.micode.notes.R;
 import net.micode.notes.data.Notes;
 import net.micode.notes.data.Notes.NoteColumns;
 import net.micode.notes.tool.ResourceParser;
 import net.micode.notes.ui.NoteEditActivity;
 import net.micode.notes.ui.NotesListActivity;
 
 /**
  * 桌面小部件提供者基类，用于显示笔记的摘要信息，并处理相关操作
  */
 public abstract class NoteWidgetProvider extends AppWidgetProvider {
     // 定义查询笔记时需要的列
     public static final String [] PROJECTION = new String [] {
         NoteColumns.ID,            // 笔记ID
         NoteColumns.BG_COLOR_ID,   // 背景颜色ID
         NoteColumns.SNIPPET        // 笔记摘要片段
     };
 
     // 列索引常量
     public static final int COLUMN_ID           = 0;
     public static final int COLUMN_BG_COLOR_ID  = 1;
     public static final int COLUMN_SNIPPET      = 2;
 
     private static final String TAG = "NoteWidgetProvider"; // 日志标签
 
     /**
      * 当小部件被删除时调用，解除笔记与小部件的关联
      */
     @Override
     public void onDeleted(Context context, int[] appWidgetIds) {
         ContentValues values = new ContentValues();
         values.put(NoteColumns.WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID); // 设置无效的widget ID
         for (int appWidgetId : appWidgetIds) {
             // 更新数据库，将对应widget ID的笔记的widget_id字段置为无效
             context.getContentResolver().update(Notes.CONTENT_NOTE_URI,
                     values,
                     NoteColumns.WIDGET_ID + "=?",
                     new String[] { String.valueOf(appWidgetId) });
         }
     }
 
     /**
      * 根据widget ID查询关联的笔记信息
      * @param widgetId 小部件ID
      * @return 包含笔记信息的Cursor对象
      */
     private Cursor getNoteWidgetInfo(Context context, int widgetId) {
         return context.getContentResolver().query(
                 Notes.CONTENT_NOTE_URI,
                 PROJECTION,
                 NoteColumns.WIDGET_ID + "=? AND " + NoteColumns.PARENT_ID + "<>?", // 排除垃圾箱中的笔记
                 new String[] { String.valueOf(widgetId), String.valueOf(Notes.ID_TRASH_FOLER) },
                 null);
     }
 
     /**
      * 更新小部件显示内容（公开方法）
      */
     protected void update(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
         update(context, appWidgetManager, appWidgetIds, false);
     }
 
     /**
      * 更新小部件显示内容的核心方法
      * @param privacyMode 是否为隐私模式（隐藏真实内容）
      */
     private void update(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds,
             boolean privacyMode) {
         for (int appWidgetId : appWidgetIds) {
             if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) continue;
 
             int bgId = ResourceParser.getDefaultBgId(context); // 默认背景
             String snippet = ""; // 摘要文本
             Intent intent = new Intent(context, NoteEditActivity.class);
             intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // 单例模式启动
             intent.putExtra(Notes.INTENT_EXTRA_WIDGET_ID, appWidgetId);
             intent.putExtra(Notes.INTENT_EXTRA_WIDGET_TYPE, getWidgetType()); // 由子类提供类型
 
             // 查询关联的笔记信息
             try (Cursor c = getNoteWidgetInfo(context, appWidgetId)) {
                 if (c != null && c.moveToFirst()) {
                     if (c.getCount() > 1) {
                         Log.e(TAG, "Multiple notes with same widget id:" + appWidgetId);
                         return;
                     }
                     // 从数据库获取数据
                     snippet = c.getString(COLUMN_SNIPPET);
                     bgId = c.getInt(COLUMN_BG_COLOR_ID);
                     intent.putExtra(Intent.EXTRA_UID, c.getLong(COLUMN_ID));
                     intent.setAction(Intent.ACTION_VIEW); // 查看现有笔记
                 } else {
                     // 没有关联笔记时显示提示
                     snippet = context.getString(R.string.widget_havenot_content);
                     intent.setAction(Intent.ACTION_INSERT_OR_EDIT); // 创建新笔记
                 }
             }
 
             // 配置RemoteViews
             RemoteViews rv = new RemoteViews(context.getPackageName(), getLayoutId());
             rv.setImageViewResource(R.id.widget_bg_image, getBgResourceId(bgId)); // 设置背景
             intent.putExtra(Notes.INTENT_EXTRA_BACKGROUND_ID, bgId);
 
             // 构建PendingIntent
             PendingIntent pendingIntent;
             if (privacyMode) {
                 // 隐私模式：显示提示，点击进入列表页
                 rv.setTextViewText(R.id.widget_text, context.getString(R.string.widget_under_visit_mode));
                 pendingIntent = PendingIntent.getActivity(context, appWidgetId, 
                         new Intent(context, NotesListActivity.class), 
                         PendingIntent.FLAG_UPDATE_CURRENT);
             } else {
                 // 正常模式：显示摘要，点击进入编辑页
                 rv.setTextViewText(R.id.widget_text, snippet);
                 pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent,
                         PendingIntent.FLAG_UPDATE_CURRENT);
             }
 
             // 设置点击事件
             rv.setOnClickPendingIntent(R.id.widget_text, pendingIntent);
             // 更新小部件
             appWidgetManager.updateAppWidget(appWidgetId, rv);
         }
     }
 
     // 以下抽象方法由具体子类实现
     protected abstract int getBgResourceId(int bgId); // 获取背景资源ID
     protected abstract int getLayoutId();            // 获取布局资源ID
     protected abstract int getWidgetType();          // 获取小部件类型
 }