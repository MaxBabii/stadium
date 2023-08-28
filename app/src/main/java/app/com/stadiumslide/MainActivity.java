package app.com.stadiumslide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MainActivity extends AppCompatActivity {
    private FirebaseRemoteConfig remoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        remoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(40)
                .build();
        remoteConfig.setConfigSettingsAsync(settings);

        remoteConfig.setDefaultsAsync(R.xml.remote_config_default);
        ImageView loading_img = findViewById(R.id.loading_img);
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(loading_img, "rotation", 0f, 360f);
        rotationAnimator.setDuration(2000);
        rotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotationAnimator.setInterpolator(new LinearInterpolator());
        rotationAnimator.start();
        boolean isSimActive = isExistsAndActiveSim(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                fadeOut.setDuration(1000);
                fadeOut.setFillAfter(true);
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(1000);
                fadeIn.setFillAfter(true);
                ImageView imageView = findViewById(R.id.imageView);
                ImageView loading_img = findViewById(R.id.loading_img);
                imageView.startAnimation(fadeOut);
                loading_img.startAnimation(fadeOut);

                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Intent intent = new Intent(MainActivity.this, StartActivity.class);
                        startActivity(intent);
                        if (!isSimActive) {
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }else{
                            overridePendingTransition(0,0);
                        }

                        finish();

                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
            }
        }, 5000);
        remoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(MainActivity.this, StartActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else {
                    boolean isSimActive = isExistsAndActiveSim(MainActivity.this);
                    if (!isSimActive) {
                        // Немає зв'язку, запускаємо StartActivity без перевірки fetch
                        Intent intent = new Intent(MainActivity.this, StartActivity.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    } else {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                                fadeOut.setDuration(1000);
                                fadeOut.setFillAfter(true);
                                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                                fadeIn.setDuration(1000);
                                fadeIn.setFillAfter(true);
                                ImageView imageView = findViewById(R.id.imageView);
                                ImageView loading_img = findViewById(R.id.loading_img);
                                imageView.startAnimation(fadeOut);
                                loading_img.startAnimation(fadeOut);

                                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        Intent intent = new Intent(MainActivity.this, StartActivity.class);
                                        startActivity(intent);
                                        if (!isSimActive) {
                                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        } else {
                                            overridePendingTransition(0, 0);
                                        }

                                        finish();

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {
                                    }
                                });
                            }
                        }, 5000);
                    }
                }
            }
        });
    }
    public static boolean isExistsAndActiveSim(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int simState = telephonyManager.getSimState();
            return simState == TelephonyManager.SIM_STATE_READY;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}