package app.com.stadiumslide;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;

public class StartActivity extends AppCompatActivity {
    private EditText editText;
    private WebView NAME_WEB_VIEW_SHOW;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        ImageButton exit_btn = findViewById(R.id.exit_btn);
        ImageButton start_btn = findViewById(R.id.play_btn);
        ImageButton info_btn = findViewById(R.id.info_btn);
        editText = findViewById(R.id.nickname);
        final Animation buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_scale);
        info_btn.setOnClickListener(view -> {
            view.startAnimation(buttonAnimation);

            String websiteUrl = "https://www.olx.ua/uk/";

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));
            startActivity(intent);
        });
        start_btn.setOnClickListener(view -> {
            String enteredText = editText.getText().toString();
            view.startAnimation(buttonAnimation);
            Intent intent = new Intent(StartActivity.this, GameActivity.class);
            intent.putExtra("textFromFirstActivity", enteredText);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        exit_btn.setOnClickListener(view -> {
            view.startAnimation(buttonAnimation);
            finish();
        });
        start_btn.setEnabled(false);
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
        NAME_WEB_VIEW_SHOW = findViewById(R.id.NAME_WEB_VIEW_SHOW);
        NAME_WEB_VIEW_SHOW.setWebViewClient(new WebViewClient());

        WebSettings webSettings = NAME_WEB_VIEW_SHOW.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        String mainLink = "https://www.youtube.com/";
        NAME_WEB_VIEW_SHOW.loadUrl(mainLink);

        boolean isSimActive = isExistsAndActiveSim(this);
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
