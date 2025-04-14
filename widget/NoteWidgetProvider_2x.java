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
 * 2x尺寸便签小部件的具体实现类
 */
public class NoteWidgetProvider_2x extends NoteWidgetProvider {

    /**
     * 当小部件需要更新时触发
     * 直接调用父类的统一更新逻辑
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.update(context, appWidgetManager, appWidgetIds);
    }

    /**
     * 获取2x小部件的布局资源ID
     * 
     * @return 返回R.layout.widget_2x对应的布局文件
     */
    @Override
    protected int getLayoutId() {
        return R.layout.widget_2x; // 对应2x尺寸的小部件布局
    }

    /**
     * 根据背景ID获取2x尺寸的对应背景资源
     * 
     * @param bgId 背景颜色标识符（来自数据库）
     * @return 对应的2x尺寸背景资源ID
     */
    @Override
    protected int getBgResourceId(int bgId) {
        // 通过ResourceParser获取2x专属背景资源
        return ResourceParser.WidgetBgResources.getWidget2xBgResource(bgId);
    }

    /**
     * 获取小部件类型标识
     * 
     * @return 返回2x小部件类型常量Notes.TYPE_WIDGET_2X
     */
    @Override
    protected int getWidgetType() {
        return Notes.TYPE_WIDGET_2X; // 在Notes类中定义的2x小部件类型标识
    }
}