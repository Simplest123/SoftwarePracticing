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

// 导入必要的Android类和组件
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

// 导入项目资源
import net.micode.notes.R;
import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.data.NotesDatabaseHelper.TABLE;

/**
 * 笔记内容提供者类，继承自ContentProvider
 * 负责管理笔记数据的存储和检索，提供CRUD操作接口
 */
public class NotesProvider extends ContentProvider {
    // URI匹配器，用于解析不同的URI请求
    private static final UriMatcher mMatcher;

    // 数据库帮助类实例
    private NotesDatabaseHelper mHelper;

    // 日志标签
    private static final String TAG = "NotesProvider";

    // URI匹配代码常量
    private static final int URI_NOTE            = 1;  // 操作整个笔记表
    private static final int URI_NOTE_ITEM       = 2;  // 操作单个笔记项
    private static final int URI_DATA            = 3;  // 操作整个数据表
    private static final int URI_DATA_ITEM       = 4;  // 操作单个数据项
    private static final int URI_SEARCH          = 5;  // 搜索操作
    private static final int URI_SEARCH_SUGGEST  = 6;  // 搜索建议操作

    // 静态初始化块，配置URI匹配规则
    static {
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mMatcher.addURI(Notes.AUTHORITY, "note", URI_NOTE);                  // content://net.micode.notes/note
        mMatcher.addURI(Notes.AUTHORITY, "note/#", URI_NOTE_ITEM);           // content://net.micode.notes/note/1
        mMatcher.addURI(Notes.AUTHORITY, "data", URI_DATA);                  // content://net.micode.notes/data
        mMatcher.addURI(Notes.AUTHORITY, "data/#", URI_DATA_ITEM);           // content://net.micode.notes/data/1
        mMatcher.addURI(Notes.AUTHORITY, "search", URI_SEARCH);              // 搜索功能
        mMatcher.addURI(Notes.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, URI_SEARCH_SUGGEST);          // 搜索建议基础URI
        mMatcher.addURI(Notes.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", URI_SEARCH_SUGGEST);   // 带查询参数的搜索建议
    }

    /**
     * 搜索结果的列投影配置
     * 使用SQLite的TRIM和REPLACE处理换行符
     * x'0A'表示SQLite中的换行符'\n'
     */
    private static final String NOTES_SEARCH_PROJECTION = NoteColumns.ID + ","
            + NoteColumns.ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA + ","
            + "TRIM(REPLACE(" + NoteColumns.SNIPPET + ", x'0A','')) AS " + SearchManager.SUGGEST_COLUMN_TEXT_1 + ","
            + "TRIM(REPLACE(" + NoteColumns.SNIPPET + ", x'0A','')) AS " + SearchManager.SUGGEST_COLUMN_TEXT_2 + ","
            + R.drawable.search_result + " AS " + SearchManager.SUGGEST_COLUMN_ICON_1 + ","
            + "'" + Intent.ACTION_VIEW + "' AS " + SearchManager.SUGGEST_COLUMN_INTENT_ACTION + ","
            + "'" + Notes.TextNote.CONTENT_TYPE + "' AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA;

    // 搜索查询SQL语句模板
    private static String NOTES_SNIPPET_SEARCH_QUERY = "SELECT " + NOTES_SEARCH_PROJECTION
            + " FROM " + TABLE.NOTE
            + " WHERE " + NoteColumns.SNIPPET + " LIKE ?"  // 模糊匹配片段内容
            + " AND " + NoteColumns.PARENT_ID + "<>" + Notes.ID_TRASH_FOLER  // 排除垃圾箱中的笔记
            + " AND " + NoteColumns.TYPE + "=" + Notes.TYPE_NOTE;  // 只匹配普通笔记类型

    @Override
    public boolean onCreate() {
        // 初始化数据库帮助类实例
        mHelper = NotesDatabaseHelper.getInstance(getContext());
        return true;  // 成功初始化返回true
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor c = null;
        SQLiteDatabase db = mHelper.getReadableDatabase();  // 获取可读数据库实例
        String id = null;

        // 根据URI匹配类型执行不同查询
        switch (mMatcher.match(uri)) {
            case URI_NOTE:
                // 查询整个笔记表
                c = db.query(TABLE.NOTE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case URI_NOTE_ITEM:
                // 查询指定ID的笔记
                id = uri.getPathSegments().get(1);  // 从URI路径获取ID
                c = db.query(TABLE.NOTE, projection, NoteColumns.ID + "=" + id
                        + parseSelection(selection), selectionArgs, null, null, sortOrder);
                break;
            case URI_DATA:
                // 查询整个数据表
                c = db.query(TABLE.DATA, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case URI_DATA_ITEM:
                // 查询指定ID的数据项
                id = uri.getPathSegments().get(1);
                c = db.query(TABLE.DATA, projection, DataColumns.ID + "=" + id
                        + parseSelection(selection), selectionArgs, null, null, sortOrder);
                break;
            case URI_SEARCH:
            case URI_SEARCH_SUGGEST:
                // 处理搜索建议请求
                if (sortOrder != null || projection != null) {
                    throw new IllegalArgumentException("禁止指定排序或投影参数");
                }

                String searchString = null;
                if (mMatcher.match(uri) == URI_SEARCH_SUGGEST) {
                    // 从建议URI获取搜索词
                    if (uri.getPathSegments().size() > 1) {
                        searchString = uri.getPathSegments().get(1);
                    }
                } else {
                    // 从普通搜索URI获取pattern参数
                    searchString = uri.getQueryParameter("pattern");
                }

                if (TextUtils.isEmpty(searchString)) {
                    return null;  // 空搜索词返回空结果
                }

                try {
                    // 构造模糊查询参数（添加%通配符）
                    searchString = String.format("%%%s%%", searchString);
                    c = db.rawQuery(NOTES_SNIPPET_SEARCH_QUERY, new String[] { searchString });
                } catch (IllegalStateException ex) {
                    Log.e(TAG, "查询异常: " + ex.toString());
                }
                break;
            default:
                throw new IllegalArgumentException("未知URI: " + uri);
        }

        // 设置内容变化通知
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mHelper.getWritableDatabase();  // 获取可写数据库实例
        long dataId = 0, noteId = 0, insertedId = 0;

        switch (mMatcher.match(uri)) {
            case URI_NOTE:
                // 插入新笔记
                insertedId = noteId = db.insert(TABLE.NOTE, null, values);
                break;
            case URI_DATA:
                // 插入新数据项
                if (values.containsKey(DataColumns.NOTE_ID)) {
                    noteId = values.getAsLong(DataColumns.NOTE_ID);
                } else {
                    Log.d(TAG, "数据格式错误，缺少note id: " + values.toString());
                }
                insertedId = dataId = db.insert(TABLE.DATA, null, values);
                break;
            default:
                throw new IllegalArgumentException("未知URI: " + uri);
        }

        // 通知相关URI的数据变化
        if (noteId > 0) {
            getContext().getContentResolver().notifyChange(
                    ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId), null);
        }
        if (dataId > 0) {
            getContext().getContentResolver().notifyChange(
                    ContentUris.withAppendedId(Notes.CONTENT_DATA_URI, dataId), null);
        }

        // 返回新插入项的URI
        return ContentUris.withAppendedId(uri, insertedId);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        String id = null;
        SQLiteDatabase db = mHelper.getWritableDatabase();
        boolean deleteData = false;  // 标记是否是删除数据表操作

        switch (mMatcher.match(uri)) {
            case URI_NOTE:
                // 删除笔记（排除系统文件夹）
                selection = "(" + selection + ") AND " + NoteColumns.ID + ">0 ";
                count = db.delete(TABLE.NOTE, selection, selectionArgs);
                break;
            case URI_NOTE_ITEM:
                // 删除指定ID的笔记
                id = uri.getPathSegments().get(1);
                long noteId = Long.valueOf(id);
                if (noteId <= 0) break;  // 系统文件夹不允许删除
                count = db.delete(TABLE.NOTE, NoteColumns.ID + "=" + id
                        + parseSelection(selection), selectionArgs);
                break;
            case URI_DATA:
                // 删除数据表记录
                count = db.delete(TABLE.DATA, selection, selectionArgs);
                deleteData = true;
                break;
            case URI_DATA_ITEM:
                // 删除指定ID的数据项
                id = uri.getPathSegments().get(1);
                count = db.delete(TABLE.DATA, DataColumns.ID + "=" + id
                        + parseSelection(selection), selectionArgs);
                deleteData = true;
                break;
            default:
                throw new IllegalArgumentException("未知URI: " + uri);
        }

        // 通知数据变化
        if (count > 0) {
            if (deleteData) {
                // 数据表删除需要通知笔记URI更新
                getContext().getContentResolver().notifyChange(Notes.CONTENT_NOTE_URI, null);
            }
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        String id = null;
        SQLiteDatabase db = mHelper.getWritableDatabase();
        boolean updateData = false;  // 标记是否是更新数据表

        switch (mMatcher.match(uri)) {
            case URI_NOTE:
                // 更新整个笔记表
                increaseNoteVersion(-1, selection, selectionArgs);
                count = db.update(TABLE.NOTE, values, selection, selectionArgs);
                break;
            case URI_NOTE_ITEM:
                // 更新指定ID的笔记
                id = uri.getPathSegments().get(1);
                increaseNoteVersion(Long.valueOf(id), selection, selectionArgs);
                count = db.update(TABLE.NOTE, values, NoteColumns.ID + "=" + id
                        + parseSelection(selection), selectionArgs);
                break;
            case URI_DATA:
                // 更新数据表
                count = db.update(TABLE.DATA, values, selection, selectionArgs);
                updateData = true;
                break;
            case URI_DATA_ITEM:
                // 更新指定ID的数据项
                id = uri.getPathSegments().get(1);
                count = db.update(TABLE.DATA, values, DataColumns.ID + "=" + id
                        + parseSelection(selection), selectionArgs);
                updateData = true;
                break;
            default:
                throw new IllegalArgumentException("未知URI: " + uri);
        }

        // 通知数据变化
        if (count > 0) {
            if (updateData) {
                getContext().getContentResolver().notifyChange(Notes.CONTENT_NOTE_URI, null);
            }
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    /**
     * 解析附加的查询条件
     * @param selection 原始查询条件
     * @return 组合后的条件字符串
     */
    private String parseSelection(String selection) {
        return (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
    }

    /**
     * 增加笔记版本号（用于同步/冲突检测）
     * @param id 笔记ID（-1表示批量更新）
     * @param selection 附加条件
     * @param selectionArgs 条件参数
     */
    private void increaseNoteVersion(long id, String selection, String[] selectionArgs) {
        StringBuilder sql = new StringBuilder(120);
        sql.append("UPDATE ").append(TABLE.NOTE)
                .append(" SET ").append(NoteColumns.VERSION).append("=").append(NoteColumns.VERSION).append("+1 ");

        // 构造WHERE条件
        if (id > 0 || !TextUtils.isEmpty(selection)) {
            sql.append(" WHERE ");
        }
        if (id > 0) {
            sql.append(NoteColumns.ID).append("=").append(id);
        }
        if (!TextUtils.isEmpty(selection)) {
            String selectString = id > 0 ? parseSelection(selection) : selection;
            // 直接替换参数（注意：存在SQL注入风险，建议使用参数化查询）
            for (String arg : selectionArgs) {
                selectString = selectString.replaceFirst("\\?", arg);
            }
            sql.append(selectString);
        }

        // 执行原始SQL语句
        mHelper.getWritableDatabase().execSQL(sql.toString());
    }

    @Override
    public String getType(Uri uri) {
        // TODO: 根据URI返回对应的MIME类型
        return null;
    }
}