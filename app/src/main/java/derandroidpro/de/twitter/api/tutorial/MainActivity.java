package derandroidpro.de.twitter.api.tutorial;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Media;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.MediaService;
import com.twitter.sdk.android.core.services.StatusesService;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.fabric.sdk.android.Fabric;
import retrofit.mime.TypedFile;

public class MainActivity extends AppCompatActivity {

    // Diese beiden Codes solltet ihr geheim halten, wenn ihr euren Code veröffentlicht:
    private static final String TWITTER_KEY = "vfgjnMm7HyFFHj2LjLTa7ncwJ";
    private static final String TWITTER_SECRET = "8UuWgSXnC0j20vGhf65JnLe8bincUkMdwPZTuc69XmMQWqWt0U";

    TwitterSession twitterSession;

    EditText et1;
    TextView tvCharacters;
    Button btnPost, btnImage;
    ImageView iv1;

    final int PICK_FILE_REQ_CODE = 16;
    final int PERMISSION_READ_REQ_CODE = 17;

    File tweetMediaFile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);

        twitterSession = Twitter.getSessionManager().getActiveSession();
        if(twitterSession == null){
            startActivity(new Intent(MainActivity.this, TwitterLoginActivity.class));
            finish();
        } else {
            // Etwas mit der Twitter API tun
        }

        setUpUi();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.menu_logout  && twitterSession != null){

            Twitter.logOut();
            Toast.makeText(MainActivity.this, "Von Twitter abgemeldet.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, TwitterLoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpUi(){
        et1 = (EditText) findViewById(R.id.editText);
        tvCharacters = (TextView) findViewById(R.id.textView);
        btnPost = (Button) findViewById(R.id.button);
        btnImage = (Button) findViewById(R.id.button_image);
        iv1 = (ImageView) findViewById(R.id.imageView);
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tweetMediaFile!= null){
                    uploadImage(tweetMediaFile, et1.getText().toString());
                } else {
                    postTweet(et1.getText().toString(),null);
                }
                et1.setText(null);
            }
        });

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    openFilePicker();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ_REQ_CODE);
                }
            }
        });


        et1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (charactersCountOk(s.toString())) {
                    btnPost.setEnabled(true);
                } else {
                    btnPost.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private boolean charactersCountOk (String text){
        int numerUrls = 0;
        int lengthAllUrls = 0;

        String regex = "\\(?\\b(http://|https://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
        Pattern urlPattern = Pattern.compile(regex);
        Matcher urlMatcher = urlPattern.matcher(text);
        while (urlMatcher.find()){
            lengthAllUrls += urlMatcher.group().length();
            numerUrls++;
        }

        int tweetLength = text.length()-lengthAllUrls+numerUrls*23;
        if(tweetMediaFile != null){
            tweetLength += 24;
        }

        tvCharacters.setText(Integer.toString(140 - tweetLength));

        if(tweetLength >0 && tweetLength <= 140){
            return true;
        } else {
            return false;
        }
    }

    private void uploadImage(File imagefile, final String tweettext){
        TypedFile typedFile = new TypedFile("image/*", imagefile);
        TwitterApiClient twitterApiClient = new TwitterApiClient(twitterSession);
        MediaService mediaservice = twitterApiClient.getMediaService();
        mediaservice.upload(typedFile, null, null, new Callback<Media>() {
            @Override
            public void success(Result<Media> result) {
                postTweet(tweettext, result.data.mediaIdString);
                tweetMediaFile = null;
                iv1.setImageDrawable(null);
            }

            @Override
            public void failure(TwitterException e) {
                Toast.makeText(MainActivity.this, "Medienupload fehlgeschlagen. Bitte erneut versuchen.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void postTweet(String text, String imageId){
        StatusesService statusesService = Twitter.getApiClient().getStatusesService();
        statusesService.update(text, null, false, null, null, null, false, false, imageId, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                Toast.makeText(MainActivity.this, "Tweet gepostet.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(TwitterException e) {
                Toast.makeText(MainActivity.this, "Tweet konnte nicht gepostet werden.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    private void openFilePicker(){
        Intent pickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickerIntent.setType("image/*");
        startActivityForResult(pickerIntent, PICK_FILE_REQ_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == PERMISSION_READ_REQ_CODE && grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            openFilePicker();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_FILE_REQ_CODE && resultCode == RESULT_OK){
            Uri contentUri = data.getData();
            String filepath = DocumentHelper.getPath(MainActivity.this, contentUri);
            tweetMediaFile = new File(filepath);
            if(tweetMediaFile.length() >5242880){
                Toast.makeText(MainActivity.this, "Datei ist zu groß! Maximalgröße: 5MB", Toast.LENGTH_SHORT).show();
                tweetMediaFile = null;
            } else {
                iv1.setImageURI(contentUri);
                if(charactersCountOk(et1.getText().toString())){
                    btnPost.setEnabled(true);
                } else {
                    btnPost.setEnabled(false);
                }
            }
        }
    }
}
