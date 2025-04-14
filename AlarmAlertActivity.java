/*
 * 闹钟提醒活动类 - 处理便签提醒通知的展示和交互逻辑
 * 当便签设置的提醒时间到达时，会启动该 Activity：
 * - 弹出提醒对话框
 * - 播放提醒铃声
 * - 提供跳转编辑便签的选项
 */
public class AlarmAlertActivity extends Activity implements OnClickListener, OnDismissListener {

    private long mNoteId;          // 当前提醒关联的便签ID，从Intent中获取
    private String mSnippet;       // 便签内容预览片段，用于展示在对话框中
    private static final int SNIPPET_PREW_MAX_LEN = 60; // 预览文本最大长度限制
    MediaPlayer mPlayer;          // 媒体播放器对象，用于播放提醒铃声

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏Activity标题栏，提升沉浸感

        // 设置窗口属性，让提醒界面可以在锁屏状态下显示
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED); // 锁屏时显示Activity

        // 如果屏幕当前是关闭状态，添加一系列标志位唤醒屏幕
        if (!isScreenOn()) {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON               // 保持屏幕常亮
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON              // 点亮屏幕
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON  // 允许锁屏状态下运行
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR);        // 设置窗口布局
        }

        // 从Intent中获取数据
        Intent intent = getIntent();
        try {
            // 提取URI路径中的便签ID (例如: content://net.micode.notes/note/1 → 获取"1")
            mNoteId = Long.valueOf(intent.getData().getPathSegments().get(1));

            // 查询该便签的内容摘要（预览）
            mSnippet = DataUtils.getSnippetById(this.getContentResolver(), mNoteId);

            // 若预览内容过长则截断，并追加省略符号（例如“…”或“更多”）
            mSnippet = mSnippet.length() > SNIPPET_PREW_MAX_LEN ?
                    mSnippet.substring(0, SNIPPET_PREW_MAX_LEN) +
                            getResources().getString(R.string.notelist_string_info) : mSnippet;

        } catch (IllegalArgumentException e) {
            e.printStackTrace(); // 如果Intent数据异常（如格式不对或内容为空），打印错误并退出
            return;
        }

        // 初始化媒体播放器
        mPlayer = new MediaPlayer();

        // 检查便签是否仍存在于数据库中（防止用户在提醒前已删除）
        if (DataUtils.visibleInNoteDatabase(getContentResolver(), mNoteId, Notes.TYPE_NOTE)) {
            showActionDialog();  // 显示提醒对话框
            playAlarmSound();    // 播放提醒铃声
        } else {
            finish(); // 若便签已被删除，则关闭该提醒页面
        }
    }

    /**
     * 检查设备屏幕是否处于亮屏状态
     * 用于决定是否需要添加点亮屏幕的窗口标志
     */
    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn(); // API < 20，建议新版本使用 isInteractive()
    }

    /**
     * 播放系统默认闹钟铃声，作为提醒音
     */
    private void playAlarmSound() {
        // 获取系统设定的默认闹钟铃声URI
        Uri url = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);

        // 查询系统设置中受静音模式影响的音频流类型
        int silentModeStreams = Settings.System.getInt(getContentResolver(),
                Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

        // 判断是否需要切换播放音频流类型（防止闹钟被静音）
        if ((silentModeStreams & (1 << AudioManager.STREAM_ALARM)) != 0) {
            mPlayer.setAudioStreamType(silentModeStreams); // 使用静音影响流
        } else {
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM); // 明确指定为闹钟流
        }

        // 设置数据源并开始播放音频
        try {
            mPlayer.setDataSource(this, url); // 设置铃声URI
            mPlayer.prepare();                // 同步准备
            mPlayer.setLooping(true);         // 设置循环播放直到用户操作
            mPlayer.start();                  // 开始播放
        } catch (Exception e) {
            e.printStackTrace(); // 捕捉各种可能的媒体播放异常，如文件不存在、权限等
        }
    }

    /**
     * 构造并显示提醒操作对话框（包括"确定"和"编辑"按钮）
     */
    private void showActionDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.app_name);    // 设置对话框标题为应用名
        dialog.setMessage(mSnippet);           // 展示便签内容预览

        // 设置“确定”按钮，点击后不会跳转，仅关闭提醒
        dialog.setPositiveButton(R.string.notealert_ok, this);

        // 如果屏幕已经点亮，则提供“编辑”按钮，允许直接跳转到便签编辑界面
        if (isScreenOn()) {
            dialog.setNegativeButton(R.string.notealert_enter, this);
        }

        // 显示对话框，并设置对话框关闭监听器
        dialog.show().setOnDismissListener(this);
    }

    /**
     * 对话框按钮点击事件处理逻辑
     * 可处理“编辑”操作
     */
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_NEGATIVE: // 编辑按钮被点击
                Intent intent = new Intent(this, NoteEditActivity.class);
                intent.setAction(Intent.ACTION_VIEW); // 设置操作类型为“查看”
                intent.putExtra(Intent.EXTRA_UID, mNoteId); // 传入便签ID
                startActivity(intent); // 跳转到便签编辑页面
                break;
            default:
                // “确定”按钮点击或其他默认行为，不需处理，直接由onDismiss关闭
                break;
        }
    }

    /**
     * 对话框关闭时调用（无论点了哪个按钮）
     * - 停止播放铃声
     * - 结束当前提醒Activity
     */
    public void onDismiss(DialogInterface dialog) {
        stopAlarmSound();
        finish();
    }

    /**
     * 停止铃声播放并释放资源，防止内存泄漏或异常
     */
    private void stopAlarmSound() {
        if (mPlayer != null) {
            mPlayer.stop();       // 停止播放
            mPlayer.release();    // 释放媒体资源（必须）
            mPlayer = null;       // 避免悬空引用
        }
    }
}
