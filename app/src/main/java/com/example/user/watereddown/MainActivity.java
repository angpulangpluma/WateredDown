package com.example.user.watereddown;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
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
    private file_aes master;

//    private Drop

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        master = new file_aes((aes)getIntent().getSerializableExtra("sys"));
        master = new file_aes(cryptoInit(new File("crypto.dat")));

//        cryptoInit(new File("crypto.dat"));

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

//        Log.w("sys", Boolean.toString(getIntent().getSerializableExtra("sys")!=null));
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

                try{

                    FullAccount account = drpBxManager.getAccount();
                    Log.w("success", account.getName().getDisplayName());

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

    private byte[] read(File file) throws IOException{
        byte[] buffer = new byte[(int) file.length()];
        InputStream ios = null;
        try{
            ios = new FileInputStream(file);
            if(ios.read(buffer)==-1){
                throw new IOException(
                        "EOF reached while trying to read the whole file.");
            }
        } finally{
            try {
                if (ios != null) ios.close();
            } catch (IOException e){
                Log.w("error", e.getMessage());
            }
        }
        return buffer;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.w("file", resultData.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
            File f = new File(resultData.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
            new UploadFiletoDropbox().execute(f.getAbsolutePath(), "/");
        }
    }


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
            checkPermissions(getApplicationContext());
            fileUri = strings[0];
            filePath = strings[1];
            Log.w("file", fileUri);
            Log.w("path", filePath);
            File f = createFileDuplicate(
                    Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS)
                            .toString(),
                    FilenameUtils.getBaseName(fileUri)+"_new",
                    fileUri);
            encryptFile(f);
//            decryptFile(f);
//            drpBxManager.init();
//            try{
//                drpBxManager.uploadFileToDropBox(fileUri, filePath);
//            } catch (Exception e){
//                Log.w("error", e.getMessage());
//            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer aInt) {
            super.onPostExecute(aInt);
            if (this.progressDialog.isShowing()) {
                this.progressDialog.dismiss();
            }


        }
    }

    private void encryptFile(File f){
        master.encryptFile(f);
    }

    private void decryptFile(File f){
        master.decryptFile(f);
    }

    private aes cryptoInit(File set) {
        checkPermissions(this);
//        File set = null;
//        OutputStream in = null;
//        DataOutputStream dos = null;
        set = createFile(this, "crypto.dat");
        aes master = null;
        if(set!=null){
            try{
                master = new aes();
                master.loadKey(set);
//                master.saveKey(master.getKey(), set);
//                in = new FileOutputStream(set);
//                dos = new DataOutputStream(in);
//                dos.write(master.getKey().getEncoded());
            } catch(Exception e){
                Log.w("error", e.getMessage());
            }
        }

        return master;
    }

    private File createFile(Context con, String newname){
        checkPermissions(this);
        File f = null;
        InputStream in;
        OutputStream out;
        boolean isFileUnlocked = false;
        try {
            f = con.getFileStreamPath(newname);
            if (!f.exists()) {
                if (f.createNewFile()) {
                    Log.w("file?", "new");
                    in = new FileInputStream(f);
                    out = new FileOutputStream(f);
                    if (IOUtils.copy(in, out)>0) {
                        Log.w("copy?", "yes");
                        out.close();
                        in.close();
                        if (f.canRead()) {
                            Log.w("read?", "yes");
                            try {
                                long lastmod = f.lastModified();
                                Log.w("last modified", Long.toString(lastmod));
                                org.apache.commons.io.FileUtils.touch(f);
                                isFileUnlocked = true;
                            } catch (IOException e) {
                                //                            isFileUnlocked = false;
                                Log.w("error", e.getMessage());
                            }
                        } else Log.w("read?", "no");
                    } else Log.w("copy?", "no");
                } else Log.w("file?", "no");
            } else Log.w("exists?", "yes");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    private File createFileDuplicate(String path, String newname, String oldfile){
//        fileUri = strings[0];
//        filePath = strings[1];
//        Log.w("file", fileUri);
//        Log.w("path", filePath);
        checkPermissions(this);
        File f = null;
        InputStream in;
        OutputStream out;
        boolean isFileUnlocked = false;
        try {
            f = new File(path, newname + "." +
                    FilenameUtils.getExtension(oldfile));
            if(!f.exists()) {
                if (f.createNewFile())
                    Log.w("file?", "new!");
            } else{
                Log.w("file?", "yes");
                in = new FileInputStream(new File(oldfile));
                out = new FileOutputStream(f);
                if (IOUtils.copy(in, out) > -1) {
                    Log.w("copy?", "yes");
                    out.close();
                    in.close();
                    Log.w("file length", Long.toString(f.length()));
                    if (f.canRead()) {
                        Log.w("exists?", "yes");
                        //try {
                            long lastmod = f.lastModified();
                            Log.w("last modified", Long.toString(lastmod));
//                            org.apache.commons.io.FileUtils.touch(f);
//                            isFileUnlocked = true;
                        //} catch (IOException e) {
                            //                            isFileUnlocked = false;
                            //Log.w("error", e.getMessage());
                        //}
                    } else Log.w("exists?", "no");
                } else Log.w("copy?", "no");
            }
                //            }
//            } else {
//                f = null;
//                Log.w("file?", "no");
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    private void checkPermissions(Context context){
        int readStuff = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeStuff = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        Log.w("read?", Integer.toString(readStuff));
//        Log.w("write?", Integer.toString(writeStuff));
        //for read stuff
        if(readStuff == PackageManager.PERMISSION_GRANTED)
            Log.w("read?", "yes");
        else if(readStuff == PackageManager.PERMISSION_DENIED)
            Log.w("read?", "no");

        //for write stuff
        if(writeStuff == PackageManager.PERMISSION_GRANTED)
            Log.w("write?", "yes");
        else if(writeStuff == PackageManager.PERMISSION_DENIED)
            Log.w("write?", "no");
    }



}
