package derandroidpro.de.twitter.api.tutorial;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class TwitterLoginActivity extends AppCompatActivity {

    TwitterLoginButton twitterLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_login);

        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_btn);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                String username = result.data.getUserName();
                Toast.makeText(TwitterLoginActivity.this, "Angemeldet mit " + username, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(TwitterLoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void failure(TwitterException e) {
                Toast.makeText(TwitterLoginActivity.this, "Login fehlgeschlagen.", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }
}
