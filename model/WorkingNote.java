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

 package net.micode.notes.model;

 import android.appwidget.AppWidgetManager;
 import android.content.ContentUris;
 import android.content.Context;
 import android.database.Cursor;
 import android.text.TextUtils;
 import android.util.Log;
 
 import net.micode.notes.data.Notes;
 import net.micode.notes.data.Notes.CallNote;
 import net.micode.notes.data.Notes.DataColumns;
 import net.micode.notes.data.Notes.DataConstants;
 import net.micode.notes.data.Notes.NoteColumns;
 import net.micode.notes.data.Notes.TextNote;
 import net.micode.notes.tool.ResourceParser.NoteBgResources;
 
 /**
  * 工作笔记类 - 处理便签的创建、加载和保存等核心功能
  */
 public class WorkingNote {
     // 当前操作的笔记对象
     private Note mNote;
     // 笔记ID
     private long mNoteId;
     // 笔记内容
     public String mContent;
     // 笔记模式（普通模式/清单模式）
     private int mMode;
 
     private long mAlertDate;       // 提醒日期
     private long mModifiedDate;    // 最后修改日期
     private int mBgColorId;        // 背景颜色ID
     private int mWidgetId;         // 桌面小部件ID
     private int mWidgetType;       // 小部件类型
     private long mFolderId;        // 所属文件夹ID
     private Context mContext;      // 上下文对象
 
     private static final String TAG = "WorkingNote";  // 日志标签
 
     private boolean mIsDeleted;    // 标记是否已删除
 
     // 笔记设置变更监听器
     private NoteSettingChangedListener mNoteSettingStatusListener;
 
     // 数据表查询字段
     public static final String[] DATA_PROJECTION = new String[] {
             DataColumns.ID,
             DataColumns.CONTENT,
             DataColumns.MIME_TYPE,
             DataColumns.DATA1,
             DataColumns.DATA2,
             DataColumns.DATA3,
             DataColumns.DATA4,
     };
 
     // 笔记表查询字段
     public static final String[] NOTE_PROJECTION = new String[] {
             NoteColumns.PARENT_ID,
             NoteColumns.ALERTED_DATE,
             NoteColumns.BG_COLOR_ID,
             NoteColumns.WIDGET_ID,
             NoteColumns.WIDGET_TYPE,
             NoteColumns.MODIFIED_DATE
     };
 
     // 数据表字段索引
     private static final int DATA_ID_COLUMN = 0;
     private static final int DATA_CONTENT_COLUMN = 1;
     private static final int DATA_MIME_TYPE_COLUMN = 2;
     private static final int DATA_MODE_COLUMN = 3;
 
     // 笔记表字段索引
     private static final int NOTE_PARENT_ID_COLUMN = 0;
     private static final int NOTE_ALERTED_DATE_COLUMN = 1;
     private static final int NOTE_BG_COLOR_ID_COLUMN = 2;
     private static final int NOTE_WIDGET_ID_COLUMN = 3;
     private static final int NOTE_WIDGET_TYPE_COLUMN = 4;
     private static final int NOTE_MODIFIED_DATE_COLUMN = 5;
 
     /**
      * 构造函数 - 创建新笔记
      * @param context 上下文
      * @param folderId 文件夹ID
      */
     private WorkingNote(Context context, long folderId) {
         mContext = context;
         mAlertDate = 0;  // 默认无提醒
         mModifiedDate = System.currentTimeMillis();  // 设置当前时间为修改时间
         mFolderId = folderId;
         mNote = new Note();  // 创建新Note对象
         mNoteId = 0;  // 新笔记ID为0
         mIsDeleted = false;
         mMode = 0;  // 默认普通模式
         mWidgetType = Notes.TYPE_WIDGET_INVALIDE;  // 默认无效小部件类型
     }
 
     /**
      * 构造函数 - 加载已有笔记
      * @param context 上下文
      * @param noteId 笔记ID
      * @param folderId 文件夹ID
      */
     private WorkingNote(Context context, long noteId, long folderId) {
         mContext = context;
         mNoteId = noteId;
         mFolderId = folderId;
         mIsDeleted = false;
         mNote = new Note();
         loadNote();  // 加载笔记数据
     }
 
     /**
      * 从数据库加载笔记基本信息
      */
     private void loadNote() {
         // 查询笔记表
         Cursor cursor = mContext.getContentResolver().query(
                 ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, mNoteId), NOTE_PROJECTION, null,
                 null, null);
 
         if (cursor != null) {
             if (cursor.moveToFirst()) {
                 // 从游标读取各字段值
                 mFolderId = cursor.getLong(NOTE_PARENT_ID_COLUMN);
                 mBgColorId = cursor.getInt(NOTE_BG_COLOR_ID_COLUMN);
                 mWidgetId = cursor.getInt(NOTE_WIDGET_ID_COLUMN);
                 mWidgetType = cursor.getInt(NOTE_WIDGET_TYPE_COLUMN);
                 mAlertDate = cursor.getLong(NOTE_ALERTED_DATE_COLUMN);
                 mModifiedDate = cursor.getLong(NOTE_MODIFIED_DATE_COLUMN);
             }
             cursor.close();
         } else {
             Log.e(TAG, "No note with id:" + mNoteId);
             throw new IllegalArgumentException("Unable to find note with id " + mNoteId);
         }
         loadNoteData();  // 加载笔记内容数据
     }
 
     /**
      * 从数据库加载笔记内容数据
      */
     private void loadNoteData() {
         // 查询数据表
         Cursor cursor = mContext.getContentResolver().query(Notes.CONTENT_DATA_URI, DATA_PROJECTION,
                 DataColumns.NOTE_ID + "=?", new String[] {
                     String.valueOf(mNoteId)
                 }, null);
 
         if (cursor != null) {
             if (cursor.moveToFirst()) {
                 do {
                     String type = cursor.getString(DATA_MIME_TYPE_COLUMN);
                     if (DataConstants.NOTE.equals(type)) {
                         // 普通笔记数据
                         mContent = cursor.getString(DATA_CONTENT_COLUMN);
                         mMode = cursor.getInt(DATA_MODE_COLUMN);
                         mNote.setTextDataId(cursor.getLong(DATA_ID_COLUMN));
                     } else if (DataConstants.CALL_NOTE.equals(type)) {
                         // 通话记录笔记
                         mNote.setCallDataId(cursor.getLong(DATA_ID_COLUMN));
                     } else {
                         Log.d(TAG, "Wrong note type with type:" + type);
                     }
                 } while (cursor.moveToNext());
             }
             cursor.close();
         } else {
             Log.e(TAG, "No data with id:" + mNoteId);
             throw new IllegalArgumentException("Unable to find note's data with id " + mNoteId);
         }
     }
 
     /**
      * 创建空笔记
      */
     public static WorkingNote createEmptyNote(Context context, long folderId, int widgetId,
             int widgetType, int defaultBgColorId) {
         WorkingNote note = new WorkingNote(context, folderId);
         note.setBgColorId(defaultBgColorId);  // 设置默认背景色
         note.setWidgetId(widgetId);           // 设置小部件ID
         note.setWidgetType(widgetType);       // 设置小部件类型
         return note;
     }
 
     /**
      * 加载已有笔记
      */
     public static WorkingNote load(Context context, long id) {
         return new WorkingNote(context, id, 0);
     }
 
     /**
      * 保存笔记
      * @return 是否保存成功
      */
     public synchronized boolean saveNote() {
         if (isWorthSaving()) {  // 检查是否需要保存
             if (!existInDatabase()) {  // 新笔记
                 if ((mNoteId = Note.getNewNoteId(mContext, mFolderId)) == 0) {
                     Log.e(TAG, "Create new note fail with id:" + mNoteId);
                     return false;
                 }
             }
 
             mNote.syncNote(mContext, mNoteId);  // 同步笔记数据到数据库
 
             // 如果有关联的小部件，更新小部件内容
             if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
                     && mWidgetType != Notes.TYPE_WIDGET_INVALIDE
                     && mNoteSettingStatusListener != null) {
                 mNoteSettingStatusListener.onWidgetChanged();
             }
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * 检查笔记是否已存在于数据库
      */
     public boolean existInDatabase() {
         return mNoteId > 0;
     }
 
     /**
      * 检查笔记是否需要保存
      */
     private boolean isWorthSaving() {
         if (mIsDeleted || (!existInDatabase() && TextUtils.isEmpty(mContent))
                 || (existInDatabase() && !mNote.isLocalModified())) {
             return false;
         } else {
             return true;
         }
     }
 
     // ==================== 设置相关方法 ====================
 
     public void setOnSettingStatusChangedListener(NoteSettingChangedListener l) {
         mNoteSettingStatusListener = l;
     }
 
     /**
      * 设置提醒日期
      */
     public void setAlertDate(long date, boolean set) {
         if (date != mAlertDate) {
             mAlertDate = date;
             mNote.setNoteValue(NoteColumns.ALERTED_DATE, String.valueOf(mAlertDate));
         }
         if (mNoteSettingStatusListener != null) {
             mNoteSettingStatusListener.onClockAlertChanged(date, set);
         }
     }
 
     /**
      * 标记删除状态
      */
     public void markDeleted(boolean mark) {
         mIsDeleted = mark;
         if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
                 && mWidgetType != Notes.TYPE_WIDGET_INVALIDE && mNoteSettingStatusListener != null) {
                 mNoteSettingStatusListener.onWidgetChanged();
         }
     }
 
     /**
      * 设置背景颜色ID
      */
     public void setBgColorId(int id) {
         if (id != mBgColorId) {
             mBgColorId = id;
             if (mNoteSettingStatusListener != null) {
                 mNoteSettingStatusListener.onBackgroundColorChanged();
             }
             mNote.setNoteValue(NoteColumns.BG_COLOR_ID, String.valueOf(id));
         }
     }
 
     /**
      * 设置清单模式
      */
     public void setCheckListMode(int mode) {
         if (mMode != mode) {
             if (mNoteSettingStatusListener != null) {
                 mNoteSettingStatusListener.onCheckListModeChanged(mMode, mode);
             }
             mMode = mode;
             mNote.setTextData(TextNote.MODE, String.valueOf(mMode));
         }
     }
 
     /**
      * 设置小部件类型
      */
     public void setWidgetType(int type) {
         if (type != mWidgetType) {
             mWidgetType = type;
             mNote.setNoteValue(NoteColumns.WIDGET_TYPE, String.valueOf(mWidgetType));
         }
     }
 
     /**
      * 设置小部件ID
      */
     public void setWidgetId(int id) {
         if (id != mWidgetId) {
             mWidgetId = id;
             mNote.setNoteValue(NoteColumns.WIDGET_ID, String.valueOf(mWidgetId));
         }
     }
 
     /**
      * 设置笔记内容
      */
     public void setWorkingText(String text) {
         if (!TextUtils.equals(mContent, text)) {
             mContent = text;
             mNote.setTextData(DataColumns.CONTENT, mContent);
         }
     }
 
     /**
      * 转换为通话记录笔记
      */
     public void convertToCallNote(String phoneNumber, long callDate) {
         mNote.setCallData(CallNote.CALL_DATE, String.valueOf(callDate));
         mNote.setCallData(CallNote.PHONE_NUMBER, phoneNumber);
         mNote.setNoteValue(NoteColumns.PARENT_ID, String.valueOf(Notes.ID_CALL_RECORD_FOLDER));
     }
 
     // ==================== 获取方法 ====================
 
     public boolean hasClockAlert() {
         return (mAlertDate > 0 ? true : false);
     }
 
     public String getContent() {
         return mContent;
     }
 
     public long getAlertDate() {
         return mAlertDate;
     }
 
     public long getModifiedDate() {
         return mModifiedDate;
     }
 
     public int getBgColorResId() {
         return NoteBgResources.getNoteBgResource(mBgColorId);
     }
 
     public int getBgColorId() {
         return mBgColorId;
     }
 
     public int getTitleBgResId() {
         return NoteBgResources.getNoteTitleBgResource(mBgColorId);
     }
 
     public int getCheckListMode() {
         return mMode;
     }
 
     public long getNoteId() {
         return mNoteId;
     }
 
     public long getFolderId() {
         return mFolderId;
     }
 
     public int getWidgetId() {
         return mWidgetId;
     }
 
     public int getWidgetType() {
         return mWidgetType;
     }
 
     /**
      * 笔记设置变更监听器接口
      */
     public interface NoteSettingChangedListener {
         /**
          * 背景颜色变更回调
          */
         void onBackgroundColorChanged();
 
         /**
          * 提醒时间变更回调
          */
         void onClockAlertChanged(long date, boolean set);
 
         /**
          * 小部件变更回调
          */
         void onWidgetChanged();
 
         /**
          * 清单模式变更回调
          * @param oldMode 旧模式
          * @param newMode 新模式
          */
         void onCheckListModeChanged(int oldMode, int newMode);
     }
 }