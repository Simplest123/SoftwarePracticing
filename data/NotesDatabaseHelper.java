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

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.DataConstants;
import net.micode.notes.data.Notes.NoteColumns;

/**
 * 笔记应用的数据库帮助类，继承自SQLiteOpenHelper
 * 负责数据库的创建、升级和管理
 */
public class NotesDatabaseHelper extends SQLiteOpenHelper {
    // 数据库名称和版本常量
    private static final String DB_NAME = "note.db";  // 数据库文件名
    private static final int DB_VERSION = 4;          // 当前数据库版本

    // 数据库表名接口
    public interface TABLE {
        String NOTE = "note";  // 笔记表名
        String DATA = "data";  // 数据表名
    }

    private static final String TAG = "NotesDatabaseHelper";  // 日志标签
    private static NotesDatabaseHelper mInstance;             // 单例实例

    // 创建笔记表的SQL语句
    private static final String CREATE_NOTE_TABLE_SQL =
        "CREATE TABLE " + TABLE.NOTE + "(" +
            NoteColumns.ID + " INTEGER PRIMARY KEY," +               // 笔记ID，主键
            NoteColumns.PARENT_ID + " INTEGER NOT NULL DEFAULT 0," + // 父文件夹ID
            NoteColumns.ALERTED_DATE + " INTEGER NOT NULL DEFAULT 0," + // 提醒时间
            NoteColumns.BG_COLOR_ID + " INTEGER NOT NULL DEFAULT 0," +  // 背景颜色ID
            NoteColumns.CREATED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," + // 创建时间
            NoteColumns.HAS_ATTACHMENT + " INTEGER NOT NULL DEFAULT 0," + // 是否有附件
            NoteColumns.MODIFIED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," + // 修改时间
            NoteColumns.NOTES_COUNT + " INTEGER NOT NULL DEFAULT 0," + // 子笔记数量(用于文件夹)
            NoteColumns.SNIPPET + " TEXT NOT NULL DEFAULT ''," +      // 内容摘要
            NoteColumns.TYPE + " INTEGER NOT NULL DEFAULT 0," +       // 笔记类型
            NoteColumns.WIDGET_ID + " INTEGER NOT NULL DEFAULT 0," +  // 关联的小部件ID
            NoteColumns.WIDGET_TYPE + " INTEGER NOT NULL DEFAULT -1," + // 小部件类型
            NoteColumns.SYNC_ID + " INTEGER NOT NULL DEFAULT 0," +    // 同步ID
            NoteColumns.LOCAL_MODIFIED + " INTEGER NOT NULL DEFAULT 0," + // 本地修改标志
            NoteColumns.ORIGIN_PARENT_ID + " INTEGER NOT NULL DEFAULT 0," + // 原始父ID
            NoteColumns.GTASK_ID + " TEXT NOT NULL DEFAULT ''," +    // Google任务ID
            NoteColumns.VERSION + " INTEGER NOT NULL DEFAULT 0" +     // 版本号
        ")";

    // 创建数据表的SQL语句
    private static final String CREATE_DATA_TABLE_SQL =
        "CREATE TABLE " + TABLE.DATA + "(" +
            DataColumns.ID + " INTEGER PRIMARY KEY," +                // 数据ID，主键
            DataColumns.MIME_TYPE + " TEXT NOT NULL," +              // MIME类型
            DataColumns.NOTE_ID + " INTEGER NOT NULL DEFAULT 0," +   // 关联的笔记ID
            NoteColumns.CREATED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," + // 创建时间
            NoteColumns.MODIFIED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," + // 修改时间
            DataColumns.CONTENT + " TEXT NOT NULL DEFAULT ''," +     // 内容
            DataColumns.DATA1 + " INTEGER," +                        // 扩展字段1
            DataColumns.DATA2 + " INTEGER," +                        // 扩展字段2
            DataColumns.DATA3 + " TEXT NOT NULL DEFAULT ''," +       // 扩展字段3
            DataColumns.DATA4 + " TEXT NOT NULL DEFAULT ''," +       // 扩展字段4
            DataColumns.DATA5 + " TEXT NOT NULL DEFAULT ''" +        // 扩展字段5
        ")";

    // 创建数据表note_id索引的SQL语句
    private static final String CREATE_DATA_NOTE_ID_INDEX_SQL =
        "CREATE INDEX IF NOT EXISTS note_id_index ON " +
        TABLE.DATA + "(" + DataColumns.NOTE_ID + ");";

