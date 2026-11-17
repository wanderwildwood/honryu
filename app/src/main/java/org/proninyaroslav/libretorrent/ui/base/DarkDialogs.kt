package org.proninyaroslav.libretorrent.ui.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import org.proninyaroslav.libretorrent.ui.compose.DarkMode

/**
 * Dialog fragments in dark mode. A dialog fragment the app does not build itself (the time
 * picker in Scheduling) opens a window of its own that dark mode does not reach unless it is
 * handed over: it stayed light on the black page. Every dialog fragment of every activity is
 * handed over here once it has started, when its dialog is showing.
 */
object DarkDialogs {
    private val fragments = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
            if (f !is DialogFragment) return
            val window = f.dialog?.window ?: return
            DarkMode.window(window.decorView)
        }
    }

    @JvmStatic
    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, state: Bundle?) {
                (activity as? FragmentActivity)?.supportFragmentManager?.registerFragmentLifecycleCallbacks(fragments, true)
            }
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
