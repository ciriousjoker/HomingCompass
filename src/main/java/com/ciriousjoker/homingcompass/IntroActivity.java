package com.ciriousjoker.homingcompass;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class IntroActivity extends AppCompatActivity {

    HorizontalScrollView scrollView;

    FrameLayout frameLayout;
    private ValueAnimator animator;
    Button button;

    private static final int FADE_DELAY = 500;
    private static final int FADE_DURATION = 500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        frameLayout = (FrameLayout) findViewById(R.id.frameLayoutIntro);
        button = (Button) findViewById(R.id.buttonIntroFinish);



        scrollView = (HorizontalScrollView) findViewById(R.id.scrollView_Intro_Background);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startSettingsActivity = new Intent(IntroActivity.this, SettingsActivity.class);
                startSettingsActivity.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(startSettingsActivity);

                finish();
            }
        });

        frameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                frameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int displayWidth = size.x;
                int measuredScrollViewWidth = scrollView.getChildAt(0).getMeasuredWidth();

                animator = ValueAnimator.ofFloat(0.0f, measuredScrollViewWidth - displayWidth);

                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setRepeatMode(ValueAnimator.REVERSE);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(30000L);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        final int progress = Math.round((float) animation.getAnimatedValue());

                        scrollView.scrollTo(progress, 0);
                    }
                });
                animator.start();
            }
        });


        animateSteps();
    }

    private void animateSteps() {
        final LinearLayout step1 = (LinearLayout) findViewById(R.id.linearLayout_Intro_Step_1);
        final LinearLayout step2 = (LinearLayout) findViewById(R.id.linearLayout_Intro_Step_2);
        final LinearLayout step3 = (LinearLayout) findViewById(R.id.linearLayout_Intro_Step_3);
        final LinearLayout step4 = (LinearLayout) findViewById(R.id.linearLayout_Intro_Step_4);
        final Button step5 = (Button) findViewById(R.id.buttonIntroFinish);

        step1.setAlpha(0);
        step2.setAlpha(0);
        step3.setAlpha(0);
        step4.setAlpha(0);
        step5.setAlpha(0);

        ValueAnimator animator_step1 = ValueAnimator.ofFloat(0.0f, 1.0f).setDuration(FADE_DURATION);
        ValueAnimator animator_step2 = ValueAnimator.ofFloat(0.0f, 1.0f).setDuration(FADE_DURATION);
        ValueAnimator animator_step3 = ValueAnimator.ofFloat(0.0f, 1.0f).setDuration(FADE_DURATION);
        ValueAnimator animator_step4 = ValueAnimator.ofFloat(0.0f, 1.0f).setDuration(FADE_DURATION);
        ValueAnimator animator_step5 = ValueAnimator.ofFloat(0.0f, 1.0f).setDuration(FADE_DURATION);

        animator_step1.setStartDelay(FADE_DELAY);
        animator_step2.setStartDelay(FADE_DELAY * 2);
        animator_step3.setStartDelay(FADE_DELAY * 3);
        animator_step4.setStartDelay(FADE_DELAY * 4);
        animator_step5.setStartDelay(FADE_DELAY * 5);

        animator_step1.setInterpolator(new AccelerateDecelerateInterpolator());
        animator_step2.setInterpolator(new AccelerateDecelerateInterpolator());
        animator_step3.setInterpolator(new AccelerateDecelerateInterpolator());
        animator_step4.setInterpolator(new AccelerateDecelerateInterpolator());
        animator_step5.setInterpolator(new AccelerateDecelerateInterpolator());

        animator_step1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                step1.setAlpha((float) animation.getAnimatedValue());
            }
        });

        animator_step2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                step2.setAlpha((float) animation.getAnimatedValue());
            }
        });

        animator_step3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                step3.setAlpha((float) animation.getAnimatedValue());
            }
        });

        animator_step4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                step4.setAlpha((float) animation.getAnimatedValue());
            }
        });

        animator_step5.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                step5.setAlpha((float) animation.getAnimatedValue());
            }
        });

        animator_step1.start();
        animator_step2.start();
        animator_step3.start();
        animator_step4.start();
        animator_step5.start();
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
}
