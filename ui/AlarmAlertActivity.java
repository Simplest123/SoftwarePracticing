/*
 * 闹钟提醒活动类 - 处理便签提醒通知的展示和交互
 */
public class AlarmAlertActivity extends Activity implements OnClickListener, OnDismissListener {
    private long mNoteId;          // 当前提醒关联的便签ID
    private String mSnippet;      // 便签内容预览片段
    private static final int SNIPPET_PREW_MAX_LEN = 60; // 预览文本最大长度
    MediaPlayer mPlayer;          // 媒体播放器用于播放提醒音

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏标题栏

        // 获取窗口对象并设置锁屏显示标志
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        // 如果屏幕关闭，设置唤醒相关标志
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
            mNoteId = Long.valueOf(intent.getData().getPathSegments().get(1));
            
            // 通过ContentResolver查询便签内容
            mSnippet = DataUtils.getSnippetById(this.getContentResolver(), mNoteId);
            
            // 截断过长的预览文本并添加省略指示
            mSnippet = mSnippet.length() > SNIPPET_PREW_MAX_LEN ? 
                mSnippet.substring(0, SNIPPET_PREW_MAX_LEN) + 
                getResources().getString(R.string.notelist_string_info) : mSnippet;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return; // 数据异常直接退出
        }

        // 初始化媒体播放器
        mPlayer = new MediaPlayer();
        
        // 检查便签是否仍然有效
        if (DataUtils.visibleInNoteDatabase(getContentResolver(), mNoteId, Notes.TYPE_NOTE)) {
            showActionDialog();  // 显示提醒对话框
            playAlarmSound();    // 播放提醒音
        } else {
            finish(); // 便签已删除则结束活动
        }
    }

    /**
     * 检查屏幕是否亮起
     */
    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }

    /**
     * 播放系统默认闹钟音
     */
    private void playAlarmSound() {
        // 获取系统默认闹钟铃声URI
        Uri url = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);

        // 检查静音模式设置
        int silentModeStreams = Settings.System.getInt(getContentResolver(),
                Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

        // 根据设置决定音频流类型
        if ((silentModeStreams & (1 << AudioManager.STREAM_ALARM)) != 0) {
            mPlayer.setAudioStreamType(silentModeStreams);
        } else {
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        }
        
        // 准备并播放音频
        try {
            mPlayer.setDataSource(this, url);
            mPlayer.prepare();
            mPlayer.setLooping(true); // 设置循环播放
            mPlayer.start();
        } catch (Exception e) {
            e.printStackTrace(); // 捕获各种媒体播放异常
        }
    }

    /**
     * 显示提醒操作对话框
     */
    private void showActionDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.app_name);       // 设置对话框标题
        dialog.setMessage(mSnippet);              // 显示便签预览内容
        
        // 确定按钮
        dialog.setPositiveButton(R.string.notealert_ok, this);
        
        // 如果屏幕已亮起，显示编辑按钮
        if (isScreenOn()) {
            dialog.setNegativeButton(R.string.notealert_enter, this);
        }
        
        // 显示对话框并设置关闭监听
        dialog.show().setOnDismissListener(this);
    }

    /**
     * 对话框按钮点击处理
     */
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_NEGATIVE: // 点击编辑按钮
                Intent intent = new Intent(this, NoteEditActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra(Intent.EXTRA_UID, mNoteId);
                startActivity(intent); // 跳转到便签编辑界面
                break;
            default:
                break;
        }
    }

    /**
     * 对话框关闭时停止铃声并结束活动
     */
    public void onDismiss(DialogInterface dialog) {
        stopAlarmSound();
        finish();
    }

    /**
     * 停止铃声播放并释放资源
     */
    private void stopAlarmSound() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release(); // 重要：必须释放媒体资源
            mPlayer = null;
        }
    }
}