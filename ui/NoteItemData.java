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
 

 /**
  * 便签数据项实体类，封装从数据库查询出的便签数据
  */
 public class NoteItemData {
     // 数据库查询列名投影
     static final String [] PROJECTION = new String [] {
         NoteColumns.ID,                // 便签ID
         NoteColumns.ALERTED_DATE,      // 提醒日期
         NoteColumns.BG_COLOR_ID,       // 背景颜色ID
         NoteColumns.CREATED_DATE,      // 创建日期
         NoteColumns.HAS_ATTACHMENT,    // 是否有附件
         NoteColumns.MODIFIED_DATE,     // 修改日期
         NoteColumns.NOTES_COUNT,       // 便签数量(用于文件夹)
         NoteColumns.PARENT_ID,         // 父文件夹ID
         NoteColumns.SNIPPET,          // 内容摘要
         NoteColumns.TYPE,             // 类型(便签/文件夹/系统文件夹)
         NoteColumns.WIDGET_ID,        // 桌面小部件ID
         NoteColumns.WIDGET_TYPE,      // 桌面小部件类型
     };
 
     // 列索引常量定义
     private static final int ID_COLUMN                    = 0;
     private static final int ALERTED_DATE_COLUMN          = 1;
     private static final int BG_COLOR_ID_COLUMN           = 2;
     private static final int CREATED_DATE_COLUMN          = 3;
     private static final int HAS_ATTACHMENT_COLUMN        = 4;
     private static final int MODIFIED_DATE_COLUMN         = 5;
     private static final int NOTES_COUNT_COLUMN           = 6;
     private static final int PARENT_ID_COLUMN             = 7;
     private static final int SNIPPET_COLUMN               = 8;
     private static final int TYPE_COLUMN                  = 9;
     private static final int WIDGET_ID_COLUMN             = 10;
     private static final int WIDGET_TYPE_COLUMN           = 11;
 
     // 便签数据字段
     private long mId;                  // 便签ID
     private long mAlertDate;           // 提醒日期
     private int mBgColorId;            // 背景颜色ID
     private long mCreatedDate;         // 创建日期
     private boolean mHasAttachment;    // 是否有附件
     private long mModifiedDate;        // 修改日期
     private int mNotesCount;           // 便签数量(用于文件夹)
     private long mParentId;            // 父文件夹ID
     private String mSnippet;           // 内容摘要
     private int mType;                 // 类型(便签/文件夹/系统文件夹)
     private int mWidgetId;             // 桌面小部件ID
     private int mWidgetType;           // 桌面小部件类型
     private String mName;              // 联系人姓名(用于通话记录)
     private String mPhoneNumber;       // 电话号码(用于通话记录)
 
     // 位置状态标志
     private boolean mIsLastItem;       // 是否是列表最后一项
     private boolean mIsFirstItem;      // 是否是列表第一项
     private boolean mIsOnlyOneItem;    // 是否是唯一一项
     private boolean mIsOneNoteFollowingFolder;    // 是否是单个便签跟随文件夹
     private boolean mIsMultiNotesFollowingFolder; // 是否是多个便签跟随文件夹
 
     /**
      * 构造函数，从Cursor初始化便签数据
      * @param context 上下文对象
      * @param cursor 数据库查询结果游标
      */
     public NoteItemData(Context context, Cursor cursor) {
         // 从Cursor读取基本数据

         mId = cursor.getLong(ID_COLUMN);
         mAlertDate = cursor.getLong(ALERTED_DATE_COLUMN);
         mBgColorId = cursor.getInt(BG_COLOR_ID_COLUMN);
         mCreatedDate = cursor.getLong(CREATED_DATE_COLUMN);

         mHasAttachment = (cursor.getInt(HAS_ATTACHMENT_COLUMN) > 0) ? true : false;

         mModifiedDate = cursor.getLong(MODIFIED_DATE_COLUMN);
         mNotesCount = cursor.getInt(NOTES_COUNT_COLUMN);
         mParentId = cursor.getLong(PARENT_ID_COLUMN);
         mSnippet = cursor.getString(SNIPPET_COLUMN);

         // 移除内容摘要中的复选框标记

         mSnippet = mSnippet.replace(NoteEditActivity.TAG_CHECKED, "").replace(
                 NoteEditActivity.TAG_UNCHECKED, "");
         mType = cursor.getInt(TYPE_COLUMN);
         mWidgetId = cursor.getInt(WIDGET_ID_COLUMN);
         mWidgetType = cursor.getInt(WIDGET_TYPE_COLUMN);
 

         // 初始化通话记录相关数据
         mPhoneNumber = "";
         if (mParentId == Notes.ID_CALL_RECORD_FOLDER) {
             // 如果是通话记录文件夹下的便签，获取电话号码
             mPhoneNumber = DataUtils.getCallNumberByNoteId(context.getContentResolver(), mId);
             if (!TextUtils.isEmpty(mPhoneNumber)) {
                 // 根据电话号码获取联系人姓名
                 mName = Contact.getContact(context, mPhoneNumber);
                 if (mName == null) {
                     mName = mPhoneNumber;

                 }
             }
         }
 
         if (mName == null) {

             mName = "";
         }
         
         // 检查当前项在列表中的位置状态
         checkPostion(cursor);
     }
 
     /**
      * 检查当前项在列表中的位置状态
      * @param cursor 数据库查询结果游标
      */
     private void checkPostion(Cursor cursor) {

         mIsLastItem = cursor.isLast();
         mIsFirstItem = cursor.isFirst();
         mIsOnlyOneItem = (cursor.getCount() == 1);
         mIsMultiNotesFollowingFolder = false;
         mIsOneNoteFollowingFolder = false;
 

         // 如果是便签类型且不是第一项
         if (mType == Notes.TYPE_NOTE && !mIsFirstItem) {
             int position = cursor.getPosition();
             if (cursor.moveToPrevious()) {
                 // 检查前一项是否是文件夹
                 if (cursor.getInt(TYPE_COLUMN) == Notes.TYPE_FOLDER
                         || cursor.getInt(TYPE_COLUMN) == Notes.TYPE_SYSTEM) {
                     // 判断是多个便签跟随文件夹还是单个便签跟随文件夹
                     if (cursor.getCount() > (position + 1)) {
                         mIsMultiNotesFollowingFolder = true;
                     } else {
                         mIsOneNoteFollowingFolder = true;
                     }
                 }
                 // 将游标移回原位

                 if (!cursor.moveToNext()) {
                     throw new IllegalStateException("cursor move to previous but can't move back");
                 }
             }
         }
     }
 

     // ==================== 公共方法 ====================
 

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

         return mName;

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

         return (mAlertDate > 0);
     }
 
     public boolean isCallRecord() {
         return (mParentId == Notes.ID_CALL_RECORD_FOLDER && !TextUtils.isEmpty(mPhoneNumber));
     }
 
     /**
      * 静态方法：从Cursor获取便签类型
      * @param cursor 数据库查询结果游标
      * @return 便签类型
      */
     public static int getNoteType(Cursor cursor) {
         return cursor.getInt(TYPE_COLUMN);
     }
 }

