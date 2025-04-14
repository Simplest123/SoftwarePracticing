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

 package net.micode.notes.tool;

 import android.content.ContentProviderOperation;
 import android.content.ContentProviderResult;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.OperationApplicationException;
 import android.database.Cursor;
 import android.os.RemoteException;
 import android.util.Log;
 
 import net.micode.notes.data.Notes;
 import net.micode.notes.data.Notes.CallNote;
 import net.micode.notes.data.Notes.NoteColumns;
 import net.micode.notes.ui.NotesListAdapter.AppWidgetAttribute;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 /**
  * 数据操作工具类 - 提供便签数据的增删改查等操作
  */
 public class DataUtils {
     private static final String TAG = "DataUtils";
 
     /**
      * 批量删除便签
      * @param resolver ContentResolver对象
      * @param ids 要删除的便签ID集合
      * @return 是否删除成功
      */
     public static boolean batchDeleteNotes(ContentResolver resolver, HashSet<Long> ids) {
         if (ids == null) {
             Log.d(TAG, "the ids is null");
             return true;
         }
         if (ids.size() == 0) {
             Log.d(TAG, "no id is in the hashset");
             return true;
         }
 
         // 构建批量删除操作列表
         ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
         for (long id : ids) {
             if(id == Notes.ID_ROOT_FOLDER) {
                 Log.e(TAG, "Don't delete system folder root");
                 continue; // 跳过系统根目录
             }
             ContentProviderOperation.Builder builder = ContentProviderOperation
                     .newDelete(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, id));
             operationList.add(builder.build());
         }
         
         // 执行批量操作
         try {
             ContentProviderResult[] results = resolver.applyBatch(Notes.AUTHORITY, operationList);
             if (results == null || results.length == 0 || results[0] == null) {
                 Log.d(TAG, "delete notes failed, ids:" + ids.toString());
                 return false;
             }
             return true;
         } catch (RemoteException e) {
             Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
         } catch (OperationApplicationException e) {
             Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
         }
         return false;
     }
 
     /**
      * 移动便签到指定文件夹
      * @param resolver ContentResolver对象
      * @param id 便签ID
      * @param srcFolderId 源文件夹ID
      * @param desFolderId 目标文件夹ID
      */
     public static void moveNoteToFoler(ContentResolver resolver, long id, long srcFolderId, long desFolderId) {
         ContentValues values = new ContentValues();
         values.put(NoteColumns.PARENT_ID, desFolderId); // 设置新父文件夹ID
         values.put(NoteColumns.ORIGIN_PARENT_ID, srcFolderId); // 记录原始父文件夹ID
         values.put(NoteColumns.LOCAL_MODIFIED, 1); // 标记为已修改
         resolver.update(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, id), values, null, null);
     }
 
     /**
      * 批量移动便签到指定文件夹
      * @param resolver ContentResolver对象
      * @param ids 要移动的便签ID集合
      * @param folderId 目标文件夹ID
      * @return 是否移动成功
      */
     public static boolean batchMoveToFolder(ContentResolver resolver, HashSet<Long> ids,
             long folderId) {
         if (ids == null) {
             Log.d(TAG, "the ids is null");
             return true;
         }
 
         // 构建批量更新操作列表
         ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
         for (long id : ids) {
             ContentProviderOperation.Builder builder = ContentProviderOperation
                     .newUpdate(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, id));
             builder.withValue(NoteColumns.PARENT_ID, folderId); // 设置新父文件夹ID
             builder.withValue(NoteColumns.LOCAL_MODIFIED, 1); // 标记为已修改
             operationList.add(builder.build());
         }
 
         // 执行批量操作
         try {
             ContentProviderResult[] results = resolver.applyBatch(Notes.AUTHORITY, operationList);
             if (results == null || results.length == 0 || results[0] == null) {
                 Log.d(TAG, "delete notes failed, ids:" + ids.toString());
                 return false;
             }
             return true;
         } catch (RemoteException e) {
             Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
         } catch (OperationApplicationException e) {
             Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
         }
         return false;
     }
 
     /**
      * 获取用户文件夹数量(排除系统文件夹)
      * @param resolver ContentResolver对象
      * @return 用户文件夹数量
      */
     public static int getUserFolderCount(ContentResolver resolver) {
         Cursor cursor = resolver.query(Notes.CONTENT_NOTE_URI,
                 new String[] { "COUNT(*)" },
                 NoteColumns.TYPE + "=? AND " + NoteColumns.PARENT_ID + "<>?", // 查询条件
                 new String[] { 
                     String.valueOf(Notes.TYPE_FOLDER), // 文件夹类型
                     String.valueOf(Notes.ID_TRASH_FOLER) // 排除回收站
                 },
                 null);
 
         int count = 0;
         if(cursor != null) {
             if(cursor.moveToFirst()) {
                 try {
                     count = cursor.getInt(0); // 获取数量
                 } catch (IndexOutOfBoundsException e) {
                     Log.e(TAG, "get folder count failed:" + e.toString());
                 } finally {
                     cursor.close();
                 }
             }
         }
         return count;
     }
 
     /**
      * 检查便签是否在数据库中且可见(不在回收站)
      * @param resolver ContentResolver对象
      * @param noteId 便签ID
      * @param type 便签类型
      * @return 是否存在且可见
      */
     public static boolean visibleInNoteDatabase(ContentResolver resolver, long noteId, int type) {
         Cursor cursor = resolver.query(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId),
                 null,
                 NoteColumns.TYPE + "=? AND " + NoteColumns.PARENT_ID + "<>" + Notes.ID_TRASH_FOLER,
                 new String [] {String.valueOf(type)},
                 null);
 
         boolean exist = false;
         if (cursor != null) {
             exist = cursor.getCount() > 0; // 判断是否存在记录
             cursor.close();
         }
         return exist;
     }
 
     /**
      * 检查便签是否存在于数据库中
      * @param resolver ContentResolver对象
      * @param noteId 便签ID
      * @return 是否存在
      */
     public static boolean existInNoteDatabase(ContentResolver resolver, long noteId) {
         Cursor cursor = resolver.query(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId),
                 null, null, null, null);
 
         boolean exist = false;
         if (cursor != null) {
             exist = cursor.getCount() > 0;
             cursor.close();
         }
         return exist;
     }
 
     /**
      * 检查数据项是否存在于数据库中
      * @param resolver ContentResolver对象
      * @param dataId 数据项ID
      * @return 是否存在
      */
     public static boolean existInDataDatabase(ContentResolver resolver, long dataId) {
         Cursor cursor = resolver.query(ContentUris.withAppendedId(Notes.CONTENT_DATA_URI, dataId),
                 null, null, null, null);
 
         boolean exist = false;
         if (cursor != null) {
             exist = cursor.getCount() > 0;
             cursor.close();
         }
         return exist;
     }
 
     /**
      * 检查指定名称的文件夹是否已存在(可见的)
      * @param resolver ContentResolver对象
      * @param name 文件夹名称
      * @return 是否存在
      */
     public static boolean checkVisibleFolderName(ContentResolver resolver, String name) {
         Cursor cursor = resolver.query(Notes.CONTENT_NOTE_URI, null,
                 NoteColumns.TYPE + "=" + Notes.TYPE_FOLDER + // 文件夹类型
                 " AND " + NoteColumns.PARENT_ID + "<>" + Notes.ID_TRASH_FOLER + // 不在回收站
                 " AND " + NoteColumns.SNIPPET + "=?", // 名称匹配
                 new String[] { name }, null);
         boolean exist = false;
         if(cursor != null) {
             exist = cursor.getCount() > 0;
             cursor.close();
         }
         return exist;
     }
 
     /**
      * 获取文件夹下所有便签的小部件属性
      * @param resolver ContentResolver对象
      * @param folderId 文件夹ID
      * @return 小部件属性集合
      */
     public static HashSet<AppWidgetAttribute> getFolderNoteWidget(ContentResolver resolver, long folderId) {
         Cursor c = resolver.query(Notes.CONTENT_NOTE_URI,
                 new String[] { NoteColumns.WIDGET_ID, NoteColumns.WIDGET_TYPE },
                 NoteColumns.PARENT_ID + "=?",
                 new String[] { String.valueOf(folderId) },
                 null);
 
         HashSet<AppWidgetAttribute> set = null;
         if (c != null) {
             if (c.moveToFirst()) {
                 set = new HashSet<AppWidgetAttribute>();
                 do {
                     try {
                         AppWidgetAttribute widget = new AppWidgetAttribute();
                         widget.widgetId = c.getInt(0); // 小部件ID
                         widget.widgetType = c.getInt(1); // 小部件类型
                         set.add(widget);
                     } catch (IndexOutOfBoundsException e) {
                         Log.e(TAG, e.toString());
                     }
                 } while (c.moveToNext());
             }
             c.close();
         }
         return set;
     }
 
     /**
      * 根据便签ID获取通话号码
      * @param resolver ContentResolver对象
      * @param noteId 便签ID
      * @return 通话号码
      */
     public static String getCallNumberByNoteId(ContentResolver resolver, long noteId) {
         Cursor cursor = resolver.query(Notes.CONTENT_DATA_URI,
                 new String [] { CallNote.PHONE_NUMBER },
                 CallNote.NOTE_ID + "=? AND " + CallNote.MIME_TYPE + "=?",
                 new String [] { 
                     String.valueOf(noteId), 
                     CallNote.CONTENT_ITEM_TYPE 
                 },
                 null);
 
         if (cursor != null && cursor.moveToFirst()) {
             try {
                 return cursor.getString(0); // 获取电话号码
             } catch (IndexOutOfBoundsException e) {
                 Log.e(TAG, "Get call number fails " + e.toString());
             } finally {
                 cursor.close();
             }
         }
         return ""; // 默认返回空字符串
     }
 
     /**
      * 根据电话号码和通话日期获取便签ID
      * @param resolver ContentResolver对象
      * @param phoneNumber 电话号码
      * @param callDate 通话日期
      * @return 便签ID，找不到返回0
      */
     public static long getNoteIdByPhoneNumberAndCallDate(ContentResolver resolver, String phoneNumber, long callDate) {
         Cursor cursor = resolver.query(Notes.CONTENT_DATA_URI,
                 new String [] { CallNote.NOTE_ID },
                 CallNote.CALL_DATE + "=? AND " + CallNote.MIME_TYPE + "=? AND PHONE_NUMBERS_EQUAL("
                 + CallNote.PHONE_NUMBER + ",?)",
                 new String [] { 
                     String.valueOf(callDate), 
                     CallNote.CONTENT_ITEM_TYPE, 
                     phoneNumber 
                 },
                 null);
 
         if (cursor != null) {
             if (cursor.moveToFirst()) {
                 try {
                     return cursor.getLong(0); // 获取便签ID
                 } catch (IndexOutOfBoundsException e) {
                     Log.e(TAG, "Get call note id fails " + e.toString());
                 }
             }
             cursor.close();
         }
         return 0; // 默认返回0
     }
 
     /**
      * 根据便签ID获取内容摘要
      * @param resolver ContentResolver对象
      * @param noteId 便签ID
      * @return 内容摘要
      * @throws IllegalArgumentException 如果便签不存在
      */
     public static String getSnippetById(ContentResolver resolver, long noteId) {
         Cursor cursor = resolver.query(Notes.CONTENT_NOTE_URI,
                 new String [] { NoteColumns.SNIPPET },
                 NoteColumns.ID + "=?",
                 new String [] { String.valueOf(noteId)},
                 null);
 
         if (cursor != null) {
             String snippet = "";
             if (cursor.moveToFirst()) {
                 snippet = cursor.getString(0); // 获取摘要内容
             }
             cursor.close();
             return snippet;
         }
         throw new IllegalArgumentException("Note is not found with id: " + noteId);
     }
 
     /**
      * 格式化内容摘要(去除换行和前后空格)
      * @param snippet 原始摘要
      * @return 格式化后的摘要
      */
     public static String getFormattedSnippet(String snippet) {
         if (snippet != null) {
             snippet = snippet.trim(); // 去除前后空格
             int index = snippet.indexOf('\n');
             if (index != -1) {
                 snippet = snippet.substring(0, index); // 截取第一行
             }
         }
         return snippet;
     }
 }