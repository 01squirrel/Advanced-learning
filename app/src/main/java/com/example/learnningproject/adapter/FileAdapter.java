package com.example.learnningproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnningproject.R;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder>{
    private final String[] filenames;
    AdapterView.OnItemSelectedListener itemSelectedListener;
    public FileAdapter(String[] filenames) {
        this.filenames = filenames;
    }

   public void setOnItemClickListener(AdapterView.OnItemSelectedListener listener){
        this.itemSelectedListener = listener;
   }
    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return FileViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.bind(filenames[position]);
        itemSelectedListener.onItemSelected((AdapterView<?>) holder.itemView,holder.textView,position,position+1);
    }

    @Override
    public int getItemCount() {
        return filenames.length;
    }


    static class FileViewHolder extends RecyclerView.ViewHolder{
        private final TextView textView;
        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_item);
        }
        static FileViewHolder create(ViewGroup parent){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.camera_item,parent,false);
            return new FileViewHolder(view);
        }
        public void bind(String text){
            textView.setText(text);
        }
    }
}
