/*

 * 闹钟初始化广播接收器 - 负责系统启动后重新注册所有未触发的便签提醒
 */
public class AlarmInitReceiver extends BroadcastReceiver {

    // 数据库查询投影列（只需ID和提醒时间列）
    private static final String [] PROJECTION = new String [] {
        NoteColumns.ID,            // 便签ID
        NoteColumns.ALERTED_DATE   // 提醒时间戳
    };

    // 列索引常量（提高查询结果读取效率）
    private static final int COLUMN_ID                = 0; // ID列索引
    private static final int COLUMN_ALERTED_DATE      = 1; // 提醒时间列索引

    @Override
    public void onReceive(Context context, Intent intent) {
        long currentDate = System.currentTimeMillis();
        
        // 查询所有未触发且有效的便签提醒（TYPE_NOTE表示普通便签）
        Cursor c = context.getContentResolver().query(
                Notes.CONTENT_NOTE_URI,    // 便签内容URI
                PROJECTION,                // 查询列
                NoteColumns.ALERTED_DATE + ">? AND " + // 提醒时间>当前时间
                NoteColumns.TYPE + "=" + Notes.TYPE_NOTE, // 且为普通便签类型
                new String[] { String.valueOf(currentDate) }, // 当前时间参数
                null); // 无排序要求

        if (c != null) {
            if (c.moveToFirst()) { // 遍历查询结果
                do {
                    // 获取该便签的提醒时间
                    long alertDate = c.getLong(COLUMN_ALERTED_DATE);
                    
                    // 创建指向AlarmReceiver的Intent
                    Intent sender = new Intent(context, AlarmReceiver.class);
                    // 设置数据URI（格式: content://net.micode.notes/note/[id]）
                    sender.setData(ContentUris.withAppendedId(
                            Notes.CONTENT_NOTE_URI, 
                            c.getLong(COLUMN_ID)));
                    
                    // 创建PendingIntent（FLAG不可变）
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            context, 
                            0, // requestCode
                            sender, 
                            0); // flags
                    
                    // 获取AlarmManager服务
                    AlarmManager alermManager = (AlarmManager) context
                            .getSystemService(Context.ALARM_SERVICE);
                    
                    // 设置精确闹钟（RTC_WAKEUP会唤醒设备）
                    alermManager.set(
                            AlarmManager.RTC_WAKEUP, // 使用实时时钟
                            alertDate,              // 触发时间
                            pendingIntent);         // 触发动作
                } while (c.moveToNext());
            }
            c.close(); // 关闭Cursor释放资源
        }
    }
}

