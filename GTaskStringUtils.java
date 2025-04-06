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

 public class GTaskStringUtils {
 
     // JSON 字段：操作ID
     public final static String GTASK_JSON_ACTION_ID = "action_id";
 
     // JSON 字段：操作列表
     public final static String GTASK_JSON_ACTION_LIST = "action_list";
 
     // JSON 字段：操作类型
     public final static String GTASK_JSON_ACTION_TYPE = "action_type";
 
     // JSON 操作类型：创建
     public final static String GTASK_JSON_ACTION_TYPE_CREATE = "create";
 
     // JSON 操作类型：获取全部
     public final static String GTASK_JSON_ACTION_TYPE_GETALL = "get_all";
 
     // JSON 操作类型：移动
     public final static String GTASK_JSON_ACTION_TYPE_MOVE = "move";
 
     // JSON 操作类型：更新
     public final static String GTASK_JSON_ACTION_TYPE_UPDATE = "update";
 
     // JSON 字段：创建者ID
     public final static String GTASK_JSON_CREATOR_ID = "creator_id";
 
     // JSON 字段：子实体（如任务或子任务）
     public final static String GTASK_JSON_CHILD_ENTITY = "child_entity";
 
     // JSON 字段：客户端版本
     public final static String GTASK_JSON_CLIENT_VERSION = "client_version";
 
     // JSON 字段：是否完成
     public final static String GTASK_JSON_COMPLETED = "completed";
 
     // JSON 字段：当前列表ID
     public final static String GTASK_JSON_CURRENT_LIST_ID = "current_list_id";
 
     // JSON 字段：默认列表ID
     public final static String GTASK_JSON_DEFAULT_LIST_ID = "default_list_id";
 
     // JSON 字段：是否已删除
     public final static String GTASK_JSON_DELETED = "deleted";
 
     // JSON 字段：目标列表
     public final static String GTASK_JSON_DEST_LIST = "dest_list";
 
     // JSON 字段：目标父级
     public final static String GTASK_JSON_DEST_PARENT = "dest_parent";
 
     // JSON 字段：目标父级类型
     public final static String GTASK_JSON_DEST_PARENT_TYPE = "dest_parent_type";
 
     // JSON 字段：实体变动数据
     public final static String GTASK_JSON_ENTITY_DELTA = "entity_delta";
 
     // JSON 字段：实体类型（GROUP 或 TASK）
     public final static String GTASK_JSON_ENTITY_TYPE = "entity_type";
 
     // JSON 字段：是否获取已删除内容
     public final static String GTASK_JSON_GET_DELETED = "get_deleted";
 
     // JSON 字段：ID
     public final static String GTASK_JSON_ID = "id";
 
     // JSON 字段：索引（排序用）
     public final static String GTASK_JSON_INDEX = "index";
 
     // JSON 字段：最后修改时间
     public final static String GTASK_JSON_LAST_MODIFIED = "last_modified";
 
     // JSON 字段：最新同步点
     public final static String GTASK_JSON_LATEST_SYNC_POINT = "latest_sync_point";
 
     // JSON 字段：列表ID
     public final static String GTASK_JSON_LIST_ID = "list_id";
 
     // JSON 字段：列表集合
     public final static String GTASK_JSON_LISTS = "lists";
 
     // JSON 字段：名称
     public final static String GTASK_JSON_NAME = "name";
 
     // JSON 字段：新的ID（更新后）
     public final static String GTASK_JSON_NEW_ID = "new_id";
 
     // JSON 字段：备注
     public final static String GTASK_JSON_NOTES = "notes";
 
     // JSON 字段：父级ID
     public final static String GTASK_JSON_PARENT_ID = "parent_id";
 
     // JSON 字段：前一个兄弟节点ID（用于排序）
     public final static String GTASK_JSON_PRIOR_SIBLING_ID = "prior_sibling_id";
 
     // JSON 字段：结果（返回的实体集合）
     public final static String GTASK_JSON_RESULTS = "results";
 
     // JSON 字段：源列表（移动操作）
     public final static String GTASK_JSON_SOURCE_LIST = "source_list";
 
     // JSON 字段：任务集合
     public final static String GTASK_JSON_TASKS = "tasks";
 
     // JSON 字段：类型（实体类型）
     public final static String GTASK_JSON_TYPE = "type";
 
     // 实体类型：分组（文件夹）
     public final static String GTASK_JSON_TYPE_GROUP = "GROUP";
 
     // 实体类型：任务
     public final static String GTASK_JSON_TYPE_TASK = "TASK";
 
     // JSON 字段：用户
     public final static String GTASK_JSON_USER = "user";
 
     // MIUI 便签专用文件夹前缀
     public final static String MIUI_FOLDER_PREFFIX = "[MIUI_Notes]";
 
     // 默认文件夹名
     public final static String FOLDER_DEFAULT = "Default";
 
     // 通话记录文件夹名
     public final static String FOLDER_CALL_NOTE = "Call_Note";
 
     // 元数据文件夹（不显示，内部使用）
     public final static String FOLDER_META = "METADATA";
 
     // 元数据字段：GTask ID
     public final static String META_HEAD_GTASK_ID = "meta_gid";
 
     // 元数据字段：关联便签内容
     public final static String META_HEAD_NOTE = "meta_note";
 
     // 元数据字段：其他数据（可能是标签、颜色等）
     public final static String META_HEAD_DATA = "meta_data";
 
     // 元数据便签名称（警告信息）
     public final static String META_NOTE_NAME = "[META INFO] DON'T UPDATE AND DELETE";
 }
 
