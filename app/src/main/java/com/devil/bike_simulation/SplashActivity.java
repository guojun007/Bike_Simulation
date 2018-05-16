package com.devil.bike_simulation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         try{
         Thread.currentThread().sleep(2000);
         }catch(InterruptedException ie){
         ie.printStackTrace();
         } */
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}