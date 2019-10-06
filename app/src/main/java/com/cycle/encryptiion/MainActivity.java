package com.cycle.encryptiion;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.cycle.encryptiion.image.ImageListActivity;
import com.cycle.encryptiion.text.TextEncryptionActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button text, image, audio, video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        text = findViewById(R.id.text);
        image = findViewById(R.id.image);
        audio = findViewById(R.id.audio);
        video = findViewById(R.id.video);

        text.setOnClickListener(this);
        image.setOnClickListener(this);
        audio.setOnClickListener(this);
        video.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text:
                startActivity(new Intent(MainActivity.this, TextEncryptionActivity.class));
                break;
            case R.id.image:
                startActivity(new Intent(MainActivity.this, ImageListActivity.class));
                break;
            case R.id.audio:
                break;
            case R.id.video:
                break;
        }
    }
}
