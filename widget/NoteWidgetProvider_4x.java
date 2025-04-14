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
  * 4x尺寸笔记桌面小部件实现类
  * 继承自通用笔记小部件基类，提供4x尺寸的特定布局和资源配置
  */
 public class NoteWidgetProvider_4x extends NoteWidgetProvider {
     
     /**
      * 小部件更新事件处理
      * 直接调用基类通用更新逻辑，保持功能一致性
      */
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
         super.update(context, appWidgetManager, appWidgetIds); // 复用基类更新逻辑
     }
 
     /**
      * 获取当前小部件的布局资源ID
      * @return 4x尺寸专用布局资源（R.layout.widget_4x）
      */
     @Override
     protected int getLayoutId() {
         return R.layout.widget_4x; // 对应4x尺寸布局文件
     }
 
     /**
      * 获取4x尺寸背景资源映射
      * @param bgId 笔记存储的背景ID
      * @return 4x尺寸专用背景资源ID
      */
     @Override
     protected int getBgResourceId(int bgId) {
         return ResourceParser.WidgetBgResources.getWidget4xBgResource(bgId); // 获取4x专用背景
     }
 
     /**
      * 获取小部件类型标识
      * @return 4x小部件类型常量（Notes.TYPE_WIDGET_4X）
      */
     @Override
     protected int getWidgetType() {
         return Notes.TYPE_WIDGET_4X; // 类型标识用于业务逻辑区分
     }

