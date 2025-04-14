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
 * 便签小部件的基础提供者类，处理小部件的更新和删除逻辑
 */
public abstract class NoteWidgetProvider extends AppWidgetProvider {
    // 定义从数据库查询的列名
    public static final String[] PROJECTION = new String[] {
            NoteColumns.ID, // 便签ID
            NoteColumns.BG_COLOR_ID, // 背景颜色ID
            NoteColumns.SNIPPET // 内容片段
    };

    // 列索引常量
    public static final int COLUMN_ID = 0; // ID列索引
    public static final int COLUMN_BG_COLOR_ID = 1; // 背景颜色列索引
    public static final int COLUMN_SNIPPET = 2; // 内容片段列索引

    private static final String TAG = "NoteWidgetProvider"; // 日志标签

    /**
     * 当小部件被删除时调用，清除数据库中对应的widget_id
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        ContentValues values = new ContentValues();
        values.put(NoteColumns.WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID); // 设置无效的widget_id

        // 遍历所有被删除的小部件ID，更新数据库
        for (int i = 0; i < appWidgetIds.length; i++) {
            context.getContentResolver().update(Notes.CONTENT_NOTE_URI,
                    values,
                    NoteColumns.WIDGET_ID + "=?", // WHERE条件：匹配当前widget_id
                    new String[] { String.valueOf(appWidgetIds[i]) });
        }
    }

    /**
     * 查询指定widget_id对应的便签信息
     * 
     * @param widgetId 小部件ID
     * @return 包含便签数据的游标
     */
    private Cursor getNoteWidgetInfo(Context context, int widgetId) {
        return context.getContentResolver().query(Notes.CONTENT_NOTE_URI,
                PROJECTION,
                NoteColumns.WIDGET_ID + "=? AND " + NoteColumns.PARENT_ID + "<>?", // 条件：匹配widget_id且不在回收站
                new String[] { String.valueOf(widgetId), String.valueOf(Notes.ID_TRASH_FOLER) },
                null);
    }

    /**
     * 更新小部件显示的公开方法
     */
    protected void update(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        update(context, appWidgetManager, appWidgetIds, false); // 默认非隐私模式
    }

    /**
     * 核心更新逻辑：设置小部件外观和点击行为
     * 
     * @param privacyMode 是否为隐私模式（显示占位文本）
     */
    private void update(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds,
            boolean privacyMode) {
        for (int i = 0; i < appWidgetIds.length; i++) {
            if (appWidgetIds[i] != AppWidgetManager.INVALID_APPWIDGET_ID) {
                int bgId = ResourceParser.getDefaultBgId(context); // 默认背景色
                String snippet = ""; // 内容片段
                Intent intent = new Intent(context, NoteEditActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // 单例模式启动
                intent.putExtra(Notes.INTENT_EXTRA_WIDGET_ID, appWidgetIds[i]); // 传递widget_id
                intent.putExtra(Notes.INTENT_EXTRA_WIDGET_TYPE, getWidgetType()); // 传递小部件类型

                // 查询数据库获取便签信息
                Cursor c = getNoteWidgetInfo(context, appWidgetIds[i]);
                if (c != null && c.moveToFirst()) {
                    if (c.getCount() > 1) {
                        Log.e(TAG, "同一个widget_id对应多个便签:" + appWidgetIds[i]);
                        c.close();
                        return; // 数据异常直接返回
                    }
                    snippet = c.getString(COLUMN_SNIPPET); // 获取内容
                    bgId = c.getInt(COLUMN_BG_COLOR_ID); // 获取背景色
                    intent.putExtra(Intent.EXTRA_UID, c.getLong(COLUMN_ID)); // 传递便签ID
                    intent.setAction(Intent.ACTION_VIEW); // 查看模式
                } else {
                    // 无关联便签时显示默认提示
                    snippet = context.getResources().getString(R.string.widget_havenot_content);
                    intent.setAction(Intent.ACTION_INSERT_OR_EDIT); // 新建便签模式
                }

                if (c != null) {
                    c.close(); // 关闭游标释放资源
                }

                // 构建RemoteViews对象
                RemoteViews rv = new RemoteViews(context.getPackageName(), getLayoutId());
                rv.setImageViewResource(R.id.widget_bg_image, getBgResourceId(bgId)); // 设置背景
                intent.putExtra(Notes.INTENT_EXTRA_BACKGROUND_ID, bgId); // 传递背景色

                // 创建点击事件的PendingIntent
                PendingIntent pendingIntent = null;
                if (privacyMode) {
                    // 隐私模式：显示占位文本，点击进入列表页
                    rv.setTextViewText(R.id.widget_text, context.getString(R.string.widget_under_visit_mode));
                    pendingIntent = PendingIntent.getActivity(context, appWidgetIds[i], new Intent(
                            context, NotesListActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                } else {
                    // 正常模式：显示内容片段，点击进入编辑页
                    rv.setTextViewText(R.id.widget_text, snippet);
                    pendingIntent = PendingIntent.getActivity(context, appWidgetIds[i], intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                }

                rv.setOnClickPendingIntent(R.id.widget_text, pendingIntent); // 绑定点击事件
                appWidgetManager.updateAppWidget(appWidgetIds[i], rv); // 更新小部件
            }
        }
    }

    // --------------- 需要子类实现的抽象方法 ---------------
    /** 根据背景色ID获取实际资源ID */
    protected abstract int getBgResourceId(int bgId);

    /** 获取小部件布局文件ID */
    protected abstract int getLayoutId();

    /** 获取小部件类型标识 */
    protected abstract int getWidgetType();
}
