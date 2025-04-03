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