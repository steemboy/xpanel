package arca.xpanel;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import static arca.xpanel.singleton.app;

public class splash extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(app.set.getBoolean("first", true))
            startActivity(new Intent(this, Intro.class));
        else
            startActivity(new Intent(this, arca.xpanel.xPanel.class));
        finish();
    }
}
