package com.example.user.watereddown;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

public class MainActivity extends AppCompatActivity {

//    private final int READ_REQUEST_CODE = 42;

    private SystemSessionManager sysSessManager;

    private AppCompatActivity activity = MainActivity.this;
    private RecyclerView recyclerViewFiles;
    private List<DBxFile> listFiles;
    private FileRecyclerAdapter filesRecyclerAdapter;
//    private DatabaseHelper dbHelper;
    private DropboxManager drpBxManager;

//    private Drop

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        sysSessManager = new SystemSessionManager(getApplicationContext());
        if(sysSessManager.checkLogin())
            finish();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fa = (FloatingActionButton) findViewById(R.id.fab_add);
        fa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                selectFile();
            }
        });

        FloatingActionButton fl = (FloatingActionButton) findViewById(R.id.fab_logout);
        fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
             sysSessManager.logoutUser();
             drpBxManager.close();
            }
        });

        Button bRefresh = (Button) findViewById(R.id.refresh_btn);
        bRefresh.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                getData();
            }
        });

        recyclerViewFiles = (RecyclerView) findViewById(R.id.recyclerViewFiles);
        listFiles = new ArrayList<>();
        filesRecyclerAdapter = new FileRecyclerAdapter(listFiles);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewFiles.setLayoutManager(mLayoutManager);
        recyclerViewFiles.setItemAnimator(new DefaultItemAnimator());
        recyclerViewFiles.setHasFixedSize(true);
        recyclerViewFiles.setAdapter(filesRecyclerAdapter);
//        dbHelper = new DatabaseHelper(activity);

        drpBxManager = new DropboxManager("I9h5pIu_C2AAAAAAAAAAX-7E5iZmUqhT3n7Jo8MQ7M3c2fyzV6tyMQsdmecbwAoO", "DrpBxWithEncryption");

        initDropbox();
        getData();
    }

    private void getData(){
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                listFiles.clear();
//                dbHelper.openDatabase();
//                listFiles.addAll(dbHelper.getAllUser());
//                dbHelper.close();
                drpBxManager.init();
                try {
                    FullAccount account = drpBxManager.getAccount();
                    Log.w("success", account.getName().getDisplayName());
                    List<DBxFile> list = drpBxManager.getFullListOfFiles("");
                    if (list.size()>0) listFiles.addAll(list);
                } catch (DbxException e) {
                    Log.w("error", e.toString());
                }
//                drpBxManager.close();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid){
                super.onPostExecute(aVoid);
                filesRecyclerAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private void initDropbox(){

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                drpBxManager.init();
//                DbxRequestConfig config = new DbxRequestConfig("DrpBxWithEncryption", "en_US");
//                DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

                try{
//                    FullAccount account = client.users().getCurrentAccount();
//            System.out.println(account.getName().getDisplayName());
                    FullAccount account = drpBxManager.getAccount();
                    Log.w("success", account.getName().getDisplayName());

//                    ListFolderResult result = drpBxManager.getListOfFiles("");
//                    while(true){
//                        for(Metadata metadata : result.getEntries()){
////                            System.out.println(metadata.getPathLower());
//                            Log.w("list", metadata.getPathLower());
//                        }
//
//                        if (!result.getHasMore())
//                            break;
//                    }
//                drpBxManager.close();
                } catch(Exception e){
                    Log.w("error", e.toString());
                }
                return null;
            }
        }.execute();

    }

    private void selectFile(){
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("*/*");
//        startActivityForResult(intent, READ_REQUEST_CODE);
        Intent intent = new Intent(this, FilePickerActivity.class);
        intent.putExtra(FilePickerActivity.ARG_CLOSEABLE, true);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.w("file", resultData.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
            File f = new File(resultData.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
            new UploadFiletoDropbox().execute(f.getAbsolutePath(), "/");
        }
//        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            Uri uri = null;
//            if (resultData != null) {
//                uri = resultData.getData();
//                try {
//                    String result = getRealPathFromURI(uri);
//                    Log.w("result", result);
//                } catch (Exception e){
//                    String error = e.getLocalizedMessage();
//                    Log.w("error", error);
//                }
//                Log.w("result", result);
//                try {
//                    File f = new File(getRealPathFromURI(uri));
//                    if(f.exists())
//                        Log.w("file", f.getPath());
//                    new UploadFiletoDropbox().execute(f.getAbsolutePath(), "/");
//                } catch(Exception e){
//                    Log.w("error", e.getMessage());
//                }
    }

//    }

//    private String getFileExtFromURI(Uri contentUri){
////        String ext = "";
//
////        if (contentUri.toString().contains("."))
////            ext = contentUri.toString()
////                    .substring(contentUri.toString().lastIndexOf("."));
////        ext = contentUri.
//        return getContentResolver().getType(contentUri);
//    }

//    private String getRealPathFromURI(Uri contentUri)
//        throws IOException, NullPointerException {
//        Log.w("uri", contentUri.toString());
//        String result = "";
//        String ext = getFileExtFromURI(contentUri);
////        Log.w("ext", ext);
//        File f = new File(
//                Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
//                "input");
//        InputStream in = getContentResolver().openInputStream(contentUri);
//        OutputStream out = new FileOutputStream(f);
//        if (in!=null)
//            IOUtils.copy(in, out);
//        else throw new NullPointerException("cannot copy file");
//        result = f.getPath();
////            String[] proj = { MediaStore.Images.Media.DATA };
////            Cursor cursor = getContentResolver().query(contentUri,  proj, null, null, null);
////            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
////            cursor.moveToFirst();
////        } finally {
////            if (cursor != null) {
////                cursor.close();
////            }
////        result = cursor.getString(column_index);
////        cursor.close();
//        return result;
////        }
//    }

    private class UploadFiletoDropbox extends AsyncTask<String, Void, Integer> {

        transient ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        String fileUri, filePath;

        @Override
        protected void onPreExecute() {
            this.progressDialog.setMessage("Uploading file to server...");
            this.progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            //put code to upload file through dropbox here
            fileUri = strings[0];
            filePath = strings[1];
            Log.w("file", fileUri);
            Log.w("path", filePath);
            drpBxManager.init();
            try{
                drpBxManager.uploadFileToDropBox(fileUri, filePath);
            } catch (Exception e){
                Log.w("error", e.getMessage());
            }
            return null;
//            return savePatientInfo();
        }

        @Override
        protected void onPostExecute(Integer aInt) {
            super.onPostExecute(aInt);
            if (this.progressDialog.isShowing()) {
                this.progressDialog.dismiss();
            }

//            Intent intent = new Intent(UpdatePatientRecordActivity.this, ViewPatientActivity.class);
//            intent.putExtra("sys", getIntent().getSerializableExtra("sys"));
//            intent.putExtra("patientId", patientId);
//            startActivity(intent);
//            finish();

        }
    }



}
