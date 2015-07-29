package com.fegeley.steamgametracker;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View.OnKeyListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements OnKeyListener{

    private Intent goToAct2 = new Intent("activity2");
    private Intent goToAct3 = new Intent("activity3");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editText = (EditText) findViewById(R.id.editText);
        editText.setOnKeyListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            act3();
            return true;
        }



        return super.onOptionsItemSelected(item);
    }

    public void act2(View v){
        EditText steamID = (EditText) findViewById(R.id.editText);
        if(steamID.getText().length() >= 1) {
            goToAct2.putExtra("steamID", steamID.getText().toString());
            hideKeyboard();
            startActivity(goToAct2);
        }else{
            Toast.makeText(getApplicationContext(), "Please enter something!!", Toast.LENGTH_LONG).show();
        }
    };
    public void act3(){
        startActivity(goToAct3);
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {

        EditText myEditText = (EditText) view;

        if (keyCode == EditorInfo.IME_ACTION_SEARCH ||
                keyCode == EditorInfo.IME_ACTION_DONE ||
                event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

            if (!event.isShiftPressed()) {
                Log.v("AndroidEnterKeyActivity","Enter Key Pressed!");
                switch (view.getId()) {
                    case R.id.editText:
                        act2(view);
                }
                return true;
            }

        }
        return false; // pass on to other listeners.

    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}