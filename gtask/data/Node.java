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

package net.micode.notes.gtask.data;

import android.database.Cursor;
import org.json.JSONObject;

/**
 * 抽象节点类，用于数据同步管理
 * 定义节点基础属性和同步操作行为模板，子类需实现具体同步逻辑
 */
public abstract class Node {
    // 同步动作类型常量
    public static final int SYNC_ACTION_NONE = 0; // 无同步操作
    public static final int SYNC_ACTION_ADD_REMOTE = 1; // 需要向远程服务器添加数据
    public static final int SYNC_ACTION_ADD_LOCAL = 2; // 需要向本地数据库添加数据
    public static final int SYNC_ACTION_DEL_REMOTE = 3; // 需要删除远程服务器数据
    public static final int SYNC_ACTION_DEL_LOCAL = 4; // 需要删除本地数据库数据
    public static final int SYNC_ACTION_UPDATE_REMOTE = 5; // 需要更新远程服务器数据
    public static final int SYNC_ACTION_UPDATE_LOCAL = 6; // 需要更新本地数据库数据
    public static final int SYNC_ACTION_UPDATE_CONFLICT = 7; // 存在数据冲突需要处理
    public static final int SYNC_ACTION_ERROR = 8; // 同步过程中发生错误

    private String mGid; // 节点在任务系统中的全局唯一标识符
    private String mName; // 节点显示名称
    private long mLastModified; // 最后修改时间戳（单位：毫秒）
    private boolean mDeleted; // 软删除标记（true表示已标记删除）

    /**
     * 初始化节点属性
     */
    public Node() {
        mGid = null;
        mName = "";
        mLastModified = 0;
        mDeleted = false;
    }

    /* 抽象方法定义（子类必须实现） */

    /**
     * 生成创建动作的JSON数据
     * 
     * @param actionId 动作ID（用于请求序列号）
     * @return 包含创建指令的JSON对象
     */
    public abstract JSONObject getCreateAction(int actionId);

    /**
     * 生成更新动作的JSON数据
     * 
     * @param actionId 动作ID（用于请求序列号）
     * @return 包含更新指令的JSON对象
     */
    public abstract JSONObject getUpdateAction(int actionId);

    /**
     * 从远程JSON数据解析节点内容
     * 
     * @param js 包含远程节点数据的JSON对象
     */
    public abstract void setContentByRemoteJSON(JSONObject js);

    /**
     * 从本地JSON数据加载节点内容
     * 
     * @param js 包含本地节点数据的JSON对象
     */
    public abstract void setContentByLocalJSON(JSONObject js);

    /**
     * 将节点内容转换为本地存储的JSON格式
     * 
     * @return 包含节点内容的JSON对象
     */
    public abstract JSONObject getLocalJSONFromContent();

    /**
     * 根据数据库记录判断同步动作类型
     * 
     * @param c 数据库游标（指向当前记录）
     * @return 需要执行的同步动作类型常量
     */
    public abstract int getSyncAction(Cursor c);

    /* 基础属性访问方法 */

    /**
     * 设置全局唯一标识符
     * 
     * @param gid 任务系统分配的全局ID
     */
    public void setGid(String gid) {
        this.mGid = gid;
    }

    /**
     * 设置节点名称
     * 
     * @param name 显示名称（最大长度受系统限制）
     */
    public void setName(String name) {
        this.mName = name;
    }

    /**
     * 设置最后修改时间
     * 
     * @param lastModified 时间戳（毫秒级）
     */
    public void setLastModified(long lastModified) {
        this.mLastModified = lastModified;
    }

    /**
     * 设置删除标记
     * 
     * @param deleted true-标记为已删除 false-正常状态
     */
    public void setDeleted(boolean deleted) {
        this.mDeleted = deleted;
    }

    /**
     * 获取全局唯一标识符
     * 
     * @return 可能为null（表示未同步到远程的新建节点）
     */
    public String getGid() {
        return this.mGid;
    }

    /**
     * 获取节点显示名称
     * 
     * @return 非null字符串（可能为空字符串）
     */
    public String getName() {
        return this.mName;
    }

    /**
     * 获取最后修改时间
     * 
     * @return 0表示时间未初始化
     */
    public long getLastModified() {
        return this.mLastModified;
    }

    /**
     * 获取删除状态
     * 
     * @return true-已标记删除（需执行删除同步）
     */
    public boolean getDeleted() {
        return this.mDeleted;
    }
}