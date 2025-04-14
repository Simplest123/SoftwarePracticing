/*
 * 闹钟提醒活动类 - 处理便签提醒通知的展示和交互
 * 主要功能：
 * 1. 在锁屏或屏幕关闭时显示提醒
 * 2. 播放提醒铃声
 * 3. 提供查看便签或关闭提醒的选项
 */
public class AlarmAlertActivity extends Activity implements OnClickListener, OnDismissListener {
    private long mNoteId;          // 当前提醒关联的便签ID，用于查询数据库
    private String mSnippet;      // 便签内容预览片段，显示在提醒对话框中
    private static final int SNIPPET_PREW_MAX_LEN = 60; // 预览文本最大长度，超过部分将被截断
    MediaPlayer mPlayer;          // 媒体播放器用于播放提醒音，需要及时释放资源

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 移除活动标题栏，使对话框更突出
        requestWindowFeature(Window.FEATURE_NO_TITLE); 

        // 获取窗口对象并设置锁屏显示标志
        // 这些标志允许提醒在锁屏界面上显示
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        // 如果屏幕关闭，设置唤醒相关标志
        // 这些标志确保设备在提醒时唤醒屏幕
        if (!isScreenOn()) {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR);
        }

        // 从Intent获取便签数据
        Intent intent = getIntent();
        try {
            // 解析URI路径获取便签ID (格式: content://net.micode.notes/note/1)
            // URI路径段示例：["note", "1"]，取第二个元素作为ID
            mNoteId = Long.valueOf(intent.getData().getPathSegments().get(1));
            
            // 通过ContentResolver查询便签内容
            // DataUtils是辅助类，封装了数据库查询逻辑
            mSnippet = DataUtils.getSnippetById(this.getContentResolver(), mNoteId);
            
            // 截断过长的预览文本并添加省略指示
            // 如果文本超过最大长度，截断并在末尾添加省略号指示
            mSnippet = mSnippet.length() > SNIPPET_PREW_MAX_LEN ? 
                mSnippet.substring(0, SNIPPET_PREW_MAX_LEN) + 
                getResources().getString(R.string.notelist_string_info) : mSnippet;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return; // 数据异常直接退出，不显示提醒
        }

        // 初始化媒体播放器，用于播放提醒铃声
        mPlayer = new MediaPlayer();
        
        // 检查便签是否仍然有效（未被删除）
        // Notes.TYPE_NOTE 参数指定只检查普通便签类型
        if (DataUtils.visibleInNoteDatabase(getContentResolver(), mNoteId, Notes.TYPE_NOTE)) {
            showActionDialog();  // 显示提醒对话框
            playAlarmSound();    // 播放提醒音
        } else {
            finish(); // 便签已删除则结束活动
        }
    }

    /**
     * 检查屏幕是否亮起
     * @return boolean - true表示屏幕已亮起，false表示屏幕关闭
     */
    private boolean isScreenOn() {
        // 获取电源管理服务
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        // 返回当前屏幕状态
        return pm.isScreenOn();
    }

    /**
     * 播放系统默认闹钟音
     * 1. 获取系统默认闹铃设置
     * 2. 检查静音模式设置
     * 3. 准备并播放音频
     */
    private void playAlarmSound() {
        // 获取系统默认闹钟铃声URI
        // RingtoneManager.TYPE_ALARM 指定获取闹钟类型的铃声
        Uri url = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);

        // 检查静音模式设置，确定哪些音频流会受静音影响
        // MODE_RINGER_STREAMS_AFFECTED 设置表示哪些音频流在静音模式下会被静音
        int silentModeStreams = Settings.System.getInt(getContentResolver(),
                Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

        // 根据设置决定音频流类型
        // 检查STREAM_ALARM是否在受影响的流中
        if ((silentModeStreams & (1 << AudioManager.STREAM_ALARM)) != 0) {
            mPlayer.setAudioStreamType(silentModeStreams);
        } else {
            // 默认使用闹钟音频流
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        }
        
        // 准备并播放音频
        try {
            // 设置音频源为系统默认闹铃
            mPlayer.setDataSource(this, url);
            // 准备播放器（同步准备，可能阻塞主线程）
            mPlayer.prepare();
            // 设置循环播放，直到用户关闭提醒
            mPlayer.setLooping(true); 
            // 开始播放
            mPlayer.start();
        } catch (Exception e) {
            // 捕获各种媒体播放异常，包括：
            // IOException, IllegalStateException, SecurityException等
            e.printStackTrace(); 
        }
    }

    /**
     * 显示提醒操作对话框
     * 对话框包含：
     * 1. 便签预览内容
     * 2. 确认按钮（关闭提醒）
     * 3. 可选编辑按钮（当屏幕亮起时显示）
     */
    private void showActionDialog() {
        // 使用AlertDialog.Builder构建对话框
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        // 设置对话框标题为应用名称
        dialog.setTitle(R.string.app_name);       
        // 显示便签预览内容       
        dialog.setMessage(mSnippet);              
        
        // 设置确定按钮（关闭提醒）
        dialog.setPositiveButton(R.string.notealert_ok, this);
        
        // 如果屏幕已亮起，显示编辑按钮
        // 屏幕关闭时不显示编辑按钮，减少用户操作复杂度
        if (isScreenOn()) {
            // 设置编辑按钮，点击后跳转到编辑界面
            dialog.setNegativeButton(R.string.notealert_enter, this);
        }
        
        // 显示对话框并设置关闭监听
        // 对话框关闭时会触发onDismiss回调
        dialog.show().setOnDismissListener(this);
    }

    /**
     * 对话框按钮点击处理
     * @param dialog 触发事件的对话框
     * @param which 被点击的按钮标识
     */
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_NEGATIVE: // 点击编辑按钮
                // 创建跳转到便签编辑界面的Intent
                Intent intent = new Intent(this, NoteEditActivity.class);
                // 设置动作为查看
                intent.setAction(Intent.ACTION_VIEW);
                // 传递便签ID作为额外数据
                intent.putExtra(Intent.EXTRA_UID, mNoteId);
                // 启动编辑活动
                startActivity(intent); 
                break;
            default:
                // 其他按钮（确定按钮）不需要特殊处理
                break;
        }
    }

    /**
     * 对话框关闭时回调
     * 无论通过哪种方式关闭对话框（点击按钮或返回键），
     * 都会停止铃声并结束活动
     * @param dialog 被关闭的对话框
     */
    public void onDismiss(DialogInterface dialog) {
        // 停止铃声播放
        stopAlarmSound();
        // 结束当前活动
        finish();
    }

    /**
     * 停止铃声播放并释放资源
     * 重要：必须释放媒体资源，避免内存泄漏
     */
    private void stopAlarmSound() {
        if (mPlayer != null) {
            // 停止播放
            mPlayer.stop();
            // 释放媒体播放器资源
            mPlayer.release(); 
            // 清空引用
            mPlayer = null;
        }
    }
}
