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

package net.micode.notes.gtask.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.DataConstants;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.data.NotesDatabaseHelper.TABLE;
import net.micode.notes.gtask.exception.ActionFailureException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 数据库交互工具类
 * 处理笔记数据的本地存储操作，支持创建/更新数据记录
 * 实现与系统ContentProvider的交互及数据版本控制
 */
public class SqlData {
    private static final String TAG = SqlData.class.getSimpleName();

    private static final int INVALID_ID = -99999; // 无效ID标识

    // 数据表查询字段投影（对应DATA表结构）
    public static final String[] PROJECTION_DATA = new String[] {
            DataColumns.ID, // 0.数据记录ID
            DataColumns.MIME_TYPE, // 1.MIME类型
            DataColumns.CONTENT, // 2.内容主体
            DataColumns.DATA1, // 3.扩展数据1（长整型）
            DataColumns.DATA3 // 4.扩展数据3（字符串）
    };

    // 列索引常量
    public static final int DATA_ID_COLUMN = 0;
    public static final int DATA_MIME_TYPE_COLUMN = 1;
    public static final int DATA_CONTENT_COLUMN = 2;
    public static final int DATA_CONTENT_DATA_1_COLUMN = 3;
    public static final int DATA_CONTENT_DATA_3_COLUMN = 4;

    private ContentResolver mContentResolver; // 内容解析器
    private boolean mIsCreate; // 新建记录标记
    private long mDataId; // 当前数据记录ID
    private String mDataMimeType; // MIME类型
    private String mDataContent; // 内容主体
    private long mDataContentData1; // 扩展数据1
    private String mDataContentData3; // 扩展数据3
    private ContentValues mDiffDataValues; // 差异数据集合（用于批量更新）

    /**
     * 新建数据构造器
     * 
     * @param context 上下文对象
     */
    public SqlData(Context context) {
        mContentResolver = context.getContentResolver();
        mIsCreate = true;
        mDataId = INVALID_ID;
        mDataMimeType = DataConstants.NOTE; // 默认MIME类型为笔记
        mDataContent = "";
        mDataContentData1 = 0;
        mDataContentData3 = "";
        mDiffDataValues = new ContentValues();
    }

    /**
     * 数据库记录构造器
     * 
     * @param context 上下文对象
     * @param c       数据库游标（需包含PROJECTION_DATA字段）
     */
    public SqlData(Context context, Cursor c) {
        mContentResolver = context.getContentResolver();
        mIsCreate = false;
        loadFromCursor(c);
        mDiffDataValues = new ContentValues();
    }

    /**
     * 从游标加载数据
     * 
     * @param c 已定位到目标记录的数据库游标
     */
    private void loadFromCursor(Cursor c) {
        mDataId = c.getLong(DATA_ID_COLUMN);
        mDataMimeType = c.getString(DATA_MIME_TYPE_COLUMN);
        mDataContent = c.getString(DATA_CONTENT_COLUMN);
        mDataContentData1 = c.getLong(DATA_CONTENT_DATA_1_COLUMN);
        mDataContentData3 = c.getString(DATA_CONTENT_DATA_3_COLUMN);
    }

