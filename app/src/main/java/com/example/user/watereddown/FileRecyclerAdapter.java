package com.example.user.watereddown;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by User on 10/20/2017.
 */

public class FileRecyclerAdapter extends RecyclerView.Adapter<FileRecyclerAdapter.FileViewHolder> {

//    private List<User> listUsers;
    private List<DBxFile> listFiles;

    public FileRecyclerAdapter(List<DBxFile> listFiles) {
        this.listFiles = listFiles;
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflating recycler item view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_recycler, parent, false);

        return new FileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
//        holder.textViewName.setText(listUsers.get(position).getName());
//        holder.textViewEmail.setText(listUsers.get(position).getEmail());
//        holder.textViewPassword.setText(listUsers.get(position).getPassword());
        holder.textViewFileName.setText(listFiles.get(position).getDBxFileName());
        holder.textViewFileId.setText(listFiles.get(position).getDBxFileId());
        holder.textViewFileSize.setText(String.format("%s", listFiles.get(position).getDBxFileSize()));
    }

    @Override
    public int getItemCount() {
        Log.v(FileRecyclerAdapter.class.getSimpleName(),""+listFiles.size());
        return listFiles.size();
    }

    public class FileViewHolder extends RecyclerView.ViewHolder {

//        public AppCompatTextView textViewName;
//        public AppCompatTextView textViewEmail;
//        public AppCompatTextView textViewPassword;
        public AppCompatTextView textViewFileName;
        public AppCompatTextView textViewFileId;
        public AppCompatTextView textViewFileSize;

        public FileViewHolder(View view) {
            super(view);
//            textViewName = (AppCompatTextView) view.findViewById(R.id.textViewName);
//            textViewEmail = (AppCompatTextView) view.findViewById(R.id.textViewEmail);
//            textViewPassword = (AppCompatTextView) view.findViewById(R.id.textViewPassword);
            textViewFileName = (AppCompatTextView) view.findViewById(R.id.textViewFileName);
            textViewFileId = (AppCompatTextView) view.findViewById(R.id.textViewFileId);
            textViewFileSize = (AppCompatTextView) view.findViewById(R.id.textViewFileSize);
        }
    }
}


