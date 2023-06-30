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

public class BodyView {
    private final Map<String, Object> bodyMap;
    private final Context context;
    private final UiBuilder uiBuilder = UiBuilder.getInstance();
    private final int bgColor;
    private final boolean isDisableClicks;
    private boolean isMicOn;

    private final SystemAlertWindowPlugin systemAlertWindowPlugin = new SystemAlertWindowPlugin();

    public BodyView(Context context, Map<String, Object> bodyMap, int bgColor, boolean isDisableClicks) {
        this.context = context;
        this.bodyMap = bodyMap;
        this.bgColor = bgColor;
        this.isDisableClicks = isDisableClicks;
        this.isMicOn = isDisableClicks;
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
        RelativeLayout columnLayout = new RelativeLayout(context);
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
        RelativeLayout.LayoutParams columnParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        columnLayout.setLayoutParams(columnParams);

        RelativeLayout.LayoutParams circleParams = new RelativeLayout.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                        context.getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                        context.getResources().getDisplayMetrics()));
        int marginVertical = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                12,
                context.getResources().getDisplayMetrics());
        circleParams.setMargins(0, marginVertical, 0, marginVertical);
        circleParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        TextView textView = uiBuilder.getTextView(context, Commons.getMapFromObject(columnMap, KEY_TEXT));
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        textView.setGravity(Gravity.CENTER);

        GradientDrawable circleBackground = new GradientDrawable();
        circleBackground.setShape(GradientDrawable.OVAL);
        circleBackground.setColor(Color.WHITE);

        RelativeLayout circleLayout = new RelativeLayout(context);
        circleLayout.setLayoutParams(circleParams);
        circleLayout.setGravity(Gravity.CENTER);
        circleLayout.addView(textView);
        circleLayout.setBackground(circleBackground);

        columnLayout.addView(circleLayout);

        // Icon Expand
        RelativeLayout.LayoutParams expandIconParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        expandIconParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        expandIconParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        ImageView expandIcon = createExpandIcon();
        columnLayout.addView(expandIcon, expandIconParams);
        expandIcon.setOnClickListener(v -> {
            if (!systemAlertWindowPlugin.sIsIsolateRunning.get()) {
                systemAlertWindowPlugin.startCallBackHandler(context);
            }
            systemAlertWindowPlugin.invokeCallBack(context, "onClick", "expand");
            openApp();
        });

        // Icon Mic
        RelativeLayout.LayoutParams micIconParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        micIconParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        micIconParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        ImageView micIcon = createIconMic(isDisableClicks);
        columnLayout.addView(micIcon, micIconParams);
        micIcon.setOnClickListener(v -> {
            isMicOn = !isMicOn;
            micIcon.setImageResource(isMicOn ? R.drawable.ic_mic : R.drawable.ic_mic_off);

            if (!systemAlertWindowPlugin.sIsIsolateRunning.get()) {
                systemAlertWindowPlugin.startCallBackHandler(context);
            }

            String micStatus = isMicOn ? "micOn" : "micOff";
            systemAlertWindowPlugin.invokeCallBack(context, "onClick", micStatus);
        });

        return columnLayout;
    }

    
    private void openApp() {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.docdoc.app");
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    private ImageView createExpandIcon() {
        ImageView expandIcon = new ImageView(context);
        expandIcon.setImageResource(R.drawable.ic_expand);
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

    private ImageView createIconMic(boolean isMicOn) {
        ImageView micIcon = new ImageView(context);
        int iconResource = isMicOn ? R.drawable.ic_mic : R.drawable.ic_mic_off;
        micIcon.setImageResource(iconResource);
        int margin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                2,
                context.getResources().getDisplayMetrics());

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(margin, 0, 0, margin);
        micIcon.setLayoutParams(params);

        return micIcon;
    }
}
