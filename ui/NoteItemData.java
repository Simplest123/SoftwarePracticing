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

 import android.content.Context;
 import android.database.Cursor;
 import android.text.TextUtils;
 
 import net.micode.notes.data.Contact;
 import net.micode.notes.data.Notes;
 import net.micode.notes.data.Notes.NoteColumns;
 import net.micode.notes.tool.DataUtils;
 
 public class NoteItemData {
     static final String[] PROJECTION = new String[]{
         NoteColumns.ID,
         NoteColumns.ALERTED_DATE,
         NoteColumns.BG_COLOR_ID,
         NoteColumns.CREATED_DATE,
         NoteColumns.HAS_ATTACHMENT,
         NoteColumns.MODIFIED_DATE,
         NoteColumns.NOTES_COUNT,
         NoteColumns.PARENT_ID,
         NoteColumns.SNIPPET,
         NoteColumns.TYPE,
         NoteColumns.WIDGET_ID,
         NoteColumns.WIDGET_TYPE,
     };
 
     // 列索引
     private static final int ID_COLUMN = 0;
     private static final int ALERTED_DATE_COLUMN = 1;
     private static final int BG_COLOR_ID_COLUMN = 2;
     private static final int CREATED_DATE_COLUMN = 3;
     private static final int HAS_ATTACHMENT_COLUMN = 4;
     private static final int MODIFIED_DATE_COLUMN = 5;
     private static final int NOTES_COUNT_COLUMN = 6;
     private static final int PARENT_ID_COLUMN = 7;
     private static final int SNIPPET_COLUMN = 8;
     private static final int TYPE_COLUMN = 9;
     private static final int WIDGET_ID_COLUMN = 10;
     private static final int WIDGET_TYPE_COLUMN = 11;
 
     private long mId; // 便签ID
     private long mAlertDate; // 提醒日期
     private int mBgColorId; // 背景颜色ID
     private long mCreatedDate; // 创建日期
     private boolean mHasAttachment; // 是否有附件
     private long mModifiedDate; // 修改日期
     private int mNotesCount; // 便签数量
     private long mParentId; // 父级ID
     private String mSnippet; // 摘要
     private int mType; // 类型
     private int mWidgetId; // 小部件ID
     private int mWidgetType; // 小部件类型
     private String mName; // 联系人姓名
     private String mPhoneNumber; // 联系人电话
 
     // 状态标记
     private boolean mIsLastItem;
     private boolean mIsFirstItem;
     private boolean mIsOnlyOneItem;
     private boolean mIsOneNoteFollowingFolder;
     private boolean mIsMultiNotesFollowingFolder;
 
     public NoteItemData(Context context, Cursor cursor) {
         // 从游标中提取数据
         mId = cursor.getLong(ID_COLUMN);
         mAlertDate = cursor.getLong(ALERTED_DATE_COLUMN);
         mBgColorId = cursor.getInt(BG_COLOR_ID_COLUMN);
         mCreatedDate = cursor.getLong(CREATED_DATE_COLUMN);
         mHasAttachment = (cursor.getInt(HAS_ATTACHMENT_COLUMN) > 0);
         mModifiedDate = cursor.getLong(MODIFIED_DATE_COLUMN);
         mNotesCount = cursor.getInt(NOTES_COUNT_COLUMN);
         mParentId = cursor.getLong(PARENT_ID_COLUMN);
         mSnippet = cursor.getString(SNIPPET_COLUMN);
         mSnippet = mSnippet.replace(NoteEditActivity.TAG_CHECKED, "").replace(
                 NoteEditActivity.TAG_UNCHECKED, "");
         mType = cursor.getInt(TYPE_COLUMN);
         mWidgetId = cursor.getInt(WIDGET_ID_COLUMN);
         mWidgetType = cursor.getInt(WIDGET_TYPE_COLUMN);
 
         mPhoneNumber = "";
         // 如果是通话记录文件夹，获取电话号码
         if (mParentId == Notes.ID_CALL_RECORD_FOLDER) {
             mPhoneNumber = DataUtils.getCallNumberByNoteId(context.getContentResolver(), mId);
             if (!TextUtils.isEmpty(mPhoneNumber)) {
                 mName = Contact.getContact(context, mPhoneNumber); // 获取联系人姓名
                 if (mName == null) {
                     mName = mPhoneNumber; // 如果没有找到姓名，使用电话号码
                 }
             }
         }
 
         if (mName == null) {
             mName = ""; // 默认姓名为空
         }
         checkPosition(cursor); // 检查位置状态
     }
 
     private void checkPosition(Cursor cursor) {
         // 检查当前项在游标中的位置
         mIsLastItem = cursor.isLast();
         mIsFirstItem = cursor.isFirst();
         mIsOnlyOneItem = (cursor.getCount() == 1);
         mIsMultiNotesFollowingFolder = false;
         mIsOneNoteFollowingFolder = false;
 
         if (mType == Notes.TYPE_NOTE && !mIsFirstItem) {
             int position = cursor.getPosition();
             if (cursor.moveToPrevious()) {
                 if (cursor.getInt(TYPE_COLUMN) == Notes.TYPE_FOLDER
                         || cursor.getInt(TYPE_COLUMN) == Notes.TYPE_SYSTEM) {
                     if (cursor.getCount() > (position + 1)) {
                         mIsMultiNotesFollowingFolder = true; // 多个便签跟随文件夹
                     } else {
                         mIsOneNoteFollowingFolder = true; // 一个便签跟随文件夹
                     }
                 }
                 if (!cursor.moveToNext()) {
                     throw new IllegalStateException("cursor move to previous but can't move back");
                 }
             }
         }
     }
 
     // 各种状态检查方法
     public boolean isOneFollowingFolder() {
         return mIsOneNoteFollowingFolder;
     }
 
     public boolean isMultiFollowingFolder() {
         return mIsMultiNotesFollowingFolder;
     }
 
     public boolean isLast() {
         return mIsLastItem;
     }
 
     public String getCallName() {
         return mName; // 获取联系人姓名
     }
 
     public boolean isFirst() {
         return mIsFirstItem;
     }
 
     public boolean isSingle() {
         return mIsOnlyOneItem;
     }
 
     // 各种属性获取方法
     public long getId() {
         return mId;
     }
 
     public long getAlertDate() {
         return mAlertDate;
     }
 
     public long getCreatedDate() {
         return mCreatedDate;
     }
 
     public boolean hasAttachment() {
         return mHasAttachment;
     }
 
     public long getModifiedDate() {
         return mModifiedDate;
     }
 
     public int getBgColorId() {
         return mBgColorId;
     }
 
     public long getParentId() {
         return mParentId;
     }
 
     public int getNotesCount() {
         return mNotesCount;
     }
 
     public long getFolderId() {
         return mParentId; // 返回文件夹ID
     }
 
     public int getType() {
         return mType;
     }
 
     public int getWidgetType() {
         return mWidgetType;
     }
 
     public int getWidgetId() {
         return mWidgetId;
     }
 
     public String getSnippet() {
         return mSnippet;
     }
 
     public boolean hasAlert() {
         return (mAlertDate > 0); // 是否有提醒
     }
 
     public boolean isCallRecord() {
         return (mParentId == Notes.ID_CALL_RECORD_FOLDER && !TextUtils.isEmpty(mPhoneNumber)); // 是否是通话记录
     }
 
     public static int getNoteType(Cursor cursor) {
         return cursor.getInt(TYPE_COLUMN); // 获取便签类型
     }
 }
 