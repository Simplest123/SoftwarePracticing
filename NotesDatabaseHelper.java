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
 
 // NotesDatabaseHelper类用于管理SQLite数据库的创建和版本管理
 public class NotesDatabaseHelper extends SQLiteOpenHelper {
     private static final String DB_NAME = "note.db"; // 数据库名称
     private static final int DB_VERSION = 4; // 数据库版本
 
     // 表名常量
     public interface TABLE {
         public static final String NOTE = "note"; // 笔记表
         public static final String DATA = "data"; // 数据表
     }
 
     private static final String TAG = "NotesDatabaseHelper"; // 日志标签
     private static NotesDatabaseHelper mInstance; // 单例实例
 
     // 创建笔记表的SQL语句
     private static final String CREATE_NOTE_TABLE_SQL =
         "CREATE TABLE " + TABLE.NOTE + "(" +
             NoteColumns.ID + " INTEGER PRIMARY KEY," +
             NoteColumns.PARENT_ID + " INTEGER NOT NULL DEFAULT 0," +
             NoteColumns.ALERTED_DATE + " INTEGER NOT NULL DEFAULT 0," +
             NoteColumns.BG_COLOR_ID + " INTEGER NOT NULL DEFAULT 0," +
             NoteColumns.CREATED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," +
             NoteColumns.HAS_ATTACHMENT + " INTEGER NOT NULL DEFAULT 0," +
             NoteColumns.MODIFIED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," +
             NoteColumns.NOTES_COUNT + " INTEGER NOT NULL DEFAULT 0," +
             NoteColumns.SNIPPET + " TEXT NOT NULL DEFAULT ''," +
             NoteColumns.TYPE + " INTEGER NOT NULL DEFAULT 0," +
             NoteColumns.WIDGET_ID + " INTEGER NOT NULL DEFAULT 0," +
             NoteColumns.WIDGET_TYPE + " INTEGER NOT NULL DEFAULT -1," +
             NoteColumns.SYNC_ID + " INTEGER NOT NULL DEFAULT 0," +
             NoteColumns.LOCAL_MODIFIED + " INTEGER NOT NULL DEFAULT 0," +
             NoteColumns.ORIGIN_PARENT_ID + " INTEGER NOT NULL DEFAULT 0," +
             NoteColumns.GTASK_ID + " TEXT NOT NULL DEFAULT ''," +
             NoteColumns.VERSION + " INTEGER NOT NULL DEFAULT 0" +
         ")";
 
     // 创建数据表的SQL语句
     private static final String CREATE_DATA_TABLE_SQL =
         "CREATE TABLE " + TABLE.DATA + "(" +
             DataColumns.ID + " INTEGER PRIMARY KEY," +
             DataColumns.MIME_TYPE + " TEXT NOT NULL," +
             DataColumns.NOTE_ID + " INTEGER NOT NULL DEFAULT 0," +
             NoteColumns.CREATED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," +
             NoteColumns.MODIFIED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," +
             DataColumns.CONTENT + " TEXT NOT NULL DEFAULT ''," +
             DataColumns.DATA1 + " INTEGER," +
             DataColumns.DATA2 + " INTEGER," +
             DataColumns.DATA3 + " TEXT NOT NULL DEFAULT ''," +
             DataColumns.DATA4 + " TEXT NOT NULL DEFAULT ''," +
             DataColumns.DATA5 + " TEXT NOT NULL DEFAULT ''" +
         ")";
 
     // 创建数据表中NOTE_ID的索引
     private static final String CREATE_DATA_NOTE_ID_INDEX_SQL =
         "CREATE INDEX IF NOT EXISTS note_id_index ON " +
         TABLE.DATA + "(" + DataColumns.NOTE_ID + ");";
 
     // 创建触发器以增加文件夹的笔记计数
     private static final String NOTE_INCREASE_FOLDER_COUNT_ON_UPDATE_TRIGGER =
         "CREATE TRIGGER increase_folder_count_on_update "+
         " AFTER UPDATE OF " + NoteColumns.PARENT_ID + " ON " + TABLE.NOTE +
         " BEGIN " +
         "  UPDATE " + TABLE.NOTE +
         "   SET " + NoteColumns.NOTES_COUNT + "=" + NoteColumns.NOTES_COUNT + " + 1" +
         "  WHERE " + NoteColumns.ID + "=new." + NoteColumns.PARENT_ID + ";" +
         " END";
 
     // 创建触发器以减少文件夹的笔记计数
     private static final String NOTE_DECREASE_FOLDER_COUNT_ON_UPDATE_TRIGGER =
         "CREATE TRIGGER decrease_folder_count_on_update " +
         " AFTER UPDATE OF " + NoteColumns.PARENT_ID + " ON " + TABLE.NOTE +
         " BEGIN " +
         "  UPDATE " + TABLE.NOTE +
         "   SET " + NoteColumns.NOTES_COUNT + "=" + NoteColumns.NOTES_COUNT + "-1" +
         "  WHERE " + NoteColumns.ID + "=old." + NoteColumns.PARENT_ID +
         "  AND " + NoteColumns.NOTES_COUNT + ">0" + ";" +
         " END";
 
     // 创建触发器以增加文件夹的笔记计数（插入新笔记时）
     private static final String NOTE_INCREASE_FOLDER_COUNT_ON_INSERT_TRIGGER =
         "CREATE TRIGGER increase_folder_count_on_insert " +
         " AFTER INSERT ON " + TABLE.NOTE +
         " BEGIN " +
         "  UPDATE " + TABLE.NOTE +
         "   SET " + NoteColumns.NOTES_COUNT + "=" + NoteColumns.NOTES_COUNT + " + 1" +
         "  WHERE " + NoteColumns.ID + "=new." + NoteColumns.PARENT_ID + ";" +
         " END";
 
     // 创建触发器以减少文件夹的笔记计数（删除笔记时）
     private static final String NOTE_DECREASE_FOLDER_COUNT_ON_DELETE_TRIGGER =
         "CREATE TRIGGER decrease_folder_count_on_delete " +
         " AFTER DELETE ON " + TABLE.NOTE +
         " BEGIN " +
         "  UPDATE " + TABLE.NOTE +
         "   SET " + NoteColumns.NOTES_COUNT + "=" + NoteColumns.NOTES_COUNT + "-1" +
         "  WHERE " + NoteColumns.ID + "=old." + NoteColumns.PARENT_ID +
         "  AND " + NoteColumns.NOTES_COUNT + ">0;" +
         " END";
 
     // 创建触发器以更新笔记内容（插入数据时）
     private static final String DATA_UPDATE_NOTE_CONTENT_ON_INSERT_TRIGGER =
         "CREATE TRIGGER update_note_content_on_insert " +
         " AFTER INSERT ON " + TABLE.DATA +
         " WHEN new." + DataColumns.MIME_TYPE + "='" + DataConstants.NOTE + "'" +
         " BEGIN" +
         "  UPDATE " + TABLE.NOTE +
         "   SET " + NoteColumns.SNIPPET + "=new." + DataColumns.CONTENT +
         "  WHERE " + NoteColumns.ID + "=new." + DataColumns.NOTE_ID + ";" +
         " END";
 
     // 创建触发器以更新笔记内容（更新数据时）
     private static final String DATA_UPDATE_NOTE_CONTENT_ON_UPDATE_TRIGGER =
         "CREATE TRIGGER update_note_content_on_update " +
         " AFTER UPDATE ON " + TABLE.DATA +
         " WHEN old." + DataColumns.MIME_TYPE + "='" + DataConstants.NOTE + "'" +
         " BEGIN" +
         "  UPDATE " + TABLE.NOTE +
         "   SET " + NoteColumns.SNIPPET + "=new." + DataColumns.CONTENT +
         "  WHERE " + NoteColumns.ID + "=new." + DataColumns.NOTE_ID + ";" +
         " END";
 
     // 创建触发器以更新笔记内容（删除数据时）
     private static final String DATA_UPDATE_NOTE_CONTENT_ON_DELETE_TRIGGER =
         "CREATE TRIGGER update_note_content_on_delete " +
         " AFTER DELETE ON " + TABLE.DATA +
         " WHEN old." + DataColumns.MIME_TYPE + "='" + DataConstants.NOTE + "'" +
         " BEGIN" +
         "  UPDATE " + TABLE.NOTE +
         "   SET " + NoteColumns.SNIPPET + "=''" +
         "  WHERE " + NoteColumns.ID + "=old." + DataColumns.NOTE_ID + ";" +
         " END";
 
     // 创建触发器以删除与已删除笔记相关的数据
     private static final String NOTE_DELETE_DATA_ON_DELETE_TRIGGER =
         "CREATE TRIGGER delete_data_on_delete " +
         " AFTER DELETE ON " + TABLE.NOTE +
         " BEGIN" +
         "  DELETE FROM " + TABLE.DATA +
         "   WHERE " + DataColumns.NOTE_ID + "=old." + NoteColumns.ID + ";" +
         " END";
 
     // 创建触发器以删除与已删除文件夹相关的笔记
     private static final String FOLDER_DELETE_NOTES_ON_DELETE_TRIGGER =
         "CREATE TRIGGER folder_delete_notes_on_delete " +
         " AFTER DELETE ON " + TABLE.NOTE +
         " BEGIN" +
         "  DELETE FROM " + TABLE.NOTE +
         "   WHERE " + NoteColumns.PARENT_ID + "=old." + NoteColumns.ID + ";" +
         " END";
 
     // 创建触发器以移动属于已移动到垃圾箱的文件夹的笔记
     private static final String FOLDER_MOVE_NOTES_ON_TRASH_TRIGGER =
         "CREATE TRIGGER folder_move_notes_on_trash " +
         " AFTER UPDATE ON " + TABLE.NOTE +
         " WHEN new." + NoteColumns.PARENT_ID + "=" + Notes.ID_TRASH_FOLER +
         " BEGIN" +
         "  UPDATE " + TABLE.NOTE +
         "   SET " + NoteColumns.PARENT_ID + "=" + Notes.ID_TRASH_FOLER +
         "  WHERE " + NoteColumns.PARENT_ID + "=old." + NoteColumns.ID + ";" +
         " END";
 
     // 构造函数
     public NotesDatabaseHelper(Context context) {
         super(context, DB_NAME, null, DB_VERSION); // 调用父类构造函数
     }
 
     // 创建笔记表
     public void createNoteTable(SQLiteDatabase db) {
         db.execSQL(CREATE_NOTE_TABLE_SQL); // 执行创建表的SQL
         reCreateNoteTableTriggers(db); // 重新创建触发器
         createSystemFolder(db); // 创建系统文件夹
         Log.d(TAG, "note table has been created"); // 日志输出
     }
 
     // 重新创建笔记表的触发器
     private void reCreateNoteTableTriggers(SQLiteDatabase db) {
         // 删除旧的触发器
         db.execSQL("DROP TRIGGER IF EXISTS increase_folder_count_on_update");
         db.execSQL("DROP TRIGGER IF EXISTS decrease_folder_count_on_update");
         db.execSQL("DROP TRIGGER IF EXISTS decrease_folder_count_on_delete");
         db.execSQL("DROP TRIGGER IF EXISTS delete_data_on_delete");
         db.execSQL("DROP TRIGGER IF EXISTS increase_folder_count_on_insert");
         db.execSQL("DROP TRIGGER IF EXISTS folder_delete_notes_on_delete");
         db.execSQL("DROP TRIGGER IF EXISTS folder_move_notes_on_trash");
 
         // 创建新的触发器
         db.execSQL(NOTE_INCREASE_FOLDER_COUNT_ON_UPDATE_TRIGGER);
         db.execSQL(NOTE_DECREASE_FOLDER_COUNT_ON_UPDATE_TRIGGER);
         db.execSQL(NOTE_DECREASE_FOLDER_COUNT_ON_DELETE_TRIGGER);
         db.execSQL(NOTE_DELETE_DATA_ON_DELETE_TRIGGER);
         db.execSQL(NOTE_INCREASE_FOLDER_COUNT_ON_INSERT_TRIGGER);
         db.execSQL(FOLDER_DELETE_NOTES_ON_DELETE_TRIGGER);
         db.execSQL(FOLDER_MOVE_NOTES_ON_TRASH_TRIGGER);
     }
 
     // 创建系统文件夹
     private void createSystemFolder(SQLiteDatabase db) {
         ContentValues values = new ContentValues();
 
         // 创建通话记录文件夹
         values.put(NoteColumns.ID, Notes.ID_CALL_RECORD_FOLDER);
         values.put(NoteColumns.TYPE, Notes.TYPE_SYSTEM);
         db.insert(TABLE.NOTE, null, values);
 
         // 创建根文件夹
         values.clear();
         values.put(NoteColumns.ID, Notes.ID_ROOT_FOLDER);
         values.put(NoteColumns.TYPE, Notes.TYPE_SYSTEM);
         db.insert(TABLE.NOTE, null, values);
 
         // 创建临时文件夹
         values.clear();
         values.put(NoteColumns.ID, Notes.ID_TEMPARAY_FOLDER);
         values.put(NoteColumns.TYPE, Notes.TYPE_SYSTEM);
         db.insert(TABLE.NOTE, null, values);
 
         // 创建垃圾箱文件夹
         values.clear();
         values.put(NoteColumns.ID, Notes.ID_TRASH_FOLER);
         values.put(NoteColumns.TYPE, Notes.TYPE_SYSTEM);
         db.insert(TABLE.NOTE, null, values);
     }
 
     // 创建数据表
     public void createDataTable(SQLiteDatabase db) {
         db.execSQL(CREATE_DATA_TABLE_SQL); // 执行创建表的SQL
         reCreateDataTableTriggers(db); // 重新创建触发器
         db.execSQL(CREATE_DATA_NOTE_ID_INDEX_SQL); // 创建索引
         Log.d(TAG, "data table has been created"); // 日志输出
     }
 
     // 重新创建数据表的触发器
     private void reCreateDataTableTriggers(SQLiteDatabase db) {
         // 删除旧的触发器
         db.execSQL("DROP TRIGGER IF EXISTS update_note_content_on_insert");
         db.execSQL("DROP TRIGGER IF EXISTS update_note_content_on_update");
         db.execSQL("DROP TRIGGER IF EXISTS update_note_content_on_delete");
 
         // 创建新的触发器
         db.execSQL(DATA_UPDATE_NOTE_CONTENT_ON_INSERT_TRIGGER);
         db.execSQL(DATA_UPDATE_NOTE_CONTENT_ON_UPDATE_TRIGGER);
         db.execSQL(DATA_UPDATE_NOTE_CONTENT_ON_DELETE_TRIGGER);
     }
 
     // 获取单例实例
     static synchronized NotesDatabaseHelper getInstance(Context context) {
         if (mInstance == null) {
             mInstance = new NotesDatabaseHelper(context);
         }
         return mInstance;
     }
 
     // 创建数据库时调用
     @Override
     public void onCreate(SQLiteDatabase db) {
         createNoteTable(db); // 创建笔记表
         createDataTable(db); // 创建数据表
     }
 
     // 升级数据库时调用
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         boolean reCreateTriggers = false; // 是否需要重新创建触发器
         boolean skipV2 = false; // 是否跳过V2升级
 
         if (oldVersion == 1) {
             upgradeToV2(db); // 升级到V2
             skipV2 = true; // 跳过V2升级
             oldVersion++;
         }
 
         if (oldVersion == 2 && !skipV2) {
             upgradeToV3(db); // 升级到V3
             reCreateTriggers = true; // 标记需要重新创建触发器
             oldVersion++;
         }
 
         if (oldVersion == 3) {
             upgradeToV4(db); // 升级到V4
             oldVersion++;
         }
 
         // 如果需要，重新创建触发器
         if (reCreateTriggers) {
             reCreateNoteTableTriggers(db);
             reCreateDataTableTriggers(db);
         }
 
         // 检查是否成功升级到新版本
         if (oldVersion != newVersion) {
             throw new IllegalStateException("Upgrade notes database to version " + newVersion
                     + " fails");
         }
     }
 
     // 升级到V2
     private void upgradeToV2(SQLiteDatabase db) {
         db.execSQL("DROP TABLE IF EXISTS " + TABLE.NOTE); // 删除旧表
         db.execSQL("DROP TABLE IF EXISTS " + TABLE.DATA); // 删除旧表
         createNoteTable(db); // 创建新表
         createDataTable(db); // 创建新表
     }
 
     // 升级到V3
     private void upgradeToV3(SQLiteDatabase db) {
         // 删除未使用的触发器
         db.execSQL("DROP TRIGGER IF EXISTS update_note_modified_date_on_insert");
         db.execSQL("DROP TRIGGER IF EXISTS update_note_modified_date_on_delete");
         db.execSQL("DROP TRIGGER IF EXISTS update_note_modified_date_on_update");
         
         // 为gtask ID添加新列
         db.execSQL("ALTER TABLE " + TABLE.NOTE + " ADD COLUMN " + NoteColumns.GTASK_ID
                 + " TEXT NOT NULL DEFAULT ''");
         
         // 添加垃圾箱系统文件夹
         ContentValues values = new ContentValues();
         values.put(NoteColumns.ID, Notes.ID_TRASH_FOLER);
         values.put(NoteColumns.TYPE, Notes.TYPE_SYSTEM);
         db.insert(TABLE.NOTE, null, values);
     }
 
     // 升级到V4
     private void upgradeToV4(SQLiteDatabase db) {
         // 为版本添加新列
         db.execSQL("ALTER TABLE " + TABLE.NOTE + " ADD COLUMN " + NoteColumns.VERSION
                 + " INTEGER NOT NULL DEFAULT 0");
     }
 }
 