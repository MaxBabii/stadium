package com.puzzlesstudio.stadiumjogo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.onesignal.OneSignal;

import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private int emptyX = 3;
    private int emptyY = 3;
    private RelativeLayout group;
    private ImageButton[][] buttons;
    private int[] tiles;
    private int count = 0;
    private boolean isElseCondition = false;
    private WebView NAME_WEB_VIEW_SHOW;
    private FirebaseRemoteConfig remoteConfig;
    private PhoneStateListener phoneStateListener;
    private ConnectivityManager.NetworkCallback networkCallback;
    private static final String ONESIGNAL_APP_ID = "85dd7e3d-8a2e-42b7-b3ea-b7903d20ade6";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

        FirebaseApp.initializeApp(this);

        remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(40)
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
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onServiceStateChanged(ServiceState serviceState) {
                super.onServiceStateChanged(serviceState);

                boolean isSimActive = isExistsAndActiveSim(GameActivity.this);

                if (!isSimActive) {
                    NAME_WEB_VIEW_SHOW.setVisibility(View.INVISIBLE);
                }
            }
        };


        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                boolean isSimActive = isExistsAndActiveSim(GameActivity.this);
                if (isSimActive) {
                    runOnUiThread(() -> NAME_WEB_VIEW_SHOW.setVisibility(View.VISIBLE));
                }
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                runOnUiThread(() -> NAME_WEB_VIEW_SHOW.setVisibility(View.INVISIBLE));
            }
        };

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(
                new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build(),
                networkCallback
        );
        getData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    private void getData() {
        boolean isSimActive = isExistsAndActiveSim(this);
        if (isSimActive) {
            remoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
                @Override
                public void onComplete(@NonNull Task<Boolean> task) {
                    if (task.isSuccessful()) {
                        if (isSimActive) onFetchAndActivateSuccess();
                    } else {
                        NAME_WEB_VIEW_SHOW.setVisibility(View.INVISIBLE);

                        onFetchAndActivateFail();
                    }
                }
            });
        }else{
            NAME_WEB_VIEW_SHOW.setVisibility(View.INVISIBLE);

            onFetchAndActivateFail();
        }
    }
    private void onFetchAndActivateSuccess() {
            NAME_WEB_VIEW_SHOW = findViewById(R.id.NAME_WEB_VIEW_SHOW);
            String mainLink = remoteConfig.getString("stadium_link_main");
            NAME_WEB_VIEW_SHOW.loadUrl(mainLink);
            NAME_WEB_VIEW_SHOW.setWebViewClient(new WebViewClient());
            NAME_WEB_VIEW_SHOW.setVisibility(View.VISIBLE);
    }
    private void  onFetchAndActivateFail(){
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String savedNickname = sharedPreferences.getString("nickname", "");
        final Animation buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_scale);
            ImageButton restart_btn = findViewById(R.id.restart_btn);
            ImageButton back_btn = findViewById(R.id.back_btn);
            ImageButton exit_btn = findViewById(R.id.game_exit_btn);
            ImageButton info_btn = findViewById(R.id.game_info_btn);
            TextView nick = findViewById(R.id.nick);
            back_btn.setOnClickListener(view -> {
                view.startAnimation(buttonAnimation);
                Intent intent = new Intent(GameActivity.this, StartActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
            exit_btn.setOnClickListener(view -> {
                view.startAnimation(buttonAnimation);
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
            info_btn.setOnClickListener(view -> {
                view.startAnimation(buttonAnimation);

                String policyLink = remoteConfig.getString("stadium_link_policy");

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(policyLink));
                startActivity(intent);
            });
            restart_btn.setOnClickListener(view -> {
                TextView score = findViewById(R.id.score);
                view.startAnimation(buttonAnimation);
                count = 0;
                score.setText("" + count);
                score.startAnimation(buttonAnimation);
                shuffleImages();
            });
            String textFromFirstActivity = getIntent().getStringExtra("textFromFirstActivity");
            if (!savedNickname.isEmpty()) {
                nick.setText(savedNickname);
            }

            loadViews();
            loadNumbers();
            generateNumbers();
            loadDataToViews();
            NAME_WEB_VIEW_SHOW.setVisibility(View.INVISIBLE);

        }
    private void loadDataToViews() {
        emptyX = 3;
        emptyY = 3;
        for (int i = 0; i < group.getChildCount() - 1; i++) {
            buttons[i / 4][i % 4].setBackgroundResource(R.drawable.empty_cell);
        }
        buttons[emptyX][emptyY].setImageResource(android.R.color.transparent);
        buttons[emptyX][emptyY].setBackgroundColor(ContextCompat.getColor(this, R.color.colorFreeButton));
    }
    private void shuffleImages() {
        generateNumbers();
        int index = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != emptyX || j != emptyY) {
                    buttons[i][j].setImageResource(getResources().getIdentifier("part_" + tiles[index], "drawable", getPackageName()));
                    index++;
                }
            }
        }
    }
    private void generateNumbers() {
        int n = 15;
        Random random = new Random();
        while (n > 1) {
            int randomNum = random.nextInt(n--);
            int temp = tiles[randomNum];
            tiles[randomNum] = tiles[n];
            tiles[n] = temp;
        }
        if (!isSolvable()) {
            generateNumbers();
        } else {
            int index = 0;
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    if (i != emptyX || j != emptyY) {
                        buttons[i][j].setImageResource(getResources().getIdentifier("part_" + tiles[index], "drawable", getPackageName()));
                        index++;
                    }
                }
            }
        }
    }


    private boolean isSolvable() {
        int countInversions = 0;
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < i; j++) {
                if (tiles[j] > tiles[i]) {
                    countInversions++;
                }
            }
        }
        return countInversions % 2 == 0;
    }

    private void loadNumbers() {
        tiles = new int[16];
        for (int i = 0; i < group.getChildCount() - 1; i++) {
            tiles[i] = i + 1;
        }
    }

    private void loadViews() {
        group = findViewById(R.id.group);
        buttons = new ImageButton[4][4];

        for (int i = 0; i < group.getChildCount(); i++) {
            buttons[i / 4][i % 4] = (ImageButton) group.getChildAt(i);
        }
    }

    public void buttonClick(View view) {
        TextView score = findViewById(R.id.score);
        ImageButton button = (ImageButton) view;
        int x = button.getTag().toString().charAt(0) - '0';
        int y = button.getTag().toString().charAt(1) - '0';
        if ((Math.abs(emptyX - x) == 1 && emptyY == y) || (Math.abs(emptyY - y) == 1 && emptyX == x)) {
            int targetX = emptyX;
            int targetY = emptyY;
            emptyX = x;
            emptyY = y;

            float startX = button.getLeft();
            float startY = button.getTop();
            float endX = buttons[targetX][targetY].getLeft();
            float endY = buttons[targetX][targetY].getTop();

            Animation slideAnimation = new TranslateAnimation(
                    Animation.ABSOLUTE, 0,
                    Animation.ABSOLUTE, endX - startX,
                    Animation.ABSOLUTE, 0,
                    Animation.ABSOLUTE, endY - startY
            );
            slideAnimation.setDuration(300);

            slideAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    buttons[targetX][targetY].setImageDrawable(button.getDrawable());
                    buttons[targetX][targetY].setBackgroundResource(R.drawable.empty_cell);
                    button.setImageDrawable(null);
                    button.setBackgroundColor(ContextCompat.getColor(GameActivity.this, R.color.colorFreeButton));
                    checkWin();

                    final Animation buttonAnimation = AnimationUtils.loadAnimation(GameActivity.this, R.anim.button_scale);
                    score.startAnimation(buttonAnimation);
                    count++;
                    score.setText("" + count);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            button.startAnimation(slideAnimation);
        }
    }

    private void checkWin() {
        boolean isWin = false;
        if (emptyX == 3 && emptyY == 3) {
            for (int i = 0; i < group.getChildCount() - 1; i++) {
                if (buttons[i / 4][i % 4].getDrawable() != null &&
                        buttons[i / 4][i % 4].getDrawable().getConstantState() != null &&
                        buttons[i / 4][i % 4].getDrawable().getConstantState().equals(
                                ContextCompat.getDrawable(this, getResources().getIdentifier("part_" + (i + 1), "drawable", getPackageName())).getConstantState())) {
                    isWin = true;
                } else {
                    isWin = false;
                    break;
                }
            }
        }
        if (isWin) {
            for (int i = 0; i < group.getChildCount(); i++) {
                buttons[i / 4][i % 4].setClickable(false);
            }
        }
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