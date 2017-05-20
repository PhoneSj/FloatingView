package com.phone.custom;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingView floatingView= (FloatingView) findViewById(R.id.floatingView);
        floatingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"点击事件",Toast.LENGTH_SHORT).show();
            }
        });
        floatingView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(MainActivity.this,"长按事件",Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

}
