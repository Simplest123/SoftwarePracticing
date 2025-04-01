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
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 
 import net.micode.notes.data.Notes;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 
 public class NotesListAdapter extends CursorAdapter {
     private static final String TAG = "NotesListAdapter"; // 日志标签
     private Context mContext; // 上下文
     private HashMap<Integer, Boolean> mSelectedIndex; // 选中的项索引
     private int mNotesCount; // 便签数量
     private boolean mChoiceMode; // 选择模式
 
     // 小部件属性类
     public static class AppWidgetAttribute {
         public int widgetId; // 小部件ID
         public int widgetType; // 小部件类型
     }
 
     public NotesListAdapter(Context context) {
         super(context, null);
         mSelectedIndex = new HashMap<Integer, Boolean>(); // 初始化选中索引
         mContext = context;
         mNotesCount = 0; // 初始化便签数量
     }
 
     @Override
     public View newView(Context context, Cursor cursor, ViewGroup parent) {
         return new NotesListItem(context); // 创建新的列表项视图
     }
 
     @Override
     public void bindView(View view, Context context, Cursor cursor) {
         if (view instanceof NotesListItem) {
             NoteItemData itemData = new NoteItemData(context, cursor); // 创建便签数据对象
             ((NotesListItem) view).bind(context, itemData, mChoiceMode,
                     isSelectedItem(cursor.getPosition())); // 绑定数据到视图
         }
     }
 
     // 设置选中项
     public void setCheckedItem(final int position, final boolean checked) {
         mSelectedIndex.put(position, checked); // 更新选中状态
         notifyDataSetChanged(); // 通知数据集已更改
     }
 
     public boolean isInChoiceMode() {
         return mChoiceMode; // 返回选择模式状态
     }
 
     public void setChoiceMode(boolean mode) {
         mSelectedIndex.clear(); // 清空选中索引
         mChoiceMode = mode; // 设置选择模式
     }
 
     // 全选或全不选
     public void selectAll(boolean checked) {
         Cursor cursor = getCursor(); // 获取游标
         for (int i = 0; i < getCount(); i++) {
             if (cursor.moveToPosition(i)) {
                 if (NoteItemData.getNoteType(cursor) == Notes.TYPE_NOTE) {
                     setCheckedItem(i, checked); // 设置选中状态
                 }
             }
         }
     }
 
     // 获取选中项的ID集合
     public HashSet<Long> getSelectedItemIds() {
         HashSet<Long> itemSet = new HashSet<Long>();
         for (Integer position : mSelectedIndex.keySet()) {
             if (mSelectedIndex.get(position) == true) {
                 Long id = getItemId(position);
                 if (id == Notes.ID_ROOT_FOLDER) {
                     Log.d(TAG, "Wrong item id, should not happen"); // 日志错误
                 } else {
                     itemSet.add(id); // 添加到集合
                 }
             }
         }
 
         return itemSet; // 返回选中项ID集合
     }
 
     // 获取选中小部件属性集合
     public HashSet<AppWidgetAttribute> getSelectedWidget() {
         HashSet<AppWidgetAttribute> itemSet = new HashSet<AppWidgetAttribute>();
         for (Integer position : mSelectedIndex.keySet()) {
             if (mSelectedIndex.get(position) == true) {
                 Cursor c = (Cursor) getItem(position);
                 if (c != null) {
                     AppWidgetAttribute widget = new AppWidgetAttribute();
                     NoteItemData item = new NoteItemData(mContext, c);
                     widget.widgetId = item.getWidgetId(); // 获取小部件ID
                     widget.widgetType = item.getWidgetType(); // 获取小部件类型
                     itemSet.add(widget);
                     // 不要在这里关闭游标，只有适配器可以关闭它
                 } else {
                     Log.e(TAG, "Invalid cursor"); // 日志错误
                     return null;
                 }
             }
         }
         return itemSet; // 返回选中小部件属性集合
     }
 
     // 获取选中项的数量
     public int getSelectedCount() {
         Collection<Boolean> values = mSelectedIndex.values();
         if (null == values) {
             return 0; // 如果没有值，返回0
         }
         Iterator<Boolean> iter = values.iterator();
         int count = 0;
         while (iter.hasNext()) {
             if (true == iter.next()) {
                 count++; // 统计选中数量
             }
         }
         return count; // 返回选中数量
     }
 
     // 检查是否所有项都被选中
     public boolean isAllSelected() {
         int checkedCount = getSelectedCount();
         return (checkedCount != 0 && checkedCount == mNotesCount); // 返回是否全选
     }
 
     // 检查特定位置项是否被选中
     public boolean isSelectedItem(final int position) {
         if (null == mSelectedIndex.get(position)) {
             return false; // 如果没有记录，返回false
         }
         return mSelectedIndex.get(position); // 返回选中状态
     }
 
     @Override
     protected void onContentChanged() {
         super.onContentChanged();
         calcNotesCount(); // 计算便签数量
     }
 
     @Override
     public void changeCursor(Cursor cursor) {
         super.changeCursor(cursor);
         calcNotesCount(); // 计算便签数量
     }
 
     // 计算便签数量
     private void calcNotesCount() {
         mNotesCount = 0; // 重置数量
         for (int i = 0; i < getCount(); i++) {
             Cursor c = (Cursor) getItem(i);
             if (c != null) {
                 if (NoteItemData.getNoteType(c) == Notes.TYPE_NOTE) {
                     mNotesCount++; // 统计便签数量
                 }
             } else {
                 Log.e(TAG, "Invalid cursor"); // 日志错误
                 return;
             }
         }
     }
 }
 