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
 * 4x尺寸便签小部件实现类
 * 功能：显示更大尺寸的便签预览，点击可快速查看/编辑便签
 */
public class NoteWidgetProvider_4x extends NoteWidgetProvider {

    /**
     * 当小部件需要更新时自动调用
     * 直接使用父类统一的更新逻辑，保持功能一致性
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.update(context, appWidgetManager, appWidgetIds); // 调用父类方法完成实际更新
    }

    /**
     * 获取4x小部件的界面布局
     * 
     * @return 返回布局文件widget_4x.xml的资源ID
     *         相当于告诉程序："请用res/layout下的widget_4x.xml文件来绘制这个部件"
     */
    @Override
    protected int getLayoutId() {
        return R.layout.widget_4x; // 对应4倍尺寸的布局文件
    }

    /**
     * 根据颜色编号获取对应的背景图片
     * 
     * @param bgId 颜色编号（比如蓝色=1，绿色=2）
     * @return 返回对应4x尺寸的背景图片资源ID
     *         示例：如果bgId是蓝色，就返回4x尺寸的蓝色背景图
     */
    @Override
    protected int getBgResourceId(int bgId) {
        return ResourceParser.WidgetBgResources.getWidget4xBgResource(bgId);
    }

    /**
     * 获取小部件类型标识
     * 
     * @return 返回常量值Notes.TYPE_WIDGET_4X
     *         相当于给这个部件贴上一个"我是4x尺寸"的标签
     */
    @Override
    protected int getWidgetType() {
        return Notes.TYPE_WIDGET_4X; // 在Notes类中定义的4x类型标识
    }
}
