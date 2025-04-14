/*
 * 闹钟触发广播接收器 - 处理实际提醒时间的到达事件
 * 
 * 功能：当预设的提醒时间到达时，启动AlarmAlertActivity展示提醒界面
 */
public class AlarmReceiver extends BroadcastReceiver {
    
    /**
     * 广播接收回调方法
     * @param context 应用上下文
     * @param intent  携带便签数据的Intent（包含Content://net.micode.notes/note/[id]格式的URI）
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 将目标Activity改为AlarmAlertActivity
        intent.setClass(context, AlarmAlertActivity.class);
        
        // 添加NEW_TASK标志（因为从广播接收器启动Activity需要新任务栈）
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        // 启动提醒界面Activity
        context.startActivity(intent);
    }
}