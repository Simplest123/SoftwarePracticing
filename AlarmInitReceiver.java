/*
 * 闹钟初始化广播接收器（AlarmInitReceiver）
 *
 * 功能：
 * 当设备重启时（如 BOOT_COMPLETED），系统会清除之前设置的 Alarm。
 * 这个广播接收器负责在开机时重新注册所有有效但尚未触发的便签提醒，
 * 确保便签的提醒不会因系统重启而失效。
 */
public class AlarmInitReceiver extends BroadcastReceiver {

    // 查询数据库所需的字段，仅便签 ID 和提醒时间
    private static final String [] PROJECTION = new String [] {
            NoteColumns.ID,            // 便签的唯一标识符
            NoteColumns.ALERTED_DATE   // 设置的提醒时间（Unix 时间戳）
    };

    // 便签查询结果中各字段的列索引，提高数据访问效率
    private static final int COLUMN_ID           = 0; // 便签ID所在列的索引
    private static final int COLUMN_ALERTED_DATE = 1; // 提醒时间所在列的索引

    @Override
    public void onReceive(Context context, Intent intent) {
        // 获取当前系统时间（单位：毫秒）
        long currentDate = System.currentTimeMillis();

        /*
         * 查询条件说明：
         * - 只获取设置了提醒时间的普通便签（Notes.TYPE_NOTE）
         * - 且提醒时间尚未到（即提醒时间 > 当前时间）
         */
        Cursor c = context.getContentResolver().query(
                Notes.CONTENT_NOTE_URI,             // 便签的内容URI
                PROJECTION,                         // 需要查询的字段
                NoteColumns.ALERTED_DATE + ">? AND " +
                        NoteColumns.TYPE + "=" + Notes.TYPE_NOTE,
                new String[] { String.valueOf(currentDate) }, // 查询参数：当前时间
                null); // 不指定排序

        // 如果查询有结果
        if (c != null) {
            if (c.moveToFirst()) {
                // 遍历所有符合条件的便签，重新为其注册 Alarm
                do {
                    // 从结果集中读取提醒时间
                    long alertDate = c.getLong(COLUMN_ALERTED_DATE);

                    // 构造将被触发的广播Intent，目标是 AlarmReceiver
                    Intent sender = new Intent(context, AlarmReceiver.class);

                    // 为Intent设置唯一标识（Data URI），便于 AlarmReceiver 判断是哪条便签
                    sender.setData(ContentUris.withAppendedId(
                            Notes.CONTENT_NOTE_URI,
                            c.getLong(COLUMN_ID)));

                    /*
                     * 构建 PendingIntent：表示未来某个时间点触发的广播事件
                     * 参数说明：
                     * - context：当前上下文
                     * - requestCode：唯一请求码，这里为 0，多个 Alarm 会共享相同的 requestCode
                     * - sender：目标 Intent
                     * - flags：标志位，这里为 0（可设置 FLAG_IMMUTABLE 等根据版本）
                     */
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            context,
                            0,
                            sender,
                            0);

                    // 获取系统的闹钟服务
                    AlarmManager alarmManager = (AlarmManager) context
                            .getSystemService(Context.ALARM_SERVICE);

                    /*
                     * 使用 AlarmManager 注册一个 RTC_WAKEUP 类型的精确闹钟：
                     * - RTC_WAKEUP：以系统实时时间为基准，并在时间到达时唤醒设备
                     * - alertDate：闹钟触发的时间点（Unix 时间戳）
                     * - pendingIntent：当时间到达时发送的广播
                     */
                    alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            alertDate,
                            pendingIntent);

                } while (c.moveToNext()); // 移动到下一个便签提醒
            }
            // 关闭 Cursor，释放数据库资源
            c.close();
        }
    }
}
