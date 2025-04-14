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


 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.app.SearchManager;
 import android.appwidget.AppWidgetManager;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.provider.DocumentsContract;
 import android.provider.MediaStore;
 import android.text.Editable;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.TextUtils;
 import android.text.format.DateUtils;
 import android.text.style.BackgroundColorSpan;
 import android.text.style.ImageSpan;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import net.micode.notes.R;
 import net.micode.notes.data.Notes;
 import net.micode.notes.data.Notes.TextNote;
 import net.micode.notes.model.WorkingNote;
 import net.micode.notes.model.WorkingNote.NoteSettingChangedListener;
 import net.micode.notes.tool.DataUtils;
 import net.micode.notes.tool.ResourceParser;
 import net.micode.notes.tool.ResourceParser.TextAppearanceResources;
 import net.micode.notes.ui.DateTimePickerDialog.OnDateTimeSetListener;
 import net.micode.notes.ui.NoteEditText.OnTextViewChangeListener;
 import net.micode.notes.widget.NoteWidgetProvider_2x;
 import net.micode.notes.widget.NoteWidgetProvider_4x;
 
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class NoteEditActivity extends Activity implements OnClickListener,
         NoteSettingChangedListener, OnTextViewChangeListener {
     
     private class HeadViewHolder {
         public TextView tvModified; // 显示修改日期的文本视图
         public ImageView ivAlertIcon; // 提醒图标
         public TextView tvAlertDate; // 提醒日期文本视图
         public ImageView ibSetBgColor; // 设置背景颜色的按钮
     }
 
     // 背景颜色选择按钮映射
     private static final Map<Integer, Integer> sBgSelectorBtnsMap = new HashMap<Integer, Integer>();
     static {
         sBgSelectorBtnsMap.put(R.id.iv_bg_yellow, ResourceParser.YELLOW);
         sBgSelectorBtnsMap.put(R.id.iv_bg_red, ResourceParser.RED);
         sBgSelectorBtnsMap.put(R.id.iv_bg_blue, ResourceParser.BLUE);
         sBgSelectorBtnsMap.put(R.id.iv_bg_green, ResourceParser.GREEN);
         sBgSelectorBtnsMap.put(R.id.iv_bg_white, ResourceParser.WHITE);
     }
 
     // 背景颜色选择状态映射
     private static final Map<Integer, Integer> sBgSelectorSelectionMap = new HashMap<Integer, Integer>();
     static {
         sBgSelectorSelectionMap.put(ResourceParser.YELLOW, R.id.iv_bg_yellow_select);
         sBgSelectorSelectionMap.put(ResourceParser.RED, R.id.iv_bg_red_select);
         sBgSelectorSelectionMap.put(ResourceParser.BLUE, R.id.iv_bg_blue_select);
         sBgSelectorSelectionMap.put(ResourceParser.GREEN, R.id.iv_bg_green_select);
         sBgSelectorSelectionMap.put(ResourceParser.WHITE, R.id.iv_bg_white_select);
     }
 
     // 字体大小选择按钮映射
     private static final Map<Integer, Integer> sFontSizeBtnsMap = new HashMap<Integer, Integer>();
     static {
         sFontSizeBtnsMap.put(R.id.ll_font_large, ResourceParser.TEXT_LARGE);
         sFontSizeBtnsMap.put(R.id.ll_font_small, ResourceParser.TEXT_SMALL);
         sFontSizeBtnsMap.put(R.id.ll_font_normal, ResourceParser.TEXT_MEDIUM);
         sFontSizeBtnsMap.put(R.id.ll_font_super, ResourceParser.TEXT_SUPER);
     }
 
     // 字体大小选择状态映射
     private static final Map<Integer, Integer> sFontSelectorSelectionMap = new HashMap<Integer, Integer>();
     static {
         sFontSelectorSelectionMap.put(ResourceParser.TEXT_LARGE, R.id.iv_large_select);
         sFontSelectorSelectionMap.put(ResourceParser.TEXT_SMALL, R.id.iv_small_select);
         sFontSelectorSelectionMap.put(ResourceParser.TEXT_MEDIUM, R.id.iv_medium_select);
         sFontSelectorSelectionMap.put(ResourceParser.TEXT_SUPER, R.id.iv_super_select);
     }
 
     private static final String TAG = "NoteEditActivity"; // 日志标签
 
     private HeadViewHolder mNoteHeaderHolder; // 头部视图持有者
     private View mHeadViewPanel; // 头部视图面板
     private View mNoteBgColorSelector; // 背景颜色选择器
     private View mFontSizeSelector; // 字体大小选择器
     private EditText mNoteEditor; // 便签编辑器
     private View mNoteEditorPanel; // 便签编辑器面板
     private WorkingNote mWorkingNote; // 当前工作便签
     private SharedPreferences mSharedPrefs; // 共享偏好设置
     private int mFontSizeId; // 字体大小ID
     private static final String PREFERENCE_FONT_SIZE = "pref_font_size"; // 字体大小偏好设置
     private static final int SHORTCUT_ICON_TITLE_MAX_LEN = 10; // 快捷方式标题最大长度
     public static final String TAG_CHECKED = String.valueOf('\u221A'); // 已检查标记
     public static final String TAG_UNCHECKED = String.valueOf('\u25A1'); // 未检查标记
     private LinearLayout mEditTextList; // 编辑文本列表
     private String mUserQuery; // 用户查询
     private Pattern mPattern; // 正则表达式模式
     private final int PHOTO_REQUEST = 1; // 请求码
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.note_edit);
 
         if (savedInstanceState == null && !initActivityState(getIntent())) {
             finish(); // 如果初始化失败，结束活动
             return;
         }
         initResources(); // 初始化资源
 
         // 根据ID获取添加图片按钮
         final ImageButton add_img_btn = (ImageButton) findViewById(R.id.add_img_btn);
         // 为点击图片按钮设置监听器
         add_img_btn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Log.d(TAG, "onClick: click add image button");
                 // ACTION_GET_CONTENT: 允许用户选择特殊种类的数据并返回
                 Intent loadImage = new Intent(Intent.ACTION_GET_CONTENT);
                 loadImage.addCategory(Intent.CATEGORY_OPENABLE); // 指定当前动作被执行的环境
                 loadImage.setType("image/*"); // 设置类型为图片
                 startActivityForResult(loadImage, PHOTO_REQUEST); // 启动选择图片的活动
             }
         });
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         if (savedInstanceState != null && savedInstanceState.containsKey(Intent.EXTRA_UID)) {
             Intent intent = new Intent(Intent.ACTION_VIEW);
             intent.putExtra(Intent.EXTRA_UID, savedInstanceState.getLong(Intent.EXTRA_UID));
             if (!initActivityState(intent)) {
                 finish(); // 如果初始化失败，结束活动
                 return;
             }
             Log.d(TAG, "Restoring from killed activity");
         }
     }
 
     private boolean initActivityState(Intent intent) {
         mWorkingNote = null; // 初始化工作便签
         if (TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())) {
             long noteId = intent.getLongExtra(Intent.EXTRA_UID, 0);
             mUserQuery = "";
 
             // 从搜索结果开始
             if (intent.hasExtra(SearchManager.EXTRA_DATA_KEY)) {
                 noteId = Long.parseLong(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
                 mUserQuery = intent.getStringExtra(SearchManager.USER_QUERY);
             }
 
             if (!DataUtils.visibleInNoteDatabase(getContentResolver(), noteId, Notes.TYPE_NOTE)) {
                 Intent jump = new Intent(this, NotesListActivity.class);
                 startActivity(jump);
                 showToast(R.string.error_note_not_exist); // 显示错误信息
                 finish();
                 return false; // 结束活动
             } else {
                 mWorkingNote = WorkingNote.load(this, noteId); // 加载便签
                 if (mWorkingNote == null) {
                     Log.e(TAG, "load note failed with note id" + noteId);
                     finish(); // 如果加载失败，结束活动
                     return false;
                 }
             }
             getWindow().setSoftInputMode(
                     WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                             | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
         } else if(TextUtils.equals(Intent.ACTION_INSERT_OR_EDIT, intent.getAction())) {
             // 新便签
             long folderId = intent.getLongExtra(Notes.INTENT_EXTRA_FOLDER_ID, 0);
             int widgetId = intent.getIntExtra(Notes.INTENT_EXTRA_WIDGET_ID,
                     AppWidgetManager.INVALID_APPWIDGET_ID);
             int widgetType = intent.getIntExtra(Notes.INTENT_EXTRA_WIDGET_TYPE,
                     Notes.TYPE_WIDGET_INVALIDE);
             int bgResId = intent.getIntExtra(Notes.INTENT_EXTRA_BACKGROUND_ID,
                     ResourceParser.getDefaultBgId(this));
 
             // 解析通话记录便签
             String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
             long callDate = intent.getLongExtra(Notes.INTENT_EXTRA_CALL_DATE, 0);
             if (callDate != 0 && phoneNumber != null) {
                 if (TextUtils.isEmpty(phoneNumber)) {
                     Log.w(TAG, "The call record number is null");
                 }
                 long noteId = 0;
                 if ((noteId = DataUtils.getNoteIdByPhoneNumberAndCallDate(getContentResolver(),
                         phoneNumber, callDate)) > 0) {
                     mWorkingNote = WorkingNote.load(this, noteId);
                     if (mWorkingNote == null) {
                         Log.e(TAG, "load call note failed with note id" + noteId);
                         finish(); // 如果加载失败，结束活动
                         return false;
                     }
                 } else {
                     mWorkingNote = WorkingNote.createEmptyNote(this, folderId, widgetId,
                             widgetType, bgResId);
                     mWorkingNote.convertToCallNote(phoneNumber, callDate); // 转换为通话记录便签
                 }
             } else {
                 mWorkingNote = WorkingNote.createEmptyNote(this, folderId, widgetId, widgetType,
                         bgResId); // 创建新的便签
             }
 
             getWindow().setSoftInputMode(
                     WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                             | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
         } else {
             Log.e(TAG, "Intent not specified action, should not support");
             finish(); // 如果动作不支持，结束活动
             return false;
         }
         mWorkingNote.setOnSettingStatusChangedListener(this); // 设置状态改变监听器
         return true; // 初始化成功
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         initNoteScreen(); // 初始化便签屏幕
     }
 
     private void initNoteScreen() {
         mNoteEditor.setTextAppearance(this, TextAppearanceResources.getTexAppearanceResource(mFontSizeId)); // 设置文本外观
         if (mWorkingNote.getCheckListMode() == TextNote.MODE_CHECK_LIST) {
             switchToListMode(mWorkingNote.getContent()); // 切换到清单模式
         } else {
             mNoteEditor.setText(getHighlightQueryResult(mWorkingNote.getContent(), mUserQuery)); // 高亮显示查询结果
             mNoteEditor.setSelection(mNoteEditor.getText().length()); // 设置光标位置
         }
         // 隐藏所有背景选择器
         for (Integer id : sBgSelectorSelectionMap.keySet()) {
             findViewById(sBgSelectorSelectionMap.get(id)).setVisibility(View.GONE);
         }
         mHeadViewPanel.setBackgroundResource(mWorkingNote.getTitleBgResId()); // 设置头部背景
         mNoteEditorPanel.setBackgroundResource(mWorkingNote.getBgColorResId()); // 设置编辑器背景
 
         // 设置修改日期
         mNoteHeaderHolder.tvModified.setText(DateUtils.formatDateTime(this,
                 mWorkingNote.getModifiedDate(), DateUtils.FORMAT_SHOW_DATE
                         | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME
                         | DateUtils.FORMAT_SHOW_YEAR));
 
         showAlertHeader(); // 显示提醒头部
         convertToImage(); // 将路径转换为图片
     }
 
     private void showAlertHeader() {
         if (mWorkingNote.hasClockAlert()) { // 如果有时钟提醒
             long time = System.currentTimeMillis();
             if (time > mWorkingNote.getAlertDate()) {
                 mNoteHeaderHolder.tvAlertDate.setText(R.string.note_alert_expired); // 提醒过期
             } else {
                 mNoteHeaderHolder.tvAlertDate.setText(DateUtils.getRelativeTimeSpanString(
                         mWorkingNote.getAlertDate(), time, DateUtils.MINUTE_IN_MILLIS)); // 显示相对时间
             }
             mNoteHeaderHolder.tvAlertDate.setVisibility(View.VISIBLE);
             mNoteHeaderHolder.ivAlertIcon.setVisibility(View.VISIBLE);
         } else {
             mNoteHeaderHolder.tvAlertDate.setVisibility(View.GONE);
             mNoteHeaderHolder.ivAlertIcon.setVisibility(View.GONE);
         }
     }
 
     // 路径字符串格式转换为图片格式
     private void convertToImage() {
         NoteEditText noteEditText = (NoteEditText) findViewById(R.id.note_edit_view); // 获取当前的编辑视图
         Editable editable = noteEditText.getText(); // 获取文本
         String noteText = editable.toString(); // 转换为字符串
         int length = editable.length(); // 内容的长度
         // 截取img片段 [local]+uri+[local]，提取uri
         for(int i = 0; i < length; i++) {
             for(int j = i; j < length; j++) {
                 String img_fragment = noteText.substring(i, j+1); // img_fragment：关于图片路径的片段
                 if(img_fragment.length() > 15 && img_fragment.endsWith("[/local]") && img_fragment.startsWith("[local]")){
                     int limit = 7;  //[local]为7个字符
                     //[local][/local]共15个字符，剩下的为真正的path长度
                     int len = img_fragment.length()-15;
                     // 从[local]之后的len个字符就是path
                     String path = img_fragment.substring(limit,limit+len); // 获取到了图片路径
                     Bitmap bitmap = null;
                     Log.d(TAG, "图片的路径是："+path);
                     try {
                         bitmap = BitmapFactory.decodeFile(path); // 将图片路径解码为图片格式
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                     if(bitmap != null) { // 若图片存在
                         Log.d(TAG, "图片不为null");
                         ImageSpan imageSpan = new ImageSpan(NoteEditActivity.this, bitmap);
                         // 创建一个SpannableString对象，以便插入用ImageSpan对象封装的图像
                         String ss = "[local]" + path + "[/local]";
                         SpannableString spannableString = new SpannableString(ss);
                         // 将指定的标记对象附加到文本的开始...结束范围
                         spannableString.setSpan(imageSpan, 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                         Log.d(TAG, "Create spannable string success!");
                         Editable edit_text = noteEditText.getEditableText();
                         edit_text.delete(i,i+len+15); // 删除图片路径的文字
                         edit_text.insert(i, spannableString); // 在路径的起始位置插入图片
                     }
                 }
             }
         }
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         initActivityState(intent); // 处理新意图
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         // 对于没有便签ID的新便签，首先保存以生成ID
         if (!mWorkingNote.existInDatabase()) {
             saveNote();
         }
         outState.putLong(Intent.EXTRA_UID, mWorkingNote.getNoteId()); // 保存便签ID
         Log.d(TAG, "Save working note id: " + mWorkingNote.getNoteId() + " onSaveInstanceState");
     }
 
     @Override
     public boolean dispatchTouchEvent(MotionEvent ev) {
         if (mNoteBgColorSelector.getVisibility() == View.VISIBLE
                 && !inRangeOfView(mNoteBgColorSelector, ev)) {
             mNoteBgColorSelector.setVisibility(View.GONE); // 隐藏背景颜色选择器
             return true;
         }
 
         if (mFontSizeSelector.getVisibility() == View.VISIBLE
                 && !inRangeOfView(mFontSizeSelector, ev)) {
            
 
=======
 import android.content.Context;
 import android.graphics.Rect;
 import android.text.Layout;
 import android.text.Selection;
 import android.text.Spanned;
 import android.text.TextUtils;
 import android.text.style.URLSpan;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.KeyEvent;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.MotionEvent;
 import android.widget.EditText;
 
 import net.micode.notes.R;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * 自定义 EditText 控件，用于便签编辑功能
  * 主要功能：
  * 1. 支持文本编辑监听（删除、回车事件）
  * 2. 支持链接处理（电话、网页、邮件）
  * 3. 优化触摸选择体验
  */
 public class NoteEditText extends EditText {
     // 日志标签
     private static final String TAG = "NoteEditText";
     
     // 当前编辑框在列表中的索引位置
     private int mIndex; 
     
     // 删除操作前的选择起始位置
     private int mSelectionStartBeforeDelete; 
 
     // 支持的链接协议
     private static final String SCHEME_TEL = "tel:";       // 电话链接
     private static final String SCHEME_HTTP = "http:";     // 网页链接
     private static final String SCHEME_EMAIL = "mailto:";  // 邮件链接
 
     // 链接类型与对应操作文本的资源ID映射
     private static final Map<String, Integer> sSchemaActionResMap = new HashMap<String, Integer>();
     static {
         sSchemaActionResMap.put(SCHEME_TEL, R.string.note_link_tel);     // 电话链接对应的字符串资源
         sSchemaActionResMap.put(SCHEME_HTTP, R.string.note_link_web);    // 网页链接对应的字符串资源
         sSchemaActionResMap.put(SCHEME_EMAIL, R.string.note_link_email);// 邮件链接对应的字符串资源
     }
 
     /**
      * 文本变化监听接口
      * 由 NoteEditActivity 实现，用于处理编辑框的增删操作
      */
     public interface OnTextViewChangeListener {
         /**
          * 当发生删除操作且文本为空时删除当前编辑框
          * @param index 当前编辑框索引
          * @param text 当前编辑框文本内容
          */
         void onEditTextDelete(int index, String text);
 
         /**
          * 当按下回车键时添加新编辑框
          * @param index 新编辑框的索引位置
          * @param text 要放入新编辑框的文本内容
          */
         void onEditTextEnter(int index, String text);
 
         /**
          * 当文本变化时显示或隐藏选项
          * @param index 当前编辑框索引
          * @param hasText 当前是否有文本内容
          */
         void onTextChange(int index, boolean hasText);
     }
 
     // 文本变化监听器
     private OnTextViewChangeListener mOnTextViewChangeListener; 
 
     // ==================== 构造方法 ====================
 
     /**
      * 默认构造方法
      * @param context 上下文对象
      */
     public NoteEditText(Context context) {
         super(context, null);
         mIndex = 0; // 初始化索引为0
     }
 
     /**
      * 带属性集的构造方法
      * @param context 上下文对象
      * @param attrs 属性集
      */
     public NoteEditText(Context context, AttributeSet attrs) {
         super(context, attrs, android.R.attr.editTextStyle);
     }
 
     /**
      * 带属性集和样式的构造方法
      * @param context 上下文对象
      * @param attrs 属性集
      * @param defStyle 默认样式
      */
     public NoteEditText(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
     }
 
     // ==================== 公共方法 ====================
 
     /**
      * 设置当前编辑框索引
      * @param index 索引值
      */
     public void setIndex(int index) {
         mIndex = index;
     }
 
     /**
      * 设置文本变化监听器
      * @param listener 监听器实例
      */
     public void setOnTextViewChangeListener(OnTextViewChangeListener listener) {
         mOnTextViewChangeListener = listener;
     }
 
     // ==================== 触摸事件处理 ====================
 
     /**
      * 处理触摸事件，优化文本选择体验
      * @param event 触摸事件
      * @return 是否处理了该事件
      */
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         switch (event.getAction()) {
             case MotionEvent.ACTION_DOWN:
                 // 计算触摸位置对应的文本偏移量
                 int x = (int) event.getX();
                 int y = (int) event.getY();
                 x -= getTotalPaddingLeft();    // 减去内边距
                 y -= getTotalPaddingTop();
                 x += getScrollX();           // 加上滚动偏移
                 y += getScrollY();
 
                 Layout layout = getLayout();
                 int line = layout.getLineForVertical(y);         // 获取垂直方向的行号
                 int off = layout.getOffsetForHorizontal(line, x); // 获取水平方向的偏移
                 Selection.setSelection(getText(), off);            // 设置选择位置
                 break;
         }
 
         return super.onTouchEvent(event);
     }
 
     // ==================== 按键事件处理 ====================
 
     /**
      * 按键按下事件处理
      * @param keyCode 按键代码
      * @param event 按键事件
      * @return 是否处理了该事件
      */
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         switch (keyCode) {
             case KeyEvent.KEYCODE_ENTER: // 回车键
                 if (mOnTextViewChangeListener != null) {
                     return false; // 交给onKeyUp处理
                 }
                 break;
             case KeyEvent.KEYCODE_DEL: // 删除键
                 mSelectionStartBeforeDelete = getSelectionStart(); // 记录删除前的位置
                 break;
             default:
                 break;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     /**
      * 按键释放事件处理
      * @param keyCode 按键代码
      * @param event 按键事件
      * @return 是否处理了该事件
      */
     @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
         switch(keyCode) {
             case KeyEvent.KEYCODE_DEL: // 删除键
                 if (mOnTextViewChangeListener != null) {
                     // 如果在文本开头删除且不是第一个编辑框，则删除当前编辑框
                     if (0 == mSelectionStartBeforeDelete && mIndex != 0) {
                         mOnTextViewChangeListener.onEditTextDelete(mIndex, getText().toString());
                         return true;
                     }
                 } else {
                     Log.d(TAG, "OnTextViewChangeListener was not set");
                 }
                 break;
             case KeyEvent.KEYCODE_ENTER: // 回车键
                 if (mOnTextViewChangeListener != null) {
                     // 将光标后的文本拆分到新编辑框中
                     int selectionStart = getSelectionStart();
                     String text = getText().subSequence(selectionStart, length()).toString();
                     setText(getText().subSequence(0, selectionStart));
                     mOnTextViewChangeListener.onEditTextEnter(mIndex + 1, text);
                 } else {
                     Log.d(TAG, "OnTextViewChangeListener was not set");
                 }
                 break;
             default:
                 break;
         }
         return super.onKeyUp(keyCode, event);
     }
 
     // ==================== 焦点变化处理 ====================
 
     /**
      * 焦点变化处理
      * @param focused 是否获得焦点
      * @param direction 焦点方向
      * @param previouslyFocusedRect 之前获得焦点的矩形区域
      */
     @Override
     protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
         if (mOnTextViewChangeListener != null) {
             // 根据是否有文本来更新UI状态
             if (!focused && TextUtils.isEmpty(getText())) {
                 mOnTextViewChangeListener.onTextChange(mIndex, false);
             } else {
                 mOnTextViewChangeListener.onTextChange(mIndex, true);
             }
         }
         super.onFocusChanged(focused, direction, previouslyFocusedRect);
     }
 
     // ==================== 上下文菜单处理 ====================
 
     /**
      * 创建上下文菜单，用于处理链接操作
      * @param menu 上下文菜单
      */
     @Override
     protected void onCreateContextMenu(ContextMenu menu) {
         if (getText() instanceof Spanned) {
             int selStart = getSelectionStart();
             int selEnd = getSelectionEnd();
 
             int min = Math.min(selStart, selEnd);
             int max = Math.max(selStart, selEnd);
 
             // 获取选择区域内的链接
             final URLSpan[] urls = ((Spanned) getText()).getSpans(min, max, URLSpan.class);
             if (urls.length == 1) {
                 int defaultResId = 0;
                 // 根据链接类型获取对应的菜单文本资源ID
                 for(String schema: sSchemaActionResMap.keySet()) {
                     if(urls[0].getURL().indexOf(schema) >= 0) {
                         defaultResId = sSchemaActionResMap.get(schema);
                         break;
                     }
                 }
 
                 if (defaultResId == 0) {
                     defaultResId = R.string.note_link_other; // 默认其他类型链接
                 }
 
                 // 添加菜单项并设置点击事件
                 menu.add(0, 0, 0, defaultResId).setOnMenuItemClickListener(
                         new OnMenuItemClickListener() {
                             public boolean onMenuItemClick(MenuItem item) {
                                 // 触发链接点击事件
                                 urls[0].onClick(NoteEditText.this);
                                 return true;
                             }
                         });
             }
         }
         super.onCreateContextMenu(menu);
     }
 }

