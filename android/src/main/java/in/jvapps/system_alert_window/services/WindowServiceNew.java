package in.jvapps.system_alert_window.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.view.ViewCompat;

import java.util.HashMap;
import java.util.Map;

import in.jvapps.system_alert_window.R;
import in.jvapps.system_alert_window.SystemAlertWindowPlugin;
import in.jvapps.system_alert_window.models.Margin;
import in.jvapps.system_alert_window.utils.Commons;
import in.jvapps.system_alert_window.utils.Constants;
import in.jvapps.system_alert_window.utils.LogUtils;
import in.jvapps.system_alert_window.utils.NumberUtils;
import in.jvapps.system_alert_window.utils.UiBuilder;
import in.jvapps.system_alert_window.views.BodyView;
import in.jvapps.system_alert_window.views.FooterView;
import in.jvapps.system_alert_window.views.HeaderView;

public class WindowServiceNew extends Service implements View.OnTouchListener {

    private static final String TAG = WindowServiceNew.class.getSimpleName();
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final int WINDOW_VIEW_ID = 1947;
    public static final String INTENT_EXTRA_IS_UPDATE_WINDOW = "IsUpdateWindow";
    public static final String INTENT_EXTRA_IS_CLOSE_WINDOW = "IsCloseWindow";

    private static final long CLICK_DURATION_THRESHOLD = 150;
    private long downTime;

    private final SystemAlertWindowPlugin systemAlertWindowPlugin = new SystemAlertWindowPlugin();

    private WindowManager wm;

    private String windowGravity;
    private String windowUserName;
    private String windowInitials;
    private String windowImageUrl;
    private int windowWidth;
    private int windowHeight;
    private Margin windowMargin;
    private int windowBgColor;
    private boolean isDisableClicks = false;

    private LinearLayout windowView;
    private LinearLayout headerView;
    private LinearLayout bodyView;
    private BodyView bodyView2;
    private LinearLayout footerView;

    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;
    private boolean moving;