    // -------------------------- 笔记表触发器定义 --------------------------
    /**
     * 当更新笔记的父文件夹时，增加新父文件夹的笔记计数
     */
    private static final String NOTE_INCREASE_FOLDER_COUNT_ON_UPDATE_TRIGGER =
        "CREATE TRIGGER increase_folder_count_on_update "+
        " AFTER UPDATE OF " + NoteColumns.PARENT_ID + " ON " + TABLE.NOTE +
        " BEGIN " +
        "  UPDATE " + TABLE.NOTE +
        "   SET " + NoteColumns.NOTES_COUNT + "=" + NoteColumns.NOTES_COUNT + " + 1" +
        "  WHERE " + NoteColumns.ID + "=new." + NoteColumns.PARENT_ID + ";" +
        " END";

    /**
     * 当更新笔记的父文件夹时，减少旧父文件夹的笔记计数
     */
    private static final String NOTE_DECREASE_FOLDER_COUNT_ON_UPDATE_TRIGGER =
        "CREATE TRIGGER decrease_folder_count_on_update " +
        " AFTER UPDATE OF " + NoteColumns.PARENT_ID + " ON " + TABLE.NOTE +
        " BEGIN " +
        "  UPDATE " + TABLE.NOTE +
        "   SET " + NoteColumns.NOTES_COUNT + "=" + NoteColumns.NOTES_COUNT + "-1" +
        "  WHERE " + NoteColumns.ID + "=old." + NoteColumns.PARENT_ID +
        "  AND " + NoteColumns.NOTES_COUNT + ">0" + ";" +
        " END";

    /**
     * 当插入新笔记时，增加父文件夹的笔记计数
     */
    private static final String NOTE_INCREASE_FOLDER_COUNT_ON_INSERT_TRIGGER =
        "CREATE TRIGGER increase_folder_count_on_insert " +
        " AFTER INSERT ON " + TABLE.NOTE +
        " BEGIN " +
        "  UPDATE " + TABLE.NOTE +
        "   SET " + NoteColumns.NOTES_COUNT + "=" + NoteColumns.NOTES_COUNT + " + 1" +
        "  WHERE " + NoteColumns.ID + "=new." + NoteColumns.PARENT_ID + ";" +
        " END";

    /**
     * 当删除笔记时，减少父文件夹的笔记计数
     */
    private static final String NOTE_DECREASE_FOLDER_COUNT_ON_DELETE_TRIGGER =
        "CREATE TRIGGER decrease_folder_count_on_delete " +
        " AFTER DELETE ON " + TABLE.NOTE +
        " BEGIN " +
        "  UPDATE " + TABLE.NOTE +
        "   SET " + NoteColumns.NOTES_COUNT + "=" + NoteColumns.NOTES_COUNT + "-1" +
        "  WHERE " + NoteColumns.ID + "=old." + NoteColumns.PARENT_ID +
        "  AND " + NoteColumns.NOTES_COUNT + ">0;" +
        " END";

    // -------------------------- 数据表触发器定义 --------------------------
    /**
     * 当插入数据类型为NOTE时，更新笔记摘要
     */
    private static final String DATA_UPDATE_NOTE_CONTENT_ON_INSERT_TRIGGER =
        "CREATE TRIGGER update_note_content_on_insert " +
        " AFTER INSERT ON " + TABLE.DATA +
        " WHEN new." + DataColumns.MIME_TYPE + "='" + DataConstants.NOTE + "'" +
        " BEGIN" +
        "  UPDATE " + TABLE.NOTE +
        "   SET " + NoteColumns.SNIPPET + "=new." + DataColumns.CONTENT +
        "  WHERE " + NoteColumns.ID + "=new." + DataColumns.NOTE_ID + ";" +
        " END";

    /**
     * 当更新数据类型为NOTE时，更新笔记摘要
     */
    private static final String DATA_UPDATE_NOTE_CONTENT_ON_UPDATE_TRIGGER =
        "CREATE TRIGGER update_note_content_on_update " +
        " AFTER UPDATE ON " + TABLE.DATA +
        " WHEN old." + DataColumns.MIME_TYPE + "='" + DataConstants.NOTE + "'" +
        " BEGIN" +
        "  UPDATE " + TABLE.NOTE +
        "   SET " + NoteColumns.SNIPPET + "=new." + DataColumns.CONTENT +
        "  WHERE " + NoteColumns.ID + "=new." + DataColumns.NOTE_ID + ";" +
        " END";

    /**
     * 当删除数据类型为NOTE时，清空笔记摘要
     */
    private static final String DATA_UPDATE_NOTE_CONTENT_ON_DELETE_TRIGGER =
        "CREATE TRIGGER update_note_content_on_delete " +
        " AFTER delete ON " + TABLE.DATA +
        " WHEN old." + DataColumns.MIME_TYPE + "='" + DataConstants.NOTE + "'" +
        " BEGIN" +
        "  UPDATE " + TABLE.NOTE +
        "   SET " + NoteColumns.SNIPPET + "=''" +
        "  WHERE " + NoteColumns.ID + "=old." + DataColumns.NOTE_ID + ";" +
        " END";

