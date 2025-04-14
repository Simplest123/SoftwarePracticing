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
 import android.text.format.DateUtils;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import net.micode.notes.R;
 import net.micode.notes.data.Notes;
 import net.micode.notes.tool.DataUtils;
 import net.micode.notes.tool.ResourceParser.NoteItemBgResources;
 

 /**
  * 笔记列表项自定义视图，继承自LinearLayout
  */
 public class NotesListItem extends LinearLayout {
     // 控件成员变量
     private ImageView mAlert;      // 提醒图标
     private TextView mTitle;       // 标题文本
     private TextView mTime;        // 时间文本
     private TextView mCallName;    // 通话记录名称
     private NoteItemData mItemData; // 笔记项数据对象
     private CheckBox mCheckBox;    // 复选框
 
     /**
      * 构造函数
      * @param context 上下文对象
      */
     public NotesListItem(Context context) {
         super(context);
         // 从布局文件填充视图
         inflate(context, R.layout.note_item, this);
         // 初始化各个视图控件

         mAlert = (ImageView) findViewById(R.id.iv_alert_icon);
         mTitle = (TextView) findViewById(R.id.tv_title);
         mTime = (TextView) findViewById(R.id.tv_time);
         mCallName = (TextView) findViewById(R.id.tv_name);
         mCheckBox = (CheckBox) findViewById(android.R.id.checkbox);
     }
 

     /**
      * 绑定数据到视图
      * @param context 上下文对象
      * @param data 笔记项数据
      * @param choiceMode 是否处于选择模式
      * @param checked 是否被选中
      */
     public void bind(Context context, NoteItemData data, boolean choiceMode, boolean checked) {
         // 处理复选框的显示逻辑
         if (choiceMode && data.getType() == Notes.TYPE_NOTE) {
             mCheckBox.setVisibility(View.VISIBLE);
             mCheckBox.setChecked(checked);
         } else {
             mCheckBox.setVisibility(View.GONE);
         }
 
         mItemData = data;
         
         // 根据不同类型的数据设置不同的显示方式
         if (data.getId() == Notes.ID_CALL_RECORD_FOLDER) {
             // 通话记录文件夹的特殊处理
             mCallName.setVisibility(View.GONE);
             mAlert.setVisibility(View.VISIBLE);
             mTitle.setTextAppearance(context, R.style.TextAppearancePrimaryItem);
             mTitle.setText(context.getString(R.string.call_record_folder_name)
                     + context.getString(R.string.format_folder_files_count, data.getNotesCount()));
             mAlert.setImageResource(R.drawable.call_record);
         } else if (data.getParentId() == Notes.ID_CALL_RECORD_FOLDER) {
             // 通话记录项的特殊处理
             mCallName.setVisibility(View.VISIBLE);
             mCallName.setText(data.getCallName());
             mTitle.setTextAppearance(context,R.style.TextAppearanceSecondaryItem);
             mTitle.setText(DataUtils.getFormattedSnippet(data.getSnippet()));
             // 设置提醒图标
             if (data.hasAlert()) {
                 mAlert.setImageResource(R.drawable.clock);
                 mAlert.setVisibility(View.VISIBLE);
             } else {
                 mAlert.setVisibility(View.GONE);
             }
         } else {
             // 普通笔记或文件夹的处理
             mCallName.setVisibility(View.GONE);
             mTitle.setTextAppearance(context, R.style.TextAppearancePrimaryItem);
 
             if (data.getType() == Notes.TYPE_FOLDER) {
                 // 文件夹的特殊处理
                 mTitle.setText(data.getSnippet()
                         + context.getString(R.string.format_folder_files_count,
                                 data.getNotesCount()));
                 mAlert.setVisibility(View.GONE);
             } else {
                 // 普通笔记的处理
                 mTitle.setText(DataUtils.getFormattedSnippet(data.getSnippet()));
                 // 设置提醒图标
                 if (data.hasAlert()) {
                     mAlert.setImageResource(R.drawable.clock);
                     mAlert.setVisibility(View.VISIBLE);
                 } else {
                     mAlert.setVisibility(View.GONE);
                 }
             }
         }
         // 设置时间文本（使用相对时间格式）
         mTime.setText(DateUtils.getRelativeTimeSpanString(data.getModifiedDate()));
 
         // 设置背景
         setBackground(data);
     }
 
     /**
      * 根据数据设置背景
      * @param data 笔记项数据
      */
     private void setBackground(NoteItemData data) {
         int id = data.getBgColorId();
         if (data.getType() == Notes.TYPE_NOTE) {
             // 根据笔记在列表中的位置设置不同的背景
             if (data.isSingle() || data.isOneFollowingFolder()) {
                 setBackgroundResource(NoteItemBgResources.getNoteBgSingleRes(id));
             } else if (data.isLast()) {
                 setBackgroundResource(NoteItemBgResources.getNoteBgLastRes(id));
             } else if (data.isFirst() || data.isMultiFollowingFolder()) {
                 setBackgroundResource(NoteItemBgResources.getNoteBgFirstRes(id));
             } else {
                 setBackgroundResource(NoteItemBgResources.getNoteBgNormalRes(id));
             }
         } else {
             // 文件夹的背景
             setBackgroundResource(NoteItemBgResources.getFolderBgRes());
         }
     }
 
     /**
      * 获取当前绑定的笔记项数据
      */
     public NoteItemData getItemData() {
         return mItemData;
     }
 }

