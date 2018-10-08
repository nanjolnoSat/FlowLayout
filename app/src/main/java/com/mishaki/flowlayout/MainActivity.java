package com.mishaki.flowlayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.mishaki.flowlayout.view.FlowLayout;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((FlowLayout) findViewById(R.id.fl)).setOnChildClickListener(new FlowLayout.OnChildClickListener() {
            @Override
            public void onChildClick(@NotNull View view, int index) {
                Toast.makeText(MainActivity.this, "index:" + index, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
