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

 package net.micode.notes.tool;

 /**
  * Google Task 字符串常量工具类
  * 
  * 该类定义了与Google Tasks API交互时使用的JSON字段名和常量值
  * 主要用于便签与Google Tasks服务之间的数据同步
  */
 public class GTaskStringUtils {
 
     // ==================== Google Tasks API JSON字段名 ====================
     
     /** 操作ID字段名 */
     public final static String GTASK_JSON_ACTION_ID = "action_id";
     
     /** 操作列表字段名 */
     public final static String GTASK_JSON_ACTION_LIST = "action_list";
     
     /** 操作类型字段名 */
     public final static String GTASK_JSON_ACTION_TYPE = "action_type";
     
     /** 创建操作类型值 */
     public final static String GTASK_JSON_ACTION_TYPE_CREATE = "create";
     
     /** 获取全部操作类型值 */
     public final static String GTASK_JSON_ACTION_TYPE_GETALL = "get_all";
     
     /** 移动操作类型值 */
     public final static String GTASK_JSON_ACTION_TYPE_MOVE = "move";
     
     /** 更新操作类型值 */
     public final static String GTASK_JSON_ACTION_TYPE_UPDATE = "update";
     
     /** 创建者ID字段名 */
     public final static String GTASK_JSON_CREATOR_ID = "creator_id";
     
     /** 子实体字段名 */
     public final static String GTASK_JSON_CHILD_ENTITY = "child_entity";
     
     /** 客户端版本字段名 */
     public final static String GTASK_JSON_CLIENT_VERSION = "client_version";
     
     /** 完成状态字段名 */
     public final static String GTASK_JSON_COMPLETED = "completed";
     
     /** 当前列表ID字段名 */
     public final static String GTASK_JSON_CURRENT_LIST_ID = "current_list_id";
     
     /** 默认列表ID字段名 */
     public final static String GTASK_JSON_DEFAULT_LIST_ID = "default_list_id";
     
     /** 删除状态字段名 */
     public final static String GTASK_JSON_DELETED = "deleted";
     
     /** 目标列表字段名 */
     public final static String GTASK_JSON_DEST_LIST = "dest_list";
     
     /** 目标父项字段名 */
     public final static String GTASK_JSON_DEST_PARENT = "dest_parent";
     
     /** 目标父项类型字段名 */
     public final static String GTASK_JSON_DEST_PARENT_TYPE = "dest_parent_type";
     
     /** 实体变更字段名 */
     public final static String GTASK_JSON_ENTITY_DELTA = "entity_delta";
     
     /** 实体类型字段名 */
     public final static String GTASK_JSON_ENTITY_TYPE = "entity_type";
     
     /** 获取已删除项字段名 */
     public final static String GTASK_JSON_GET_DELETED = "get_deleted";
     
     /** ID字段名 */
     public final static String GTASK_JSON_ID = "id";
     
     /** 索引字段名 */
     public final static String GTASK_JSON_INDEX = "index";
     
     /** 最后修改时间字段名 */
     public final static String GTASK_JSON_LAST_MODIFIED = "last_modified";
     
     /** 最新同步点字段名 */
     public final static String GTASK_JSON_LATEST_SYNC_POINT = "latest_sync_point";
     
     /** 列表ID字段名 */
     public final static String GTASK_JSON_LIST_ID = "list_id";
     
     /** 列表集合字段名 */
     public final static String GTASK_JSON_LISTS = "lists";
     
     /** 名称字段名 */
     public final static String GTASK_JSON_NAME = "name";
     
     /** 新ID字段名 */
     public final static String GTASK_JSON_NEW_ID = "new_id";
     
     /** 笔记字段名 */
     public final static String GTASK_JSON_NOTES = "notes";
     
     /** 父ID字段名 */
     public final static String GTASK_JSON_PARENT_ID = "parent_id";
     
     /** 前兄弟节点ID字段名 */
     public final static String GTASK_JSON_PRIOR_SIBLING_ID = "prior_sibling_id";
     
     /** 结果字段名 */
     public final static String GTASK_JSON_RESULTS = "results";
     
     /** 源列表字段名 */
     public final static String GTASK_JSON_SOURCE_LIST = "source_list";
     
     /** 任务字段名 */
     public final static String GTASK_JSON_TASKS = "tasks";
     
     /** 类型字段名 */
     public final static String GTASK_JSON_TYPE = "type";
     
     /** 组类型值 */
     public final static String GTASK_JSON_TYPE_GROUP = "GROUP";
     
     /** 任务类型值 */
     public final static String GTASK_JSON_TYPE_TASK = "TASK";
     
     /** 用户字段名 */
     public final static String GTASK_JSON_USER = "user";
 
     // ==================== MIUI特定字段 ====================
     
     /** MIUI文件夹前缀 */
     public final static String MIUI_FOLDER_PREFFIX = "[MIUI_Notes]";
     
     /** 默认文件夹名称 */
     public final static String FOLDER_DEFAULT = "Default";
     
     /** 通话记录文件夹名称 */
     public final static String FOLDER_CALL_NOTE = "Call_Note";
     
     /** 元数据文件夹名称 */
     public final static String FOLDER_META = "METADATA";
     
     /** 元数据头-GTask ID */
     public final static String META_HEAD_GTASK_ID = "meta_gid";
     
     /** 元数据头-笔记 */
     public final static String META_HEAD_NOTE = "meta_note";
     
     /** 元数据头-数据 */
     public final static String META_HEAD_DATA = "meta_data";
     
     /** 元数据笔记名称(提示不要更新或删除) */
     public final static String META_NOTE_NAME = "[META INFO] DON'T UPDATE AND DELETE";
 }