package com.example.learnningproject.ui.main;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.learnningproject.R;
import com.example.learnningproject.WordsActivity;
import com.example.learnningproject.cameraSample.Camera2Activity;
import com.example.learnningproject.cameraSample.Camera2BasicActivity;
import com.example.learnningproject.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {

    public static MainFragment newInstance() {
        return new MainFragment();
    }
    FragmentMainBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_main,null, true);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.mbCamera2.setOnClickListener(view1 -> {
           startActivity(new Intent(getContext(),Camera2Activity.class));
        });
        binding.mbCamera2Basic.setOnClickListener(view2 ->{
            startActivity(new Intent(getContext(), Camera2BasicActivity.class));
        });
        binding.mbRoom.setOnClickListener(view3 ->{
            startActivity(new Intent(getContext(), WordsActivity.class));
        });
    }
}