    private Context mContext = this;
    boolean isEnableDraggable = true;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        if (null != intent && intent.getExtras() != null) {
            mContext = this;
            LogUtils.getInstance().setContext(this.mContext);
            @SuppressWarnings("unchecked")
            HashMap<String, Object> paramsMap = (HashMap<String, Object>) intent
                    .getSerializableExtra(Constants.INTENT_EXTRA_PARAMS_MAP);
            boolean isCloseWindow = intent.getBooleanExtra(INTENT_EXTRA_IS_CLOSE_WINDOW, false);
            if (!isCloseWindow) {
                assert paramsMap != null;
                boolean isUpdateWindow = intent.getBooleanExtra(INTENT_EXTRA_IS_UPDATE_WINDOW, false);
                if (isUpdateWindow && wm != null && windowView != null) {
                    if (ViewCompat.isAttachedToWindow(windowView)) {
                        updateWindow(paramsMap);
                    } else {
                        createWindow(paramsMap);
                    }
                } else {
                    createWindow(paramsMap);
                }
                showNotification();
            } else {
                closeWindow(true);
            }
        }
        return START_STICKY;
    }

    private void showNotification() {
        mContext = this;
        createNotificationChannel();
        // Intent notificationIntent = new Intent(this, SystemAlertWindowPlugin.class);
        String packageName = mContext.getPackageName();
        Intent notificationIntent = mContext.getPackageManager()
                .getLaunchIntentForPackage(packageName);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Video llamada con " + windowUserName)
                .setSmallIcon(R.drawable.ic_call_black)
                .setContentIntent(pendingIntent)
                .setColor(0xFF00FF00)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void setWindowManager() {
        if (wm == null) {
            wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        }
    }

    private void setWindowLayoutFromMap(HashMap<String, Object> paramsMap) {
        Map<String, Object> headersMap = Commons.getMapFromObject(paramsMap, Constants.KEY_HEADER);
        Map<String, Object> bodyMap = Commons.getMapFromObject(paramsMap, Constants.KEY_BODY);
        Map<String, Object> footerMap = Commons.getMapFromObject(paramsMap, Constants.KEY_FOOTER);
        windowMargin = UiBuilder.getInstance().getMargin(mContext, paramsMap.get(Constants.KEY_MARGIN));
        windowBgColor = Commons.getBgColorFromParams(paramsMap);
        isDisableClicks = Commons.getIsClicksDisabled(paramsMap);
        LogUtils.getInstance().i(TAG, String.valueOf(isDisableClicks));
        windowGravity = (String) paramsMap.get(Constants.KEY_GRAVITY);
        windowUserName = (String) paramsMap.get(Constants.KEY_USERNAME);
        windowInitials = (String) paramsMap.get(Constants.KEY_INITIALS);
        windowImageUrl = (String) paramsMap.get(Constants.KEY_IMAGE_URL);
        windowWidth = NumberUtils.getInt(paramsMap.get(Constants.KEY_WIDTH));
        windowHeight = NumberUtils.getInt(paramsMap.get(Constants.KEY_HEIGHT));
        bodyView2 = new BodyView(mContext, bodyMap, windowBgColor, isDisableClicks, windowInitials, windowImageUrl);
        bodyView = bodyView2.getView();
        systemAlertWindowPlugin.setBodyView(bodyView2);
        if (headersMap != null)
            headerView = new HeaderView(mContext, headersMap, windowBgColor).getView();
        if (footerMap != null)
            footerView = new FooterView(mContext, footerMap, windowBgColor).getView();
    }

    private WindowManager.LayoutParams getLayoutParams() {
        final WindowManager.LayoutParams params;
        params = new WindowManager.LayoutParams();
        params.width = (windowWidth == 0) ? android.view.WindowManager.LayoutParams.MATCH_PARENT
                : Commons.getPixelsFromDp(mContext, windowWidth);
        params.height = (windowHeight == 0) ? android.view.WindowManager.LayoutParams.WRAP_CONTENT
                : Commons.getPixelsFromDp(mContext, windowHeight);
        params.format = PixelFormat.TRANSLUCENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            // if (isDisableClicks) {
            // params.flags = android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            // | android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            // | android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            // } else {
            params.flags = android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            // }
        } else {
            params.type = android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            // if (isDisableClicks) {
            // params.flags = android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            // | android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            // } else {
            params.flags = android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            // }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isDisableClicks) {
            params.alpha = 0.8f;
        }
        params.gravity = Commons.getGravity(windowGravity, Gravity.TOP);
        int marginTop = windowMargin.getTop();
        int marginBottom = windowMargin.getBottom();
        int marginLeft = windowMargin.getLeft();
        int marginRight = windowMargin.getRight();
        params.x = Math.max(marginLeft, marginRight);
        params.y = (params.gravity == Gravity.TOP) ? marginTop
                : (params.gravity == Gravity.BOTTOM) ? marginBottom : Math.max(marginTop, marginBottom);
        return params;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setWindowView(WindowManager.LayoutParams params, boolean isCreate) {
        // params.width == WindowManager.LayoutParams.MATCH_PARENT;
        if (isCreate) {
            windowView = new LinearLayout(mContext);
            windowView.setId(WINDOW_VIEW_ID);
        }
        windowView.setOrientation(LinearLayout.VERTICAL);
        windowView.setLayoutParams(params);
        windowView.setBackgroundColor(windowBgColor);
        windowView.removeAllViews();
        windowView.addView(bodyView);
        if (headerView != null)
            windowView.addView(headerView);
        if (footerView != null)
            windowView.addView(footerView);
        if (isEnableDraggable)
            windowView.setOnTouchListener(this);
    }

    private void createWindow(HashMap<String, Object> paramsMap) {
        closeWindow(false);
        setWindowManager();
        setWindowLayoutFromMap(paramsMap);
        WindowManager.LayoutParams params = getLayoutParams();
        setWindowView(params, true);
        try {
            wm.addView(windowView, params);
        } catch (Exception ex) {
            LogUtils.getInstance().e(TAG, ex.toString());
            retryCreateWindow();
        }
    }

    private void retryCreateWindow() {
        try {
            closeWindow(false);
            setWindowManager();
            // setWindowLayoutFromMap(paramsMap);
            WindowManager.LayoutParams params = getLayoutParams();
            setWindowView(params, true);
            wm.addView(windowView, params);
        } catch (Exception ex) {
            LogUtils.getInstance().e(TAG, ex.toString());
        }
    }

    private void updateWindow(HashMap<String, Object> paramsMap) {
        setWindowLayoutFromMap(paramsMap);
        WindowManager.LayoutParams newParams = getLayoutParams();
        WindowManager.LayoutParams previousParams = (WindowManager.LayoutParams) windowView.getLayoutParams();
        previousParams.width = (windowWidth == 0) ? android.view.WindowManager.LayoutParams.MATCH_PARENT
                : Commons.getPixelsFromDp(mContext, windowWidth);
        previousParams.height = (windowHeight == 0) ? android.view.WindowManager.LayoutParams.WRAP_CONTENT
                : Commons.getPixelsFromDp(mContext, windowHeight);
        previousParams.flags = newParams.flags;
        previousParams.alpha = newParams.alpha;
        setWindowView(previousParams, false);
        wm.updateViewLayout(windowView, previousParams);
    }

    private void closeWindow(boolean isEverythingDone) {
        LogUtils.getInstance().i(TAG, "Closing the overlay window");
        try {
            if (wm != null) {
                if (windowView != null) {
                    wm.removeView(windowView);
                    windowView = null;
                }
            }
            wm = null;
        } catch (IllegalArgumentException e) {
            LogUtils.getInstance().e(TAG, "view not found");
        }
        if (isEverythingDone) {
            stopSelf();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (null != wm) {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) windowView.getLayoutParams();

            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                downTime = System.currentTimeMillis();
                float x = event.getRawX();
                float y = event.getRawY();
                moving = false;
                originalXPos = params.x;
                originalYPos = params.y;
                offsetX = x - params.x;
                offsetY = y - params.y;
            } else if (action == MotionEvent.ACTION_MOVE) {
                float x = event.getRawX();
                float y = event.getRawY();
                params.x = Math.round(x - offsetX);
                params.y = Math.round(y - offsetY);
                wm.updateViewLayout(windowView, params);
                moving = true;
            } else if (action == MotionEvent.ACTION_UP) {
                long clickDuration = System.currentTimeMillis() - downTime;
                if (clickDuration <= CLICK_DURATION_THRESHOLD) {
                    if (systemAlertWindowPlugin != null) {
                        if (!systemAlertWindowPlugin.sIsIsolateRunning.get()) {
                            systemAlertWindowPlugin.startCallBackHandler(mContext);
                        }
                        systemAlertWindowPlugin.invokeCallBack(mContext, "onClick", "expand");
                    }
                    openApp();
                }
                return moving;
            }
        }
        return false;
    }

    private void openApp() {
        String packageName = mContext.getPackageName();
        Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            mContext.startActivity(launchIntent);
        }
    }

    @Override
    public void onDestroy() {
        // Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        LogUtils.getInstance().d(TAG, "Destroying the overlay window service");
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
