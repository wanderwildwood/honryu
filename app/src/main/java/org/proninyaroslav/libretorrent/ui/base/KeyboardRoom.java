package org.proninyaroslav.libretorrent.ui.base;

import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/*
 * A dialog that is typed into, kept whole above the keyboard: it sits at the top of the screen
 * and, while the keyboard is up, is made exactly as tall as the room left above it. Its buttons
 * keep their place and its body, which scrolls, takes what is left. Left to Android, a tall
 * dialog keeps its height and has the keyboard's height taken out of its inside, which squeezed
 * the field and the buttons out of sight.
 */
public final class KeyboardRoom {

    private KeyboardRoom() {
    }

    /* Call once the dialog is made, before it is shown. The keyboard is asked for as it opens. */
    public static void fit(Dialog dialog, Activity activity) {
        Window window = dialog.getWindow();
        if (window == null || activity == null) {
            return;
        }
        // Nothing taken out by Android: the height is set here instead.
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        window.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        View decor = window.getDecorView();
        View page = activity.getWindow().getDecorView();
        // Nor inside the dialog: the keyboard's inset is not handed on to its content, which
        // newer Android squeezes by it even when nothing is asked for.
        ViewCompat.setOnApplyWindowInsetsListener(decor, (v, insets) ->
                ViewCompat.onApplyWindowInsets(v, new WindowInsetsCompat.Builder(insets)
                        .setInsets(WindowInsetsCompat.Type.ime(), Insets.NONE)
                        .build()));
        int[] at = new int[2];
        ViewTreeObserver.OnGlobalLayoutListener listener = () -> {
            WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(page);
            // The keyboard's height from the foot of the screen, read off the page behind,
            // which fills the screen whatever the dialog's own size.
            int keyboard = insets == null ? 0 : insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            int want = WindowManager.LayoutParams.WRAP_CONTENT;
            if (keyboard > 0) {
                page.getLocationOnScreen(at);
                int keyboardTop = at[1] + page.getHeight() - keyboard;
                decor.getLocationOnScreen(at);
                // The dialog's drawn edge sits inside a clear margin of its window (Material's
                // background inset); that margin may go under the keyboard.
                android.graphics.Rect margin = new android.graphics.Rect();
                if (decor.getBackground() != null) {
                    decor.getBackground().getPadding(margin);
                }
                int room = keyboardTop - at[1] + margin.bottom;
                if (room > 0 && decor.getHeight() > room) {
                    want = room;
                } else if (window.getAttributes().height > 0 && window.getAttributes().height <= room) {
                    want = window.getAttributes().height;
                }
            }
            WindowManager.LayoutParams attrs = window.getAttributes();
            if (attrs.height != want) {
                attrs.height = want;
                window.setAttributes(attrs);
            }
        };
        // Either window may be laid out again when the keyboard comes or goes: both are watched,
        // the page's only while the dialog is up.
        decor.getViewTreeObserver().addOnGlobalLayoutListener(listener);
        decor.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                page.getViewTreeObserver().addOnGlobalLayoutListener(listener);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                page.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
            }
        });
    }
}
