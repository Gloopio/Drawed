package io.gloop.drawed;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * This activity is shown only on the first run of the app. It shows a intro screen.
 */
public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Just set a title, description, background and image. AppIntro will do the rest.
        // addSlide(AppIntroFragment.newInstance(title, description, image, backgroundColor));
        // TODO set real content
        addSlide(AppIntroFragment.newInstance("Draw",
                "Create a board and start drawing with your fingers.",
                R.drawable.intro_pencil,
                getResources().getColor(R.color.intro_1)));

        addSlide(AppIntroFragment.newInstance("Work Together",
                "Draw amazing boards together with your friends.",
                R.drawable.intro_share,
                getResources().getColor(R.color.intro_2)));

        addSlide(AppIntroFragment.newInstance("Public Boards",
                "Make your boards accessible to the wide world. Let everyone contribute to your board.",
                R.drawable.intro_public,
                getResources().getColor(R.color.intro_3)));

        // OPTIONAL METHODS
        // Override bar/separator color.
        // setBarColor(Color.parseColor("#3F51B5"));
        // setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button.
        showSkipButton(true);
        // setProgressButtonEnabled(false);

        setColorTransitionsEnabled(true);
//        setFlowAnimation();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
        startMainIntent();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
        startMainIntent();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }

    private void startMainIntent() {
        Intent i = new Intent(getApplicationContext(), SplashActivity.class);
        startActivity(i);
        finish();
    }
}