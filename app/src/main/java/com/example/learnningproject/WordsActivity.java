package com.example.learnningproject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.learnningproject.adapter.WordListAdapter;
import com.example.learnningproject.database.WordViewModel;
import com.example.learnningproject.database.entity.Word;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class WordsActivity extends AppCompatActivity {

    private WordViewModel mWordViewModel;
    public static final int NEW_WORD_ACTIVITY_REQUEST_CODE = 1;
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == RESULT_OK && result.getData() != null){
            Word word = new Word(result.getData().getStringExtra(NewWordActivity.EXTRA_REPLY));
            mWordViewModel.insert(word);
        }else{
            Toast.makeText(
                    getApplicationContext(),
                    R.string.empty_not_saved,
                    Toast.LENGTH_LONG).show();
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final WordListAdapter adapter = new WordListAdapter(new WordListAdapter.WordDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Use ViewModelProvider to associate ViewModel with Activity.
        mWordViewModel = new ViewModelProvider(this).get(WordViewModel.class);
        // Update the cached copy of the words in the adapter.
        mWordViewModel.getAllWords().observe(this, adapter::submitList);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener( view -> {
            Intent intent = new Intent(this, NewWordActivity.class);
            //startActivityForResult(intent, NEW_WORD_ACTIVITY_REQUEST_CODE);
            resultLauncher.launch(intent);
        });

    }
}