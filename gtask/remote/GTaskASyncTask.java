
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

package net.micode.notes.gtask.remote;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import net.micode.notes.R;
import net.micode.notes.ui.NotesListActivity;
import net.micode.notes.ui.NotesPreferenceActivity;

/**
 * Google任务同步后台任务
 * 功能：在后台执行同步操作，通过通知栏显示进度，支持取消同步
 * 相当于手机的"后台小助手"，默默同步便签数据到Google账号
 */
public class GTaskASyncTask extends AsyncTask<Void, String, Integer> {

    // 同步通知的ID（随便取的大数字避免与其他通知冲突）
    private static int GTASK_SYNC_NOTIFICATION_ID = 5234235;

    /** 同步完成的回调接口 */
    public interface OnCompleteListener {
        void onComplete(); // 同步结束后要执行的操作
    }

    private Context mContext; // 上下文对象（获取系统服务用）
    private NotificationManager mNotifiManager; // 通知管理器（显示进度条用）
    private GTaskManager mTaskManager; // 真正的同步管理者（干活的）
    private OnCompleteListener mOnCompleteListener; // 同步结束后的回调

    /**
     * 构造方法：准备同步所需工具
     * 
     * @param context  上下文（一般是Activity或Service）
     * @param listener 同步结束后的回调（比如刷新界面）
     */
    public GTaskASyncTask(Context context, OnCompleteListener listener) {
        mContext = context;
        mOnCompleteListener = listener;
        mNotifiManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mTaskManager = GTaskManager.getInstance(); // 获取同步管理器单例
    }

    /** 取消同步（比如用户点了取消按钮时调用） */
    public void cancelSync() {
        mTaskManager.cancelSync(); // 告诉同步管理器："别干了！"
    }

    /** 更新进度（内部用，把进度消息发送给通知栏） */
    public void publishProgess(String message) {
        publishProgress(new String[] { message }); // 调用父类方法更新进度
    }

    /**
     * 显示通知栏提示
     * 
     * @param tickerId 状态文字资源ID（如"正在同步..."）
     * @param content  通知详细内容（如"正在登录xxx账号"）
     */
    private void showNotification(int tickerId, String content) {
        // 创建基础通知（图标+文字+时间）
        Notification notification = new Notification(R.drawable.notification,
                mContext.getString(tickerId),
                System.currentTimeMillis());

        notification.defaults = Notification.DEFAULT_LIGHTS; // 呼吸灯提醒
        notification.flags = Notification.FLAG_AUTO_CANCEL; // 点击后自动消失

        // 设置点击通知后的动作：失败时跳转到设置页，成功跳转到列表页
        PendingIntent pendingIntent;
        if (tickerId != R.string.ticker_success) {
            pendingIntent = PendingIntent.getActivity(mContext, 0,
                    new Intent(mContext, NotesPreferenceActivity.class), 0);
        } else {
            pendingIntent = PendingIntent.getActivity(mContext, 0,
                    new Intent(mContext, NotesListActivity.class), 0);
        }

        // 设置通知详情（标题+内容+点击动作）
        notification.setLatestEventInfo(mContext,
                mContext.getString(R.string.app_name), // 标题："便签"
                content, // 详细内容
                pendingIntent);

        mNotifiManager.notify(GTASK_SYNC_NOTIFICATION_ID, notification); // 显示通知
    }

    /**
     * 后台执行同步（自动在新线程运行）
     * 
     * @return 同步结果状态码（成功/网络错误等）
     */
    @Override
    protected Integer doInBackground(Void... unused) {
        // 显示登录提示："正在登录xxx@gmail.com"
        publishProgess(mContext.getString(R.string.sync_progress_login,
                NotesPreferenceActivity.getSyncAccountName(mContext)));

        // 真正开始同步，并把进度反馈给这个类
        return mTaskManager.sync(mContext, this);
    }

    /**
     * 更新进度时调用（自动在主线程运行）
     * 
     * @param progress 进度信息数组（这里只传第一个元素）
     */
    @Override
    protected void onProgressUpdate(String... progress) {
        // 显示带进度的通知（比如："正在同步：5/10条已完成"）
        showNotification(R.string.ticker_syncing, progress[0]);

        // 如果是通过服务启动的，发送广播通知其他组件
        if (mContext instanceof GTaskSyncService) {
            ((GTaskSyncService) mContext).sendBroadcast(progress[0]);
        }
    }

    /**
     * 同步结束后处理（自动在主线程运行）
     * 
     * @param result 同步结果状态码
     */
    @Override
    protected void onPostExecute(Integer result) {
        // 根据不同状态显示不同通知
        if (result == GTaskManager.STATE_SUCCESS) {
            showNotification(R.string.ticker_success,
                    mContext.getString(R.string.success_sync_account, mTaskManager.getSyncAccount()));
            NotesPreferenceActivity.setLastSyncTime(mContext, System.currentTimeMillis()); // 记录同步时间
        } else if (result == GTaskManager.STATE_NETWORK_ERROR) {
            showNotification(R.string.ticker_fail, mContext.getString(R.string.error_sync_network));
        } else if (result == GTaskManager.STATE_INTERNAL_ERROR) {
            showNotification(R.string.ticker_fail, mContext.getString(R.string.error_sync_internal));
        } else if (result == GTaskManager.STATE_SYNC_CANCELLED) {
            showNotification(R.string.ticker_cancel, mContext.getString(R.string.error_sync_cancelled));
        }

        // 同步完成后执行回调（比如刷新界面）
        if (mOnCompleteListener != null) {
            new Thread(new Runnable() { // 新建线程防止阻塞主线程
                public void run() {
                    mOnCompleteListener.onComplete();
                }
            }).start();
        }
    }
}
