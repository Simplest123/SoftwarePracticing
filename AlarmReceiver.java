/*
 * 闹钟触发广播接收器 - 处理实际提醒时间的到达事件
 * 
 * 功能职责：
 * 1. 接收系统AlarmManager触发的定时提醒广播
 * 2. 转换Intent目标为提醒展示界面Activity
 * 3. 确保从后台正确启动提醒界面
 * 
 * 触发场景：
 * - 由AlarmManager在预设提醒时间到达时触发
 * - 由AlarmInitReceiver在系统重启后重新注册触发
 */
public class AlarmReceiver extends BroadcastReceiver {
    
    /**
     * 广播接收回调方法
     * 
     * 执行流程：
     * 1. 接收包含便签数据的原始Intent
     * 2. 修改Intent目标为提醒展示界面
     * 3. 添加必要的Activity启动标志
     * 4. 启动目标Activity
     * 
     * @param context 应用上下文，用于启动Activity和获取资源
     * @param intent  携带便签数据的Intent，必须包含：
     *                - Data URI: content://net.micode.notes/note/[id] 格式
     *                - 由AlarmManager传递的原始PendingIntent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 修改Intent的目标组件为AlarmAlertActivity
        // 原始Intent来自AlarmManager，需要重定向到实际展示界面
        intent.setClass(context, AlarmAlertActivity.class);
        
        // 添加NEW_TASK启动标志（必须条件）
        // 原因：
        // 1. 广播接收器没有Activity任务栈
        // 2. 必须创建新任务栈来承载Activity
        // 3. 兼容不同Android版本的背景限制
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        // 启动提醒界面Activity
        // 注意事项：
        // - 系统会自动处理锁屏状态下的显示（见AlarmAlertActivity的窗口标志设置）
        // - 实际展示由AlarmAlertActivity处理便签数据加载和界面呈现
        context.startActivity(intent);
    }
}
