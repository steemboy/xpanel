package arca.xpanel.services;

import android.accessibilityservice.AccessibilityService;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.os.Vibrator;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.Toast;

import arca.xpanel.R;
import arca.xpanel.custome_interfaces.FancyToast;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static arca.xpanel.singleton.app;

public class service_accessibility extends AccessibilityService implements View.OnTouchListener, AppOpsManager.OnOpChangedListener, ValueAnimator.AnimatorUpdateListener {
    public static boolean isEnabled = false;
    private boolean torch = false;
    private String no_action;
    private WindowManager wm;
    private Vibrator vibrator;
    private final DisplayMetrics dm = new DisplayMetrics();
    private GestureDetectorCompat detector;
    private WindowManager.LayoutParams params;
    public static final String ACTION_UPDATE_SERVICE = "update-update";
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_PARAM = "extra_par";
    private Intent cameraIntent;
    private Button b;
    private int defX;
    private int defY;

    private boolean shadow = false;
    private int s_power = 0;
    private boolean vibrate = false;
    private int sv_power = 15;
    private int ev_power = 15;
    private int barHeight;

    @Override public void onCreate() {
        super.onCreate();

        cameraIntent = new Intent();
        cameraIntent.setAction(Intent.ACTION_CAMERA_BUTTON);
        cameraIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CAMERA));

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);

        barHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, dm);
        min = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, dm);

        s_power = app.set.getInt("shadow_power", 5);
        shadow = app.set.getBoolean("shadow", false);
        vibrate = app.set.getBoolean("vibrate", false);
        sv_power = app.set.getInt("vibrate_start_power", 15);
        ev_power = app.set.getInt("vibrate_end_power", 15);
        b = new Button(this);
        b.setBackground(setColor(app.set.getString("color", "#ffffff"), ContextCompat.getDrawable(this, R.drawable.less_rounded_corner)));
        b.setElevation(shadow ? s_power : 0);
        b.setOnTouchListener(this);


        params = new WindowManager.LayoutParams(app.set.getInt("width", 100), app.set.getInt("height", 30), 2038, 8, 1);
        params.gravity = Gravity.START | Gravity.TOP;

        setMargins(app.set.getInt("margin", 5));

        detector = new GestureDetectorCompat(this, gdl);
        no_action = getResources().getString(R.string.no_gesture);
    }

    private void setMargins(int mar) {
        switch (app.set.getString("gravity", "1")) {
            case "0":
                defX = mar;
                break;
            case "2":
                defX = dm.widthPixels - mar - params.width;
                break;
            default:
                defX = (dm.widthPixels - params.width) / 2;
                break;
        }
        defY = dm.heightPixels - barHeight - params.height - mar;

        params.y = defY;
        params.x = defX;
    }

    private void update_button(Intent i) {
        switch (i.getStringExtra(EXTRA_NAME)) {
            case "color":
                b.setBackground(setColor(i.getStringExtra(EXTRA_PARAM), ContextCompat.getDrawable(this, R.drawable.less_rounded_corner)));
                break;
            case "width":
                params.width = i.getIntExtra(EXTRA_PARAM, 600);
                wm.updateViewLayout(b, params);
                break;
            case "height":
                params.height = i.getIntExtra(EXTRA_PARAM, 25);
                wm.updateViewLayout(b, params);
                break;
            case "margin":
                setMargins(i.getIntExtra(EXTRA_PARAM, 25));
                wm.updateViewLayout(b, params);
                break;
            case "shadow":
                shadow = i.getBooleanExtra(EXTRA_PARAM, false);
                b.setElevation(shadow ? s_power : 0);
                break;
            case "shadow_power":
                s_power = i.getIntExtra(EXTRA_PARAM, 5);
                if(shadow)
                    b.setElevation(s_power);
                break;
            case "vibrate":
                vibrate = i.getBooleanExtra(EXTRA_PARAM, false);
                break;
            case "vibrate_end_power":
                ev_power = i.getIntExtra(EXTRA_PARAM, 15);
                break;
            case "vibrate_start_power":
                sv_power = i.getIntExtra(EXTRA_PARAM, 15);
                break;
            case "gravity":
                switch (i.getStringExtra(EXTRA_PARAM)) {
                    case "0":
                        params.gravity = Gravity.START | Gravity.BOTTOM;
                        break;
                    case "2":
                        params.gravity = Gravity.END | Gravity.BOTTOM;
                        break;
                    default:
                        params.gravity = Gravity.CENTER | Gravity.BOTTOM;
                        break;
                }
                if(isEnabled)
                    wm.updateViewLayout(b, params);
                break;
        }
    }

    public Drawable setColor(String color, Drawable background) {
        if (background instanceof ShapeDrawable)
            ((ShapeDrawable) background.mutate()).getPaint().setColor(Color.parseColor(color));
        else if (background instanceof GradientDrawable)
            ((GradientDrawable) background.mutate()).setColor(Color.parseColor(color));
        else if (background instanceof ColorDrawable)
            ((ColorDrawable) background.mutate()).setColor(Color.parseColor(color));
        return background;
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && intent.getAction() != null)
            if(intent.getAction().equals(ACTION_UPDATE_SERVICE))
                update_button(intent);
        return START_NOT_STICKY;
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        accessibilityEvent.getAction();
    }
    @Override public void onInterrupt() { }
    @Override public void onServiceConnected() {
        isEnabled = true;
        app.ops.startWatchingMode(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, app.name, this);
        if(app.check_alert())
            wm.addView(b, params);
    }

    @Override public void onDestroy(){
        isEnabled = false;
        try {
            wm.removeViewImmediate(b);
        } catch (Exception ignored) {}
        try {
            app.ops.stopWatchingMode(this);
        } catch (Exception ignored) {}
    }

    float dX, dY, sX, sY;
    float min;
    float max = 250;
    boolean moving = false;
    @Override public boolean onTouch(View v, MotionEvent e1) {
        if(MotionEvent.ACTION_DOWN == e1.getAction()) {
            int[] location = new int[2];
            b.getLocationOnScreen(location);
            sX = location[0];
            sY = location[1];
            dX = sX - e1.getRawX();
            dY = sY - e1.getRawY();
            if(vibrate)
                vibrator.vibrate(sv_power);
        } else if(MotionEvent.ACTION_MOVE == e1.getAction()) {
            float newX = dX + e1.getRawX();
            float newY = dY + e1.getRawY();

            if (Math.abs(newX - sX) < min && Math.abs(newY - sY) < min && !moving) {
            } else if (Math.abs(newX - sX) >= max && Math.abs(newY - sY) >= max && moving) {
                double fi = Math.atan((newY - sY) / (newX - sX)) * 57.3 + (newX >= 0 ? 0 : 180);
                if (fi < 23) do_action("right"); //left to right
                else if (fi < 67) do_action("upright");  //left-up to right
                else if (fi < 112) do_action("up"); //up to right
                else if (fi < 157) do_action("upleft"); //right-up to right
                else if (fi < 200) do_action("left"); //right to right
                else if (fi > 337) do_action("right"); //left to right
            } else {
                params.x = (int) newX;
                params.y = (int) newY;
                wm.updateViewLayout(b, params);
                moving = true;
            }
        } else if(MotionEvent.ACTION_UP == e1.getAction()) {
            if(moving)
                toHome();
        }
        return detector.onTouchEvent(e1);
    }

    private void toHome() {
        params.y = defY;
        params.x = defX;
        wm.updateViewLayout(b, params);
        moving = false;
    }

    private void do_action(String str) {
        toHome();
        int i = app.set.getInt(str, 0);
        if(vibrate)
            vibrator.vibrate(ev_power);
        try {
            switch (i) {
                case 1:
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                    break;
                case 2:
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    break;
                case 3:
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                    break;
                case 4:
                    String p = app.get_last_app();
                    if(p.isEmpty())
                        performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                    else {
                        Intent inte = app.pm.getLaunchIntentForPackage(p);
                        if (inte != null)
                            startActivity(inte.addFlags(FLAG_ACTIVITY_NEW_TASK));
                        else
                            performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                    }
                    break;
                case 5:
                    if(app.chec_devadmin())
                        app.dpm.lockNow();
                    else
                        FancyToast.makeText(service_accessibility.this, "Не получено разрешение для блокировки телефона", Toast.LENGTH_SHORT, FancyToast.WARNING).show();
                    break;
                case 6:
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS);
                    break;
                case 7:
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
                    break;
                case 8:
                    if(app.check_camera())
                        app.camera.setTorchMode(app.camera.getCameraIdList()[0], torch = !torch);
                    else
                        FancyToast.makeText(service_accessibility.this, "Эта функция доступно только для телефонов с версией андроид 6.0 и выше", Toast.LENGTH_SHORT, FancyToast.INFO).show();
                    break;
                case 9:
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
                    break;
                case 10:
                    sendOrderedBroadcast(cameraIntent, null);
                    break;
                case 11:
                    String s = app.set.getString(str + "_packge", "");
                    if(s.isEmpty())
                        FancyToast.makeText(service_accessibility.this, "Не выбрано приложение", Toast.LENGTH_SHORT, FancyToast.INFO).show();
                    else {
                        Intent intent = app.pm.getLaunchIntentForPackage(s);
                        if(intent != null)
                            startActivity(intent.setFlags(FLAG_ACTIVITY_NEW_TASK));
                        else
                            FancyToast.makeText(service_accessibility.this, "Не удалось запустить прложение", Toast.LENGTH_SHORT, FancyToast.WARNING).show();
                    }
                    break;
                default:
                    FancyToast.makeText(service_accessibility.this, no_action, Toast.LENGTH_SHORT, FancyToast.INFO).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    GestureDetector.SimpleOnGestureListener gdl = new GestureDetector.SimpleOnGestureListener() {
        @Override public boolean onDoubleTap(MotionEvent e) {
            do_action("dtap");
            return true;
        }

        @Override public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            do_action("ltap");
        }

        @Override public boolean onSingleTapConfirmed(MotionEvent e) {
            do_action("tap");
            return true;
        }
    };

    @TargetApi(Build.VERSION_CODES.M)
    @Override public void onOpChanged(String s, String s1) {
        if (s1.equals(app.name)) {
            try {
                wm.removeViewImmediate(b);
            } catch (Exception ignored) {}
            if(app.check_alert())
                wm.addView(b, params);
        }
    }

    @Override public void onAnimationUpdate(ValueAnimator p0) {
        params.x = (int) p0.getAnimatedValue();
        wm.updateViewLayout(b, params);
    }
}
