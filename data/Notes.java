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

 import android.net.Uri;
 
 // Notes类用于定义笔记相关的常量和URI
 public class Notes {
     // 笔记的内容提供者的授权字符串
     public static final String AUTHORITY = "micode_notes";
     // 日志标签
     public static final String TAG = "Notes";
     
     // 笔记、文件夹和系统的类型常量
     public static final int TYPE_NOTE     = 0;
     public static final int TYPE_FOLDER   = 1;
     public static final int TYPE_SYSTEM   = 2;
 
     /**
      * 以下ID是系统文件夹的标识符
      * {@link Notes#ID_ROOT_FOLDER} 是默认文件夹
      * {@link Notes#ID_TEMPARAY_FOLDER} 是没有文件夹的笔记
      * {@link Notes#ID_CALL_RECORD_FOLDER} 用于存储通话记录
      */
     public static final int ID_ROOT_FOLDER = 0; // 根文件夹ID
     public static final int ID_TEMPARAY_FOLDER = -1; // 临时文件夹ID
     public static final int ID_CALL_RECORD_FOLDER = -2; // 通话记录文件夹ID
     public static final int ID_TRASH_FOLER = -3; // 垃圾文件夹ID
 
     // Intent中用于传递额外数据的常量
     public static final String INTENT_EXTRA_ALERT_DATE = "net.micode.notes.alert_date";
     public static final String INTENT_EXTRA_BACKGROUND_ID = "net.micode.notes.background_color_id";
     public static final String INTENT_EXTRA_WIDGET_ID = "net.micode.notes.widget_id";
     public static final String INTENT_EXTRA_WIDGET_TYPE = "net.micode.notes.widget_type";
     public static final String INTENT_EXTRA_FOLDER_ID = "net.micode.notes.folder_id";
     public static final String INTENT_EXTRA_CALL_DATE = "net.micode.notes.call_date";
 
     // 小部件的类型常量
     public static final int TYPE_WIDGET_INVALIDE      = -1; // 无效的小部件类型
     public static final int TYPE_WIDGET_2X            = 0; // 2x小部件类型
     public static final int TYPE_WIDGET_4X            = 1; // 4x小部件类型
 
     // 数据常量类
     public static class DataConstants {
         public static final String NOTE = TextNote.CONTENT_ITEM_TYPE; // 文本笔记的内容类型
         public static final String CALL_NOTE = CallNote.CONTENT_ITEM_TYPE; // 通话笔记的内容类型
     }
 
     /**
      * 查询所有笔记和文件夹的URI
      */
     public static final Uri CONTENT_NOTE_URI = Uri.parse("content://" + AUTHORITY + "/note");
 
     /**
      * 查询数据的URI
      */
     public static final Uri CONTENT_DATA_URI = Uri.parse("content://" + AUTHORITY + "/data");
 
     // 笔记列的接口
     public interface NoteColumns {
         /**
          * 行的唯一ID
          * <P> 类型: INTEGER (long) </P>
          */
         public static final String ID = "_id";
 
         /**
          * 笔记或文件夹的父ID
          * <P> 类型: INTEGER (long) </P>
          */
         public static final String PARENT_ID = "parent_id";
 
         /**
          * 笔记或文件夹的创建日期
          * <P> 类型: INTEGER (long) </P>
          */
         public static final String CREATED_DATE = "created_date";
 
         /**
          * 最新修改日期
          * <P> 类型: INTEGER (long) </P>
          */
         public static final String MODIFIED_DATE = "modified_date";
 
         /**
          * 提醒日期
          * <P> 类型: INTEGER (long) </P>
          */
         public static final String ALERTED_DATE = "alert_date";
 
         /**
          * 文件夹的名称或笔记的文本内容
          * <P> 类型: TEXT </P>
          */
         public static final String SNIPPET = "snippet";
 
         /**
          * 笔记的小部件ID
          * <P> 类型: INTEGER (long) </P>
          */
         public static final String WIDGET_ID = "widget_id";
 
         /**
          * 笔记的小部件类型
          * <P> 类型: INTEGER (long) </P>
          */
         public static final String WIDGET_TYPE = "widget_type";
 
         /**
          * 笔记的背景颜色ID
          * <P> 类型: INTEGER (long) </P>
          */
         public static final String BG_COLOR_ID = "bg_color_id";
 
         /**
          * 对于文本笔记，没有附件；对于多媒体笔记，至少有一个附件
          * <P> 类型: INTEGER </P>
          */
         public static final String HAS_ATTACHMENT = "has_attachment";
 
         /**
          * 文件夹中的笔记数量
          * <P> 类型: INTEGER (long) </P>
          */
         public static final String NOTES_COUNT = "notes_count";
 
         /**
          * 文件夹或笔记的类型
          * <P> 类型: INTEGER </P>
          */
         public static final String TYPE = "type";
 
         /**
          * 最后同步ID
          * <P> 类型: INTEGER (long) </P>
          */
         public static final String SYNC_ID = "sync_id";
 
         /**
          * 标记本地是否已修改
          * <P> 类型: INTEGER </P>
          */
         public static final String LOCAL_MODIFIED = "local_modified";
 
         /**
          * 移动到临时文件夹前的原始父ID
          * <P> 类型 : INTEGER </P>
          */
         public static final String ORIGIN_PARENT_ID = "origin_parent_id";
 
         /**
          * gtask ID
          * <P> 类型 : TEXT </P>
          */
         public static final String GTASK_ID = "gtask_id";
 
         /**
          * 版本代码
          * <P> 类型 : INTEGER (long) </P>
          */
         public static final String VERSION = "version";
     }
 
     // 数据列接口
     public interface DataColumns {
         /**
          * 行的唯一ID
          * <P> 类型: INTEGER (long) </P>
          */
         public static final String ID = "_id";
 
         /**
          * 此行表示的项目的MIME类型。
          * <P> 类型: Text </P>
          */
         public static final String MIME_TYPE = "mime_type";
 
         /**
          * 此数据所属的笔记的引用ID
          * <P> 类型: INTEGER (long) </P>
          */
         public static final String NOTE_ID = "note_id";
 
         /**
          * 笔记或文件夹的创建日期
          * <P> 类型: INTEGER (long) </P>
          */
         public static final String CREATED_DATE = "created_date";
 
         /**
          * 最新修改日期
          * <P> 类型: INTEGER (long) </P>
          */
         public static final String MODIFIED_DATE = "modified_date";
 
         /**
          * 数据的内容
          * <P> 类型: TEXT </P>
          */
         public static final String CONTENT = "content";
 
         /**
          * 通用数据列，具体含义取决于 {@link #MIMETYPE}，用于
          * 整数数据类型
          * <P> 类型: INTEGER </P>
          */
         public static final String DATA1 = "data1";
 
         /**
          * 通用数据列，具体含义取决于 {@link #MIMETYPE}，用于
          * 整数数据类型
          * <P> 类型: INTEGER </P>
          */
         public static final String DATA2 = "data2";
 
         /**
          * 通用数据列，具体含义取决于 {@link #MIMETYPE}，用于
          * 文本数据类型
          * <P> 类型: TEXT </P>
          */
         public static final String DATA3 = "data3";
 
         /**
          * 通用数据列，具体含义取决于 {@link #MIMETYPE}，用于
          * 文本数据类型
          * <P> 类型: TEXT </P>
          */
         public static final String DATA4 = "data4";
 
         /**
          * 通用数据列，具体含义取决于 {@link #MIMETYPE}，用于
          * 文本数据类型
          * <P> 类型: TEXT </P>
          */
         public static final String DATA5 = "data5";
     }
 
     // 文本笔记类，继承自数据列接口
     public static final class TextNote implements DataColumns {
         /**
          * 指示文本是否处于检查列表模式的模式
          * <P> 类型: Integer 1:检查列表模式 0:普通模式 </P>
          */
         public static final String MODE = DATA1;
 
         public static final int MODE_CHECK_LIST = 1; // 检查列表模式常量
 
         public static final String CONTENT_TYPE = "vnd.android.cursor.dir/text_note"; // 文本笔记的内容类型
 
         public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/text_note"; // 单个文本笔记的内容类型
 
         public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/text_note"); // 文本笔记的URI
     }
 
     // 通话笔记类，继承自数据列接口
     public static final class CallNote implements DataColumns {
         /**
          * 该记录的通话日期
          * <P> 类型: INTEGER (long) </P>
          */
         public static final String CALL_DATE = DATA1;
 
         /**
          * 该记录的电话号码
          * <P> 类型: TEXT </P>
          */
         public static final String PHONE_NUMBER = DATA3;
 
         public static final String CONTENT_TYPE = "vnd.android.cursor.dir/call_note"; // 通话笔记的内容类型
 
         public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/call_note"; // 单个通话笔记的内容类型
 
         public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/call_note"); // 通话笔记的URI
     }
 }
 