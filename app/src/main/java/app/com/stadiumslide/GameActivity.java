package app.com.stadiumslide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private int emptyX = 3;
    private int emptyY = 3;
    private RelativeLayout group;
    private ImageButton[][] buttons;
    private int[] tiles;
    private int count = 0;
    private WebView NAME_WEB_VIEW_SHOW;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        ImageButton restart_btn = findViewById(R.id.restart_btn);
        ImageButton back_btn = findViewById(R.id.back_btn);
        ImageButton exit_btn = findViewById(R.id.game_exit_btn);
        ImageButton info_btn = findViewById(R.id.game_info_btn);
        TextView nick = findViewById(R.id.nick);
        final Animation buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_scale);
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

            String websiteUrl = "https://www.olx.ua/uk/";

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));
            startActivity(intent);
        });
        restart_btn.setOnClickListener(view -> {
            view.startAnimation(buttonAnimation);
            shuffleImages();
        });
        String textFromFirstActivity = getIntent().getStringExtra("textFromFirstActivity");
        nick.setText(textFromFirstActivity);
        NAME_WEB_VIEW_SHOW = findViewById(R.id.NAME_WEB_VIEW_SHOW);
        NAME_WEB_VIEW_SHOW.setWebViewClient(new WebViewClient());

        WebSettings webSettings = NAME_WEB_VIEW_SHOW.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        String mainLink = "https://www.youtube.com/";
        NAME_WEB_VIEW_SHOW.loadUrl(mainLink);

        boolean isSimActive = isExistsAndActiveSim(this);

        loadViews();
        loadNumbers();
        generateNumbers();
        loadDataToViews();
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
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            button.startAnimation(slideAnimation);
        }
        count++;
        score.setText("" + count);
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