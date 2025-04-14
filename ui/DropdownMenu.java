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

 package net.micode.notes.ui;

 import android.content.Context;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.PopupMenu;
 import android.widget.PopupMenu.OnMenuItemClickListener;
 
 import net.micode.notes.R;
 
 public class DropdownMenu {
     private Button mButton; // 下拉菜单按钮
     private PopupMenu mPopupMenu; // 弹出菜单
     private Menu mMenu; // 菜单对象
 
     // 构造函数
     public DropdownMenu(Context context, Button button, int menuId) {
         mButton = button; // 初始化按钮
         mButton.setBackgroundResource(R.drawable.dropdown_icon); // 设置按钮背景图标
         mPopupMenu = new PopupMenu(context, mButton); // 创建弹出菜单
         mMenu = mPopupMenu.getMenu(); // 获取菜单对象
         mPopupMenu.getMenuInflater().inflate(menuId, mMenu); // 从资源文件中加载菜单
         mButton.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 mPopupMenu.show(); // 显示弹出菜单
             }
         });
     }
 
     // 设置下拉菜单项点击监听器
     public void setOnDropdownMenuItemClickListener(OnMenuItemClickListener listener) {
         if (mPopupMenu != null) {
             mPopupMenu.setOnMenuItemClickListener(listener); // 设置菜单项点击监听器
         }
     }
 
     // 根据ID查找菜单项
     public MenuItem findItem(int id) {
         return mMenu.findItem(id); // 查找并返回菜单项
     }
 
     // 设置按钮标题
     public void setTitle(CharSequence title) {
         mButton.setText(title); // 更新按钮文本
     }
 }
 