    /**
     * 当删除笔记时，级联删除关联数据
     */
    private static final String NOTE_DELETE_DATA_ON_DELETE_TRIGGER =
        "CREATE TRIGGER delete_data_on_delete " +
        " AFTER DELETE ON " + TABLE.NOTE +
        " BEGIN" +
        "  DELETE FROM " + TABLE.DATA +
        "   WHERE " + DataColumns.NOTE_ID + "=old." + NoteColumns.ID + ";" +
        " END";

    /**
     * 当删除文件夹时，级联删除其下所有笔记
     */
    private static final String FOLDER_DELETE_NOTES_ON_DELETE_TRIGGER =
        "CREATE TRIGGER folder_delete_notes_on_delete " +
        " AFTER DELETE ON " + TABLE.NOTE +
        " BEGIN" +
        "  DELETE FROM " + TABLE.NOTE +
        "   WHERE " + NoteColumns.PARENT_ID + "=old." + NoteColumns.ID + ";" +
        " END";

    /**
     * 当移动文件夹到回收站时，将其下所有笔记也移动到回收站
     */
    private static final String FOLDER_MOVE_NOTES_ON_TRASH_TRIGGER =
        "CREATE TRIGGER folder_move_notes_on_trash " +
        " AFTER UPDATE ON " + TABLE.NOTE +
        " WHEN new." + NoteColumns.PARENT_ID + "=" + Notes.ID_TRASH_FOLER +
        " BEGIN" +
        "  UPDATE " + TABLE.NOTE +
        "   SET " + NoteColumns.PARENT_ID + "=" + Notes.ID_TRASH_FOLER +
        "  WHERE " + NoteColumns.PARENT_ID + "=old." + NoteColumns.ID + ";" +
        " END";

