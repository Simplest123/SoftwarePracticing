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

 package net.micode.notes.data;

 import android.app.SearchManager;
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.text.TextUtils;
 import android.util.Log;
 
 import net.micode.notes.R;
 import net.micode.notes.data.Notes.DataColumns;
 import net.micode.notes.data.Notes.NoteColumns;
 import net.micode.notes.data.NotesDatabaseHelper.TABLE;
 
 // NotesProvider类实现ContentProvider，用于管理笔记数据
 public class NotesProvider extends ContentProvider {
     private static final UriMatcher mMatcher; // URI匹配器
     private NotesDatabaseHelper mHelper; // 数据库帮助类
     private static final String TAG = "NotesProvider"; // 日志标签
 
     // URI常量
     private static final int URI_NOTE            = 1; // 笔记URI
     private static final int URI_NOTE_ITEM       = 2; // 单个笔记URI
     private static final int URI_DATA            = 3; // 数据URI
     private static final int URI_DATA_ITEM       = 4; // 单个数据URI
     private static final int URI_SEARCH          = 5; // 搜索URI
     private static final int URI_SEARCH_SUGGEST  = 6; // 搜索建议URI
 
     // 静态代码块初始化URI匹配器
     static {
         mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
         mMatcher.addURI(Notes.AUTHORITY, "note", URI_NOTE);
         mMatcher.addURI(Notes.AUTHORITY, "note/#", URI_NOTE_ITEM);
         mMatcher.addURI(Notes.AUTHORITY, "data", URI_DATA);
         mMatcher.addURI(Notes.AUTHORITY, "data/#", URI_DATA_ITEM);
         mMatcher.addURI(Notes.AUTHORITY, "search", URI_SEARCH);
         mMatcher.addURI(Notes.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, URI_SEARCH_SUGGEST);
         mMatcher.addURI(Notes.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", URI_SEARCH_SUGGEST);
     }
 
     // 搜索投影，定义搜索结果的列
     private static final String NOTES_SEARCH_PROJECTION = NoteColumns.ID + ","
         + NoteColumns.ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA + ","
         + "TRIM(REPLACE(" + NoteColumns.SNIPPET + ", x'0A','')) AS " + SearchManager.SUGGEST_COLUMN_TEXT_1 + ","
         + "TRIM(REPLACE(" + NoteColumns.SNIPPET + ", x'0A','')) AS " + SearchManager.SUGGEST_COLUMN_TEXT_2 + ","
         + R.drawable.search_result + " AS " + SearchManager.SUGGEST_COLUMN_ICON_1 + ","
         + "'" + Intent.ACTION_VIEW + "' AS " + SearchManager.SUGGEST_COLUMN_INTENT_ACTION + ","
         + "'" + Notes.TextNote.CONTENT_TYPE + "' AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA;
 
     // 搜索查询语句
     private static String NOTES_SNIPPET_SEARCH_QUERY = "SELECT " + NOTES_SEARCH_PROJECTION
         + " FROM " + TABLE.NOTE
         + " WHERE " + NoteColumns.SNIPPET + " LIKE ?"
         + " AND " + NoteColumns.PARENT_ID + "<>" + Notes.ID_TRASH_FOLER
         + " AND " + NoteColumns.TYPE + "=" + Notes.TYPE_NOTE;
 
     // 创建时调用
     @Override
     public boolean onCreate() {
         mHelper = NotesDatabaseHelper.getInstance(getContext()); // 获取数据库帮助类实例
         return true;
     }
 
     // 查询数据
     @Override
     public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
             String sortOrder) {
         Cursor c = null; // 游标初始化
         SQLiteDatabase db = mHelper.getReadableDatabase(); // 获取可读数据库
         String id = null; // ID初始化
         switch (mMatcher.match(uri)) {
             case URI_NOTE:
                 c = db.query(TABLE.NOTE, projection, selection, selectionArgs, null, null,
                         sortOrder); // 查询笔记
                 break;
             case URI_NOTE_ITEM:
                 id = uri.getPathSegments().get(1); // 获取ID
                 c = db.query(TABLE.NOTE, projection, NoteColumns.ID + "=" + id
                         + parseSelection(selection), selectionArgs, null, null, sortOrder); // 查询单个笔记
                 break;
             case URI_DATA:
                 c = db.query(TABLE.DATA, projection, selection, selectionArgs, null, null,
                         sortOrder); // 查询数据
                 break;
             case URI_DATA_ITEM:
                 id = uri.getPathSegments().get(1); // 获取ID
                 c = db.query(TABLE.DATA, projection, DataColumns.ID + "=" + id
                         + parseSelection(selection), selectionArgs, null, null, sortOrder); // 查询单个数据
                 break;
             case URI_SEARCH:
             case URI_SEARCH_SUGGEST:
                 // 不允许指定排序、选择或投影参数
                 if (sortOrder != null || projection != null) {
                     throw new IllegalArgumentException(
                             "do not specify sortOrder, selection, selectionArgs, or projection" + "with this query");
                 }
 
                 String searchString = null; // 搜索字符串初始化
                 if (mMatcher.match(uri) == URI_SEARCH_SUGGEST) {
                     if (uri.getPathSegments().size() > 1) {
                         searchString = uri.getPathSegments().get(1); // 从路径中获取搜索字符串
                     }
                 } else {
                     searchString = uri.getQueryParameter("pattern"); // 从查询参数中获取搜索字符串
                 }
 
                 if (TextUtils.isEmpty(searchString)) {
                     return null; // 如果搜索字符串为空，返回null
                 }
 
                 try {
                     searchString = String.format("%%%s%%", searchString); // 格式化搜索字符串
                     c = db.rawQuery(NOTES_SNIPPET_SEARCH_QUERY,
                             new String[] { searchString }); // 执行搜索查询
                 } catch (IllegalStateException ex) {
                     Log.e(TAG, "got exception: " + ex.toString()); // 记录异常
                 }
                 break;
             default:
                 throw new IllegalArgumentException("Unknown URI " + uri); // 未知URI异常
         }
         if (c != null) {
             c.setNotificationUri(getContext().getContentResolver(), uri); // 设置通知URI
         }
         return c; // 返回游标
     }
 
     // 插入数据
     @Override
     public Uri insert(Uri uri, ContentValues values) {
         SQLiteDatabase db = mHelper.getWritableDatabase(); // 获取可写数据库
         long dataId = 0, noteId = 0, insertedId = 0; // ID初始化
         switch (mMatcher.match(uri)) {
             case URI_NOTE:
                 insertedId = noteId = db.insert(TABLE.NOTE, null, values); // 插入笔记
                 break;
             case URI_DATA:
                 if (values.containsKey(DataColumns.NOTE_ID)) {
                     noteId = values.getAsLong(DataColumns.NOTE_ID); // 获取笔记ID
                 } else {
                     Log.d(TAG, "Wrong data format without note id:" + values.toString()); // 记录错误日志
                 }
                 insertedId = dataId = db.insert(TABLE.DATA, null, values); // 插入数据
                 break;
             default:
                 throw new IllegalArgumentException("Unknown URI " + uri); // 未知URI异常
         }
         // 通知笔记URI
         if (noteId > 0) {
             getContext().getContentResolver().notifyChange(
                     ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId), null);
         }
 
         // 通知数据URI
         if (dataId > 0) {
             getContext().getContentResolver().notifyChange(
                     ContentUris.withAppendedId(Notes.CONTENT_DATA_URI, dataId), null);
         }
 
         return ContentUris.withAppendedId(uri, insertedId); // 返回插入的URI
     }
 
     // 删除数据
     @Override
     public int delete(Uri uri, String selection, String[] selectionArgs) {
         int count = 0; // 删除计数
         String id = null; // ID初始化
         SQLiteDatabase db = mHelper.getWritableDatabase(); // 获取可写数据库
         boolean deleteData = false; // 是否删除数据标志
         switch (mMatcher.match(uri)) {
             case URI_NOTE:
                 selection = "(" + selection + ") AND " + NoteColumns.ID + ">0 "; // 添加条件
                 count = db.delete(TABLE.NOTE, selection, selectionArgs); // 删除笔记
                 break;
             case URI_NOTE_ITEM:
                 id = uri.getPathSegments().get(1); // 获取ID
                 long noteId = Long.valueOf(id); // 转换为长整型
                 if (noteId <= 0) {
                     break; // 系统文件夹不允许删除
                 }
                 count = db.delete(TABLE.NOTE,
                         NoteColumns.ID + "=" + id + parseSelection(selection), selectionArgs); // 删除单个笔记
                 break;
             case URI_DATA:
                 count = db.delete(TABLE.DATA, selection, selectionArgs); // 删除数据
                 deleteData = true; // 设置删除数据标志
                 break;
             case URI_DATA_ITEM:
                 id = uri.getPathSegments().get(1); // 获取ID
                 count = db.delete(TABLE.DATA,
                         DataColumns.ID + "=" + id + parseSelection(selection), selectionArgs); // 删除单个数据
                 deleteData = true; // 设置删除数据标志
                 break;
             default:
                 throw new IllegalArgumentException("Unknown URI " + uri); // 未知URI异常
         }
         if (count > 0) {
             if (deleteData) {
                 getContext().getContentResolver().notifyChange(Notes.CONTENT_NOTE_URI, null); // 通知笔记URI
             }
             getContext().getContentResolver().notifyChange(uri, null); // 通知当前URI
         }
         return count; // 返回删除计数
     }
 
     // 更新数据
     @Override
     public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
         int count = 0; // 更新计数
         String id = null; // ID初始化
         SQLiteDatabase db = mHelper.getWritableDatabase(); // 获取可写数据库
         boolean updateData = false; // 是否更新数据标志
         switch (mMatcher.match(uri)) {
             case URI_NOTE:
                 increaseNoteVersion(-1, selection, selectionArgs); // 增加笔记版本
                 count = db.update(TABLE.NOTE, values, selection, selectionArgs); // 更新笔记
                 break;
             case URI_NOTE_ITEM:
                 id = uri.getPathSegments().get(1); // 获取ID
                 increaseNoteVersion(Long.valueOf(id), selection, selectionArgs); // 增加笔记版本
                 count = db.update(TABLE.NOTE, values, NoteColumns.ID + "=" + id
                         + parseSelection(selection), selectionArgs); // 更新单个笔记
                 break;
             case URI_DATA:
                 count = db.update(TABLE.DATA, values, selection, selectionArgs); // 更新数据
                 updateData = true; // 设置更新数据标志
                 break;
             case URI_DATA_ITEM:
                 id = uri.getPathSegments().get(1); // 获取ID
                 count = db.update(TABLE.DATA, values, DataColumns.ID + "=" + id
                         + parseSelection(selection), selectionArgs); // 更新单个数据
                 updateData = true; // 设置更新数据标志
                 break;
             default:
                 throw new IllegalArgumentException("Unknown URI " + uri); // 未知URI异常
         }
 
         if (count > 0) {
             if (updateData) {
                 getContext().getContentResolver().notifyChange(Notes.CONTENT_NOTE_URI, null); // 通知笔记URI
             }
             getContext().getContentResolver().notifyChange(uri, null); // 通知当前URI
         }
         return count; // 返回更新计数
     }
 
     // 解析选择条件
     private String parseSelection(String selection) {
         return (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""); // 返回选择条件
     }
 
     // 增加笔记版本
     private void increaseNoteVersion(long id, String selection, String[] selectionArgs) {
         StringBuilder sql = new StringBuilder(120); // SQL语句构建器
         sql.append("UPDATE ");
         sql.append(TABLE.NOTE);
         sql.append(" SET ");
         sql.append(NoteColumns.VERSION);
         sql.append("=" + NoteColumns.VERSION + "+1 "); // 增加版本
 
         if (id > 0 || !TextUtils.isEmpty(selection)) {
             sql.append(" WHERE "); // 添加WHERE条件
         }
         if (id > 0) {
             sql.append(NoteColumns.ID + "=" + String.valueOf(id)); // 添加ID条件
         }
         if (!TextUtils.isEmpty(selection)) {
             String selectString = id > 0 ? parseSelection(selection) : selection; // 处理选择条件
             for (String args : selectionArgs) {
                 selectString = selectString.replaceFirst("\\?", args); // 替换参数
             }
             sql.append(selectString); // 添加选择条件
         }
 
         mHelper.getWritableDatabase().execSQL(sql.toString()); // 执行更新SQL
     }
 
     // 获取数据类型
     @Override
     public String getType(Uri uri) {
         // TODO Auto-generated method stub
         return null; // 返回null，待实现
     }
 }
 