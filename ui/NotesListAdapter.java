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
 
 /**
  * 笔记列表适配器，用于在ListView中显示笔记数据
  */
 public class NotesListAdapter extends CursorAdapter {
     private static final String TAG = "NotesListAdapter";
     private Context mContext; // 上下文对象
     private HashMap<Integer, Boolean> mSelectedIndex; // 存储选中项的位置
     private int mNotesCount; // 笔记总数
     private boolean mChoiceMode; // 是否处于选择模式
 
     // 定义应用小部件属性类
     public static class AppWidgetAttribute {
         public int widgetId; // 小部件ID
         public int widgetType; // 小部件类型
     };
 
     /**
      * 构造函数
      * @param context 上下文对象
      */
     public NotesListAdapter(Context context) {
         super(context, null);
         mSelectedIndex = new HashMap<Integer, Boolean>();
         mContext = context;
         mNotesCount = 0;
     }
 
     /**
      * 创建新视图
      */
     @Override
     public View newView(Context context, Cursor cursor, ViewGroup parent) {
         return new NotesListItem(context);
     }
 
     /**
      * 绑定数据到视图
      */
     @Override
     public void bindView(View view, Context context, Cursor cursor) {
         if (view instanceof NotesListItem) {
             // 创建笔记项数据对象
             NoteItemData itemData = new NoteItemData(context, cursor);
             // 绑定数据到笔记列表项视图
             ((NotesListItem) view).bind(context, itemData, mChoiceMode,
                     isSelectedItem(cursor.getPosition()));
         }
     }
 
     /**
      * 设置项选中状态
      * @param position 项位置
      * @param checked 是否选中
      */
     public void setCheckedItem(final int position, final boolean checked) {
         mSelectedIndex.put(position, checked);
         notifyDataSetChanged(); // 通知数据变化
     }
 
     /**
      * 是否处于选择模式
      */
     public boolean isInChoiceMode() {
         return mChoiceMode;
     }
 
     /**
      * 设置选择模式
      * @param mode 是否开启选择模式
      */
     public void setChoiceMode(boolean mode) {
         mSelectedIndex.clear(); // 清空选中项
         mChoiceMode = mode; // 设置选择模式
     }
 
     /**
      * 全选或取消全选
      * @param checked 是否全选
      */
     public void selectAll(boolean checked) {
         Cursor cursor = getCursor();
         // 遍历所有项
         for (int i = 0; i < getCount(); i++) {
             if (cursor.moveToPosition(i)) {
                 // 只处理普通笔记类型
                 if (NoteItemData.getNoteType(cursor) == Notes.TYPE_NOTE) {
                     setCheckedItem(i, checked);
                 }
             }
         }
     }
 
     /**
      * 获取选中项的ID集合
      */
     public HashSet<Long> getSelectedItemIds() {
         HashSet<Long> itemSet = new HashSet<Long>();
         // 遍历选中项
         for (Integer position : mSelectedIndex.keySet()) {
             if (mSelectedIndex.get(position) == true) {
                 Long id = getItemId(position);
                 // 过滤根文件夹ID
                 if (id == Notes.ID_ROOT_FOLDER) {
                     Log.d(TAG, "Wrong item id, should not happen");
                 } else {
                     itemSet.add(id);
                 }
             }
         }
         return itemSet;
     }
 
     /**
      * 获取选中项的小部件属性集合
      */
     public HashSet<AppWidgetAttribute> getSelectedWidget() {
         HashSet<AppWidgetAttribute> itemSet = new HashSet<AppWidgetAttribute>();
         // 遍历选中项
         for (Integer position : mSelectedIndex.keySet()) {
             if (mSelectedIndex.get(position) == true) {
                 Cursor c = (Cursor) getItem(position);
                 if (c != null) {
                     AppWidgetAttribute widget = new AppWidgetAttribute();
                     NoteItemData item = new NoteItemData(mContext, c);
                     widget.widgetId = item.getWidgetId();
                     widget.widgetType = item.getWidgetType();
                     itemSet.add(widget);
                     // 注意：不要在这里关闭cursor，只有适配器可以关闭它
                 } else {
                     Log.e(TAG, "Invalid cursor");
                     return null;
                 }
             }
         }
         return itemSet;
     }
 
     /**
      * 获取选中项数量
      */
     public int getSelectedCount() {
         Collection<Boolean> values = mSelectedIndex.values();
         if (null == values) {
             return 0;
         }
         Iterator<Boolean> iter = values.iterator();
         int count = 0;
         // 统计选中项数量
         while (iter.hasNext()) {
             if (true == iter.next()) {
                 count++;
             }
         }
         return count;
     }
 
     /**
      * 是否全部选中
      */
     public boolean isAllSelected() {
         int checkedCount = getSelectedCount();
         return (checkedCount != 0 && checkedCount == mNotesCount);
     }
 
     /**
      * 判断指定位置项是否被选中
      */
     public boolean isSelectedItem(final int position) {
         if (null == mSelectedIndex.get(position)) {
             return false;
         }
         return mSelectedIndex.get(position);
     }
 
     /**
      * 内容变化时的回调
      */
     @Override
     protected void onContentChanged() {
         super.onContentChanged();
         calcNotesCount(); // 重新计算笔记数量
     }
 
     /**
      * 更换Cursor时的回调
      */
     @Override
     public void changeCursor(Cursor cursor) {
         super.changeCursor(cursor);
         calcNotesCount(); // 重新计算笔记数量
     }
 
     /**
      * 计算笔记数量
      */
     private void calcNotesCount() {
         mNotesCount = 0;
         // 遍历所有项
         for (int i = 0; i < getCount(); i++) {
             Cursor c = (Cursor) getItem(i);
             if (c != null) {
                 // 只统计普通笔记类型
                 if (NoteItemData.getNoteType(c) == Notes.TYPE_NOTE) {
                     mNotesCount++;
                 }
             } else {
                 Log.e(TAG, "Invalid cursor");
                 return;
             }
         }
     }
 }