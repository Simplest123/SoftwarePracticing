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

             + " WHERE min_match = '+')"; // '+'将在运行时替换为最小匹配位数
 
     /**
      * 根据电话号码查询联系人名称
      * @param context 上下文对象
      * @param phoneNumber 要查询的电话号码
      * @return 对应的联系人名称，未找到时返回null
      */

     public static String getContact(Context context, String phoneNumber) {
         // 初始化缓存
         if(sContactCache == null) {
             sContactCache = new HashMap<String, String>();
         }
 

         if(sContactCache.containsKey(phoneNumber)) {
             return sContactCache.get(phoneNumber);
         }
 

         // 构建完整的查询条件：替换最小匹配位数
         String selection = CALLER_ID_SELECTION.replace("+",
                 PhoneNumberUtils.toCallerIDMinMatch(phoneNumber));
         
         // 查询联系人数据库
         Cursor cursor = context.getContentResolver().query(
                 Data.CONTENT_URI,                // 数据URI
                 new String [] { Phone.DISPLAY_NAME }, // 要获取的列（显示名称）
                 selection,                       // 查询条件
                 new String[] { phoneNumber },    // 查询参数
                 null);                           // 排序方式
 
         if (cursor != null && cursor.moveToFirst()) {
             try {
                 // 获取并缓存联系人名称
                 String name = cursor.getString(0);
                 sContactCache.put(phoneNumber, name);
                 return name;
             } catch (IndexOutOfBoundsException e) {
                 Log.e(TAG, "Cursor字段获取错误: " + e.toString());
                 return null;
             } finally {
                 cursor.close(); // 确保关闭Cursor释放资源
             }
         } else {
             Log.d(TAG, "未找到匹配的联系人号码:" + phoneNumber);
             return null;
         }
     }
 }

