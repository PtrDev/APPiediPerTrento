package com.example.appiedipertrento;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Pietro on 22/01/14.
 * Copyright Pietro 2014
 */
public class ShareActivity extends Activity {

    EditText editText = null;
    Button share = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.share_layout);

        share = (Button)findViewById(R.id.sa_button);

        Intent intent = getIntent();
        int color_share_button = intent.getIntExtra("color",0);
        share.setBackgroundColor(color_share_button);

        editText = (EditText) findViewById(R.id.sa_text);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishWithResult(String.valueOf(editText.getText()));
            }
        });


    }

    private void finishWithResult(String text)
    {
        Bundle conData = new Bundle();
        conData.putString("param_result", text);
        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(RESULT_OK, intent);
        finish();
    }
}
