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

 package net.micode.notes.widget;

 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 
 import net.micode.notes.R;
 import net.micode.notes.data.Notes;
 import net.micode.notes.tool.ResourceParser;
 
 /**
  * 2x尺寸的笔记桌面小部件具体实现类
  * 继承自NoteWidgetProvider基类，提供特定尺寸小部件的布局和配置
  */
 public class NoteWidgetProvider_2x extends NoteWidgetProvider {
     /**
      * 小部件更新回调方法
      * 直接调用基类update方法完成通用更新逻辑
      */
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
         super.update(context, appWidgetManager, appWidgetIds); // 调用基类统一更新逻辑
     }
 
     /**
      * 获取当前小部件的布局资源ID
      * @return 2x尺寸小部件的布局资源ID（R.layout.widget_2x）
      */
     @Override
     protected int getLayoutId() {
         return R.layout.widget_2x; // 对应2x尺寸的布局文件
     }
 
     /**
      * 获取指定背景ID对应的资源ID（2x专用）
      * @param bgId 背景颜色ID（来自数据库）
      * @return 对应的2x尺寸背景资源ID
      */
     @Override
     protected int getBgResourceId(int bgId) {
         // 通过ResourceParser获取2x专用背景资源
         return ResourceParser.WidgetBgResources.getWidget2xBgResource(bgId);
     }
 
     /**
      * 获取小部件类型标识
      * @return 2x小部件类型常量（Notes.TYPE_WIDGET_2X）
      */
     @Override
     protected int getWidgetType() {
         return Notes.TYPE_WIDGET_2X; // 类型标识用于区分不同尺寸小部件
     }
 }