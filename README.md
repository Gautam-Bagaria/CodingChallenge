# CodingChallenge
This is a for a android coding challenge. The application is used to upload files to dropbox.
The apk is present for you to install directly.

ACTIVITIES 
-LoginActivity
This is the entry point of the application. The users will require to log into dropbox and give permission. Once authentication is successful users can take a photo or record audio and upload the files to dropbox.

-RecordAudioActivity
This is where the users will be navigated if they chose to record audio and then upload it to dropbox.

-ShowListActivity
This activity is used to fetch the list of image files that are available on dropbox.

ASYNC TASKS
-ShowImage
This task will fetch an image from dropbox and display it.

-UploadFile
This task will upload files to dropbox.

LAYOUT
-activity_layout
Simple layout with buttons to log in/out of dropbox, record audio, capture image, show image file list

-activity_record_audio
Simple layout with buttons to start audio recording, stop recording and upload the recording to dropbox.

-activty_show_list
Simple layout with listview and image view.



