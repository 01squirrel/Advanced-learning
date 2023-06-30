package com.example.learnningproject.cameraSample.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.learnningproject.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PermissionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PermissionFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private final String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION};

    public PermissionFragment() {
        // Required empty public constructor
    }

    ActivityResultLauncher<String[]> launcher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        if (Boolean.TRUE.equals(result.get(Manifest.permission.CAMERA))) {
            navigateToCamera();
        } else {
            Toast.makeText(requireContext(), "Permission request denied", Toast.LENGTH_LONG).show();
        }
    });
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PermissionFragment.
     */

    public static PermissionFragment newInstance(String param1, String param2) {
        PermissionFragment fragment = new PermissionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }

        if (hasPermissions(requireContext())) {
            //startActivity(new Intent(requireContext(), Camera2BasicActivity.class));
            navigateToCamera();
        } else if (shouldShowRequestPermissionRationale()) {

        } else {
            launcher.launch(permissions);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_permission, container, false);
    }

    public boolean hasPermissions(Context context) {
        boolean granted = false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
                granted = true;
        }
        return granted;
    }

    private void navigateToCamera() {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(R.id.action_permissions_to_selector);
    }
}