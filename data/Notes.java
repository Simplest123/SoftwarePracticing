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

/**
 * 笔记应用的核心常量定义类
 * 包含URI定义、数据类型、系统文件夹ID等重要常量
 */
public class Notes {
    // 内容提供者的授权标识（对应AndroidManifest.xml中的配置）
    public static final String AUTHORITY = "micode_notes";

    // 日志标签
    public static final String TAG = "Notes";

    // 笔记类型常量
    public static final int TYPE_NOTE     = 0;  // 普通笔记
    public static final int TYPE_FOLDER   = 1;  // 文件夹
    public static final int TYPE_SYSTEM   = 2;  // 系统文件夹

    /**
     * 系统文件夹ID定义：
     * - ID_ROOT_FOLDER: 根文件夹（默认文件夹）
     * - ID_TEMPARAY_FOLDER: 临时文件夹（存放未分类笔记）
     * - ID_CALL_RECORD_FOLDER: 通话记录文件夹
     * - ID_TRASH_FOLER: 回收站文件夹
     */
    public static final int ID_ROOT_FOLDER = 0;
    public static final int ID_TEMPARAY_FOLDER = -1;
    public static final int ID_CALL_RECORD_FOLDER = -2;
    public static final int ID_TRASH_FOLER = -3;

    // Intent附加数据键名常量
    public static final String INTENT_EXTRA_ALERT_DATE = "net.micode.notes.alert_date";
    public static final String INTENT_EXTRA_BACKGROUND_ID = "net.micode.notes.background_color_id";
    public static final String INTENT_EXTRA_WIDGET_ID = "net.micode.notes.widget_id";
    public static final String INTENT_EXTRA_WIDGET_TYPE = "net.micode.notes.widget_type";
    public static final String INTENT_EXTRA_FOLDER_ID = "net.micode.notes.folder_id";
    public static final String INTENT_EXTRA_CALL_DATE = "net.micode.notes.call_date";

    // 小部件类型常量
    public static final int TYPE_WIDGET_INVALIDE      = -1;  // 无效小部件
    public static final int TYPE_WIDGET_2X            = 0;   // 2x尺寸小部件
    public static final int TYPE_WIDGET_4X            = 1;   // 4x尺寸小部件

    /**
     * 数据类型的常量定义（内部类）
     */
    public static class DataConstants {
        public static final String NOTE = TextNote.CONTENT_ITEM_TYPE;      // 普通笔记类型
        public static final String CALL_NOTE = CallNote.CONTENT_ITEM_TYPE; // 通话笔记类型
    }

    // 笔记内容URI（查询所有笔记和文件夹）
    public static final Uri CONTENT_NOTE_URI = Uri.parse("content://" + AUTHORITY + "/note");

    // 数据内容URI（查询所有数据）
    public static final Uri CONTENT_DATA_URI = Uri.parse("content://" + AUTHORITY + "/data");

    /**
     * 笔记表的列定义接口
     */
    public interface NoteColumns {
        /** 行唯一ID，类型：LONG */
        public static final String ID = "_id";

        /** 父项ID（用于构建层级结构），类型：LONG */
        public static final String PARENT_ID = "parent_id";

        /** 创建日期，类型：LONG（时间戳） */
        public static final String CREATED_DATE = "created_date";

        /** 最后修改日期，类型：LONG（时间戳） */
        public static final String MODIFIED_DATE = "modified_date";

        /** 提醒日期，类型：LONG（时间戳） */
        public static final String ALERTED_DATE = "alert_date";

        /** 片段内容（文件夹名或笔记摘要），类型：TEXT */
        public static final String SNIPPET = "snippet";

        /** 关联的小部件ID，类型：LONG */
        public static final String WIDGET_ID = "widget_id";

        /** 小部件类型，类型：INT */
        public static final String WIDGET_TYPE = "widget_type";

        /** 背景颜色ID，类型：INT */
        public static final String BG_COLOR_ID = "bg_color_id";

        /** 是否有附件（0无/1有），类型：INT */
        public static final String HAS_ATTACHMENT = "has_attachment";

        /** 文件夹内的笔记数量，类型：LONG */
        public static final String NOTES_COUNT = "notes_count";

        /** 类型（笔记/文件夹/系统），类型：INT */
        public static final String TYPE = "type";

        /** 同步ID（用于云端同步），类型：LONG */
        public static final String SYNC_ID = "sync_id";

        /** 本地修改标志（0未修改/1已修改），类型：INT */
        public static final String LOCAL_MODIFIED = "local_modified";

        /** 原始父ID（移动文件时保留原父ID），类型：INT */
        public static final String ORIGIN_PARENT_ID = "origin_parent_id";

        /** Google任务ID（用于GTask同步），类型：TEXT */
        public static final String GTASK_ID = "gtask_id";

        /** 数据版本号（用于冲突检测），类型：LONG */
        public static final String VERSION = "version";
    }

    /**
     * 数据表的列定义接口
     */
    public interface DataColumns {
        /** 行唯一ID，类型：LONG */
        public static final String ID = "_id";

        /** MIME类型（区分不同数据类型），类型：TEXT */
        public static final String MIME_TYPE = "mime_type";

        /** 关联的笔记ID，类型：LONG */
        public static final String NOTE_ID = "note_id";

        /** 创建日期，类型：LONG（时间戳） */
        public static final String CREATED_DATE = "created_date";

        /** 最后修改日期，类型：LONG（时间戳） */
        public static final String MODIFIED_DATE = "modified_date";

        /** 数据内容（根据MIME类型变化），类型：TEXT */
        public static final String CONTENT = "content";

        /** 通用数据列1（整数类型），类型：INT */
        public static final String DATA1 = "data1";

        /** 通用数据列2（整数类型），类型：INT */
        public static final String DATA2 = "data2";

        /** 通用数据列3（文本类型），类型：TEXT */
        public static final String DATA3 = "data3";

        /** 通用数据列4（文本类型），类型：TEXT */
        public static final String DATA4 = "data4";

        /** 通用数据列5（文本类型），类型：TEXT */
        public static final String DATA5 = "data5";
    }

    /**
     * 文本笔记的特定列定义（扩展自DataColumns）
     */
    public static final class TextNote implements DataColumns {
        /** 模式（0普通/1清单），类型：INT */
        public static final String MODE = DATA1;
        public static final int MODE_CHECK_LIST = 1;  // 清单模式常量

        // MIME类型定义
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/text_note";      // 多项目类型
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/text_note"; // 单项目类型

        // 文本笔记专用URI
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/text_note");
    }

    /**
     * 通话笔记的特定列定义（扩展自DataColumns）
     */
    public static final class CallNote implements DataColumns {
        /** 通话日期，类型：LONG（时间戳） */
        public static final String CALL_DATE = DATA1;

        /** 电话号码，类型：TEXT */
        public static final String PHONE_NUMBER = DATA3;

        // MIME类型定义
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/call_note";      // 多项目类型
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/call_note"; // 单项目类型

        // 通话笔记专用URI
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/call_note");
    }
}