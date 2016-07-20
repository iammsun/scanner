package com.simon.lib.scanner;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * @author sunmeng
 * @date 16/6/17
 */
public class CaptureActivity extends AppCompatActivity implements CameraPreview
                                                                          .PreviewStateListener,
                                                                  DecodeHandler.OnResultListener {

    protected CaptureFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutID());
        if (savedInstanceState == null) {
            fragment = CaptureFragment.newInstance();
            View fragmentContainer = findViewById(R.id.capture_fragment);
            if (fragmentContainer == null) {
                throw new RuntimeException("Your content must have a fragment container whose id " +
                        "attribute is R.id.fragment'");
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.capture_fragment, fragment)
                    .commit();
        } else {
            fragment = (CaptureFragment) getSupportFragmentManager().findFragmentById(R.id
                    .capture_fragment);
        }
        fragment.setPreviewStateListener(this);
        fragment.setOnResultListener(this);
    }

    public int getLayoutID() {
        return R.layout.activity_capture;
    }

    @Override
    public void onPreviewStateChanged(boolean preview) {

    }

    @Override
    public void onPreviewError(Throwable error) {
        Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    @Override
    public void onResult(String text) {
        Intent data = new Intent();
        data.putExtra(Intent.EXTRA_TEXT, text);
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
