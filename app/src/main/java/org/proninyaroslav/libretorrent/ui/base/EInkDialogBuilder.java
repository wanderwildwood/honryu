package org.proninyaroslav.libretorrent.ui.base;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

/*
 * A dialog as EInkDialog draws it in the Compose apps: white, a 2dp black rim, 12dp corners.
 * With nothing dimmed behind it, the rim is what separates it from the page.
 */
public class EInkDialogBuilder extends MaterialAlertDialogBuilder {

    public EInkDialogBuilder(@NonNull Context context) {
        super(context);
        init(context);
    }

    public EInkDialogBuilder(@NonNull Context context, int overrideThemeResId) {
        super(context, overrideThemeResId);
        init(context);
    }

    private void init(Context context) {
        float corner = dp(context, 12);
        var background = new MaterialShapeDrawable(
                ShapeAppearanceModel.builder().setAllCornerSizes(corner).build());
        background.setFillColor(ColorStateList.valueOf(Color.WHITE));
        background.setStroke(dp(context, 2), Color.BLACK);
        setBackground(background);
    }

    // No dimming or shadow behind, whichever overlay the dialog was built with.
    @NonNull
    @Override
    public AlertDialog create() {
        AlertDialog dialog = super.create();
        Window window = dialog.getWindow();
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setElevation(0f);
        }
        return dialog;
    }

    private static float dp(Context context, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }
}
