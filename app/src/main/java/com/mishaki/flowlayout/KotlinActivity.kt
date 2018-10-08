package com.mishaki.flowlayout

import android.app.Activity
import android.os.Bundle
import com.mishaki.flowlayout.util.flowLayout
import org.jetbrains.anko.button

class KotlinActivity : Activity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        flowLayout {
            repeat(10){
                button("button:$it") {}
            }
        }
    }
}