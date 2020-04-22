package com.gnex.savemyride;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;

import com.gnex.savemyride.Models.RuleBreaks;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class LobbyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        ButterKnife.bind(this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


    }


    @OnClick(R.id.relativeLayout_rules)
    public void openRules() {
        Intent intent = new Intent(LobbyActivity.this, RuleBreaksActivity.class);
        startActivity(intent);
    }


    @OnClick(R.id.relLayout_map)
    public void openMap() {
        Intent intent = new Intent(LobbyActivity.this, MapsActivity.class);
        startActivity(intent);
    }
}
