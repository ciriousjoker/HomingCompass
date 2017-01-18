package com.ciriousjoker.homingcompass;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.RatingBar;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Random;

public class AboutActivity extends AppCompatActivity implements RatingBar.OnRatingBarChangeListener {
    public static  String MY_PREFS_FILE;

    HorizontalScrollView scrollView;
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Save rating
        MY_PREFS_FILE = getString(R.string.shared_pref_file);
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);

        ((TextView) findViewById(R.id.txt_about_signature)).append(" ©" + Calendar.getInstance().get(Calendar.YEAR));

        // Set toolbar as actionbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Add "back" button in the toolbar
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // Make links clickable
        TextView textView_Attribution = (TextView) findViewById(R.id.about_icon_attribution);
        textView_Attribution.setMovementMethod(LinkMovementMethod.getInstance());

        TextView textView_License = (TextView) findViewById(R.id.about_license_notice);
        textView_License.setMovementMethod(LinkMovementMethod.getInstance());

        RatingBar ratingBar = (RatingBar) findViewById(R.id.about_rating_bar);
        ratingBar.setRating(prefs.getFloat(getString(R.string.shared_pref_setting_about_rating), 0));
        ratingBar.setOnRatingBarChangeListener(this);

        TextView textView_Feedback = (TextView) findViewById(R.id.textView_about_feedback);
        textView_Feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                composeEmail(getResources().getStringArray(R.array.about_feedback_emails), getString(R.string.about_feedback_subject));
            }
        });



        chooseBackground();
    }

    private void chooseBackground() {
        frameLayout = (FrameLayout) findViewById(R.id.frameLayoutAbout);
        scrollView = (HorizontalScrollView) findViewById(R.id.scrollView_About_Background);

        frameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                frameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                float[] array = { 0.04f, 0.2f, 0.35f, 1f};
                float randomStr = array[new Random().nextInt(array.length)];

                int measuredScrollViewWidth = scrollView.getChildAt(0).getMeasuredWidth();

                scrollView.scrollTo(Math.round(measuredScrollViewWidth * randomStr), 0);
            }
        });
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        editor.putFloat(getString(R.string.shared_pref_setting_about_rating), v);
        editor.apply();

        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (hasFocus) {
                View decorView = getWindow().getDecorView();

                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }

    public void composeEmail(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
