package com.puzzlesstudio.stadiumjogo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.onesignal.OneSignal;

public class StartActivity extends AppCompatActivity {
    private static final String ONESIGNAL_APP_ID = "85dd7e3d-8a2e-42b7-b3ea-b7903d20ade6";
    private WebView NAME_WEB_VIEW_SHOW;
    private FirebaseRemoteConfig remoteConfig;
    private String savedNickname = "";
    private SharedPreferences sharedPreferences;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

        FirebaseApp.initializeApp(this);
        remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(5)
                .build();
        remoteConfig.setConfigSettingsAsync(settings);
        remoteConfig.setDefaultsAsync(R.xml.remote_config_default);
        NAME_WEB_VIEW_SHOW = findViewById(R.id.NAME_WEB_VIEW_SHOW);
        NAME_WEB_VIEW_SHOW.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        WebSettings webSettings = NAME_WEB_VIEW_SHOW.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        savedNickname = sharedPreferences.getString("nickname", "");

        getData();

    }

    private void getData() {
        boolean isSimActive = isExistsAndActiveSim(this);
        if (isSimActive) {
            remoteConfig.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        onFetchAndActivateSuccess();

                    } else {
                        NAME_WEB_VIEW_SHOW.setVisibility(View.INVISIBLE);
                        onFetchAndActivateFail();
                    }
                }
            });
        }else {
            NAME_WEB_VIEW_SHOW.setVisibility(View.INVISIBLE);
            onFetchAndActivateFail();
        }
    }
    private void onFetchAndActivateSuccess() {
        NAME_WEB_VIEW_SHOW.setVisibility(View.VISIBLE);
            String mainLink = remoteConfig.getString("stadium_link_main");
            NAME_WEB_VIEW_SHOW.loadUrl(mainLink);
            NAME_WEB_VIEW_SHOW.setWebViewClient(new WebViewClient());

    }
    private void  onFetchAndActivateFail(){
        final Animation buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_scale);
        editText = findViewById(R.id.nickname);
        ImageButton exit_btn = findViewById(R.id.exit_btn);
        ImageButton start_btn = findViewById(R.id.play_btn);
        ImageButton info_btn = findViewById(R.id.info_btn);
        String policyLink = remoteConfig.getString("stadium_link_policy");
        info_btn.setOnClickListener(view -> {
            view.startAnimation(buttonAnimation);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(policyLink));
            startActivity(intent);
        });

        start_btn.setOnClickListener(view -> {
            String enteredText = editText.getText().toString();
            view.startAnimation(buttonAnimation);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("nickname", enteredText);
            editor.apply();

            Intent intent = new Intent(StartActivity.this, GameActivity.class);
            startActivity(intent);

            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        });

        exit_btn.setOnClickListener(view -> {
            view.startAnimation(buttonAnimation);
            finish();
        });

        start_btn.setEnabled(!savedNickname.isEmpty());
        editText.setText(savedNickname);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                start_btn.setEnabled(charSequence.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
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
