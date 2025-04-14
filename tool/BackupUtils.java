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

 import android.content.Context;
 import android.database.Cursor;
 import android.os.Environment;
 import android.text.TextUtils;
 import android.text.format.DateFormat;
 import android.util.Log;
 
 import net.micode.notes.R;
 import net.micode.notes.data.Notes;
 import net.micode.notes.data.Notes.DataColumns;
 import net.micode.notes.data.Notes.DataConstants;
 import net.micode.notes.data.Notes.NoteColumns;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 
 /**
  * 备份工具类 - 处理便签数据的导出和恢复功能
  */
 public class BackupUtils {
     private static final String TAG = "BackupUtils";
     
     // 单例模式实现
     private static BackupUtils sInstance;
 
     /**
      * 获取备份工具单例
      * @param context 上下文对象
      * @return BackupUtils实例
      */
     public static synchronized BackupUtils getInstance(Context context) {
         if (sInstance == null) {
             sInstance = new BackupUtils(context);
         }
         return sInstance;
     }
 
     /**
      * 备份/恢复状态码定义
      */
     public static final int STATE_SD_CARD_UNMOUONTED        = 0; // SD卡未挂载
     public static final int STATE_BACKUP_FILE_NOT_EXIST     = 1; // 备份文件不存在
     public static final int STATE_DATA_DESTROIED            = 2; // 数据损坏
     public static final int STATE_SYSTEM_ERROR              = 3; // 系统错误
     public static final int STATE_SUCCESS                   = 4; // 操作成功
 
     private TextExport mTextExport; // 文本导出处理器
 
     /**
      * 私有构造函数
      */
     private BackupUtils(Context context) {
         mTextExport = new TextExport(context);
     }
 
     /**
      * 检查外部存储是否可用
      */
     private static boolean externalStorageAvailable() {
         return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
     }
 
     /**
      * 导出便签到文本文件
      * @return 操作状态码
      */
     public int exportToText() {
         return mTextExport.exportToText();
     }
 
     /**
      * 获取导出的文本文件名
      */
     public String getExportedTextFileName() {
         return mTextExport.mFileName;
     }
 
     /**
      * 获取导出的文本文件目录
      */
     public String getExportedTextFileDir() {
         return mTextExport.mFileDirectory;
     }
 
     /**
      * 文本导出内部类 - 处理便签数据到文本文件的转换
      */
     private static class TextExport {
         // 笔记查询字段
         private static final String[] NOTE_PROJECTION = {
                 NoteColumns.ID,
                 NoteColumns.MODIFIED_DATE,
                 NoteColumns.SNIPPET,
                 NoteColumns.TYPE
         };
 
         private static final int NOTE_COLUMN_ID = 0;
         private static final int NOTE_COLUMN_MODIFIED_DATE = 1;
         private static final int NOTE_COLUMN_SNIPPET = 2;
 
         // 数据查询字段
         private static final String[] DATA_PROJECTION = {
                 DataColumns.CONTENT,
                 DataColumns.MIME_TYPE,
                 DataColumns.DATA1,
                 DataColumns.DATA2,
                 DataColumns.DATA3,
                 DataColumns.DATA4,
         };
 
         private static final int DATA_COLUMN_CONTENT = 0;
         private static final int DATA_COLUMN_MIME_TYPE = 1;
         private static final int DATA_COLUMN_CALL_DATE = 2;
         private static final int DATA_COLUMN_PHONE_NUMBER = 4;
 
         // 文本格式定义
         private final String [] TEXT_FORMAT;
         private static final int FORMAT_FOLDER_NAME = 0;  // 文件夹名称格式
         private static final int FORMAT_NOTE_DATE = 1;    // 笔记日期格式
         private static final int FORMAT_NOTE_CONTENT = 2; // 笔记内容格式
 
         private Context mContext;
         private String mFileName;      // 导出文件名
         private String mFileDirectory; // 导出目录
 
         /**
          * 构造函数
          */
         public TextExport(Context context) {
             // 从资源文件加载文本格式
             TEXT_FORMAT = context.getResources().getStringArray(R.array.format_for_exported_note);
             mContext = context;
             mFileName = "";
             mFileDirectory = "";
         }
 
         /**
          * 获取指定格式
          */
         private String getFormat(int id) {
             return TEXT_FORMAT[id];
         }
 
         /**
          * 导出指定文件夹下的所有便签
          * @param folderId 文件夹ID
          * @param ps 输出流
          */
         private void exportFolderToText(String folderId, PrintStream ps) {
             // 查询该文件夹下的所有便签
             Cursor notesCursor = mContext.getContentResolver().query(
                     Notes.CONTENT_NOTE_URI,
                     NOTE_PROJECTION, 
                     NoteColumns.PARENT_ID + "=?", 
                     new String[] { folderId }, 
                     null);
 
             if (notesCursor != null) {
                 if (notesCursor.moveToFirst()) {
                     do {
                         // 输出便签修改日期
                         ps.println(String.format(getFormat(FORMAT_NOTE_DATE), 
                                 DateFormat.format(
                                     mContext.getString(R.string.format_datetime_mdhm),
                                     notesCursor.getLong(NOTE_COLUMN_MODIFIED_DATE))));
                         
                         // 导出该便签内容
                         String noteId = notesCursor.getString(NOTE_COLUMN_ID);
                         exportNoteToText(noteId, ps);
                     } while (notesCursor.moveToNext());
                 }
                 notesCursor.close();
             }
         }
 
         /**
          * 导出单个便签内容
          * @param noteId 便签ID
          * @param ps 输出流
          */
         private void exportNoteToText(String noteId, PrintStream ps) {
             // 查询该便签的所有数据项
             Cursor dataCursor = mContext.getContentResolver().query(
                     Notes.CONTENT_DATA_URI,
                     DATA_PROJECTION, 
                     DataColumns.NOTE_ID + "=?", 
                     new String[] { noteId }, 
                     null);
 
             if (dataCursor != null) {
                 if (dataCursor.moveToFirst()) {
                     do {
                         String mimeType = dataCursor.getString(DATA_COLUMN_MIME_TYPE);
                         if (DataConstants.CALL_NOTE.equals(mimeType)) {
                             // 处理通话记录类型便签
                             String phoneNumber = dataCursor.getString(DATA_COLUMN_PHONE_NUMBER);
                             long callDate = dataCursor.getLong(DATA_COLUMN_CALL_DATE);
                             String location = dataCursor.getString(DATA_COLUMN_CONTENT);
 
                             // 输出电话号码
                             if (!TextUtils.isEmpty(phoneNumber)) {
                                 ps.println(String.format(getFormat(FORMAT_NOTE_CONTENT),
                                         phoneNumber));
                             }
                             // 输出通话日期
                             ps.println(String.format(getFormat(FORMAT_NOTE_CONTENT), 
                                     DateFormat.format(
                                         mContext.getString(R.string.format_datetime_mdhm),
                                         callDate)));
                             // 输出通话位置
                             if (!TextUtils.isEmpty(location)) {
                                 ps.println(String.format(getFormat(FORMAT_NOTE_CONTENT),
                                         location));
                             }
                         } else if (DataConstants.NOTE.equals(mimeType)) {
                             // 处理普通文本便签
                             String content = dataCursor.getString(DATA_COLUMN_CONTENT);
                             if (!TextUtils.isEmpty(content)) {
                                 ps.println(String.format(getFormat(FORMAT_NOTE_CONTENT),
                                         content));
                             }
                         }
                     } while (dataCursor.moveToNext());
                 }
                 dataCursor.close();
             }
             
             // 输出便签分隔符
             try {
                 ps.write(new byte[] {
                         Character.LINE_SEPARATOR, Character.LETTER_NUMBER
                 });
             } catch (IOException e) {
                 Log.e(TAG, e.toString());
             }
         }
 
         /**
          * 执行导出操作
          * @return 操作状态码
          */
         public int exportToText() {
             // 检查存储状态
             if (!externalStorageAvailable()) {
                 Log.d(TAG, "Media was not mounted");
                 return STATE_SD_CARD_UNMOUONTED;
             }
 
             // 获取输出流
             PrintStream ps = getExportToTextPrintStream();
             if (ps == null) {
                 Log.e(TAG, "get print stream error");
                 return STATE_SYSTEM_ERROR;
             }
             
             // 导出所有文件夹及其便签
             Cursor folderCursor = mContext.getContentResolver().query(
                     Notes.CONTENT_NOTE_URI,
                     NOTE_PROJECTION,
                     "(" + NoteColumns.TYPE + "=" + Notes.TYPE_FOLDER + " AND "
                             + NoteColumns.PARENT_ID + "<>" + Notes.ID_TRASH_FOLER + ") OR "
                             + NoteColumns.ID + "=" + Notes.ID_CALL_RECORD_FOLDER, 
                     null, null);
 
             if (folderCursor != null) {
                 if (folderCursor.moveToFirst()) {
                     do {
                         // 输出文件夹名称
                         String folderName = "";
                         if(folderCursor.getLong(NOTE_COLUMN_ID) == Notes.ID_CALL_RECORD_FOLDER) {
                             folderName = mContext.getString(R.string.call_record_folder_name);
                         } else {
                             folderName = folderCursor.getString(NOTE_COLUMN_SNIPPET);
                         }
                         if (!TextUtils.isEmpty(folderName)) {
                             ps.println(String.format(getFormat(FORMAT_FOLDER_NAME), folderName));
                         }
                         // 导出该文件夹下的便签
                         String folderId = folderCursor.getString(NOTE_COLUMN_ID);
                         exportFolderToText(folderId, ps);
                     } while (folderCursor.moveToNext());
                 }
                 folderCursor.close();
             }
 
             // 导出根目录下的便签
             Cursor noteCursor = mContext.getContentResolver().query(
                     Notes.CONTENT_NOTE_URI,
                     NOTE_PROJECTION,
                     NoteColumns.TYPE + "=" + Notes.TYPE_NOTE + " AND " + NoteColumns.PARENT_ID
                             + "=0", 
                     null, null);
 
             if (noteCursor != null) {
                 if (noteCursor.moveToFirst()) {
                     do {
                         ps.println(String.format(getFormat(FORMAT_NOTE_DATE), 
                                 DateFormat.format(
                                     mContext.getString(R.string.format_datetime_mdhm),
                                     noteCursor.getLong(NOTE_COLUMN_MODIFIED_DATE))));
                         // 导出该便签内容
                         String noteId = noteCursor.getString(NOTE_COLUMN_ID);
                         exportNoteToText(noteId, ps);
                     } while (noteCursor.moveToNext());
                 }
                 noteCursor.close();
             }
             ps.close();
 
             return STATE_SUCCESS;
         }
 
         /**
          * 获取导出文件的输出流
          */
         private PrintStream getExportToTextPrintStream() {
             // 生成导出文件
             File file = generateFileMountedOnSDcard(
                     mContext, 
                     R.string.file_path,
                     R.string.file_name_txt_format);
             if (file == null) {
                 Log.e(TAG, "create file to exported failed");
                 return null;
             }
             
             // 记录文件信息
             mFileName = file.getName();
             mFileDirectory = mContext.getString(R.string.file_path);
             
             // 创建输出流
             PrintStream ps = null;
             try {
                 FileOutputStream fos = new FileOutputStream(file);
                 ps = new PrintStream(fos);
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
                 return null;
             }
             return ps;
         }
     }
 
     /**
      * 在SD卡上创建导出文件
      * @param context 上下文
      * @param filePathResId 文件路径资源ID
      * @param fileNameFormatResId 文件名格式资源ID
      * @return 创建的文件对象
      */
     private static File generateFileMountedOnSDcard(Context context, 
             int filePathResId, int fileNameFormatResId) {
         // 构建文件路径
         StringBuilder sb = new StringBuilder();
         sb.append(Environment.getExternalStorageDirectory());
         sb.append(context.getString(filePathResId));
         File filedir = new File(sb.toString());
         
         // 构建完整文件名(含日期)
         sb.append(context.getString(
                 fileNameFormatResId,
                 DateFormat.format(
                     context.getString(R.string.format_date_ymd),
                     System.currentTimeMillis())));
         File file = new File(sb.toString());
 
         try {
             // 创建目录和文件
             if (!filedir.exists()) {
                 filedir.mkdir();
             }
             if (!file.exists()) {
                 file.createNewFile();
             }
             return file;
         } catch (SecurityException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 }