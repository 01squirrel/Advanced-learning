package com.example.learnningproject.adapter;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnningproject.R;
import com.example.learnningproject.cameraSample.fragments.SelectorFragment;

import java.util.List;

public class GenericListAdapter extends RecyclerView.Adapter<GenericListAdapter.ViewHolder> {
    private final List<SelectorFragment.FormatItem> cameraList;
    String TAG = "Camera Adapter";
    public GenericListAdapter(List<SelectorFragment.FormatItem> cameraList) {
        this.cameraList = cameraList;
    }

    public interface ItemClickListener{
        void onItemClicked(int position);
        void BindCallback(View view, List<Bitmap> data,int position);
    }
    ItemClickListener listener;
    public void setOnItemClickListener(ItemClickListener itemClickListener){
        this.listener = itemClickListener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.camera_item,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "Element " + position + " set.");
        holder.textView.setText(cameraList.get(position).getTitle());
        holder.textView.setOnClickListener(view -> {
            Log.d("CAMERA ITEM", "Element " + position+ " clicked.");
            listener.onItemClicked(position);
        });
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
    private final TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_item);
        }
    }

}
