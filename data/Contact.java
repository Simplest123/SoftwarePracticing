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

/**
 * 该类用于查询联系人信息。
 * 它通过电话号码查询联系人姓名，并使用缓存机制减少数据库查询次数。
 */
public class Contact {
    // 用于缓存电话号码和联系人姓名的映射，以减少数据库查询次数
    private static HashMap<String, String> sContactCache;
    private static final String TAG = "Contact";

    /**
     * 查询联系人数据库的 SQL 语句。
     * 通过电话号码匹配联系人。
     * 这里使用了 PHONE_NUMBERS_EQUAL 以便处理不同格式的电话号码。
     */
    private static final String CALLER_ID_SELECTION = "PHONE_NUMBERS_EQUAL(" + Phone.NUMBER
            + ",?) AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'"
            + " AND " + Data.RAW_CONTACT_ID + " IN "
            + "(SELECT raw_contact_id "
            + " FROM phone_lookup"
            + " WHERE min_match = '+')";

    /**
     * 根据电话号码获取联系人姓名。
     *
     * @param context 应用程序的上下文，用于访问 ContentResolver。
     * @param phoneNumber 要查询的电话号码。
     * @return 如果找到联系人，则返回联系人姓名，否则返回 null。
     */
    public static String getContact(Context context, String phoneNumber) {
        // 初始化联系人缓存 HashMap
        if (sContactCache == null) {
            sContactCache = new HashMap<String, String>();
        }

        // 如果缓存中已有该电话号码对应的联系人姓名，则直接返回
        if (sContactCache.containsKey(phoneNumber)) {
            return sContactCache.get(phoneNumber);
        }

        // 生成匹配的 SQL 查询语句
        String selection = CALLER_ID_SELECTION.replace("+",
                PhoneNumberUtils.toCallerIDMinMatch(phoneNumber));

        // 查询联系人数据库
        Cursor cursor = context.getContentResolver().query(
                Data.CONTENT_URI,
                new String[]{ Phone.DISPLAY_NAME },  // 只查询联系人姓名
                selection,
                new String[]{ phoneNumber },  // 绑定参数，防止 SQL 注入
                null);

        // 处理查询结果
        if (cursor != null && cursor.moveToFirst()) {
            try {
                // 获取联系人姓名
                String name = cursor.getString(0);
                // 将结果存入缓存
                sContactCache.put(phoneNumber, name);
                return name;
            } catch (IndexOutOfBoundsException e) {
                // 捕获异常，防止崩溃，并记录日志
                Log.e(TAG, " Cursor get string error " + e.toString());
                return null;
            } finally {
                // 关闭游标，防止内存泄漏
                cursor.close();
            }
        } else {
            // 如果没有匹配的联系人，记录日志
            Log.d(TAG, "No contact matched with number:" + phoneNumber);
            return null;
        }
    }
}
