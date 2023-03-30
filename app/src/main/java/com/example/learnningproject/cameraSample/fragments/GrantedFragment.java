package com.example.learnningproject.cameraSample.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.learnningproject.R;

public class GrantedFragment extends Fragment {

    private final String[] permissions = {Manifest.permission.CAMERA};
    ActivityResultLauncher<String[]> launcher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        if(Boolean.TRUE.equals(result.get(Manifest.permission.CAMERA))){
            navigateToCamera();
        }else {
            Toast.makeText(requireContext(), "Permission request denied", Toast.LENGTH_LONG).show();
        }
    });
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(hasPermissions(requireContext())){
            //startActivity(new Intent(requireContext(), Camera2BasicActivity.class));
            navigateToCamera();
        }else{
            launcher.launch(permissions);
        }
    }
    public boolean hasPermissions(Context context){
        boolean granted = false;
        for(String permission : permissions){
            if( ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED)
                granted = true;
        }
        return granted;
    }
    private void navigateToCamera(){
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(R.id.action_permissions_to_selector);
    }
}
