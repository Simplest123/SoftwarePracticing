/*
 * 闹钟初始化广播接收器 - 负责系统启动后重新注册所有未触发的便签提醒
 * 主要功能：
 * 1. 监听系统启动完成广播(BOOT_COMPLETED)
 * 2. 查询数据库中所有未来时间的便签提醒
 * 3. 为每个有效提醒重新设置系统闹钟
 */
public class AlarmInitReceiver extends BroadcastReceiver {

    // 数据库查询投影列（只需ID和提醒时间列）
    // 优化查询性能，只选择必要的列
    private static final String [] PROJECTION = new String [] {
        NoteColumns.ID,            // 便签ID - 主键
        NoteColumns.ALERTED_DATE   // 提醒时间戳 - 毫秒级Unix时间
    };

    // 列索引常量（提高查询结果读取效率）
    // 使用常量代替魔法数字，提高代码可读性
    private static final int COLUMN_ID                = 0; // ID列索引
    private static final int COLUMN_ALERTED_DATE      = 1; // 提醒时间列索引

    /**
     * 广播接收器回调方法
     * @param context 应用上下文
     * @param intent 接收到的广播Intent（应为BOOT_COMPLETED）
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 获取当前系统时间（毫秒）
        long currentDate = System.currentTimeMillis();
        
        // 查询所有未触发且有效的便签提醒（TYPE_NOTE表示普通便签）
        // 查询条件：提醒时间 > 当前时间 AND 便签类型 = 普通便签
        Cursor c = context.getContentResolver().query(
                Notes.CONTENT_NOTE_URI,    // 便签内容URI (content://net.micode.notes/note)
                PROJECTION,                // 查询列 - 只获取ID和提醒时间
                NoteColumns.ALERTED_DATE + ">? AND " + // 提醒时间>当前时间
                NoteColumns.TYPE + "=" + Notes.TYPE_NOTE, // 且为普通便签类型
                new String[] { String.valueOf(currentDate) }, // 当前时间参数
                null); // 无排序要求

        // 检查查询结果是否有效
        if (c != null) {
            // 遍历查询结果（至少有一条记录）
            if (c.moveToFirst()) {
                do {
                    // 获取该便签的提醒时间（毫秒级时间戳）
                    long alertDate = c.getLong(COLUMN_ALERTED_DATE);
                    
                    // 创建指向AlarmReceiver的Intent
                    // 用于在提醒时间到达时触发AlarmReceiver
                    Intent sender = new Intent(context, AlarmReceiver.class);
                    // 设置数据URI（格式: content://net.micode.notes/note/[id]）
                    // 将便签ID附加到基础URI后形成完整URI
                    sender.setData(ContentUris.withAppendedId(
                            Notes.CONTENT_NOTE_URI, 
                            c.getLong(COLUMN_ID)));
                    
                    // 创建PendingIntent（FLAG不可变）
                    // 参数说明：
                    // context - 上下文
                    // 0 - requestCode（不用于区分）
                    // sender - 要执行的Intent
                    // 0 - flags（FLAG_IMMUTABLE）
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            context, 
                            0, 
                            sender, 
                            PendingIntent.FLAG_IMMUTABLE); // Android 12+需要此标志
                    
                    // 获取AlarmManager系统服务
                    AlarmManager alermManager = (AlarmManager) context
                            .getSystemService(Context.ALARM_SERVICE);
                    
                    // 设置精确闹钟（RTC_WAKEUP会唤醒设备）
                    // 参数说明：
                    // RTC_WAKEUP - 使用实时时钟并在触发时唤醒设备
                    // alertDate - 精确触发时间（毫秒）
                    // pendingIntent - 触发时执行的动作
                    alermManager.setExact(
                            AlarmManager.RTC_WAKEUP, // 使用实时时钟
                            alertDate,              // 触发时间
                            pendingIntent);         // 触发动作
                } while (c.moveToNext()); // 处理下一条记录
            }
            // 关闭Cursor释放资源
            // 重要：避免内存泄漏
            c.close(); 
        }
    }
}