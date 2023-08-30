package in.jvapps.system_alert_window.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.content.Intent;
import android.text.TextUtils;
import android.graphics.Typeface;

import java.util.List;
import java.util.Map;

import in.jvapps.system_alert_window.models.Decoration;
import in.jvapps.system_alert_window.models.Padding;
import in.jvapps.system_alert_window.utils.Commons;
import in.jvapps.system_alert_window.utils.UiBuilder;
import in.jvapps.system_alert_window.R;

import in.jvapps.system_alert_window.SystemAlertWindowPlugin;
import in.jvapps.system_alert_window.utils.Constants;
import static in.jvapps.system_alert_window.utils.Constants.KEY_COLUMNS;
import static in.jvapps.system_alert_window.utils.Constants.KEY_DECORATION;
import static in.jvapps.system_alert_window.utils.Constants.KEY_GRAVITY;
import static in.jvapps.system_alert_window.utils.Constants.KEY_PADDING;
import static in.jvapps.system_alert_window.utils.Constants.KEY_ROWS;
import static in.jvapps.system_alert_window.utils.Constants.KEY_TEXT;
import static in.jvapps.system_alert_window.utils.Constants.KEY_INITIALS;
import static in.jvapps.system_alert_window.utils.Constants.PACKAGE_NAME;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.AccelerateDecelerateInterpolator;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class BodyView {
    private final Map<String, Object> bodyMap;
    private static Context context;
    private final UiBuilder uiBuilder = UiBuilder.getInstance();
    private final int bgColor;
    private final boolean isDisableClicks;
    private boolean isMuted;
    private String initials;
    private String imageUrl;
    private static int audioInfo = 0;
    private static RelativeLayout circleLayout;
    private static ScaleAnimation scaleAnimation;
    private RelativeLayout columnLayout;

    private final SystemAlertWindowPlugin systemAlertWindowPlugin = new SystemAlertWindowPlugin();

    public BodyView(Context context, Map<String, Object> bodyMap, int bgColor, boolean isDisableClicks, String initials,
            String imageUrl) {
        this.context = context;
        this.bodyMap = bodyMap;
        this.bgColor = bgColor;
        this.isDisableClicks = isDisableClicks;
        this.isMuted = isDisableClicks;
        this.initials = initials;
        this.imageUrl = imageUrl;
    }

    public static void getValueInt(int value) {
        audioInfo = value;
        if (circleLayout != null) {
            float valueMax = value / 100.0f;
            int valueInt = (int) (45 * valueMax);
            RelativeLayout.LayoutParams circleParams3 = new RelativeLayout.LayoutParams(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInt,
                            context.getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInt,
                            context.getResources().getDisplayMetrics()));
            circleParams3.addRule(RelativeLayout.CENTER_IN_PARENT);
            circleLayout.setLayoutParams(circleParams3);
        }
    }

    public LinearLayout getView() {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        Decoration decoration = uiBuilder.getDecoration(context, Commons.getMapFromObject(bodyMap, KEY_DECORATION));
        if (decoration != null) {
            GradientDrawable gd = uiBuilder.getGradientDrawable(decoration);
            linearLayout.setBackground(gd);
        } else {
            linearLayout.setBackgroundColor(bgColor);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        Commons.setMargin(context, params, bodyMap);
        linearLayout.setLayoutParams(params);
        Padding padding = uiBuilder.getPadding(context, bodyMap.get(KEY_PADDING));
        linearLayout.setPadding(
                padding.getLeft(),
                padding.getTop(),
                padding.getRight(),
                padding.getBottom());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rowsMap = (List<Map<String, Object>>) bodyMap.get(KEY_ROWS);
        if (rowsMap != null) {
            for (int i = 0; i < rowsMap.size(); i++) {
                Map<String, Object> row = rowsMap.get(i);
                linearLayout.addView(createRow(row));
            }
        }
        return linearLayout;
    }

    private View createRow(Map<String, Object> rowMap) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        Commons.setMargin(context, params, rowMap);
        linearLayout.setLayoutParams(params);
        linearLayout.setGravity(Commons.getGravity((String) rowMap.get(KEY_GRAVITY), Gravity.END));
        Padding padding = uiBuilder.getPadding(context, rowMap.get(KEY_PADDING));
        linearLayout.setPadding(padding.getLeft(), padding.getTop(), padding.getRight(), padding.getBottom());
        Decoration decoration = uiBuilder.getDecoration(context, Commons.getMapFromObject(rowMap, KEY_DECORATION));
        if (decoration != null) {
            GradientDrawable gd = uiBuilder.getGradientDrawable(decoration);
            linearLayout.setBackground(gd);
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> columnsMap = (List<Map<String, Object>>) rowMap.get(KEY_COLUMNS);
        if (columnsMap != null) {
            for (int j = 0; j < columnsMap.size(); j++) {
                Map<String, Object> column = columnsMap.get(j);
                linearLayout.addView(createColumn(column));
            }
        }
        return linearLayout;
    }

    private View createColumn(Map<String, Object> columnMap) {
        if (!systemAlertWindowPlugin.sIsIsolateRunning.get()) {
            systemAlertWindowPlugin.startCallBackHandler(context);
        }

        columnLayout = new RelativeLayout(context);
        columnLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        Padding padding = uiBuilder.getPadding(context, columnMap.get(KEY_PADDING));
        columnLayout.setPadding(padding.getLeft(), padding.getTop(), padding.getRight(), padding.getBottom());
        Decoration decoration = uiBuilder.getDecoration(context, Commons.getMapFromObject(columnMap, KEY_DECORATION));
        if (decoration != null) {
            GradientDrawable gd = uiBuilder.getGradientDrawable(decoration);
            columnLayout.setBackground(gd);
        }

        // Circle Decoration
        RelativeLayout.LayoutParams circleParams = new RelativeLayout.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        context.getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        context.getResources().getDisplayMetrics()));
        int marginVertical = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                6,
                context.getResources().getDisplayMetrics());
        circleParams.setMargins(0, marginVertical, 0, marginVertical);
        circleParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        circleLayout = new RelativeLayout(context);
        circleLayout.setLayoutParams(circleParams);
        circleLayout.setGravity(Gravity.CENTER);

        // Circle Background
        GradientDrawable circleBackground = new GradientDrawable();
        circleBackground.setShape(GradientDrawable.OVAL);
        circleBackground.setColor(Color.argb(64, 255, 255, 255));
        circleLayout.setBackground(circleBackground);

        RelativeLayout.LayoutParams circleParams2 = new RelativeLayout.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45,
                        context.getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45,
                        context.getResources().getDisplayMetrics()));
        circleParams2.addRule(RelativeLayout.CENTER_IN_PARENT);

        circleLayout.setLayoutParams(circleParams2);

        // circleLayout.startAnimation(scaleAnimation);

        columnLayout.addView(circleLayout);

        TextView textView = new TextView(context);
        textView.setText(initials);
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setGravity(Gravity.CENTER);

        GradientDrawable textBackground = new GradientDrawable();
        textBackground.setShape(GradientDrawable.OVAL);
        textBackground.setColor(Color.parseColor("#00DEDB"));
        textView.setBackground(textBackground);

        int textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55,
                context.getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(textSize, textSize);
        textParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        textView.setLayoutParams(textParams);

        String imageURLView = imageUrl;

        if (TextUtils.isEmpty(imageURLView)) {
            columnLayout.addView(textView);
        } else {
            int circleSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                    context.getResources().getDisplayMetrics());

            RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(circleSize, circleSize);
            imageParams.addRule(RelativeLayout.CENTER_IN_PARENT);

            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Glide.with(context)
                    .load(imageURLView)
                    .apply(new RequestOptions()
                            .override(circleSize, circleSize)
                            .centerCrop())
                    .into(imageView);

            columnLayout.addView(imageView);
        }

        // Icon Mic
        RelativeLayout.LayoutParams micIconParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        micIconParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        micIconParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        ImageView micIcon = createIconMic(isDisableClicks);
        columnLayout.addView(micIcon, micIconParams);
        micIcon.setOnClickListener(v -> {
            isMuted = !isMuted;
            micIcon.setImageResource(!isMuted ? R.drawable.ic_mic : R.drawable.ic_mic_off);

            if (!systemAlertWindowPlugin.sIsIsolateRunning.get()) {
                systemAlertWindowPlugin.startCallBackHandler(context);
            }

            String micStatus = !isMuted ? "micOn" : "micOff";
            systemAlertWindowPlugin.invokeCallBack(context, "onClick", micStatus);
        });

        // closeSystemWindowFromBody
        RelativeLayout.LayoutParams deleteParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        deleteParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        deleteParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        ImageView deleteIcon = createDeleteButton();
        columnLayout.addView(deleteIcon, deleteParams);
        deleteIcon.setOnClickListener(v -> {
            systemAlertWindowPlugin.closeSystemWindowFromBody(context);
        });

        return columnLayout;
    }

    private void openApp() {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            context.startActivity(launchIntent);
        }
    }

    private ImageView createIconMic(boolean isMuted) {
        ImageView micIcon = new ImageView(context);
        int iconResource = !isMuted ? R.drawable.ic_mic : R.drawable.ic_mic_off;
        micIcon.setImageResource(iconResource);
        int paddingLB = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                3,
                context.getResources().getDisplayMetrics());
        int paddingRT = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                15,
                context.getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        // params.setMargins(margin, 0, 0, margin);
        micIcon.setPadding(paddingLB, paddingRT, paddingRT, paddingLB);
        micIcon.setLayoutParams(params);

        return micIcon;
    }

    private ImageView createDeleteButton() {
        ImageView expandIcon = new ImageView(context);
        expandIcon.setImageResource(R.drawable.ic_close);
        int margin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                2,
                context.getResources().getDisplayMetrics());

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, margin, margin, 0);
        expandIcon.setLayoutParams(params);

        return expandIcon;
    }
}
