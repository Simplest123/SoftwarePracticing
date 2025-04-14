/*
 * 闹钟触发广播接收器（AlarmReceiver）
 *
 * 功能：
 * 当通过 AlarmManager 设置的提醒时间到达时，系统会发送一个广播，
 * AlarmReceiver 监听该广播，并启动 AlarmAlertActivity 来展示提醒界面（弹窗+响铃）。
 */
public class AlarmReceiver extends BroadcastReceiver {

    /**
     * 当系统广播触发时自动调用的回调方法。
     *
     * @param context 应用上下文对象，用于访问应用资源、启动组件等。
     * @param intent  携带便签数据的 Intent，包含数据 URI（如 content://net.micode.notes/note/1）
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 将原始的 Intent 设置为启动 AlarmAlertActivity 的显式 Intent
        // AlarmAlertActivity 是用于显示提醒界面和播放铃声的 Activity
        intent.setClass(context, AlarmAlertActivity.class);

        /*
         * 添加 FLAG_ACTIVITY_NEW_TASK 标志位：
         * - 原因：BroadcastReceiver 没有自己的 UI 或任务栈。
         * - 因此启动 Activity 时必须指定新任务栈，否则会抛出异常。
         */
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // 启动 AlarmAlertActivity 来显示提醒对话框并播放闹铃
        context.startActivity(intent);
    }
}
