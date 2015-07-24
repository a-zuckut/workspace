package com.example.codewars.app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.codewars.app.R;

public class Activity extends ActionBarActivity {

    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_);
        final EditText text = (EditText)findViewById(R.id.editText);
        Button button = (Button)findViewById(R.id.ButtonStart);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                name = text.getText().toString();
                Intent intent = new Intent(view.getContext(), ShowData.class);
                intent.putExtra("name", name);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return R.id.action_settings == id ||super.onOptionsItemSelected(item);
    }
}
