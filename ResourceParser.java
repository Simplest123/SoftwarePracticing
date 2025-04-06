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
 
 public class ResourceParser {
 
     // 定义背景颜色的枚举常量
     public static final int YELLOW           = 0;
     public static final int BLUE             = 1;
     public static final int WHITE            = 2;
     public static final int GREEN            = 3;
     public static final int RED              = 4;
 
     // 默认背景颜色为黄色
     public static final int BG_DEFAULT_COLOR = YELLOW;
 
     // 定义文字大小的枚举常量
     public static final int TEXT_SMALL       = 0;
     public static final int TEXT_MEDIUM      = 1;
     public static final int TEXT_LARGE       = 2;
     public static final int TEXT_SUPER       = 3;
 
     // 默认字体大小为中等
     public static final int BG_DEFAULT_FONT_SIZE = TEXT_MEDIUM;
 
     // 笔记背景资源类
     public static class NoteBgResources {
         // 笔记编辑背景资源数组
         private final static int [] BG_EDIT_RESOURCES = new int [] {
             R.drawable.edit_yellow,
             R.drawable.edit_blue,
             R.drawable.edit_white,
             R.drawable.edit_green,
             R.drawable.edit_red
         };
 
         // 笔记标题背景资源数组
         private final static int [] BG_EDIT_TITLE_RESOURCES = new int [] {
             R.drawable.edit_title_yellow,
             R.drawable.edit_title_blue,
             R.drawable.edit_title_white,
             R.drawable.edit_title_green,
             R.drawable.edit_title_red
         };
 
         // 根据ID获取对应的笔记编辑背景
         public static int getNoteBgResource(int id) {
             return BG_EDIT_RESOURCES[id];
         }
 
         // 根据ID获取对应的笔记标题背景
         public static int getNoteTitleBgResource(int id) {
             return BG_EDIT_TITLE_RESOURCES[id];
         }
     }
 
     // 获取用户设置的默认背景颜色ID
     public static int getDefaultBgId(Context context) {
         // 如果用户启用了随机背景设置，返回一个随机颜色ID
         if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                 NotesPreferenceActivity.PREFERENCE_SET_BG_COLOR_KEY, false)) {
             return (int) (Math.random() * NoteBgResources.BG_EDIT_RESOURCES.length);
         } else {
             // 否则返回默认背景颜色
             return BG_DEFAULT_COLOR;
         }
     }
 
     // 笔记列表项背景资源类（用于列表模式）
     public static class NoteItemBgResources {
         // 第一项背景资源
         private final static int [] BG_FIRST_RESOURCES = new int [] {
             R.drawable.list_yellow_up,
             R.drawable.list_blue_up,
             R.drawable.list_white_up,
             R.drawable.list_green_up,
             R.drawable.list_red_up
         };
 
         // 中间项背景资源
         private final static int [] BG_NORMAL_RESOURCES = new int [] {
             R.drawable.list_yellow_middle,
             R.drawable.list_blue_middle,
             R.drawable.list_white_middle,
             R.drawable.list_green_middle,
             R.drawable.list_red_middle
         };
 
         // 最后一项背景资源
         private final static int [] BG_LAST_RESOURCES = new int [] {
             R.drawable.list_yellow_down,
             R.drawable.list_blue_down,
             R.drawable.list_white_down,
             R.drawable.list_green_down,
             R.drawable.list_red_down,
         };
 
         // 单项背景资源（仅一条笔记时）
         private final static int [] BG_SINGLE_RESOURCES = new int [] {
             R.drawable.list_yellow_single,
             R.drawable.list_blue_single,
             R.drawable.list_white_single,
             R.drawable.list_green_single,
             R.drawable.list_red_single
         };
 
         // 获取不同位置对应的背景资源
         public static int getNoteBgFirstRes(int id) {
             return BG_FIRST_RESOURCES[id];
         }
 
         public static int getNoteBgLastRes(int id) {
             return BG_LAST_RESOURCES[id];
         }
 
         public static int getNoteBgSingleRes(int id) {
             return BG_SINGLE_RESOURCES[id];
         }
 
         public static int getNoteBgNormalRes(int id) {
             return BG_NORMAL_RESOURCES[id];
         }
 
         // 获取文件夹项的背景资源
         public static int getFolderBgRes() {
             return R.drawable.list_folder;
         }
     }
 
     // Widget（桌面插件）背景资源类
     public static class WidgetBgResources {
         // 2x 大小的 widget 背景资源
         private final static int [] BG_2X_RESOURCES = new int [] {
             R.drawable.widget_2x_yellow,
             R.drawable.widget_2x_blue,
             R.drawable.widget_2x_white,
             R.drawable.widget_2x_green,
             R.drawable.widget_2x_red,
         };
 
         public static int getWidget2xBgResource(int id) {
             return BG_2X_RESOURCES[id];
         }
 
         // 4x 大小的 widget 背景资源
         private final static int [] BG_4X_RESOURCES = new int [] {
             R.drawable.widget_4x_yellow,
             R.drawable.widget_4x_blue,
             R.drawable.widget_4x_white,
             R.drawable.widget_4x_green,
             R.drawable.widget_4x_red
         };
 
         public static int getWidget4xBgResource(int id) {
             return BG_4X_RESOURCES[id];
         }
     }
 
     // 文本样式资源类
     public static class TextAppearanceResources {
         // 不同文字大小的样式资源
         private final static int [] TEXTAPPEARANCE_RESOURCES = new int [] {
             R.style.TextAppearanceNormal,
             R.style.TextAppearanceMedium,
             R.style.TextAppearanceLarge,
             R.style.TextAppearanceSuper
         };
 
         public static int getTexAppearanceResource(int id) {
             /**
              * BUG修复：有可能共享设置中保存的id大于资源数组长度，
              * 为避免数组越界，返回默认字体大小。
              */
             if (id >= TEXTAPPEARANCE_RESOURCES.length) {
                 return BG_DEFAULT_FONT_SIZE;
             }
             return TEXTAPPEARANCE_RESOURCES[id];
         }
 
         // 获取字体样式数量
         public static int getResourcesSize() {
             return TEXTAPPEARANCE_RESOURCES.length;
         }
     }
 }
 
