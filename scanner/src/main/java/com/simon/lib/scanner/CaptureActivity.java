package com.simon.lib.scanner;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * @author sunmeng
 * @date 16/6/17
 */
public class CaptureActivity extends AppCompatActivity implements CameraPreview
                                                                          .PreviewStateListener,
                                                                  DecodeHandler.OnResultListener {

    private CaptureFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        int layout = getLayoutID();
        if (layout != 0) {
            fragment = CaptureFragment.newInstance(layout);
        } else {
            fragment = CaptureFragment.newInstance(null);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment).commit();
        fragment.setPreviewStateListener(this);
        fragment.setOnResultListener(this);
    }

    public int getLayoutID() {
        return R.layout.fragment_capture;
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
        finish();
    }

    @Override
    public void onResult(String text) {
        Intent data = new Intent();
        data.putExtra(Intent.EXTRA_TEXT, text);
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
