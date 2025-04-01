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
 
 public class NotesListItem extends LinearLayout {
     private ImageView mAlert; // 提醒图标
     private TextView mTitle; // 便签标题
     private TextView mTime; // 便签时间
     private TextView mCallName; // 联系人姓名
     private NoteItemData mItemData; // 便签数据
     private CheckBox mCheckBox; // 选择框
 
     public NotesListItem(Context context) {
         super(context);
         inflate(context, R.layout.note_item, this); // 加载布局
         mAlert = (ImageView) findViewById(R.id.iv_alert_icon);
         mTitle = (TextView) findViewById(R.id.tv_title);
         mTime = (TextView) findViewById(R.id.tv_time);
         mCallName = (TextView) findViewById(R.id.tv_name);
         mCheckBox = (CheckBox) findViewById(android.R.id.checkbox);
     }
 
     // 绑定数据到视图
     public void bind(Context context, NoteItemData data, boolean choiceMode, boolean checked) {
         if (choiceMode && data.getType() == Notes.TYPE_NOTE) {
             mCheckBox.setVisibility(View.VISIBLE); // 显示选择框
             mCheckBox.setChecked(checked); // 设置选择状态
         } else {
             mCheckBox.setVisibility(View.GONE); // 隐藏选择框
         }
 
         mItemData = data; // 保存便签数据
         if (data.getId() == Notes.ID_CALL_RECORD_FOLDER) {
             mCallName.setVisibility(View.GONE); // 隐藏联系人姓名
             mAlert.setVisibility(View.VISIBLE); // 显示提醒图标
             mTitle.setTextAppearance(context, R.style.TextAppearancePrimaryItem);
             mTitle.setText(context.getString(R.string.call_record_folder_name)
                     + context.getString(R.string.format_folder_files_count, data.getNotesCount()));
             mAlert.setImageResource(R.drawable.call_record); // 设置通话记录图标
         } else if (data.getParentId() == Notes.ID_CALL_RECORD_FOLDER) {
             mCallName.setVisibility(View.VISIBLE); // 显示联系人姓名
             mCallName.setText(data.getCallName());
             mTitle.setTextAppearance(context, R.style.TextAppearanceSecondaryItem);
             mTitle.setText(DataUtils.getFormattedSnippet(data.getSnippet())); // 设置便签内容
             if (data.hasAlert()) {
                 mAlert.setImageResource(R.drawable.clock); // 设置提醒图标
                 mAlert.setVisibility(View.VISIBLE);
             } else {
                 mAlert.setVisibility(View.GONE); // 隐藏提醒图标
             }
         } else {
             mCallName.setVisibility(View.GONE); // 隐藏联系人姓名
             mTitle.setTextAppearance(context, R.style.TextAppearancePrimaryItem);
 
             if (data.getType() == Notes.TYPE_FOLDER) {
                 mTitle.setText(data.getSnippet()
                         + context.getString(R.string.format_folder_files_count,
                                 data.getNotesCount())); // 显示文件夹名称及数量
                 mAlert.setVisibility(View.GONE); // 隐藏提醒图标
             } else {
                 mTitle.setText(DataUtils.getFormattedSnippet(data.getSnippet())); // 设置便签内容
                 if (data.hasAlert()) {
                     mAlert.setImageResource(R.drawable.clock); // 设置提醒图标
                     mAlert.setVisibility(View.VISIBLE);
                 } else {
                     mAlert.setVisibility(View.GONE); // 隐藏提醒图标
                 }
             }
         }
         mTime.setText(DateUtils.getRelativeTimeSpanString(data.getModifiedDate())); // 显示修改时间
 
         setBackground(data); // 设置背景
     }
 
     // 设置背景
     private void setBackground(NoteItemData data) {
         int id = data.getBgColorId();
         if (data.getType() == Notes.TYPE_NOTE) {
             if (data.isSingle() || data.isOneFollowingFolder()) {
                 setBackgroundResource(NoteItemBgResources.getNoteBgSingleRes(id)); // 单个便签背景
             } else if (data.isLast()) {
                 setBackgroundResource(NoteItemBgResources.getNoteBgLastRes(id)); // 最后一个便签背景
             } else if (data.isFirst() || data.isMultiFollowingFolder()) {
                 setBackgroundResource(NoteItemBgResources.getNoteBgFirstRes(id)); // 第一个便签背景
             } else {
                 setBackgroundResource(NoteItemBgResources.getNoteBgNormalRes(id)); // 普通便签背景
             }
         } else {
             setBackgroundResource(NoteItemBgResources.getFolderBgRes()); // 文件夹背景
         }
     }
 
     // 获取便签数据
     public NoteItemData getItemData() {
         return mItemData;
     }
 }
 