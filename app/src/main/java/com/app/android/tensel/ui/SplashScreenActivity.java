package com.app.android.tensel.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.app.android.tensel.R;
import com.app.android.tensel.utility.PrefManager;
import com.app.android.tensel.utility.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SplashScreenActivity extends AppCompatActivity {

    private TextView sloganTextView;
    private FirebaseAuth mAuth;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        sloganTextView = (TextView) findViewById(R.id.textViewSlogan);
        mAuth = FirebaseAuth.getInstance();
        new CountDownTimer(3000, 1000){

            int i = 10;

            @Override
            public void onTick(long l) {
                //change the text in the splash message
                sloganTextView.setText(getString(R.string.slogan, i--));
            }

            @Override
            public void onFinish() {
                processLaunch();
            }
        }.start();
    }

    /**
     * Check if is first time launch or if the slider is repeating
     */
    private void processLaunch() {
        Intent startIntent = new Intent();
        prefManager = new PrefManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null){
                //USER is connected and logged-in, go to Home
                startIntent.setClass(this, MainActivity.class);
            }else{
                //Proceed to login Screen
                startIntent.setClass(this, RegistrationActivity.class);
            }
        }else{
            startIntent.setClass(this, IntroSliderActivity.class);
        }
        startActivity(startIntent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        //Log launch/app_open event
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, Utils.ANALYTICS_PARAM__LAUNCH_ID);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, Utils.ANALYTICS_PARAM__LAUNCH_NAME);
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, Utils.ANALYTICS_PARAM__LAUNCH_CATEGORY);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text");
        FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
    }
}