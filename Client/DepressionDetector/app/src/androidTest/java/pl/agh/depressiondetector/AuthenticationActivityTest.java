package pl.agh.depressiondetector;


import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import pl.agh.depressiondetector.authentication.AuthenticationActivity;
import pl.agh.depressiondetector.authentication.LoginActivity;
import pl.agh.depressiondetector.authentication.SignUpActivity;


@RunWith(AndroidJUnit4.class)
public class AuthenticationActivityTest {

    @Rule
    public IntentsTestRule<AuthenticationActivity> activityTestRule = new IntentsTestRule<>(AuthenticationActivity.class);

    @Test
    public void shouldOpenSignUpActivityAfterSignUpButtonClick(){
        onView(withId(R.id.button_sign_up)).perform(click());
        intended(hasComponent(SignUpActivity.class.getName()));
    }

    @Test
    public void shouldOpenLoginActivityAfterLoginButtonClick(){
        onView(withId(R.id.button_login)).perform(click());
        intended(hasComponent(LoginActivity.class.getName()));
    }
}