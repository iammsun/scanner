package com.simon.lib.scanner.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.simon.lib.scanner.CaptureActivity;

public class MainActivity extends Activity {

    private String[] mimeTypes;

    private static final String EXTRA_MIME_TYPES = "mime_types";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mimeTypes = getResources().getStringArray(R.array.options_mimetype_view);
            startActivityForResult(new Intent(MainActivity.this, CaptureActivity.class), 0);
        } else {
            mimeTypes = savedInstanceState.getStringArray(EXTRA_MIME_TYPES);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray(EXTRA_MIME_TYPES, mimeTypes);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            onResult(data.getStringExtra(Intent.EXTRA_TEXT));
        } else {
            finish();
        }
    }

    private void onResult(final String text) {
        new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setItems(R
                        .array.options_view, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openAsMimeType(text, mimeTypes[which]);
                        finish();
                    }
                });
                return builder.create();
            }

            @Override
            public void onDismiss(DialogInterface dialog) {
                super.onDismiss(dialog);
                finish();
            }
        }.show(getFragmentManager(), "dialog");
    }


    private void openAsMimeType(String text, String mimeType) {
        if (mimeTypes[0].equals(mimeType)) {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            return;
        }
        Uri uri = Uri.parse(text);
        if (uri.getScheme() == null) {
            uri = Uri.parse("http://" + text);
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, mimeType);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