    /**
     * 从JSON对象加载数据
     * 
     * @param js 包含数据字段的JSON对象
     * @throws JSONException 解析异常
     */
    public void setContent(JSONObject js) throws JSONException {
        // ID处理
        long dataId = js.has(DataColumns.ID) ? js.getLong(DataColumns.ID) : INVALID_ID;
        if (mIsCreate || mDataId != dataId) {
            mDiffDataValues.put(DataColumns.ID, dataId);
        }
        mDataId = dataId;

        // MIME类型处理
        String dataMimeType = js.has(DataColumns.MIME_TYPE) ? js.getString(DataColumns.MIME_TYPE)
                : DataConstants.NOTE;
        if (mIsCreate || !mDataMimeType.equals(dataMimeType)) {
            mDiffDataValues.put(DataColumns.MIME_TYPE, dataMimeType);
        }
        mDataMimeType = dataMimeType;

        // 内容主体处理
        String dataContent = js.has(DataColumns.CONTENT) ? js.getString(DataColumns.CONTENT) : "";
        if (mIsCreate || !mDataContent.equals(dataContent)) {
            mDiffDataValues.put(DataColumns.CONTENT, dataContent);
        }
        mDataContent = dataContent;

        // 扩展数据1处理（长整型）
        long dataContentData1 = js.has(DataColumns.DATA1) ? js.getLong(DataColumns.DATA1) : 0;
        if (mIsCreate || mDataContentData1 != dataContentData1) {
            mDiffDataValues.put(DataColumns.DATA1, dataContentData1);
        }
        mDataContentData1 = dataContentData1;

        // 扩展数据3处理（字符串）
        String dataContentData3 = js.has(DataColumns.DATA3) ? js.getString(DataColumns.DATA3) : "";
        if (mIsCreate || !mDataContentData3.equals(dataContentData3)) {
            mDiffDataValues.put(DataColumns.DATA3, dataContentData3);
        }
        mDataContentData3 = dataContentData3;
    }

    /**
     * 生成当前数据的JSON表示
     * 
     * @return JSON对象（包含所有数据字段）
     * @throws JSONException 序列化异常
     */
    public JSONObject getContent() throws JSONException {
        if (mIsCreate) {
            Log.e(TAG, "数据尚未持久化，无法生成JSON");
            return null;
        }
        JSONObject js = new JSONObject();
        js.put(DataColumns.ID, mDataId);
        js.put(DataColumns.MIME_TYPE, mDataMimeType);
        js.put(DataColumns.CONTENT, mDataContent);
        js.put(DataColumns.DATA1, mDataContentData1);
        js.put(DataColumns.DATA3, mDataContentData3);
        return js;
    }

    /**
     * 提交数据变更到数据库
     * 
     * @param noteId          关联的笔记ID
     * @param validateVersion 是否启用版本验证
     * @param version         当前数据版本号
     * @throws ActionFailureException 数据库操作失败时抛出
     */
    public void commit(long noteId, boolean validateVersion, long version) {
        if (mIsCreate) {
            // 新建记录处理
            if (mDataId == INVALID_ID && mDiffDataValues.containsKey(DataColumns.ID)) {
                mDiffDataValues.remove(DataColumns.ID); // 清除无效ID
            }

            mDiffDataValues.put(DataColumns.NOTE_ID, noteId); // 绑定笔记ID

            // 执行插入操作
            Uri uri = mContentResolver.insert(Notes.CONTENT_DATA_URI, mDiffDataValues);
            try {
                // 解析新记录的ID
                mDataId = Long.valueOf(uri.getPathSegments().get(1));
            } catch (NumberFormatException e) {
                Log.e(TAG, "ID解析异常：" + e);
                throw new ActionFailureException("创建笔记失败");
            }
        } else {
            // 更新记录处理
            if (mDiffDataValues.size() > 0) {
                int result = 0;
                Uri updateUri = ContentUris.withAppendedId(Notes.CONTENT_DATA_URI, mDataId);

                if (!validateVersion) {
                    // 无版本验证的更新
                    result = mContentResolver.update(updateUri, mDiffDataValues, null, null);
                } else {
                    // 带版本控制的更新（防止并发修改）
                    String where = " ? IN (SELECT " + NoteColumns.ID + " FROM " + TABLE.NOTE
                            + " WHERE " + NoteColumns.VERSION + "=?)";
                    String[] args = { String.valueOf(noteId), String.valueOf(version) };

                    result = mContentResolver.update(updateUri, mDiffDataValues, where, args);
                }

                if (result == 0) {
                    Log.w(TAG, "更新未生效（可能同步期间用户修改了数据）");
                }
            }
        }

        // 清理差异数据
        mDiffDataValues.clear();
        mIsCreate = false;
    }

    /**
     * 获取当前数据记录ID
     * 
     * @return 有效ID或INVALID_ID
     */
    public long getId() {
        return mDataId;
    }
}