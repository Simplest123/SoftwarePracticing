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
 import android.preference.PreferenceManager;
 
 import net.micode.notes.R;
 import net.micode.notes.ui.NotesPreferenceActivity;
 
 /**
  * 资源解析工具类
  * 
  * 该类负责管理便签应用中的各种资源ID，包括：
  * 1. 背景颜色资源
  * 2. 字体大小资源
  * 3. 笔记编辑界面背景
  * 4. 笔记列表项背景
  * 5. 小部件背景
  * 6. 文本外观样式
  */
 public class ResourceParser {
 
     // ==================== 背景颜色常量 ====================
     public static final int YELLOW           = 0; // 黄色
     public static final int BLUE             = 1; // 蓝色
     public static final int WHITE            = 2; // 白色
     public static final int GREEN            = 3; // 绿色
     public static final int RED              = 4; // 红色
 
     public static final int BG_DEFAULT_COLOR = YELLOW; // 默认背景颜色
 
     // ==================== 字体大小常量 ====================
     public static final int TEXT_SMALL       = 0; // 小号字体
     public static final int TEXT_MEDIUM      = 1; // 中号字体
     public static final int TEXT_LARGE       = 2; // 大号字体
     public static final int TEXT_SUPER       = 3; // 超大字体
 
     public static final int BG_DEFAULT_FONT_SIZE = TEXT_MEDIUM; // 默认字体大小
 
     /**
      * 笔记编辑界面背景资源类
      */
     public static class NoteBgResources {
         // 笔记编辑区域背景资源数组
         private final static int [] BG_EDIT_RESOURCES = new int [] {
             R.drawable.edit_yellow,    // 黄色背景
             R.drawable.edit_blue,      // 蓝色背景
             R.drawable.edit_white,     // 白色背景
             R.drawable.edit_green,     // 绿色背景
             R.drawable.edit_red        // 红色背景
         };
 
         // 笔记标题区域背景资源数组
         private final static int [] BG_EDIT_TITLE_RESOURCES = new int [] {
             R.drawable.edit_title_yellow,  // 黄色标题背景
             R.drawable.edit_title_blue,    // 蓝色标题背景
             R.drawable.edit_title_white,   // 白色标题背景
             R.drawable.edit_title_green,   // 绿色标题背景
             R.drawable.edit_title_red      // 红色标题背景
         };
 
         /**
          * 获取笔记编辑区域背景资源ID
          * @param id 颜色ID (YELLOW/BLUE/WHITE/GREEN/RED)
          * @return 对应的背景资源ID
          */
         public static int getNoteBgResource(int id) {
             return BG_EDIT_RESOURCES[id];
         }
 
         /**
          * 获取笔记标题区域背景资源ID
          * @param id 颜色ID (YELLOW/BLUE/WHITE/GREEN/RED)
          * @return 对应的标题背景资源ID
          */
         public static int getNoteTitleBgResource(int id) {
             return BG_EDIT_TITLE_RESOURCES[id];
         }
     }
 
     /**
      * 获取默认背景颜色ID
      * @param context 上下文对象
      * @return 背景颜色ID
      */
     public static int getDefaultBgId(Context context) {
         // 检查用户是否设置了随机背景颜色
         if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                 NotesPreferenceActivity.PREFERENCE_SET_BG_COLOR_KEY, false)) {
             // 随机选择一个背景颜色
             return (int) (Math.random() * NoteBgResources.BG_EDIT_RESOURCES.length);
         } else {
             // 使用默认背景颜色
             return BG_DEFAULT_COLOR;
         }
     }
 
     /**
      * 笔记列表项背景资源类
      */
     public static class NoteItemBgResources {
         // 列表第一项背景资源数组
         private final static int [] BG_FIRST_RESOURCES = new int [] {
             R.drawable.list_yellow_up,     // 黄色列表第一项
             R.drawable.list_blue_up,       // 蓝色列表第一项
             R.drawable.list_white_up,      // 白色列表第一项
             R.drawable.list_green_up,      // 绿色列表第一项
             R.drawable.list_red_up         // 红色列表第一项
         };
 
         // 列表中间项背景资源数组
         private final static int [] BG_NORMAL_RESOURCES = new int [] {
             R.drawable.list_yellow_middle, // 黄色列表中间项
             R.drawable.list_blue_middle,   // 蓝色列表中间项
             R.drawable.list_white_middle,  // 白色列表中间项
             R.drawable.list_green_middle,  // 绿色列表中间项
             R.drawable.list_red_middle     // 红色列表中间项
         };
 
         // 列表最后一项背景资源数组
         private final static int [] BG_LAST_RESOURCES = new int [] {
             R.drawable.list_yellow_down,   // 黄色列表最后一项
             R.drawable.list_blue_down,     // 蓝色列表最后一项
             R.drawable.list_white_down,    // 白色列表最后一项
             R.drawable.list_green_down,    // 绿色列表最后一项
             R.drawable.list_red_down,      // 红色列表最后一项
         };
 
         // 列表单项背景资源数组(当列表只有一项时使用)
         private final static int [] BG_SINGLE_RESOURCES = new int [] {
             R.drawable.list_yellow_single, // 黄色列表单项
             R.drawable.list_blue_single,   // 蓝色列表单项
             R.drawable.list_white_single,  // 白色列表单项
             R.drawable.list_green_single,  // 绿色列表单项
             R.drawable.list_red_single     // 红色列表单项
         };
 
         /**
          * 获取列表第一项背景资源
          * @param id 颜色ID
          * @return 背景资源ID
          */
         public static int getNoteBgFirstRes(int id) {
             return BG_FIRST_RESOURCES[id];
         }
 
         /**
          * 获取列表最后一项背景资源
          * @param id 颜色ID
          * @return 背景资源ID
          */
         public static int getNoteBgLastRes(int id) {
             return BG_LAST_RESOURCES[id];
         }
 
         /**
          * 获取列表单项背景资源(当列表只有一项时)
          * @param id 颜色ID
          * @return 背景资源ID
          */
         public static int getNoteBgSingleRes(int id) {
             return BG_SINGLE_RESOURCES[id];
         }
 
         /**
          * 获取列表中间项背景资源
          * @param id 颜色ID
          * @return 背景资源ID
          */
         public static int getNoteBgNormalRes(int id) {
             return BG_NORMAL_RESOURCES[id];
         }
 
         /**
          * 获取文件夹项背景资源
          * @return 文件夹背景资源ID
          */
         public static int getFolderBgRes() {
             return R.drawable.list_folder;
         }
     }
 
     /**
      * 小部件背景资源类
      */
     public static class WidgetBgResources {
         // 2x大小小部件背景资源数组
         private final static int [] BG_2X_RESOURCES = new int [] {
             R.drawable.widget_2x_yellow,   // 黄色2x小部件
             R.drawable.widget_2x_blue,     // 蓝色2x小部件
             R.drawable.widget_2x_white,    // 白色2x小部件
             R.drawable.widget_2x_green,    // 绿色2x小部件
             R.drawable.widget_2x_red,      // 红色2x小部件
         };
 
         /**
          * 获取2x大小小部件背景资源
          * @param id 颜色ID
          * @return 背景资源ID
          */
         public static int getWidget2xBgResource(int id) {
             return BG_2X_RESOURCES[id];
         }
 
         // 4x大小小部件背景资源数组
         private final static int [] BG_4X_RESOURCES = new int [] {
             R.drawable.widget_4x_yellow,  // 黄色4x小部件
             R.drawable.widget_4x_blue,    // 蓝色4x小部件
             R.drawable.widget_4x_white,   // 白色4x小部件
             R.drawable.widget_4x_green,   // 绿色4x小部件
             R.drawable.widget_4x_red      // 红色4x小部件
         };
 
         /**
          * 获取4x大小小部件背景资源
          * @param id 颜色ID
          * @return 背景资源ID
          */
         public static int getWidget4xBgResource(int id) {
             return BG_4X_RESOURCES[id];
         }
     }
 
     /**
      * 文本外观样式资源类
      */
     public static class TextAppearanceResources {
         // 文本外观样式资源数组
         private final static int [] TEXTAPPEARANCE_RESOURCES = new int [] {
             R.style.TextAppearanceNormal, // 普通文本样式
             R.style.TextAppearanceMedium,  // 中等文本样式
             R.style.TextAppearanceLarge,   // 大文本样式
             R.style.TextAppearanceSuper    // 超大文本样式
         };
 
         /**
          * 获取文本外观样式资源
          * @param id 字体大小ID (TEXT_SMALL/MEDIUM/LARGE/SUPER)
          * @return 样式资源ID
          */
         public static int getTexAppearanceResource(int id) {
             /**
              * 修复在SharedPreference中存储资源ID时的bug
              * 当ID大于资源数组长度时，返回默认字体大小
              */
             if (id >= TEXTAPPEARANCE_RESOURCES.length) {
                 return BG_DEFAULT_FONT_SIZE;
             }
             return TEXTAPPEARANCE_RESOURCES[id];
         }
 
         /**
          * 获取文本外观样式资源总数
          * @return 样式资源总数
          */
         public static int getResourcesSize() {
             return TEXTAPPEARANCE_RESOURCES.length;
         }
     }
 }