    // -------------------------- 构造函数和单例方法 --------------------------
    public NotesDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * 获取数据库帮助类单例
     * @param context 应用上下文
     * @return 数据库帮助类实例
     */
    public static synchronized NotesDatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NotesDatabaseHelper(context);
        }
        return mInstance;
    }

    // -------------------------- 数据库创建方法 --------------------------
    @Override
    public void onCreate(SQLiteDatabase db) {
        createNoteTable(db);  // 创建笔记表
        createDataTable(db);  // 创建数据表
    }

    /**
     * 创建笔记表及其触发器
     * @param db SQLite数据库对象
     */
    public void createNoteTable(SQLiteDatabase db) {
        db.execSQL(CREATE_NOTE_TABLE_SQL);  // 执行创建表SQL
        reCreateNoteTableTriggers(db);      // 重建触发器
        createSystemFolder(db);            // 创建系统文件夹
        Log.d(TAG, "note表已创建");
    }

    /**
     * 重建所有笔记表触发器
     */
    private void reCreateNoteTableTriggers(SQLiteDatabase db) {
        // 删除旧触发器
        db.execSQL("DROP TRIGGER IF EXISTS increase_folder_count_on_update");
        db.execSQL("DROP TRIGGER IF EXISTS decrease_folder_count_on_update");
        db.execSQL("DROP TRIGGER IF EXISTS decrease_folder_count_on_delete");
        db.execSQL("DROP TRIGGER IF EXISTS delete_data_on_delete");
        db.execSQL("DROP TRIGGER IF EXISTS increase_folder_count_on_insert");
        db.execSQL("DROP TRIGGER IF EXISTS folder_delete_notes_on_delete");
        db.execSQL("DROP TRIGGER IF EXISTS folder_move_notes_on_trash");

        // 创建新触发器
        db.execSQL(NOTE_INCREASE_FOLDER_COUNT_ON_UPDATE_TRIGGER);
        db.execSQL(NOTE_DECREASE_FOLDER_COUNT_ON_UPDATE_TRIGGER);
        db.execSQL(NOTE_DECREASE_FOLDER_COUNT_ON_DELETE_TRIGGER);
        db.execSQL(NOTE_DELETE_DATA_ON_DELETE_TRIGGER);
        db.execSQL(NOTE_INCREASE_FOLDER_COUNT_ON_INSERT_TRIGGER);
        db.execSQL(FOLDER_DELETE_NOTES_ON_DELETE_TRIGGER);
        db.execSQL(FOLDER_MOVE_NOTES_ON_TRASH_TRIGGER);
    }

    /**
     * 创建系统文件夹
     */
    private void createSystemFolder(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        // 通话记录文件夹
        values.put(NoteColumns.ID, Notes.ID_CALL_RECORD_FOLDER);
        values.put(NoteColumns.TYPE, Notes.TYPE_SYSTEM);
        db.insert(TABLE.NOTE, null, values);

        // 根文件夹
        values.clear();
        values.put(NoteColumns.ID, Notes.ID_ROOT_FOLDER);
        values.put(NoteColumns.TYPE, Notes.TYPE_SYSTEM);
        db.insert(TABLE.NOTE, null, values);

        // 临时文件夹
        values.clear();
        values.put(NoteColumns.ID, Notes.ID_TEMPARAY_FOLDER);
        values.put(NoteColumns.TYPE, Notes.TYPE_SYSTEM);
        db.insert(TABLE.NOTE, null, values);

        // 回收站
        values.clear();
        values.put(NoteColumns.ID, Notes.ID_TRASH_FOLER);
        values.put(NoteColumns.TYPE, Notes.TYPE_SYSTEM);
        db.insert(TABLE.NOTE, null, values);
    }

    /**
     * 创建数据表及其触发器
     */
    public void createDataTable(SQLiteDatabase db) {
        db.execSQL(CREATE_DATA_TABLE_SQL);  // 执行创建表SQL
        reCreateDataTableTriggers(db);      // 重建触发器
        db.execSQL(CREATE_DATA_NOTE_ID_INDEX_SQL); // 创建索引
        Log.d(TAG, "data表已创建");
    }

    /**
     * 重建所有数据表触发器
     */
    private void reCreateDataTableTriggers(SQLiteDatabase db) {
        // 删除旧触发器
        db.execSQL("DROP TRIGGER IF EXISTS update_note_content_on_insert");
        db.execSQL("DROP TRIGGER IF EXISTS update_note_content_on_update");
        db.execSQL("DROP TRIGGER IF EXISTS update_note_content_on_delete");

        // 创建新触发器
        db.execSQL(DATA_UPDATE_NOTE_CONTENT_ON_INSERT_TRIGGER);
        db.execSQL(DATA_UPDATE_NOTE_CONTENT_ON_UPDATE_TRIGGER);
        db.execSQL(DATA_UPDATE_NOTE_CONTENT_ON_DELETE_TRIGGER);
    }

    // -------------------------- 数据库升级方法 --------------------------
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        boolean reCreateTriggers = false;
        boolean skipV2 = false;

        // 从V1升级到V2（完全重建表）
        if (oldVersion == 1) {
            upgradeToV2(db);
            skipV2 = true; // 该升级包含V2到V3的升级
            oldVersion++;
        }

        // 从V2升级到V3（添加回收站和GTASK字段）
        if (oldVersion == 2 && !skipV2) {
            upgradeToV3(db);
            reCreateTriggers = true;
            oldVersion++;
        }

        // 从V3升级到V4（添加版本号字段）
        if (oldVersion == 3) {
            upgradeToV4(db);
            oldVersion++;
        }

        // 重建触发器
        if (reCreateTriggers) {
            reCreateNoteTableTriggers(db);
            reCreateDataTableTriggers(db);
        }

        if (oldVersion != newVersion) {
            throw new IllegalStateException("数据库升级失败：当前版本" + oldVersion + "无法升级到" + newVersion);
        }
    }

    /**
     * 升级到V2版本（完全重建表）
     */
    private void upgradeToV2(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE.NOTE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE.DATA);
        createNoteTable(db);
        createDataTable(db);
    }

    /**
     * 升级到V3版本（添加GTASK_ID字段和回收站）
     */
    private void upgradeToV3(SQLiteDatabase db) {
        // 删除旧触发器
        db.execSQL("DROP TRIGGER IF EXISTS update_note_modified_date_on_insert");
        db.execSQL("DROP TRIGGER IF EXISTS update_note_modified_date_on_delete");
        db.execSQL("DROP TRIGGER IF EXISTS update_note_modified_date_on_update");

        // 添加GTASK_ID字段
        db.execSQL("ALTER TABLE " + TABLE.NOTE + " ADD COLUMN " + NoteColumns.GTASK_ID
                + " TEXT NOT NULL DEFAULT ''");

        // 添加回收站系统文件夹
        ContentValues values = new ContentValues();
        values.put(NoteColumns.ID, Notes.ID_TRASH_FOLER);
        values.put(NoteColumns.TYPE, Notes.TYPE_SYSTEM);
        db.insert(TABLE.NOTE, null, values);
    }

    /**
     * 升级到V4版本（添加版本号字段）
     */
    private void upgradeToV4(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + TABLE.NOTE + " ADD COLUMN " + NoteColumns.VERSION
                + " INTEGER NOT NULL DEFAULT 0");
    }
}