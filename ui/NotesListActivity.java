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
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.appwidget.AppWidgetManager;
 import android.content.AsyncQueryHandler;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.TextUtils;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.ActionMode;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Display;
 import android.view.HapticFeedbackConstants;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnCreateContextMenuListener;
 import android.view.View.OnTouchListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.PopupMenu;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import net.micode.notes.R;
 import net.micode.notes.data.Notes;
 import net.micode.notes.data.Notes.NoteColumns;
 import net.micode.notes.gtask.remote.GTaskSyncService;
 import net.micode.notes.model.WorkingNote;
 import net.micode.notes.tool.BackupUtils;
 import net.micode.notes.tool.DataUtils;
 import net.micode.notes.tool.ResourceParser;
 import net.micode.notes.ui.NotesListAdapter.AppWidgetAttribute;
 import net.micode.notes.widget.NoteWidgetProvider_2x;
 import net.micode.notes.widget.NoteWidgetProvider_4x;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashSet;
 
 /**
  * 便签列表主界面Activity，负责显示和管理便签列表
  */
 public class NotesListActivity extends Activity implements OnClickListener, OnItemLongClickListener {
     // 异步查询Token常量
     private static final int FOLDER_NOTE_LIST_QUERY_TOKEN = 0;  // 文件夹便签列表查询
     private static final int FOLDER_LIST_QUERY_TOKEN      = 1;  // 文件夹列表查询
 
     // 上下文菜单项ID
     private static final int MENU_FOLDER_DELETE = 0;     // 删除文件夹
     private static final int MENU_FOLDER_VIEW = 1;       // 查看文件夹
     private static final int MENU_FOLDER_CHANGE_NAME = 2;// 修改文件夹名
 
     // 首选项键名
     private static final String PREFERENCE_ADD_INTRODUCTION = "net.micode.notes.introduction";
 
     // 列表编辑状态枚举
     private enum ListEditState {
         NOTE_LIST,       // 普通便签列表
         SUB_FOLDER,      // 子文件夹
         CALL_RECORD_FOLDER // 通话记录文件夹
     };
 
     // 成员变量
     private ListEditState mState;                      // 当前列表状态
     private BackgroundQueryHandler mBackgroundQueryHandler; // 后台查询处理器
     private NotesListAdapter mNotesListAdapter;        // 便签列表适配器
     private ListView mNotesListView;                   // 便签列表视图
     private Button mAddNewNote;                        // 添加新便签按钮
     private boolean mDispatch;                         // 是否分发触摸事件标志
     private int mOriginY;                              // 触摸起始Y坐标
     private int mDispatchY;                            // 分发触摸事件Y坐标
     private TextView mTitleBar;                        // 标题栏
     private long mCurrentFolderId;                     // 当前文件夹ID
     private ContentResolver mContentResolver;          // 内容解析器
     private ModeCallback mModeCallBack;                // 多选模式回调
     private static final String TAG = "NotesListActivity"; // 日志标签
     public static final int NOTES_LISTVIEW_SCROLL_RATE = 30; // 列表滚动速率
     private NoteItemData mFocusNoteDataItem;           // 当前焦点便签数据项
 
     // 查询条件
     private static final String NORMAL_SELECTION = NoteColumns.PARENT_ID + "=?";
     private static final String ROOT_FOLDER_SELECTION = "(" + NoteColumns.TYPE + "<>"
             + Notes.TYPE_SYSTEM + " AND " + NoteColumns.PARENT_ID + "=?)" + " OR ("
             + NoteColumns.ID + "=" + Notes.ID_CALL_RECORD_FOLDER + " AND "
             + NoteColumns.NOTES_COUNT + ">0)";
 
     // 请求码
     private final static int REQUEST_CODE_OPEN_NODE = 102; // 打开便签请求码
     private final static int REQUEST_CODE_NEW_NODE  = 103; // 新建便签请求码
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.note_list);
         initResources(); // 初始化资源
 
         // 首次使用时插入介绍便签
         setAppInfoFromRawRes();
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == RESULT_OK
                 && (requestCode == REQUEST_CODE_OPEN_NODE || requestCode == REQUEST_CODE_NEW_NODE)) {
             mNotesListAdapter.changeCursor(null); // 结果返回时刷新列表
         } else {
             super.onActivityResult(requestCode, resultCode, data);
         }
     }
 
     /**
      * 从raw资源读取并设置应用介绍信息
      */
     private void setAppInfoFromRawRes() {
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
         if (!sp.getBoolean(PREFERENCE_ADD_INTRODUCTION, false)) {
             StringBuilder sb = new StringBuilder();
             InputStream in = null;
             try {
                  in = getResources().openRawResource(R.raw.introduction);
                 if (in != null) {
                     InputStreamReader isr = new InputStreamReader(in);
                     BufferedReader br = new BufferedReader(isr);
                     char [] buf = new char[1024];
                     int len = 0;
                     while ((len = br.read(buf)) > 0) {
                         sb.append(buf, 0, len);
                     }
                 } else {
                     Log.e(TAG, "Read introduction file error");
                     return;
                 }
             } catch (IOException e) {
                 e.printStackTrace();
                 return;
             } finally {
                 if(in != null) {
                     try {
                         in.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
 
             // 创建介绍便签
             WorkingNote note = WorkingNote.createEmptyNote(this, Notes.ID_ROOT_FOLDER,
                     AppWidgetManager.INVALID_APPWIDGET_ID, Notes.TYPE_WIDGET_INVALIDE,
                     ResourceParser.RED);
             note.setWorkingText(sb.toString());
             if (note.saveNote()) {
                 sp.edit().putBoolean(PREFERENCE_ADD_INTRODUCTION, true).commit();
             } else {
                 Log.e(TAG, "Save introduction note error");
                 return;
             }
         }
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         startAsyncNotesListQuery(); // 启动异步查询便签列表
     }
 
     /**
      * 初始化资源
      */
     private void initResources() {
         mContentResolver = this.getContentResolver();
         mBackgroundQueryHandler = new BackgroundQueryHandler(this.getContentResolver());
         mCurrentFolderId = Notes.ID_ROOT_FOLDER; // 初始化为根文件夹
         mNotesListView = (ListView) findViewById(R.id.notes_list);
         mNotesListView.addFooterView(LayoutInflater.from(this).inflate(R.layout.note_list_footer, null),
                 null, false);
         mNotesListView.setOnItemClickListener(new OnListItemClickListener());
         mNotesListView.setOnItemLongClickListener(this);
         mNotesListAdapter = new NotesListAdapter(this);
         mNotesListView.setAdapter(mNotesListAdapter);
         mAddNewNote = (Button) findViewById(R.id.btn_new_note);
         mAddNewNote.setOnClickListener(this);
         mAddNewNote.setOnTouchListener(new NewNoteOnTouchListener());
         mDispatch = false;
         mDispatchY = 0;
         mOriginY = 0;
         mTitleBar = (TextView) findViewById(R.id.tv_title_bar);
         mState = ListEditState.NOTE_LIST;
         mModeCallBack = new ModeCallback(); // 多选模式回调
     }
 
     /**
      * 多选模式回调类
      */
     private class ModeCallback implements ListView.MultiChoiceModeListener, OnMenuItemClickListener {
         private DropdownMenu mDropDownMenu; // 下拉菜单
         private ActionMode mActionMode;     // 操作模式
         private MenuItem mMoveMenu;         // 移动菜单项
 
         public boolean onCreateActionMode(ActionMode mode, Menu menu) {
             getMenuInflater().inflate(R.menu.note_list_options, menu);
             menu.findItem(R.id.delete).setOnMenuItemClickListener(this);
             mMoveMenu = menu.findItem(R.id.move);
             if (mFocusNoteDataItem.getParentId() == Notes.ID_CALL_RECORD_FOLDER
                     || DataUtils.getUserFolderCount(mContentResolver) == 0) {
                 mMoveMenu.setVisible(false); // 通话记录或无用户文件夹时隐藏移动菜单
             } else {
                 mMoveMenu.setVisible(true);
                 mMoveMenu.setOnMenuItemClickListener(this);
             }
             mActionMode = mode;
             mNotesListAdapter.setChoiceMode(true); // 进入选择模式
             mNotesListView.setLongClickable(false);
             mAddNewNote.setVisibility(View.GONE); // 隐藏新建按钮
 
             // 设置自定义视图
             View customView = LayoutInflater.from(NotesListActivity.this).inflate(
                     R.layout.note_list_dropdown_menu, null);
             mode.setCustomView(customView);
             mDropDownMenu = new DropupMenu(NotesListActivity.this,
                     (Button) customView.findViewById(R.id.selection_menu),
                     R.menu.note_list_dropdown);
             mDropDownMenu.setOnDropdownMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
                 public boolean onMenuItemClick(MenuItem item) {
                     // 全选/取消全选
                     mNotesListAdapter.selectAll(!mNotesListAdapter.isAllSelected());
                     updateMenu();
                     return true;
                 }
             });
             return true;
         }
 
         /**
          * 更新菜单状态
          */
         private void updateMenu() {
             int selectedCount = mNotesListAdapter.getSelectedCount();
             // 更新下拉菜单标题
             String format = getResources().getString(R.string.menu_select_title, selectedCount);
             mDropDownMenu.setTitle(format);
             MenuItem item = mDropDownMenu.findItem(R.id.action_select_all);
             if (item != null) {
                 if (mNotesListAdapter.isAllSelected()) {
                     item.setChecked(true);
                     item.setTitle(R.string.menu_deselect_all);
                 } else {
                     item.setChecked(false);
                     item.setTitle(R.string.menu_select_all);
                 }
             }
         }
 
         public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
             return false;
         }
 
         public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
             return false;
         }
 
         public void onDestroyActionMode(ActionMode mode) {
             // 退出选择模式
             mNotesListAdapter.setChoiceMode(false);
             mNotesListView.setLongClickable(true);
             mAddNewNote.setVisibility(View.VISIBLE);
         }
 
         public void finishActionMode() {
             mActionMode.finish();
         }
 
         public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                 boolean checked) {
             mNotesListAdapter.setCheckedItem(position, checked);
             updateMenu();
         }
 
         public boolean onMenuItemClick(MenuItem item) {
             if (mNotesListAdapter.getSelectedCount() == 0) {
                 Toast.makeText(NotesListActivity.this, getString(R.string.menu_select_none),
                         Toast.LENGTH_SHORT).show();
                 return true;
             }
 
             switch (item.getItemId()) {
                 case R.id.delete:
                     // 删除确认对话框
                     AlertDialog.Builder builder = new AlertDialog.Builder(NotesListActivity.this);
                     builder.setTitle(getString(R.string.alert_title_delete));
                     builder.setIcon(android.R.drawable.ic_dialog_alert);
                     builder.setMessage(getString(R.string.alert_message_delete_notes,
                                              mNotesListAdapter.getSelectedCount()));
                     builder.setPositiveButton(android.R.string.ok,
                                              new DialogInterface.OnClickListener() {
                                                  public void onClick(DialogInterface dialog,
                                                          int which) {
                                                      batchDelete(); // 批量删除
                                                  }
                                              });
                     builder.setNegativeButton(android.R.string.cancel, null);
                     builder.show();
                     break;
                 case R.id.move:
                     startQueryDestinationFolders(); // 查询目标文件夹
                     break;
                 default:
                     return false;
             }
             return true;
         }
     }
 
     /**
      * 新建便签按钮触摸监听器
      */
     private class NewNoteOnTouchListener implements OnTouchListener {
         public boolean onTouch(View v, MotionEvent event) {
             switch (event.getAction()) {
                 case MotionEvent.ACTION_DOWN: {
                     Display display = getWindowManager().getDefaultDisplay();
                     int screenHeight = display.getHeight();
                     int newNoteViewHeight = mAddNewNote.getHeight();
                     int start = screenHeight - newNoteViewHeight;
                     int eventY = start + (int) event.getY();
                     
                     // 减去标题栏高度(子文件夹模式下)
                     if (mState == ListEditState.SUB_FOLDER) {
                         eventY -= mTitleBar.getHeight();
                         start -= mTitleBar.getHeight();
                     }
                     
                     /**
                      * HACK: 当点击"新建便签"按钮的透明部分时，将事件分发给列表视图
                      * 透明部分由公式 y=-0.12x+94 定义(像素单位)
                      */
                     if (event.getY() < (event.getX() * (-0.12) + 94)) {
                         View view = mNotesListView.getChildAt(mNotesListView.getChildCount() - 1
                                 - mNotesListView.getFooterViewsCount());
                         if (view != null && view.getBottom() > start
                                 && (view.getTop() < (start + 94))) {
                             mOriginY = (int) event.getY();
                             mDispatchY = eventY;
                             event.setLocation(event.getX(), mDispatchY);
                             mDispatch = true;
                             return mNotesListView.dispatchTouchEvent(event);
                         }
                     }
                     break;
                 }
                 case MotionEvent.ACTION_MOVE: {
                     if (mDispatch) {
                         mDispatchY += (int) event.getY() - mOriginY;
                         event.setLocation(event.getX(), mDispatchY);
                         return mNotesListView.dispatchTouchEvent(event);
                     }
                     break;
                 }
                 default: {
                     if (mDispatch) {
                         event.setLocation(event.getX(), mDispatchY);
                         mDispatch = false;
                         return mNotesListView.dispatchTouchEvent(event);
                     }
                     break;
                 }
             }
             return false;
         }
     };
 
     /**
      * 启动异步查询便签列表
      */
     private void startAsyncNotesListQuery() {
         String selection = (mCurrentFolderId == Notes.ID_ROOT_FOLDER) ? ROOT_FOLDER_SELECTION
                 : NORMAL_SELECTION;
         mBackgroundQueryHandler.startQuery(FOLDER_NOTE_LIST_QUERY_TOKEN, null,
                 Notes.CONTENT_NOTE_URI, NoteItemData.PROJECTION, selection, new String[] {
                     String.valueOf(mCurrentFolderId)
                 }, NoteColumns.TYPE + " DESC," + NoteColumns.MODIFIED_DATE + " DESC");
     }
 
     /**
      * 后台查询处理器
      */
     private final class BackgroundQueryHandler extends AsyncQueryHandler {
         public BackgroundQueryHandler(ContentResolver contentResolver) {
             super(contentResolver);
         }
 
         @Override
         protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
             switch (token) {
                 case FOLDER_NOTE_LIST_QUERY_TOKEN:
                     mNotesListAdapter.changeCursor(cursor); // 更新便签列表适配器
                     break;
                 case FOLDER_LIST_QUERY_TOKEN:
                     if (cursor != null && cursor.getCount() > 0) {
                         showFolderListMenu(cursor); // 显示文件夹选择菜单
                     } else {
                         Log.e(TAG, "Query folder failed");
                     }
                     break;
                 default:
                     return;
             }
         }
     }
 
     /**
      * 显示文件夹列表菜单
      * @param cursor 文件夹数据游标
      */
     private void showFolderListMenu(Cursor cursor) {
         AlertDialog.Builder builder = new AlertDialog.Builder(NotesListActivity.this);
         builder.setTitle(R.string.menu_title_select_folder);
         final FoldersListAdapter adapter = new FoldersListAdapter(this, cursor);
         builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 // 批量移动到选定文件夹
                 DataUtils.batchMoveToFolder(mContentResolver,
                         mNotesListAdapter.getSelectedItemIds(), adapter.getItemId(which));
                 Toast.makeText(
                         NotesListActivity.this,
                         getString(R.string.format_move_notes_to_folder,
                                 mNotesListAdapter.getSelectedCount(),
                                 adapter.getFolderName(NotesListActivity.this, which)),
                         Toast.LENGTH_SHORT).show();
                 mModeCallBack.finishActionMode(); // 完成操作模式
             }
         });
         builder.show();
     }
 
     /**
      * 创建新便签
      */
     private void createNewNote() {
         Intent intent = new Intent(this, NoteEditActivity.class);
         intent.setAction(Intent.ACTION_INSERT_OR_EDIT);
         intent.putExtra(Notes.INTENT_EXTRA_FOLDER_ID, mCurrentFolderId);
         this.startActivityForResult(intent, REQUEST_CODE_NEW_NODE);
     }
 
     /**
      * 批量删除便签
      */
     private void batchDelete() {
         new AsyncTask<Void, Void, HashSet<AppWidgetAttribute>>() {
             protected HashSet<AppWidgetAttribute> doInBackground(Void... unused) {
                 HashSet<AppWidgetAttribute> widgets = mNotesListAdapter.getSelectedWidget();
                 if (!isSyncMode()) {
                     // 非同步模式直接删除便签
                     if (DataUtils.batchDeleteNotes(mContentResolver, mNotesListAdapter
                             .getSelectedItemIds())) {
                     } else {
                         Log.e(TAG, "Delete notes error, should not happens");
                     }
                 } else {
                     // 同步模式下将便签移动到回收站
                     if (!DataUtils.batchMoveToFolder(mContentResolver, mNotesListAdapter
                             .getSelectedItemIds(), Notes.ID_TRASH_FOLER)) {
                         Log.e(TAG, "Move notes to trash folder error, should not happens");
                     }
                 }
                 return widgets;
             }
 
             @Override
             protected void onPostExecute(HashSet<AppWidgetAttribute> widgets) {
                 if (widgets != null) {
                     for (AppWidgetAttribute widget : widgets) {
                         if (widget.widgetId != AppWidgetManager.INVALID_APPWIDGET_ID
                                 && widget.widgetType != Notes.TYPE_WIDGET_INVALIDE) {
                             updateWidget(widget.widgetId, widget.widgetType); // 更新小部件
                         }
                     }
                 }
                 mModeCallBack.finishActionMode();
             }
         }.execute();
     }
 
     /**
      * 删除文件夹
      * @param folderId 文件夹ID
      */
     private void deleteFolder(long folderId) {
         if (folderId == Notes.ID_ROOT_FOLDER) {
             Log.e(TAG, "Wrong folder id, should not happen " + folderId);
             return;
         }
 
         HashSet<Long> ids = new HashSet<Long>();
         ids.add(folderId);
         HashSet<AppWidgetAttribute> widgets = DataUtils.getFolderNoteWidget(mContentResolver,
                 folderId);
         if (!isSyncMode()) {
             // 非同步模式直接删除文件夹
             DataUtils.batchDeleteNotes(mContentResolver, ids);
         } else {
             // 同步模式下将文件夹移动到回收站
             DataUtils.batchMoveToFolder(mContentResolver, ids, Notes.ID_TRASH_FOLER);
         }
         if (widgets != null) {
             for (AppWidgetAttribute widget : widgets) {
                 if (widget.widgetId != AppWidgetManager.INVALID_APPWIDGET_ID
                         && widget.widgetType != Notes.TYPE_WIDGET_INVALIDE) {
                     updateWidget(widget.widgetId, widget.widgetType); // 更新小部件
                 }
             }
         }
     }
 
     /**
      * 打开便签
      * @param data 便签数据项
      */
     private void openNode(NoteItemData data) {
         Intent intent = new Intent(this, NoteEditActivity.class);
         intent.setAction(Intent.ACTION_VIEW);
         intent.putExtra(Intent.EXTRA_UID, data.getId());
         this.startActivityForResult(intent, REQUEST_CODE_OPEN_NODE);
     }
 
     /**
      * 打开文件夹
      * @param data 文件夹数据项
      */
     private void openFolder(NoteItemData data) {
         mCurrentFolderId = data.getId();
         startAsyncNotesListQuery(); // 查询文件夹内容
         if (data.getId() == Notes.ID_CALL_RECORD_FOLDER) {
             mState = ListEditState.CALL_RECORD_FOLDER;
             mAddNewNote.setVisibility(View.GONE); // 通话记录文件夹不显示新建按钮
         } else {
             mState = ListEditState.SUB_FOLDER;
         }
         // 设置标题
         if (data.getId() == Notes.ID_CALL_RECORD_FOLDER) {
             mTitleBar.setText(R.string.call_record_folder_name);
         } else {
             mTitleBar.setText(data.getSnippet());
         }
         mTitleBar.setVisibility(View.VISIBLE);
     }
 
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.btn_new_note:
                 createNewNote(); // 新建便签
                 break;
             default:
                 break;
         }
     }
 
     /**
      * 显示软键盘
      */
     private void showSoftInput() {
         InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         if (inputMethodManager != null) {
             inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
         }
     }
 
     /**
      * 隐藏软键盘
      * @param view 当前焦点视图
      */
     private void hideSoftInput(View view) {
         InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
     }
 
     /**
      * 显示创建或修改文件夹对话框
      * @param create true表示创建，false表示修改
      */
     private void showCreateOrModifyFolderDialog(final boolean create) {
         final AlertDialog.Builder builder = new AlertDialog.Builder(this);
         View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
         final EditText etName = (EditText) view.findViewById(R.id.et_foler_name);
         showSoftInput();
         if (!create) {
             // 修改文件夹名
             if (mFocusNoteDataItem != null) {
                 etName.setText(mFocusNoteDataItem.getSnippet());
                 builder.setTitle(getString(R.string.menu_folder_change_name));
             } else {
                 Log.e(TAG, "The long click data item is null");
                 return;
             }
         } else {
             // 创建新文件夹
             etName.setText("");
             builder.setTitle(this.getString(R.string.menu_create_folder));
         }
 
         builder.setPositiveButton(android.R.string.ok, null);
         builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 hideSoftInput(etName);
             }
         });
 
         final Dialog dialog = builder.setView(view).show();
         final Button positive = (Button)dialog.findViewById(android.R.id.button1);
         positive.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 hideSoftInput(etName);
                 String name = etName.getText().toString();
                 if (DataUtils.checkVisibleFolderName(mContentResolver, name)) {
                     Toast.makeText(NotesListActivity.this, getString(R.string.folder_exist, name),
                             Toast.LENGTH_LONG).show();
                     etName.setSelection(0, etName.length());
                     return;
                 }
                 if (!create) {
                     // 更新文件夹名
                     if (!TextUtils.isEmpty(name)) {
                         ContentValues values = new ContentValues();
                         values.put(NoteColumns.SNIPPET, name);
                         values.put(NoteColumns.TYPE, Notes.TYPE_FOLDER);
                         values.put(NoteColumns.LOCAL_MODIFIED, 1);
                         mContentResolver.update(Notes.CONTENT_NOTE_URI, values, NoteColumns.ID
                                 + "=?", new String[] {
                             String.valueOf(mFocusNoteDataItem.getId())
                         });
                     }
                 } else if (!TextUtils.isEmpty(name)) {
                     // 创建新文件夹
                     ContentValues values = new ContentValues();
                     values.put(NoteColumns.SNIPPET, name);
                     values.put(NoteColumns.TYPE, Notes.TYPE_FOLDER);
                     mContentResolver.insert(Notes.CONTENT_NOTE_URI, values);
                 }
                 dialog.dismiss();
             }
         });
 
         if (TextUtils.isEmpty(etName.getText())) {
             positive.setEnabled(false);
         }
         // 文本变化监听
         etName.addTextChangedListener(new TextWatcher() {
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
             }
 
             public void onTextChanged(CharSequence s, int start, int before, int count) {
                 if (TextUtils.isEmpty(etName.getText())) {
                     positive.setEnabled(false);
                 } else {
                     positive.setEnabled(true);
                 }
             }
 
             public void afterTextChanged(Editable s) {
             }
         });
     }
 
     @Override
     public void onBackPressed() {
         switch (mState) {
             case SUB_FOLDER:
                 // 返回根文件夹
                 mCurrentFolderId = Notes.ID_ROOT_FOLDER;
                 mState = ListEditState.NOTE_LIST;
                 startAsyncNotesListQuery();
                 mTitleBar.setVisibility(View.GONE);
                 break;
             case CALL_RECORD_FOLDER:
                 // 返回根文件夹
                 mCurrentFolderId = Notes.ID_ROOT_FOLDER;
                 mState = ListEditState.NOTE_LIST;
                 mAddNewNote.setVisibility(View.VISIBLE);
                 mTitleBar.setVisibility(View.GONE);
                 startAsyncNotesListQuery();
                 break;
             case NOTE_LIST:
                 super.onBackPressed(); // 退出应用
                 break;
             default:
                 break;
         }
     }
 
     /**
      * 更新小部件
      * @param appWidgetId 小部件ID
      * @param appWidgetType 小部件类型
      */
     private void updateWidget(int appWidgetId, int appWidgetType) {
         Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
         if (appWidgetType == Notes.TYPE_WIDGET_2X) {
             intent.setClass(this, NoteWidgetProvider_2x.class);
         } else if (appWidgetType == Notes.TYPE_WIDGET_4X) {
             intent.setClass(this, NoteWidgetProvider_4x.class);
         } else {
             Log.e(TAG, "Unspported widget type");
             return;
         }
 
         intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {
             appWidgetId
         });
 
         sendBroadcast(intent);
         setResult(RESULT_OK, intent);
     }
 
     // 文件夹上下文菜单监听器
     private final OnCreateContextMenuListener mFolderOnCreateContextMenuListener = new OnCreateContextMenuListener() {
         public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
             if (mFocusNoteDataItem != null) {
                 menu.setHeaderTitle(mFocusNoteDataItem.getSnippet());
                 menu.add(0, MENU_FOLDER_VIEW, 0, R.string.menu_folder_view);
                 menu.add(0, MENU_FOLDER_DELETE, 0, R.string.menu_folder_delete);
                 menu.add(0, MENU_FOLDER_CHANGE_NAME, 0, R.string.menu_folder_change_name);
             }
         }
     };
 
     @Override
     public void onContextMenuClosed(Menu menu) {
         if (mNotesListView != null) {
             mNotesListView.setOnCreateContextMenuListener(null);
         }
         super.onContextMenuClosed(menu);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         if (mFocusNoteDataItem == null) {
             Log.e(TAG, "The long click data item is null");
             return false;
         }
         switch (item.getItemId()) {
             case MENU_FOLDER_VIEW:
                 openFolder(mFocusNoteDataItem); // 查看文件夹
                 break;
             case MENU_FOLDER_DELETE:
                 // 删除文件夹确认对话框
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setTitle(getString(R.string.alert_title_delete));
                 builder.setIcon(android.R.drawable.ic_dialog_alert);
                 builder.setMessage(getString(R.string.alert_message_delete_folder));
                 builder.setPositiveButton(android.R.string.ok,
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int which) {
                                 deleteFolder(mFocusNoteDataItem.getId()); // 删除文件夹
                             }
                         });
                 builder.setNegativeButton(android.R.string.cancel, null);
                 builder.show();
                 break;
             case MENU_FOLDER_CHANGE_NAME:
                 showCreateOrModifyFolderDialog(false); // 修改文件夹名
                 break;
             default:
                 break;
         }
 
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         menu.clear();
         // 根据当前状态加载不同菜单
         if (mState == ListEditState.NOTE_LIST) {
             getMenuInflater().inflate(R.menu.note_list, menu);
             // 设置同步/取消同步菜单项
             menu.findItem(R.id.menu_sync).setTitle(
                     GTaskSyncService.isSyncing() ? R.string.menu_sync_cancel : R.string.menu_sync);
         } else if (mState == ListEditState.SUB_FOLDER) {
             getMenuInflater().inflate(R.menu.sub_folder, menu);
         } else if (mState == ListEditState.CALL_RECORD_FOLDER) {
             getMenuInflater().inflate(R.menu.call_record_folder, menu);
         } else {
             Log.e(TAG, "Wrong state:" + mState);
         }
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_new_folder: {
                 showCreateOrModifyFolderDialog(true); // 新建文件夹
                 break;
             }
             case R.id.menu_export_text: {
                 exportNoteToText(); // 导出为文本
                 break;
             }
             case R.id.menu_sync: {
                 if (isSyncMode()) {
                     // 同步模式下切换同步/取消同步
                     if (TextUtils.equals(item.getTitle(), getString(R.string.menu_sync))) {
                         GTaskSyncService.startSync(this);
                     } else {
                         GTaskSyncService.cancelSync(this);
                     }
                 } else {
                     startPreferenceActivity(); // 非同步模式进入设置
                 }
                 break;
             }
             case R.id.menu_setting: {
                 startPreferenceActivity(); // 进入设置
                 break;
             }
             case R.id.menu_new_note: {
                 createNewNote(); // 新建便签
                 break;
             }
             case R.id.menu_search:
                 onSearchRequested(); // 搜索
                 break;
             default:
                 break;
         }
         return true;
     }
 
     @Override
     public boolean onSearchRequested() {
         startSearch(null, false, null /* appData */, false);
         return true;
     }
 
     /**
      * 导出便签到文本文件
      */
     private void exportNoteToText() {
         final BackupUtils backup = BackupUtils.getInstance(NotesListActivity.this);
         new AsyncTask<Void, Void, Integer>() {
             @Override
             protected Integer doInBackground(Void... unused) {
                 return backup.exportToText(); // 执行导出
             }
 
             @Override
             protected void onPostExecute(Integer result) {
                 // 处理导出结果
                 if (result == BackupUtils.STATE_SD_CARD_UNMOUONTED) {
                     AlertDialog.Builder builder = new AlertDialog.Builder(NotesListActivity.this);
                     builder.setTitle(NotesListActivity.this
                             .getString(R.string.failed_sdcard_export));
                     builder.setMessage(NotesListActivity.this
                             .getString(R.string.error_sdcard_unmounted));
                     builder.setPositiveButton(android.R.string.ok, null);
                     builder.show();
                 } else if (result == BackupUtils.STATE_SUCCESS) {
                     AlertDialog.Builder builder = new AlertDialog.Builder(NotesListActivity.this);
                     builder.setTitle(NotesListActivity.this
                             .getString(R.string.success_sdcard_export));
                     builder.setMessage(NotesListActivity.this.getString(
                             R.string.format_exported_file_location, backup
                                     .getExportedTextFileName(), backup.getExportedTextFileDir()));
                     builder.setPositiveButton(android.R.string.ok, null);
                     builder.show();
                 } else if (result == BackupUtils.STATE_SYSTEM_ERROR) {
                     AlertDialog.Builder builder = new AlertDialog.Builder(NotesListActivity.this);
                     builder.setTitle(NotesListActivity.this
                             .getString(R.string.failed_sdcard_export));
                     builder.setMessage(NotesListActivity.this
                             .getString(R.string.error_sdcard_export));
                     builder.setPositiveButton(android.R.string.ok, null);
                     builder.show();
                 }
             }
         }.execute();
     }
 
     /**
      * 检查是否处于同步模式
      * @return true表示同步模式
      */
     private boolean isSyncMode() {
         return NotesPreferenceActivity.getSyncAccountName(this).trim().length() > 0;
     }
 
     /**
      * 启动设置Activity
      */
     private void startPreferenceActivity() {
         Activity from = getParent() != null ? getParent() : this;
         Intent intent = new Intent(from, NotesPreferenceActivity.class);
         from.startActivityIfNeeded(intent, -1);
     }
 
     /**
      * 列表项点击监听器
      */
     private class OnListItemClickListener implements OnItemClickListener {
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             if (view instanceof NotesListItem) {
                 NoteItemData item = ((NotesListItem) view).getItemData();
                 if (mNotesListAdapter.isInChoiceMode()) {
                     // 选择模式下处理选择状态
                     if (item.getType() == Notes.TYPE_NOTE) {
                         position = position - mNotesListView.getHeaderViewsCount();
                         mModeCallBack.onItemCheckedStateChanged(null, position, id,
                                 !mNotesListAdapter.isSelectedItem(position));
                     }
                     return;
                 }
 
                 // 根据当前状态处理点击
                 switch (mState) {
                     case NOTE_LIST:
                         if (item.getType() == Notes.TYPE_FOLDER
                                 || item.getType() == Notes.TYPE_SYSTEM) {
                             openFolder(item); // 打开文件夹
                         } else if (item.getType() == Notes.TYPE_NOTE) {
                             openNode(item); // 打开便签
                         } else {
                             Log.e(TAG, "Wrong note type in NOTE_LIST");
                         }
                         break;
                     case SUB_FOLDER:
                     case CALL_RECORD_FOLDER:
                         if (item.getType() == Notes.TYPE_NOTE) {
                             openNode(item); // 打开便签
                         } else {
                             Log.e(TAG, "Wrong note type in SUB_FOLDER");
                         }
                         break;
                     default:
                         break;
                 }
             }
         }
     }
 
     /**
      * 启动查询目标文件夹
      */
     private void startQueryDestinationFolders() {
         String selection = NoteColumns.TYPE + "=? AND " + NoteColumns.PARENT_ID + "<>? AND " + NoteColumns.ID + "<>?";
         selection = (mState == ListEditState.NOTE_LIST) ? selection:
             "(" + selection + ") OR (" + NoteColumns.ID + "=" + Notes.ID_ROOT_FOLDER + ")";
 
         mBackgroundQueryHandler.startQuery(FOLDER_LIST_QUERY_TOKEN,
                 null,
                 Notes.CONTENT_NOTE_URI,
                 FoldersListAdapter.PROJECTION,
                 selection,
                 new String[] {
                         String.valueOf(Notes.TYPE_FOLDER),
                         String.valueOf(Notes.ID_TRASH_FOLER),
                         String.valueOf(mCurrentFolderId)
                 },
                 NoteColumns.MODIFIED_DATE + " DESC");
     }
 
     public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
         if (view instanceof NotesListItem) {
             mFocusNoteDataItem = ((NotesListItem) view).getItemData();
             if (mFocusNoteDataItem.getType() == Notes.TYPE_NOTE && !mNotesListAdapter.isInChoiceMode()) {
                 // 长按便签进入多选模式
                 if (mNotesListView.startActionMode(mModeCallBack) != null) {
                     mModeCallBack.onItemCheckedStateChanged(null, position, id, true);
                     mNotesListView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                 } else {
                     Log.e(TAG, "startActionMode fails");
                 }
             } else if (mFocusNoteDataItem.getType() == Notes.TYPE_FOLDER) {
                 // 长按文件夹显示上下文菜单
                 mNotesListView.setOnCreateContextMenuListener(mFolderOnCreateContextMenuListener);
             }
         }
         return false;
     }
 }

