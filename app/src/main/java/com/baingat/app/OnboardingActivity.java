package com.baingat.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private OnboardingAdapter onboardingAdapter;
    private LinearLayout layoutDots;
    private ImageView buttonNext;
    private ImageView buttonBack;
    private TextView buttonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }

        layoutDots = findViewById(R.id.layoutDots);
        buttonNext = findViewById(R.id.buttonNext);
        buttonBack = findViewById(R.id.buttonBack);
        buttonStart = findViewById(R.id.buttonStart);

        setupOnboardingItems();

        ViewPager2 viewPagerOnboarding = findViewById(R.id.viewPagerOnboarding);
        viewPagerOnboarding.setAdapter(onboardingAdapter);

        setupDots();
        setCurrentDot(0);
        updateButtonsVisibility(0);

        viewPagerOnboarding.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentDot(position);
                updateButtonsVisibility(position);
            }
        });

        buttonNext.setOnClickListener(v -> {
            if (viewPagerOnboarding.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
                viewPagerOnboarding.setCurrentItem(viewPagerOnboarding.getCurrentItem() + 1);
            } else {
                navigateToLogin();
            }
        });

        buttonBack.setOnClickListener(v -> {
            if (viewPagerOnboarding.getCurrentItem() > 0) {
                viewPagerOnboarding.setCurrentItem(viewPagerOnboarding.getCurrentItem() - 1);
            }
        });

        buttonStart.setOnClickListener(v -> navigateToLogin());
    }

    private void updateButtonsVisibility(int position) {
        if (position == 0) {
            buttonBack.setVisibility(View.GONE);
            buttonNext.setVisibility(View.VISIBLE);
            buttonStart.setVisibility(View.GONE);
            layoutDots.setVisibility(View.VISIBLE);
        } else if (position == onboardingAdapter.getItemCount() - 1) {
            buttonBack.setVisibility(View.VISIBLE);
            buttonNext.setVisibility(View.GONE);
            buttonStart.setVisibility(View.VISIBLE);
            layoutDots.setVisibility(View.GONE);
        } else {
            buttonBack.setVisibility(View.VISIBLE);
            buttonNext.setVisibility(View.VISIBLE);
            buttonStart.setVisibility(View.GONE);
            layoutDots.setVisibility(View.VISIBLE);
        }
    }

    private void setupOnboardingItems() {
        List<OnboardingItem> onboardingItems = new ArrayList<>();

        OnboardingItem item1 = new OnboardingItem(
                R.drawable.onboarding_1_welcome,
                "Selamat datang di <br><b><font color='#004DFF'>Baingat</font></b>",
                "Sistem monitoring ketinggian air banjir secara real-time untuk membantu menjaga keamanan motormu."
        );

        OnboardingItem item2 = new OnboardingItem(
                R.drawable.onboarding_2_map,
                "Pantau ketinggian air secara <b><font color='#004DFF'>real-time</font></b>",
                "Dapatkan informasi akurat tentang ketinggian air di lokasi sekitar kamu secara langsung."
        );

        OnboardingItem item3 = new OnboardingItem(
                R.drawable.onboarding_3_alert,
                "Dapatkan peringatan dini saat keadaan <b><font color='#004DFF'>darurat</font></b>",
                "Kami akan memberi notifikasi jika ketinggian air meningkat dan berpotensi membahayakan."
        );

        onboardingItems.add(item1);
        onboardingItems.add(item2);
        onboardingItems.add(item3);

        onboardingAdapter = new OnboardingAdapter(onboardingItems);
    }

    private void setupDots() {
        ImageView[] dots = new ImageView[onboardingAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 0, 8, 0);
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(getApplicationContext());
            dots[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(), R.drawable.onboarding_dot_inactive
            ));
            dots[i].setLayoutParams(layoutParams);
            layoutDots.addView(dots[i]);
        }
    }

    private void setCurrentDot(int index) {
        int childCount = layoutDots.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutDots.getChildAt(i);
            if (i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(), R.drawable.onboarding_dot_active
                ));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(), R.drawable.onboarding_dot_inactive
                ));
            }
        }
        
        if (index == onboardingAdapter.getItemCount() - 1) {
            buttonNext.setImageResource(R.drawable.ic_arrow_right);
        } else {
            buttonNext.setImageResource(R.drawable.ic_arrow_right);
        }
    }

    private void navigateToLogin() {
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.setFirstTimeLaunch(false);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
