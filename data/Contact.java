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

 package net.micode.notes.data;

 import android.content.Context;
 import android.database.Cursor;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.Data;
 import android.telephony.PhoneNumberUtils;
 import android.util.Log;
 
 import java.util.HashMap;
 
 // Contact类用于获取联系人信息
 public class Contact {
     // 用于缓存联系人信息的HashMap
     private static HashMap<String, String> sContactCache;
     // 日志标签
     private static final String TAG = "Contact";
 
     // 查询条件，用于匹配来电号码
     private static final String CALLER_ID_SELECTION = "PHONE_NUMBERS_EQUAL(" + Phone.NUMBER
     + ",?) AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'"
     + " AND " + Data.RAW_CONTACT_ID + " IN "
             + "(SELECT raw_contact_id "
             + " FROM phone_lookup"
             + " WHERE min_match = '+')";
 
     // 根据电话号码获取联系人名字
     public static String getContact(Context context, String phoneNumber) {
         // 初始化缓存
         if(sContactCache == null) {
             sContactCache = new HashMap<String, String>();
         }
 
         // 如果缓存中已存在该电话号码的联系人，则直接返回
         if(sContactCache.containsKey(phoneNumber)) {
             return sContactCache.get(phoneNumber);
         }
 
         // 替换查询条件中的"+"为电话号码的最小匹配格式
         String selection = CALLER_ID_SELECTION.replace("+",
                 PhoneNumberUtils.toCallerIDMinMatch(phoneNumber));
         
         // 查询联系人信息
         Cursor cursor = context.getContentResolver().query(
                 Data.CONTENT_URI,
                 new String [] { Phone.DISPLAY_NAME }, // 查询显示名称
                 selection, // 查询条件
                 new String[] { phoneNumber }, // 查询参数
                 null);
 
         // 如果查询结果不为空且移动到第一条记录
         if (cursor != null && cursor.moveToFirst()) {
             try {
                 // 获取联系人名称
                 String name = cursor.getString(0);
                 // 将联系人名称存入缓存
                 sContactCache.put(phoneNumber, name);
                 return name; // 返回联系人名称
             } catch (IndexOutOfBoundsException e) {
                 // 处理索引越界异常
                 Log.e(TAG, " Cursor get string error " + e.toString());
                 return null; // 返回null表示未找到联系人
             } finally {
                 cursor.close(); // 关闭游标
             }
         } else {
             // 如果没有匹配的联系人，记录日志
             Log.d(TAG, "No contact matched with number:" + phoneNumber);
             return null; // 返回null表示未找到联系人
         }
     }
 }
 