package com.example.ocr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public final class OCR_ResultActivity extends Activity {

    EditText et_result;
    Context context;
    String resultData;
    ImageView iv_back;
    TextView tv_copy, tv_share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocr_result_activity);

        context = OCR_ResultActivity.this;

        resultData = getIntent().getStringExtra("ResultData").trim().replaceAll("[\r\n]+", "\n").toString();

        et_result = (EditText) findViewById(R.id.et_result);
        et_result.setText(resultData);
        et_result.setSelection(et_result.getText().length());

        tv_share = (TextView) findViewById(R.id.tv_share);
        tv_copy = (TextView) findViewById(R.id.tv_copy);

        iv_back = (ImageView) findViewById(R.id.iv_back);

        tv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareRecognizedTextIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareRecognizedTextIntent.setType("text/plain");
                shareRecognizedTextIntent.putExtra(android.content.Intent.EXTRA_TEXT, et_result.getText().toString());
                startActivity(Intent.createChooser(shareRecognizedTextIntent, "Share via"));
            }
        });

        tv_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setClipboard(et_result.getText().toString());
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setClipboard(String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }
}