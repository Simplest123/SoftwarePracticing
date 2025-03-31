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

 import android.content.ContentProviderOperation;
 import android.content.ContentProviderResult;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.OperationApplicationException;
 import android.net.Uri;
 import android.os.RemoteException;
 import android.util.Log;
 
 import net.micode.notes.data.Notes;
 import net.micode.notes.data.Notes.CallNote;
 import net.micode.notes.data.Notes.DataColumns;
 import net.micode.notes.data.Notes.NoteColumns;
 import net.micode.notes.data.Notes.TextNote;
 
 import java.util.ArrayList;
 
 /**
  * 笔记数据模型类 - 处理笔记数据的CRUD操作
  */
 public class Note {
     // 笔记元数据的变更值集合（用于更新Note表）
     private ContentValues mNoteDiffValues;
     // 笔记内容数据（文本数据/通话记录数据）
     private NoteData mNoteData;
     private static final String TAG = "Note";
 
     /**
      * 创建新笔记ID并插入数据库
      * @param context 上下文对象
      * @param folderId 所属文件夹ID
      * @return 新笔记ID，创建失败返回0
      */
     public static synchronized long getNewNoteId(Context context, long folderId) {
         // 准备新笔记的初始值
         ContentValues values = new ContentValues();
         long createdTime = System.currentTimeMillis();
         values.put(NoteColumns.CREATED_DATE, createdTime);  // 创建时间
         values.put(NoteColumns.MODIFIED_DATE, createdTime); // 修改时间
         values.put(NoteColumns.TYPE, Notes.TYPE_NOTE);      // 笔记类型
         values.put(NoteColumns.LOCAL_MODIFIED, 1);         // 标记为已修改
         values.put(NoteColumns.PARENT_ID, folderId);        // 父文件夹ID
         
         // 插入数据库并获取URI
         Uri uri = context.getContentResolver().insert(Notes.CONTENT_NOTE_URI, values);
 
         long noteId = 0;
         try {
             // 从URI中解析出新笔记ID
             noteId = Long.valueOf(uri.getPathSegments().get(1));
         } catch (NumberFormatException e) {
             Log.e(TAG, "Get note id error :" + e.toString());
             noteId = 0;
         }
         if (noteId == -1) {
             throw new IllegalStateException("Wrong note id:" + noteId);
         }
         return noteId;
     }
 
     /**
      * 构造函数 - 初始化笔记数据和变更值集合
      */
     public Note() {
         mNoteDiffValues = new ContentValues();
         mNoteData = new NoteData();
     }
 
     /**
      * 设置笔记元数据值
      * @param key 列名
      * @param value 列值
      */
     public void setNoteValue(String key, String value) {
         mNoteDiffValues.put(key, value);
         // 设置修改标记和时间戳
         mNoteDiffValues.put(NoteColumns.LOCAL_MODIFIED, 1);
         mNoteDiffValues.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
     }
 
     /**
      * 设置文本数据值
      */
     public void setTextData(String key, String value) {
         mNoteData.setTextData(key, value);
     }
 
     /**
      * 设置文本数据ID
      */
     public void setTextDataId(long id) {
         mNoteData.setTextDataId(id);
     }
 
     /**
      * 获取文本数据ID
      */
     public long getTextDataId() {
         return mNoteData.mTextDataId;
     }
 
     /**
      * 设置通话记录数据ID
      */
     public void setCallDataId(long id) {
         mNoteData.setCallDataId(id);
     }
 
     /**
      * 设置通话记录数据值
      */
     public void setCallData(String key, String value) {
         mNoteData.setCallData(key, value);
     }
 
     /**
      * 检查笔记是否有本地修改
      * @return true表示有未同步的修改
      */
     public boolean isLocalModified() {
         return mNoteDiffValues.size() > 0 || mNoteData.isLocalModified();
     }
 
     /**
      * 同步笔记数据到数据库
      * @param context 上下文对象
      * @param noteId 笔记ID
      * @return 同步是否成功
      */
     public boolean syncNote(Context context, long noteId) {
         if (noteId <= 0) {
             throw new IllegalArgumentException("Wrong note id:" + noteId);
         }
 
         // 如果没有修改则直接返回成功
         if (!isLocalModified()) {
             return true;
         }
 
         /* 
          * 理论上数据变更后应该更新LOCAL_MODIFIED和MODIFIED_DATE字段
          * 为了数据安全，即使笔记更新失败，我们也会尝试更新笔记数据
          */
         if (context.getContentResolver().update(
                 ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId), mNoteDiffValues, null,
                 null) == 0) {
             Log.e(TAG, "Update note error, should not happen");
             // 继续执行而不返回，尝试保存数据
         }
         mNoteDiffValues.clear();  // 清空变更值
 
         // 同步笔记内容数据
         if (mNoteData.isLocalModified()
                 && (mNoteData.pushIntoContentResolver(context, noteId) == null)) {
             return false;
         }
 
         return true;
     }
 
     /**
      * 笔记内容数据内部类 - 处理文本数据和通话记录数据的存储
      */
     private class NoteData {
         private long mTextDataId;          // 文本数据ID
         private ContentValues mTextDataValues; // 文本数据变更值
         private long mCallDataId;           // 通话记录数据ID
         private ContentValues mCallDataValues; // 通话记录变更值
         private static final String TAG = "NoteData";
 
         /**
          * 构造函数 - 初始化数据和ID
          */
         public NoteData() {
             mTextDataValues = new ContentValues();
             mCallDataValues = new ContentValues();
             mTextDataId = 0;  // 0表示新数据
             mCallDataId = 0;
         }
 
         /**
          * 检查内容数据是否有本地修改
          */
         boolean isLocalModified() {
             return mTextDataValues.size() > 0 || mCallDataValues.size() > 0;
         }
 
         /**
          * 设置文本数据ID
          */
         void setTextDataId(long id) {
             if(id <= 0) {
                 throw new IllegalArgumentException("Text data id should larger than 0");
             }
             mTextDataId = id;
         }
 
         /**
          * 设置通话记录数据ID
          */
         void setCallDataId(long id) {
             if (id <= 0) {
                 throw new IllegalArgumentException("Call data id should larger than 0");
             }
             mCallDataId = id;
         }
 
         /**
          * 设置通话记录数据值
          */
         void setCallData(String key, String value) {
             mCallDataValues.put(key, value);
             // 同时标记笔记为已修改
             mNoteDiffValues.put(NoteColumns.LOCAL_MODIFIED, 1);
             mNoteDiffValues.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
         }
 
         /**
          * 设置文本数据值
          */
         void setTextData(String key, String value) {
             mTextDataValues.put(key, value);
             // 同时标记笔记为已修改
             mNoteDiffValues.put(NoteColumns.LOCAL_MODIFIED, 1);
             mNoteDiffValues.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
         }
 
         /**
          * 将内容数据推送到ContentResolver
          * @param context 上下文对象
          * @param noteId 所属笔记ID
          * @return 操作结果URI，失败返回null
          */
         Uri pushIntoContentResolver(Context context, long noteId) {
             // 安全检查
             if (noteId <= 0) {
                 throw new IllegalArgumentException("Wrong note id:" + noteId);
             }
 
             ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
             ContentProviderOperation.Builder builder = null;
 
             // 处理文本数据
             if(mTextDataValues.size() > 0) {
                 mTextDataValues.put(DataColumns.NOTE_ID, noteId);  // 设置所属笔记ID
                 if (mTextDataId == 0) {
                     // 新文本数据 - 执行插入操作
                     mTextDataValues.put(DataColumns.MIME_TYPE, TextNote.CONTENT_ITEM_TYPE);
                     Uri uri = context.getContentResolver().insert(Notes.CONTENT_DATA_URI,
                             mTextDataValues);
                     try {
                         // 从返回URI中获取新数据ID
                         setTextDataId(Long.valueOf(uri.getPathSegments().get(1)));
                     } catch (NumberFormatException e) {
                         Log.e(TAG, "Insert new text data fail with noteId" + noteId);
                         mTextDataValues.clear();
                         return null;
                     }
                 } else {
                     // 已有文本数据 - 执行更新操作
                     builder = ContentProviderOperation.newUpdate(ContentUris.withAppendedId(
                             Notes.CONTENT_DATA_URI, mTextDataId));
                     builder.withValues(mTextDataValues);
                     operationList.add(builder.build());
                 }
                 mTextDataValues.clear();  // 清空变更值
             }
 
             // 处理通话记录数据（逻辑同文本数据）
             if(mCallDataValues.size() > 0) {
                 mCallDataValues.put(DataColumns.NOTE_ID, noteId);
                 if (mCallDataId == 0) {
                     mCallDataValues.put(DataColumns.MIME_TYPE, CallNote.CONTENT_ITEM_TYPE);
                     Uri uri = context.getContentResolver().insert(Notes.CONTENT_DATA_URI,
                             mCallDataValues);
                     try {
                         setCallDataId(Long.valueOf(uri.getPathSegments().get(1)));
                     } catch (NumberFormatException e) {
                         Log.e(TAG, "Insert new call data fail with noteId" + noteId);
                         mCallDataValues.clear();
                         return null;
                     }
                 } else {
                     builder = ContentProviderOperation.newUpdate(ContentUris.withAppendedId(
                             Notes.CONTENT_DATA_URI, mCallDataId));
                     builder.withValues(mCallDataValues);
                     operationList.add(builder.build());
                 }
                 mCallDataValues.clear();
             }
 
             // 执行批量操作（如果有）
             if (operationList.size() > 0) {
                 try {
                     ContentProviderResult[] results = context.getContentResolver().applyBatch(
                             Notes.AUTHORITY, operationList);
                     return (results == null || results.length == 0 || results[0] == null) ? null
                             : ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId);
                 } catch (RemoteException e) {
                     Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                     return null;
                 } catch (OperationApplicationException e) {
                     Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                     return null;
                 }
             }
             return null;
         }
     }
 }