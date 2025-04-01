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
 import android.database.Cursor;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import net.micode.notes.R;
 import net.micode.notes.data.Notes;
 import net.micode.notes.data.Notes.NoteColumns;
 
 public class FoldersListAdapter extends CursorAdapter {
     public static final String[] PROJECTION = {
         NoteColumns.ID, // 文件夹ID
         NoteColumns.SNIPPET // 文件夹名称
     };
 
     public static final int ID_COLUMN   = 0; // ID列索引
     public static final int NAME_COLUMN = 1; // 名称列索引
 
     // 构造函数
     public FoldersListAdapter(Context context, Cursor c) {
         super(context, c);
     }
 
     // 创建新视图
     @Override
     public View newView(Context context, Cursor cursor, ViewGroup parent) {
         return new FolderListItem(context); // 返回新的文件夹列表项视图
     }
 
     // 绑定视图数据
     @Override
     public void bindView(View view, Context context, Cursor cursor) {
         if (view instanceof FolderListItem) {
             String folderName = (cursor.getLong(ID_COLUMN) == Notes.ID_ROOT_FOLDER) 
                 ? context.getString(R.string.menu_move_parent_folder) 
                 : cursor.getString(NAME_COLUMN);
             ((FolderListItem) view).bind(folderName); // 绑定文件夹名称
         }
     }
 
     // 根据位置获取文件夹名称
     public String getFolderName(Context context, int position) {
         Cursor cursor = (Cursor) getItem(position);
         return (cursor.getLong(ID_COLUMN) == Notes.ID_ROOT_FOLDER) 
             ? context.getString(R.string.menu_move_parent_folder) 
             : cursor.getString(NAME_COLUMN);
     }
 
     // 内部类：文件夹列表项
     private class FolderListItem extends LinearLayout {
         private TextView mName; // 文件夹名称文本视图
 
         // 构造函数
         public FolderListItem(Context context) {
             super(context);
             inflate(context, R.layout.folder_list_item, this); // 加载布局
             mName = (TextView) findViewById(R.id.tv_folder_name); // 初始化文本视图
         }
 
         // 绑定文件夹名称
         public void bind(String name) {
             mName.setText(name); // 设置文件夹名称
         }
     }
 }
 