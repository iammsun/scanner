package com.simon.lib.scanner.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.simon.lib.scanner.CaptureActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivityForResult(new Intent(MainActivity.this, CaptureActivity.class), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            open(data.getStringExtra(Intent.EXTRA_TEXT));
        }
        finish();
    }

    private void open(String content) {
        Uri uri = Uri.parse(content);
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(it);
        } catch (Exception e) {
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
        }
    }

}
