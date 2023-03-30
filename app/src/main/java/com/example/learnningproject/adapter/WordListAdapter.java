package com.example.learnningproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnningproject.R;
import com.example.learnningproject.database.entity.Word;

public class WordListAdapter extends ListAdapter<Word, WordListAdapter.WordViewHolder> {
    public WordListAdapter(@NonNull DiffUtil.ItemCallback<Word> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return WordViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word current = getItem(position);
        holder.bind(current.getWord());
    }


    static class WordViewHolder extends RecyclerView.ViewHolder{
        private final TextView wordItemView;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            wordItemView = itemView.findViewById(R.id.textview);
        }

        public void bind(String text){
            wordItemView.setText(text);
        }
        static WordViewHolder create(ViewGroup parent){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item,parent,false);
            return new WordViewHolder(view);
        }
    }

    public static class WordDiff extends DiffUtil.ItemCallback<Word>{

        @Override
        public boolean areItemsTheSame(@NonNull Word oldItem, @NonNull Word newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Word oldItem, @NonNull Word newItem) {
            return oldItem.getWord().equals(newItem.getWord());
        }
    }
}
