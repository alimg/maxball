package com.xsonsui.maxball;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;


public class MainActivity extends Activity {

    private EditText textName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textName =  (EditText)findViewById(R.id.text_name);
        findViewById(R.id.button_accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = textName.getText().toString();
                Intent i = new Intent(MainActivity.this, LobbyBrowserActivity.class);
                i.putExtra("name", name);
                startActivity(i);
            }
        });
    }


}
