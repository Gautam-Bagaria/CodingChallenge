package com.gautam.mobilecodechallenge;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * this is the entry point of the application and will require users to login before they can use the features of the application.
 * 
 */
public class LoginActivity extends Activity {
	// The keys should not be hard coded into the application
	private static final String APP_KEY = "ji90ps8dyvk42z7";
    private static final String APP_SECRET = "kb4wrq82ksyutxm";
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    
    private boolean mLoggedIn;
    static DropboxAPI<AndroidAuthSession> mApi;
    //Declare all UserInterface for this screen here
    private Button Login;
    private Button takePhoto;
    private Button displayPhotoList;
    private Button recordAudio;
    
    private final String PHOTO_DIR = "/Photos/";
    
    private static final int NEW_PICTURE = 1;
    private String mCameraFileName;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		Login = (Button)findViewById(R.id.login_button);
		takePhoto = (Button)findViewById(R.id.take_photo_button);
		displayPhotoList = (Button)findViewById(R.id.show_photos_button);
		recordAudio = (Button)findViewById(R.id.record_audio_button);
		
		//set the buttons not usable before logging in.
		takePhoto.setVisibility(View.GONE);
        displayPhotoList.setVisibility(View.GONE);
        recordAudio.setVisibility(View.GONE);
		if (savedInstanceState != null) {
            mCameraFileName = savedInstanceState.getString("mCameraFileName");
        }
		
		// We create a new AuthSession so that we can use the Dropbox API.
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        
        //onClickListeners. These can be implemented using Views and setting the onclick property of buttons in the XML file.
        recordAudio.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Start activity for audio screen
				Intent RecordAudioIntent = new Intent(LoginActivity.this,RecordAudioActivity.class);
				startActivity(RecordAudioIntent);
			}
		});
        
        Login.setOnClickListener(new OnClickListener() {
			// check if logged in or not
			@Override
			public void onClick(View v) {
				
				if(mLoggedIn)
				{
					logout();
				}
				else
				{
					mApi.getSession().startOAuth2Authentication(LoginActivity.this);
				}
				
			}
		});
        
        displayPhotoList.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// start activity to show list of photos
				Intent ShowListIntent = new Intent(LoginActivity.this,ShowListActivity.class);
				startActivity(ShowListIntent);
							
				
			}
		});
        
        takePhoto.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
                // Picture from camera.
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                
                // Set a name for the file.
                Date date = new Date();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss", Locale.US);

                String newPicFile = df.format(date) + ".jpeg";
                String outPath = new File(Environment.getExternalStorageDirectory(), newPicFile).getPath();
                          
                // Create the file.
                File outFile = new File(outPath);

                mCameraFileName = outFile.toString();
                Uri outuri = Uri.fromFile(outFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
                Log.i("TAKE_PHOTO", "Importing New Picture: " + mCameraFileName);
                try {
                    startActivityForResult(intent, NEW_PICTURE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(LoginActivity.this, "There doesn't seem to be a camera.", Toast.LENGTH_SHORT).show();
                }
				
			}
		});
	}
    
	@Override
    protected void onResume() {
        super.onResume();
        AndroidAuthSession session = mApi.getSession();

        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth.
                session.finishAuthentication();

                // Store it locally in our app for later use.
                storeAuth(session);
                setLoggedIn(true);
            } catch (IllegalStateException e) {
                Toast.makeText(LoginActivity.this, "Couldn't authenticate with Dropbox:" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("AUTH_ERROR", "Error authenticating", e);
            }
        }
    }
	
	//Once a photo has been taken we need to upload it to dropbox.
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == NEW_PICTURE) {
	        // return from file upload
	        if (resultCode == Activity.RESULT_OK) {
	            Uri uri = null;
	            if (data != null) {
	                uri = data.getData();
	            }
	            if (uri == null && mCameraFileName != null) {
	                uri = Uri.fromFile(new File(mCameraFileName));
	            }
	            File file = new File(mCameraFileName);
	
	            if (uri != null) {
	            	// if photo was taken successfully by the camera then upload it.
	            	
	            	//TODO maybe we can make this better and show a dialogue box to confirm upload.
	                UploadFile upload = new UploadFile(this, mApi, PHOTO_DIR, file);
	                upload.execute();
	                
	            }
	        } else {
	            Log.w("CAMERA_ERROR", "Unknown Activity Result from mediaImport: "
	                    + resultCode);
	        }
	    }
	}
	
	private void storeAuth(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            return;
        }
        // Store the OAuth 1 access token, if there is one.  This is only necessary if
        // you're still using OAuth 1.
        AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
        if (oauth1AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, oauth1AccessToken.key);
            edit.putString(ACCESS_SECRET_NAME, oauth1AccessToken.secret);
            edit.commit();
            return;
        }
    }
	
	private AndroidAuthSession buildSession() {
		//The session is made in here using the keys.
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
	}

	private void loadAuth(AndroidAuthSession session) {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        if (key.equals("oauth2:")) {
            // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
            session.setOAuth2AccessToken(secret);
        } else {
            // Still support using old OAuth 1 tokens.
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
		
	}

	private void logout() {
		 // Remove credentials from the session
        mApi.getSession().unlink();

        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
        setLoggedIn(false);
		
	}

	private void clearKeys() {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
		
	}


	private void setLoggedIn(boolean loggedIn) {
		mLoggedIn = loggedIn;
    	if (loggedIn) {
    		Login.setText("Logout from Dropbox");
            takePhoto.setVisibility(View.VISIBLE);
            displayPhotoList.setVisibility(View.VISIBLE);
            recordAudio.setVisibility(View.VISIBLE);
    	} else {
    		Login.setText("Log Into Dropbox");
            takePhoto.setVisibility(View.GONE);
            displayPhotoList.setVisibility(View.GONE);
            recordAudio.setVisibility(View.GONE);
    	}
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
