/*
 * 版权声明，代码归属于MiCode开源社区（www.micode.net）
 * 遵循Apache License 2.0开源协议
 */
package net.micode.notes.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;

/**
 * 文件夹列表适配器，继承自CursorAdapter
 * 用于将数据库中的文件夹数据展示在ListView中
 */
public class FoldersListAdapter extends CursorAdapter {
    // 查询投影，定义需要从数据库中获取的列
    public static final String [] PROJECTION = {
        NoteColumns.ID,         // 文件夹ID
        NoteColumns.SNIPPET     // 文件夹名称（使用SNIPPET列存储）
    };

    // 列索引常量
    public static final int ID_COLUMN   = 0;  // ID列索引
    public static final int NAME_COLUMN = 1;  // 名称列索引

    /**
     * 构造函数
     * @param context 上下文
     * @param c 数据库游标，包含文件夹数据
     */
    public FoldersListAdapter(Context context, Cursor c) {
        super(context, c);
    }

    /**
     * 创建新的列表项视图
     * @param context 上下文
     * @param cursor 数据库游标
     * @param parent 父视图组
     * @return 新建的FolderListItem视图
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new FolderListItem(context);
    }

    /**
     * 绑定数据到视图
     * @param view 待绑定数据的视图
     * @param context 上下文
     * @param cursor 数据库游标
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (view instanceof FolderListItem) {
            // 如果是根文件夹，显示特定的父文件夹名称，否则显示数据库中的名称
            String folderName = (cursor.getLong(ID_COLUMN) == Notes.ID_ROOT_FOLDER) 
                    ? context.getString(R.string.menu_move_parent_folder) 
                    : cursor.getString(NAME_COLUMN);
            ((FolderListItem) view).bind(folderName);
        }
    }

    /**
     * 获取指定位置的文件夹名称
     * @param context 上下文
     * @param position 位置索引
     * @return 文件夹名称
     */
    public String getFolderName(Context context, int position) {
        Cursor cursor = (Cursor) getItem(position);
        return (cursor.getLong(ID_COLUMN) == Notes.ID_ROOT_FOLDER) 
                ? context.getString(R.string.menu_move_parent_folder) 
                : cursor.getString(NAME_COLUMN);
    }

    /**
     * 自定义文件夹列表项视图，继承自LinearLayout
     */
    private class FolderListItem extends LinearLayout {
        private TextView mName;  // 显示文件夹名称的TextView

        /**
         * 构造函数
         * @param context 上下文
         */
        public FolderListItem(Context context) {
            super(context);
            // 从布局文件inflate视图
            inflate(context, R.layout.folder_list_item, this);
            // 获取名称TextView的引用
            mName = (TextView) findViewById(R.id.tv_folder_name);
        }

        /**
         * 绑定文件夹名称到视图
         * @param name 文件夹名称
         */
        public void bind(String name) {
            mName.setText(name);  // 设置文件夹名称
        }
    }
}