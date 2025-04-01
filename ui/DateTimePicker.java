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

 import java.text.DateFormatSymbols;
 import java.util.Calendar;
 
 import net.micode.notes.R;
 
 import android.content.Context;
 import android.text.format.DateFormat;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.NumberPicker;
 
 public class DateTimePicker extends FrameLayout {
 
     private static final boolean DEFAULT_ENABLE_STATE = true;
 
     private static final int HOURS_IN_HALF_DAY = 12; // 半天的小时数
     private static final int HOURS_IN_ALL_DAY = 24; // 全天的小时数
     private static final int DAYS_IN_ALL_WEEK = 7; // 一周的天数
     private static final int DATE_SPINNER_MIN_VAL = 0; // 日期选择器最小值
     private static final int DATE_SPINNER_MAX_VAL = DAYS_IN_ALL_WEEK - 1; // 日期选择器最大值
     private static final int HOUR_SPINNER_MIN_VAL_24_HOUR_VIEW = 0; // 24小时制小时选择器最小值
     private static final int HOUR_SPINNER_MAX_VAL_24_HOUR_VIEW = 23; // 24小时制小时选择器最大值
     private static final int HOUR_SPINNER_MIN_VAL_12_HOUR_VIEW = 1; // 12小时制小时选择器最小值
     private static final int HOUR_SPINNER_MAX_VAL_12_HOUR_VIEW = 12; // 12小时制小时选择器最大值
     private static final int MINUT_SPINNER_MIN_VAL = 0; // 分钟选择器最小值
     private static final int MINUT_SPINNER_MAX_VAL = 59; // 分钟选择器最大值
     private static final int AMPM_SPINNER_MIN_VAL = 0; // AM/PM选择器最小值
     private static final int AMPM_SPINNER_MAX_VAL = 1; // AM/PM选择器最大值
 
     private final NumberPicker mDateSpinner; // 日期选择器
     private final NumberPicker mHourSpinner; // 小时选择器
     private final NumberPicker mMinuteSpinner; // 分钟选择器
     private final NumberPicker mAmPmSpinner; // AM/PM选择器
     private Calendar mDate; // 日期对象
 
     private String[] mDateDisplayValues = new String[DAYS_IN_ALL_WEEK]; // 日期显示值数组
 
     private boolean mIsAm; // 是否为上午
 
     private boolean mIs24HourView; // 是否为24小时制
 
     private boolean mIsEnabled = DEFAULT_ENABLE_STATE; // 控件是否启用
 
     private boolean mInitialising; // 初始化标志
 
     private OnDateTimeChangedListener mOnDateTimeChangedListener; // 日期时间变化监听器
 
     // 日期变化监听器
     private NumberPicker.OnValueChangeListener mOnDateChangedListener = new NumberPicker.OnValueChangeListener() {
         @Override
         public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
             mDate.add(Calendar.DAY_OF_YEAR, newVal - oldVal);
             updateDateControl(); // 更新日期控件
             onDateTimeChanged(); // 通知日期时间变化
         }
     };
 
     // 小时变化监听器
     private NumberPicker.OnValueChangeListener mOnHourChangedListener = new NumberPicker.OnValueChangeListener() {
         @Override
         public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
             boolean isDateChanged = false;
             Calendar cal = Calendar.getInstance();
             if (!mIs24HourView) {
                 // 处理12小时制的AM/PM切换
                 if (!mIsAm && oldVal == HOURS_IN_HALF_DAY - 1 && newVal == HOURS_IN_HALF_DAY) {
                     cal.setTimeInMillis(mDate.getTimeInMillis());
                     cal.add(Calendar.DAY_OF_YEAR, 1);
                     isDateChanged = true;
                 } else if (mIsAm && oldVal == HOURS_IN_HALF_DAY && newVal == HOURS_IN_HALF_DAY - 1) {
                     cal.setTimeInMillis(mDate.getTimeInMillis());
                     cal.add(Calendar.DAY_OF_YEAR, -1);
                     isDateChanged = true;
                 }
                 if (oldVal == HOURS_IN_HALF_DAY - 1 && newVal == HOURS_IN_HALF_DAY ||
                         oldVal == HOURS_IN_HALF_DAY && newVal == HOURS_IN_HALF_DAY - 1) {
                     mIsAm = !mIsAm; // 切换AM/PM
                     updateAmPmControl(); // 更新AM/PM控件
                 }
             } else {
                 // 处理24小时制的日期变化
                 if (oldVal == HOURS_IN_ALL_DAY - 1 && newVal == 0) {
                     cal.setTimeInMillis(mDate.getTimeInMillis());
                     cal.add(Calendar.DAY_OF_YEAR, 1);
                     isDateChanged = true;
                 } else if (oldVal == 0 && newVal == HOURS_IN_ALL_DAY - 1) {
                     cal.setTimeInMillis(mDate.getTimeInMillis());
                     cal.add(Calendar.DAY_OF_YEAR, -1);
                     isDateChanged = true;
                 }
             }
             int newHour = mHourSpinner.getValue() % HOURS_IN_HALF_DAY + (mIsAm ? 0 : HOURS_IN_HALF_DAY);
             mDate.set(Calendar.HOUR_OF_DAY, newHour); // 设置小时
             onDateTimeChanged(); // 通知日期时间变化
             if (isDateChanged) {
                 setCurrentYear(cal.get(Calendar.YEAR)); // 设置当前年份
                 setCurrentMonth(cal.get(Calendar.MONTH)); // 设置当前月份
                 setCurrentDay(cal.get(Calendar.DAY_OF_MONTH)); // 设置当前日期
             }
         }
     };
 
     // 分钟变化监听器
     private NumberPicker.OnValueChangeListener mOnMinuteChangedListener = new NumberPicker.OnValueChangeListener() {
         @Override
         public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
             int minValue = mMinuteSpinner.getMinValue();
             int maxValue = mMinuteSpinner.getMaxValue();
             int offset = 0;
             if (oldVal == maxValue && newVal == minValue) {
                 offset += 1; // 处理分钟的循环
             } else if (oldVal == minValue && newVal == maxValue) {
                 offset -= 1; // 处理分钟的循环
             }
             if (offset != 0) {
                 mDate.add(Calendar.HOUR_OF_DAY, offset); // 根据分钟变化调整小时
                 mHourSpinner.setValue(getCurrentHour()); // 更新小时选择器
                 updateDateControl(); // 更新日期控件
                 int newHour = getCurrentHourOfDay();
                 if (newHour >= HOURS_IN_HALF_DAY) {
                     mIsAm = false; // 设置为下午
                     updateAmPmControl(); // 更新AM/PM控件
                 } else {
                     mIsAm = true; // 设置为上午
                     updateAmPmControl(); // 更新AM/PM控件
                 }
             }
             mDate.set(Calendar.MINUTE, newVal); // 设置分钟
             onDateTimeChanged(); // 通知日期时间变化
         }
     };
 
     // AM/PM变化监听器
     private NumberPicker.OnValueChangeListener mOnAmPmChangedListener = new NumberPicker.OnValueChangeListener() {
         @Override
         public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
             mIsAm = !mIsAm; // 切换AM/PM
             if (mIsAm) {
                 mDate.add(Calendar.HOUR_OF_DAY, -HOURS_IN_HALF_DAY); // 上午减去12小时
             } else {
                 mDate.add(Calendar.HOUR_OF_DAY, HOURS_IN_HALF_DAY); // 下午加上12小时
             }
             updateAmPmControl(); // 更新AM/PM控件
             onDateTimeChanged(); // 通知日期时间变化
         }
     };
 
     // 日期时间变化监听器接口
     public interface OnDateTimeChangedListener {
         void onDateTimeChanged(DateTimePicker view, int year, int month,
                 int dayOfMonth, int hourOfDay, int minute);
     }
 
     // 构造函数
     public DateTimePicker(Context context) {
         this(context, System.currentTimeMillis());
     }
 
     public DateTimePicker(Context context, long date) {
         this(context, date, DateFormat.is24HourFormat(context));
     }
 
     public DateTimePicker(Context context, long date, boolean is24HourView) {
         super(context);
         mDate = Calendar.getInstance(); // 初始化日期对象
         mInitialising = true; // 设置初始化标志
         mIsAm = getCurrentHourOfDay() >= HOURS_IN_HALF_DAY; // 根据当前小时判断AM/PM
         inflate(context, R.layout.datetime_picker, this); // 加载布局
 
         // 初始化日期选择器
         mDateSpinner = (NumberPicker) findViewById(R.id.date);
         mDateSpinner.setMinValue(DATE_SPINNER_MIN_VAL);
         mDateSpinner.setMaxValue(DATE_SPINNER_MAX_VAL);
         mDateSpinner.setOnValueChangedListener(mOnDateChangedListener);
 
         // 初始化小时选择器
         mHourSpinner = (NumberPicker) findViewById(R.id.hour);
         mHourSpinner.setOnValueChangedListener(mOnHourChangedListener);
         
         // 初始化分钟选择器
         mMinuteSpinner =  (NumberPicker) findViewById(R.id.minute);
         mMinuteSpinner.setMinValue(MINUT_SPINNER_MIN_VAL);
         mMinuteSpinner.setMaxValue(MINUT_SPINNER_MAX_VAL);
         mMinuteSpinner.setOnLongPressUpdateInterval(100);
         mMinuteSpinner.setOnValueChangedListener(mOnMinuteChangedListener);
 
         // 初始化AM/PM选择器
         String[] stringsForAmPm = new DateFormatSymbols().getAmPmStrings();
         mAmPmSpinner = (NumberPicker) findViewById(R.id.amPm);
         mAmPmSpinner.setMinValue(AMPM_SPINNER_MIN_VAL);
         mAmPmSpinner.setMaxValue(AMPM_SPINNER_MAX_VAL);
         mAmPmSpinner.setDisplayedValues(stringsForAmPm);
         mAmPmSpinner.setOnValueChangedListener(mOnAmPmChangedListener);
 
         // 更新控件到初始状态
         updateDateControl();
         updateHourControl();
         updateAmPmControl();
 
         set24HourView(is24HourView); // 设置24小时制
 
         // 设置为当前时间
         setCurrentDate(date);
 
         setEnabled(isEnabled()); // 设置控件的启用状态
 
         // 设置内容描述
         mInitialising = false; // 结束初始化
     }
 
     @Override
     public void setEnabled(boolean enabled) {
         if (mIsEnabled == enabled) {
             return;
         }
         super.setEnabled(enabled);
         mDateSpinner.setEnabled(enabled);
         mMinuteSpinner.setEnabled(enabled);
         mHourSpinner.setEnabled(enabled);
         mAmPmSpinner.setEnabled(enabled);
         mIsEnabled = enabled; // 更新启用状态
     }
 
     @Override
     public boolean isEnabled() {
         return mIsEnabled; // 返回启用状态
     }
 
     /**
      * 获取当前日期的毫秒值
      *
      * @return 当前日期的毫秒值
      */
     public long getCurrentDateInTimeMillis() {
         return mDate.getTimeInMillis();
     }
 
     /**
      * 设置当前日期
      *
      * @param date 当前日期的毫秒值
      */
     public void setCurrentDate(long date) {
         Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(date); // 设置时间
         setCurrentDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                 cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)); // 更新日期时间
     }
 
     /**
      * 设置当前日期
      *
      * @param year 当前年份
      * @param month 当前月份
      * @param dayOfMonth 当前日期
      * @param hourOfDay 当前小时
      * @param minute 当前分钟
      */
     public void setCurrentDate(int year, int month,
             int dayOfMonth, int hourOfDay, int minute) {
         setCurrentYear(year); // 设置年份
         setCurrentMonth(month); // 设置月份
         setCurrentDay(dayOfMonth); // 设置日期
         setCurrentHour(hourOfDay); // 设置小时
         setCurrentMinute(minute); // 设置分钟
     }
 
     /**
      * 获取当前年份
      *
      * @return 当前年份
      */
     public int getCurrentYear() {
         return mDate.get(Calendar.YEAR);
     }
 
     /**
      * 设置当前年份
      *
      * @param year 当前年份
      */
     public void setCurrentYear(int year) {
         if (!mInitialising && year == getCurrentYear()) {
             return; // 如果未初始化且年份未改变，返回
         }
         mDate.set(Calendar.YEAR, year); // 设置年份
         updateDateControl(); // 更新日期控件
         onDateTimeChanged(); // 通知日期时间变化
     }
 
     /**
      * 获取当前月份
      *
      * @return 当前月份
      */
     public int getCurrentMonth() {
         return mDate.get(Calendar.MONTH);
     }
 
     /**
      * 设置当前月份
      *
      * @param month 当前月份
      */
     public void setCurrentMonth(int month) {
         if (!mInitialising && month == getCurrentMonth()) {
             return; // 如果未初始化且月份未改变，返回
         }
         mDate.set(Calendar.MONTH, month); // 设置月份
         updateDateControl(); // 更新日期控件
         onDateTimeChanged(); // 通知日期时间变化
     }
 
     /**
      * 获取当前日期
      *
      * @return 当前日期
      */
     public int getCurrentDay() {
         return mDate.get(Calendar.DAY_OF_MONTH);
     }
 
     /**
      * 设置当前日期
      *
      * @param dayOfMonth 当前日期
      */
     public void setCurrentDay(int dayOfMonth) {
         if (!mInitialising && dayOfMonth == getCurrentDay()) {
             return; // 如果未初始化且日期未改变，返回
         }
         mDate.set(Calendar.DAY_OF_MONTH, dayOfMonth); // 设置日期
         updateDateControl(); // 更新日期控件
         onDateTimeChanged(); // 通知日期时间变化
     }
 
     /**
      * 获取当前小时（24小时制）
      * @return 当前小时（24小时制）
      */
     public int getCurrentHourOfDay() {
         return mDate.get(Calendar.HOUR_OF_DAY);
     }
 
     private int getCurrentHour() {
         if (mIs24HourView){
             return getCurrentHourOfDay(); // 如果是24小时制，返回当前小时
         } else {
             int hour = getCurrentHourOfDay();
             if (hour > HOURS_IN_HALF_DAY) {
                 return hour - HOURS_IN_HALF_DAY; // 下午
             } else {
                 return hour == 0 ? HOURS_IN_HALF_DAY : hour; // 上午
             }
         }
     }
 
     /**
      * 设置当前小时（24小时制）
      *
      * @param hourOfDay 当前小时
      */
     public void setCurrentHour(int hourOfDay) {
         if (!mInitialising && hourOfDay == getCurrentHourOfDay()) {
             return; // 如果未初始化且小时未改变，返回
         }
         mDate.set(Calendar.HOUR_OF_DAY, hourOfDay); // 设置小时
         if (!mIs24HourView) {
             if (hourOfDay >= HOURS_IN_HALF_DAY) {
                 mIsAm = false; // 设置为下午
                 if (hourOfDay > HOURS_IN_HALF_DAY) {
                     hourOfDay -= HOURS_IN_HALF_DAY; // 转换为12小时制
                 }
             } else {
                 mIsAm = true; // 设置为上午
                 if (hourOfDay == 0) {
                     hourOfDay = HOURS_IN_HALF_DAY; // 0小时转换为12小时
                 }
             }
             updateAmPmControl(); // 更新AM/PM控件
         }
         mHourSpinner.setValue(hourOfDay); // 更新小时选择器
         onDateTimeChanged(); // 通知日期时间变化
     }
 
     /**
      * 获取当前分钟
      *
      * @return 当前分钟
      */
     public int getCurrentMinute() {
         return mDate.get(Calendar.MINUTE);
     }
 
     /**
      * 设置当前分钟
      */
     public void setCurrentMinute(int minute) {
         if (!mInitialising && minute == getCurrentMinute()) {
             return; // 如果未初始化且分钟未改变，返回
         }
         mMinuteSpinner.setValue(minute); // 更新分钟选择器
         mDate.set(Calendar.MINUTE, minute); // 设置分钟
         onDateTimeChanged(); // 通知日期时间变化
     }
 
     /**
      * @return 如果是24小时制返回true，否则返回false。
      */
     public boolean is24HourView () {
         return mIs24HourView;
     }
 
     /**
      * 设置是否为24小时制或AM/PM模式。
      *
      * @param is24HourView 如果为24小时制则为true，否则为false。
      */
     public void set24HourView(boolean is24HourView) {
         if (mIs24HourView == is24HourView) {
             return; // 如果模式未改变，返回
         }
         mIs24HourView = is24HourView; // 更新模式
         mAmPmSpinner.setVisibility(is24HourView ? View.GONE : View.VISIBLE); // 显示或隐藏AM/PM选择器
         int hour = getCurrentHourOfDay();
         updateHourControl(); // 更新小时控件
         setCurrentHour(hour); // 设置当前小时
         updateAmPmControl(); // 更新AM/PM控件
     }
 
     private void updateDateControl() {
 
    private void updateDateControl() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mDate.getTimeInMillis());
        cal.add(Calendar.DAY_OF_YEAR, -DAYS_IN_ALL_WEEK / 2 - 1);
        mDateSpinner.setDisplayedValues(null);
        for (int i = 0; i < DAYS_IN_ALL_WEEK; ++i) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            mDateDisplayValues[i] = (String) DateFormat.format("MM.dd EEEE", cal);
        }
        mDateSpinner.setDisplayedValues(mDateDisplayValues);
        mDateSpinner.setValue(DAYS_IN_ALL_WEEK / 2);
        mDateSpinner.invalidate();
    }

    private void updateAmPmControl() {
        if (mIs24HourView) {
            mAmPmSpinner.setVisibility(View.GONE);
        } else {
            int index = mIsAm ? Calendar.AM : Calendar.PM;
            mAmPmSpinner.setValue(index);
            mAmPmSpinner.setVisibility(View.VISIBLE);
        }
    }

    private void updateHourControl() {
        if (mIs24HourView) {
            mHourSpinner.setMinValue(HOUR_SPINNER_MIN_VAL_24_HOUR_VIEW);
            mHourSpinner.setMaxValue(HOUR_SPINNER_MAX_VAL_24_HOUR_VIEW);
        } else {
            mHourSpinner.setMinValue(HOUR_SPINNER_MIN_VAL_12_HOUR_VIEW);
            mHourSpinner.setMaxValue(HOUR_SPINNER_MAX_VAL_12_HOUR_VIEW);
        }
    }

    /**
     * Set the callback that indicates the 'Set' button has been pressed.
     * @param callback the callback, if null will do nothing
     */
    public void setOnDateTimeChangedListener(OnDateTimeChangedListener callback) {
        mOnDateTimeChangedListener = callback;
    }

    private void onDateTimeChanged() {
        if (mOnDateTimeChangedListener != null) {
            mOnDateTimeChangedListener.onDateTimeChanged(this, getCurrentYear(),
                    getCurrentMonth(), getCurrentDay(), getCurrentHourOfDay(), getCurrentMinute());
        }
    }
}
