package com.gautam.mobilecodechallenge;

import java.util.ArrayList;


import android.app.Activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import android.widget.ImageView;
import android.widget.ListView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;

import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
/**
 * Fetching the list of image file names that are there in by Dropbox folder and display it in listview.
 * clicking on a listview item will display the image associated with it in a imageview.
 *
 */
public class ShowListActivity extends Activity {
	
	
	private ListView listView ;
	private ArrayList<Entry> thumbs;

    private Context mContext;
    public static DropboxAPI<?> mApi;
    private String mPath;
    private ImageView mImage;
    private String mErrorMsg;
    private ArrayAdapter<String> mAdapter;
   
   
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_showlist);
		 listView = (ListView) findViewById(R.id.audio_file_listview);
		 mImage = (ImageView)findViewById(R.id.imageView1);
		 listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>()));
		 ShowListAsyncTask download = new ShowListAsyncTask(ShowListActivity.this, LoginActivity.mApi, "/Photos/");
		 download.execute();
          	 
         listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
               
            	//String selectedFromList =(listView.getItemAtPosition(position).toString());
            	ShowImage showImage = new ShowImage(ShowListActivity.this, mApi, "/Photos/", mImage,position);
                showImage.execute();
                
                
            	//Toast.makeText(getBaseContext(), selectedFromList, Toast.LENGTH_LONG).show();
               
            }
        });   	
		
	}
	
	// this task will fetch the file names and display them in the list.
	
public class ShowListAsyncTask extends AsyncTask<Void, String, Void> {
 	    public ShowListAsyncTask(Context context, DropboxAPI<?> api, String dropboxPath) {
	        // We set the context this way so we don't accidentally leak activities
	        mContext = context.getApplicationContext();
	        mApi = api;
	        mPath = dropboxPath;
	      }

	    @Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			mAdapter= (ArrayAdapter<String>) listView.getAdapter();
		}
		@Override
	    protected Void doInBackground(Void... params) {
	        try {
	            

	            // Get the metadata for a directory
	            thumbs = new ArrayList<Entry>();
	            Entry dirent = mApi.metadata(mPath, 1000, null, true, null);

	            if (!dirent.isDir || dirent.contents == null) {
	                // It's not a directory, or there's nothing in it
	                mErrorMsg = "File or empty directory";
	                
	            }

	            // Make a list of everything in it that we can get a thumbnail for
	            int count =0;
	            for (Entry ent: dirent.contents) {
	                if (ent.thumbExists) {
	                    // Add it to the list of thumbs we can choose from
	                    thumbs.add(ent);
	                    publishProgress(thumbs.get(count).fileName().toString());
	                    count++;
	                }
	            }
	            
	            if (thumbs.size() == 0) {
	                // No thumbs in that directory
	                mErrorMsg = "No pictures in that directory";
	                
	            }
	           

	        } catch (DropboxUnlinkedException e) {
	            // The AuthSession wasn't properly authenticated or user unlinked.
	        } catch (DropboxPartialFileException e) {
	            // We canceled the operation
	            mErrorMsg = "Download canceled";
	        } catch (DropboxServerException e) {
	            // Server-side exception.  These are examples of what could happen,
	            // but we don't do anything special with them here.
	            if (e.error == DropboxServerException._304_NOT_MODIFIED) {
	                // won't happen since we don't pass in revision with metadata
	            } else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
	                // Unauthorized, so we should unlink them.  You may want to
	                // automatically log the user out in this case.
	            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
	                // Not allowed to access this
	            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
	                // path not found (or if it was the thumbnail, can't be
	                // thumbnailed)
	            } else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
	                // too many entries to return
	            } else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
	                // can't be thumbnailed
	            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
	                // user is over quota
	            } else {
	                // Something else
	            }
	            // This gets the Dropbox error, translated into the user's language
	            mErrorMsg = e.body.userError;
	            if (mErrorMsg == null) {
	                mErrorMsg = e.body.error;
	            }
	        } catch (DropboxIOException e) {
	            // Happens all the time, probably want to retry automatically.
	            mErrorMsg = "Network error.  Try again.";
	        } catch (DropboxParseException e) {
	            // Probably due to Dropbox server restarting, should retry
	            mErrorMsg = "Dropbox error.  Try again.";
	        } catch (DropboxException e) {
	            // Unknown error
	            mErrorMsg = "Unknown error.  Try again.";
	        }
			return null;
	        
	    }

		//add the items to list as they are fecthed.
		@Override
		protected void onProgressUpdate(String... values) {
			mAdapter.add(values[0]);
			
			
		}
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			
		}

   }

}
