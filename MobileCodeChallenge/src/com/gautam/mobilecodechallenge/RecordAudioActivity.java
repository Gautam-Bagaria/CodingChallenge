package com.gautam.mobilecodechallenge;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;


import android.app.Activity;
import android.content.Context;

import android.media.MediaRecorder;


import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.ListView;
import android.widget.Toast;

/**
 * Provide user interface to record audio. Caputer the audio and allow for upload to dropbox.
 *
 */
public class RecordAudioActivity extends Activity {

	   private MediaRecorder mAudioRecorder;
	   private String outputFile = null;
	   private Button record;
	   private Button stop;
	   private Button upload_audio;
	   
	  
	   @Override
	   protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.activity_record_audio);
	      record = (Button)findViewById(R.id.audio_record_button);
	      stop = (Button)findViewById(R.id.audio_record_stop_button);
	      upload_audio = (Button)findViewById(R.id.audio_upload_button);
	     
	      stop.setEnabled(false);
	      upload_audio.setEnabled(false);
	
	      //onClickListeners for buttons in this class.
	      record.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				try {
					// get the date to create a filename
					  Date date = new Date();
			          DateFormat df = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss", Locale.US);
			          
			          //set the outfile path.
				      outputFile = Environment.getExternalStorageDirectory().
				      getAbsolutePath() + "/" +df.format(date)+".3gp";
				      
				      //create a MediaRecorder.
				      mAudioRecorder = new MediaRecorder();
				      mAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				      mAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				      mAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
				      mAudioRecorder.setOutputFile(outputFile);
				      
			          mAudioRecorder.prepare();
			          mAudioRecorder.start();
			      } catch (IllegalStateException e) {
			         e.printStackTrace();
			      } catch (IOException e) {
			         e.printStackTrace();
			      }
			      record.setEnabled(false);
			      stop.setEnabled(true);
			      Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
				
			}
		});
	      
	      stop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				  mAudioRecorder.stop();
			      mAudioRecorder.release();
			      mAudioRecorder  = null;
			      stop.setEnabled(false);
			      upload_audio.setEnabled(true);
			      Toast.makeText(getApplicationContext(), "Audio recorded successfully",
			      Toast.LENGTH_LONG).show();
				
			}
		});
	      
	      upload_audio.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Upload audio file to dropbox.
				try{	
					File file = new File(outputFile);
					UploadFile upload = new UploadFile(RecordAudioActivity.this, LoginActivity.mApi, "/Audio/", file);
			        upload.execute();
			        record.setEnabled(true);
							   
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
	 
			}
		});

	   }
	   
	   @Override
	   public boolean onCreateOptionsMenu(Menu menu) {
	      // Inflate the menu; this adds items to the action bar if it is present.
	     
	      return true;
	   }
}