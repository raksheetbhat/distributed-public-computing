package com.example.raksheet.majorproject.Storage;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.raksheet.majorproject.Database.DatabaseHandler;
import com.example.raksheet.majorproject.FileFunctions.FileUtils;
import com.example.raksheet.majorproject.Process.BeanService;
import com.example.raksheet.majorproject.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.util.List;

import static java.lang.Boolean.FALSE;

/**
 * Created by Raksheet on 07-11-2016.
 */

public class StorageActivity extends AppCompatActivity {
    private static final int STORAGE_FILE_CHOOSER = 1;
    private static final int READ_REQUEST_CODE = 2;
    private static final boolean DEBUG = FALSE;
    ImageView uploadImage;
    private String server = BeanService.server_url+"/FileUpload";
    private ProgressDialog fileUploadProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.storage_layout);

        //Toast.makeText(this,server,Toast.LENGTH_SHORT).show();

        uploadImage = (ImageView) findViewById(R.id.storage_file_choose);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("file/*");
//                startActivityForResult(intent, STORAGE_FILE_CHOOSER);
                performFileSearch();
            }
        });

        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        // Reading all contacts
        Log.d("Reading: ", "Reading all contacts..");
        List<StorageMaster> contacts = databaseHandler.getAllStorageUsers();

        for (StorageMaster cn : contacts) {
            String log = "Id: " + cn.getStorageID() + " ,fragment id: " + cn.getFragmentID() + " ,filename: " + cn.getFileName()
                    + " ,filesize" + cn.getFilesize();
            // Writing Contacts to log
            Log.d("Name: ", log);
        }
    }

    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        AlertDialog.Builder dialog = new AlertDialog.Builder(StorageActivity.this);
//        final ProgressDialog driveFileUploadProgress = new ProgressDialog(StorageActivity.this);
//        driveFileUploadProgress.setMessage("File is uploading");
//        driveFileUploadProgress.setIndeterminate(true);
//        driveFileUploadProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        driveFileUploadProgress.setCancelable(false);
//        driveFileUploadProgress.show();

//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                //Do something after 100ms
//                driveFileUploadProgress.dismiss();
//            }
//        }, 3000);

        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {

            // Get (file) URI of the vid from the return Intent's data
            Uri videoUri = data.getData();
            String selectedPath = "";
            isStoragePermissionGranted();

            //selectedPath = getRealPathFromURI(StorageActivity.this,videoUri);
            //String selectedPath = getRealPathFromURI(StorageActivity.this,videoUri);

            selectedPath = FileUtils.getPath(StorageActivity.this,videoUri);

            //System.out.println("real path: "+selectedPath);

            // Actually upload the video to the server
            new UploadFile().execute(selectedPath);
        }
    }


    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(StorageActivity.this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    public void dumpImageMetaData(Uri uri) {

        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        Cursor cursor = StorageActivity.this.getContentResolver()
                .query(uri, null, null, null, null, null);

        try {
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                String displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i("SA", "Display Name: " + displayName);
                System.out.println("display name: "+displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null.  But since an
                // int can't be null in Java, the behavior is implementation-specific,
                // which is just a fancy term for "unpredictable".  So as
                // a rule, check if it's null before assigning to an int.  This will
                // happen often:  The storage API allows for remote files, whose
                // size might not be locally known.
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                Log.i("SA", "Size: " + size);
            }
        } finally {
            cursor.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v("StorageActivity","Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    private String getPath(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getRealPathFromURI(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //Log.v(TAG,"Permission is granted");
                return true;
            } else {

                //Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            //Log.v(TAG,"Permission is granted");
            return true;
        }
    }

//    private void uploadVideo(String videoPath) {
//        try
//        {
//            HttpClient client = new DefaultHttpClient();
//            File file = new File(videoPath);
//            HttpPost post = new HttpPost(server +"/api/docfile/");
//
//            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
//            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//            entityBuilder.addBinaryBody("uploadfile", file);
//            // add more key/value pairs here as needed
//
//            HttpEntity entity = entityBuilder.build();
//            post.setEntity(entity);
//
//            HttpResponse response = client.execute(post);
//            HttpEntity httpEntity = response.getEntity();
//
//            Log.v("result", EntityUtils.toString(httpEntity));
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();
//        }
//    }

    class UploadFile extends AsyncTask<String,Void,String>{
        String result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            fileUploadProgress = new ProgressDialog(StorageActivity.this);
            fileUploadProgress.setMessage("File is uploading");
            fileUploadProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            fileUploadProgress.setCancelable(false);
            fileUploadProgress.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try
            {
                HttpClient client = new DefaultHttpClient();
                File file = new File(params[0]);
                HttpPost post = new HttpPost(server);

                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addBinaryBody("uploadfile", file);
                // add more key/value pairs here as needed



                HttpEntity entity = entityBuilder.build();
                post.setEntity(entity);

                HttpResponse response = client.execute(post);
                HttpEntity httpEntity = response.getEntity();

                Log.v("result", EntityUtils.toString(httpEntity));

                result = EntityUtils.toString(httpEntity);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(fileUploadProgress.isShowing())fileUploadProgress.dismiss();
            //Toast.makeText(StorageActivity.this,result,Toast.LENGTH_SHORT).show();
        }
    }
}
