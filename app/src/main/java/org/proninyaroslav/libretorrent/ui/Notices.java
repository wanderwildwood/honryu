package org.proninyaroslav.libretorrent.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;

/**
 * A notice stays until the next press. On this panel one on a timer (a short snackbar, a
 * toast) could be gone before it had been read; one that waits is read first and goes on the
 * way to whatever the next press does.
 *
 * MainActivity hands its touches and Back here ({@link #pressed(MotionEvent)}), and says
 * which activity is in front, so a notice from work that ends as a page closes, or from the
 * engine, still has somewhere to be said.
 */
public final class Notices {
    private static WeakReference<Snackbar> shown = new WeakReference<>(null);
    private static WeakReference<Activity> front = new WeakReference<>(null);
    private static final Handler main = new Handler(Looper.getMainLooper());

    private Notices() {
    }

    /** Shows [snackbar] until the next press, in place of whatever it was told to last. */
    public static void stay(@NonNull Snackbar snackbar) {
        Snackbar before = shown.get();
        if (before != null && before != snackbar) {
            before.dismiss();
        }
        snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        shown = new WeakReference<>(snackbar);
        snackbar.show();
    }

    /**
     * Says [text] at the foot of the page in front, until the next press. From any thread. With
     * no page of this app in front, the phone's own toast is all there is.
     */
    public static void say(@NonNull Context context, @NonNull CharSequence text) {
        Context app = context.getApplicationContext();
        main.post(() -> {
            Activity activity = front.get();
            View root = activity == null ? null : activity.findViewById(android.R.id.content);
            if (root != null) {
                stay(Snackbar.make(root, text, Snackbar.LENGTH_INDEFINITE));
            } else {
                Toast.makeText(app, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void say(@NonNull Context context, int text) {
        say(context, context.getString(text));
    }

    /** Called by the activity as it comes to the front (with itself) and goes (with null). */
    public static void front(@Nullable Activity activity) {
        front = new WeakReference<>(activity);
    }

    /**
     * A finger down anywhere takes the notice away, except on its own button (Undo and the
     * like), which then does what it says and takes it away itself.
     */
    public static void pressed(@NonNull MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return;
        }
        Snackbar snackbar = shown.get();
        if (snackbar == null || !snackbar.isShown()) {
            return;
        }
        View action = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_action);
        if (action != null && action.getVisibility() == View.VISIBLE && within(action, event)) {
            return;
        }
        snackbar.dismiss();
    }

    public static void pressed(@NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            Snackbar snackbar = shown.get();
            if (snackbar != null) {
                snackbar.dismiss();
            }
        }
    }

    private static boolean within(View view, MotionEvent event) {
        int[] at = new int[2];
        view.getLocationOnScreen(at);
        float x = event.getRawX();
        float y = event.getRawY();
        return x >= at[0] && x < at[0] + view.getWidth() && y >= at[1] && y < at[1] + view.getHeight();
    }
}
