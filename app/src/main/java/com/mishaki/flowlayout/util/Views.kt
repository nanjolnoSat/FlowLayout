package com.mishaki.flowlayout.util

import android.app.Activity
import android.content.Context
import android.view.ViewManager
import com.mishaki.flowlayout.view.FlowLayout
import com.mishaki.flowlayout.view.PercentAbsoluteLayout
import org.jetbrains.anko.custom.ankoView

val FLOW_LAYOUT = { ctx: Context -> FlowLayout(ctx) }
val PERCENT_ABSOLUTE_LAYOUT = { ctx: Context -> PercentAbsoluteLayout(ctx) }

inline fun Context.flowLayout(init: FlowLayout.() -> Unit): FlowLayout {
    return ankoView(FLOW_LAYOUT) { init() }
}

inline fun Activity.flowLayout(init: FlowLayout.() -> Unit): FlowLayout {
    return ankoView(FLOW_LAYOUT) { init() }
}

inline fun ViewManager.flowLayout(init: FlowLayout.() -> Unit): FlowLayout {
    return ankoView(FLOW_LAYOUT) { init() }
}

inline fun Context.percentAbsoluteLayout(init: PercentAbsoluteLayout.() -> Unit): PercentAbsoluteLayout {
    return ankoView(PERCENT_ABSOLUTE_LAYOUT) { init() }
}

inline fun Activity.percentAbsoluteLayout(init: PercentAbsoluteLayout.() -> Unit): PercentAbsoluteLayout {
    return ankoView(PERCENT_ABSOLUTE_LAYOUT) { init() }
}

inline fun ViewManager.percentAbsoluteLayout(init: PercentAbsoluteLayout.() -> Unit): PercentAbsoluteLayout {
    return ankoView(PERCENT_ABSOLUTE_LAYOUT) { init() }
}