package com.example.user.watereddown;

import android.os.AsyncTask;
import android.os.Bundle;
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

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SystemSessionManager sysSessManager;

    private AppCompatActivity activity = MainActivity.this;
    private RecyclerView recyclerViewFiles;
    private List<User> listFiles;
    private FileRecyclerAdapter filesRecyclerAdapter;
    private DatabaseHelper dbHelper;

    private static final String ACCESS_TOKEN =
            "I9h5pIu_C2AAAAAAAAAAX-7E5iZmUqhT3n7Jo8MQ7M3c2fyzV6tyMQsdmecbwAoO";

//    private Drop

    @Override
    protected void onCreate(Bundle savedInstanceState) {

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        FloatingActionButton fl = (FloatingActionButton) findViewById(R.id.fab_logout);
        fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
             sysSessManager.logoutUser();
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
        dbHelper = new DatabaseHelper(activity);

        getData();
        initDropbox();
    }

    private void getData(){
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                listFiles.clear();
                dbHelper.openDatabase();
                listFiles.addAll(dbHelper.getAllUser());
                dbHelper.close();
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
                DbxRequestConfig config = new DbxRequestConfig("DrpBxWithEncryption", "en_US");
                DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

                try{
                    FullAccount account = client.users().getCurrentAccount();
//            System.out.println(account.getName().getDisplayName());
                    Log.w("success", account.getName().getDisplayName());
                } catch(Exception e){
                    Log.w("error", e.toString());
                }
                return null;
            }
        }.execute();

    }



}
