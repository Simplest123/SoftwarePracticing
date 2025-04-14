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

 import java.util.Calendar;
 
 import net.micode.notes.R;
 import net.micode.notes.ui.DateTimePicker;
 import net.micode.notes.ui.DateTimePicker.OnDateTimeChangedListener;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.text.format.DateFormat;
 import android.text.format.DateUtils;
 
 public class DateTimePickerDialog extends AlertDialog implements OnClickListener {
 
     private Calendar mDate = Calendar.getInstance(); // 当前日期对象
     private boolean mIs24HourView; // 是否为24小时制
     private OnDateTimeSetListener mOnDateTimeSetListener; // 日期时间设置监听器
     private DateTimePicker mDateTimePicker; // 日期时间选择器
 
     // 日期时间设置监听器接口
     public interface OnDateTimeSetListener {
         void OnDateTimeSet(AlertDialog dialog, long date); // 设置日期时间的方法
     }
 
     // 构造函数
     public DateTimePickerDialog(Context context, long date) {
         super(context);
         mDateTimePicker = new DateTimePicker(context); // 初始化日期时间选择器
         setView(mDateTimePicker); // 设置对话框视图为日期时间选择器
         mDateTimePicker.setOnDateTimeChangedListener(new OnDateTimeChangedListener() {
             public void onDateTimeChanged(DateTimePicker view, int year, int month,
                     int dayOfMonth, int hourOfDay, int minute) {
                 // 更新日期对象
                 mDate.set(Calendar.YEAR, year);
                 mDate.set(Calendar.MONTH, month);
                 mDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                 mDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                 mDate.set(Calendar.MINUTE, minute);
                 updateTitle(mDate.getTimeInMillis()); // 更新对话框标题
             }
         });
         mDate.setTimeInMillis(date); // 设置初始时间
         mDate.set(Calendar.SECOND, 0); // 秒数设为0
         mDateTimePicker.setCurrentDate(mDate.getTimeInMillis()); // 设置选择器的当前日期
         setButton(context.getString(R.string.datetime_dialog_ok), this); // 设置确认按钮
         setButton2(context.getString(R.string.datetime_dialog_cancel), (OnClickListener)null); // 设置取消按钮
         set24HourView(DateFormat.is24HourFormat(this.getContext())); // 根据系统设置24小时制
         updateTitle(mDate.getTimeInMillis()); // 更新标题
     }
 
     // 设置是否为24小时制
     public void set24HourView(boolean is24HourView) {
         mIs24HourView = is24HourView;
     }
 
     // 设置日期时间设置监听器
     public void setOnDateTimeSetListener(OnDateTimeSetListener callBack) {
         mOnDateTimeSetListener = callBack;
     }
 
     // 更新对话框标题
     private void updateTitle(long date) {
         int flag =
             DateUtils.FORMAT_SHOW_YEAR | // 显示年份
             DateUtils.FORMAT_SHOW_DATE | // 显示日期
             DateUtils.FORMAT_SHOW_TIME; // 显示时间
         flag |= mIs24HourView ? DateUtils.FORMAT_24HOUR : DateUtils.FORMAT_12HOUR; // 根据模式选择格式
         setTitle(DateUtils.formatDateTime(this.getContext(), date, flag)); // 设置标题
     }
 
     // 按钮点击事件处理
     public void onClick(DialogInterface arg0, int arg1) {
         if (mOnDateTimeSetListener != null) {
             mOnDateTimeSetListener.OnDateTimeSet(this, mDate.getTimeInMillis()); // 通知监听器设置的日期时间
         }
     }
 
 }
 