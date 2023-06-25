package com.example.learnningproject.ui.main;

import androidx.databinding.DataBindingUtil;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;
import androidx.savedstate.SavedStateRegistry;
import androidx.savedstate.SavedStateRegistryOwner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.learnningproject.R;
import com.example.learnningproject.WordsActivity;
import com.example.learnningproject.base.BaseApplication;
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
        SaveManager manager = new SaveManager(this);
        manager.saveState();
        MainViewModel model = new ViewModelProvider(this).get(MainViewModel.class);
        Observer<String> observer = s -> Toast.makeText(getContext(),"data update--"+s,Toast.LENGTH_SHORT).show();
        model.getCurrentText().observe(getViewLifecycleOwner(),observer);//观察livedata对象
        binding.mbCamera2.setOnClickListener(view1 -> {
            startActivity(new Intent(getContext(),Camera2Activity.class));
        });
        binding.mbCamera2Basic.setOnClickListener(view2 ->{
            startActivity(new Intent(getContext(), Camera2BasicActivity.class));
        });
        binding.mbRoom.setOnClickListener(view3 ->{
            startActivity(new Intent(getContext(), WordsActivity.class));
            model.getCurrentText().setValue("new value");//更新livedata对象
        });
    }
}

/**
 * 通常，保存的实例状态中存储的数据是临时状态，根据用户的输入或导航而定。
 * 保存的实例状态：存储少量的数据，以便在系统停止界面后又重新创建时，用于轻松重新加载界面状态。
 * 这里不存储复杂对象，而是将复杂对象保留在本地存储空间中，并将这些对象的唯一 ID 存储在保存的实例状态 API 中
 */
class SaveManager implements SavedStateRegistry.SavedStateProvider {
    private static String PROVIDER = "save_manager";
    private static String QUERY = "query";
    private String query = null;

    public SaveManager(SavedStateRegistryOwner owner) {
        owner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_CREATE) {
                    SavedStateRegistry registry = owner.getSavedStateRegistry();
                    // Register this object for future calls to saveState()
                    registry.registerSavedStateProvider(PROVIDER, SaveManager.this);
                    // Get the previously saved state and restore it
                    Bundle state = registry.consumeRestoredStateForKey(PROVIDER);
                    // Apply the previously saved state
                    if (state != null) {
                        query = state.getString(QUERY);
                    }
                }
            }
        });
    }
    @NonNull
    @Override
    public Bundle saveState() {
        Bundle bundle = new Bundle();
        bundle.putString(QUERY,query);
        return bundle;
    }
}