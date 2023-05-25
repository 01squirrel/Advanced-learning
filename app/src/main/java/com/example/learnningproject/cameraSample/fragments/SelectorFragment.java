package com.example.learnningproject.cameraSample.fragments;

import static com.google.android.gms.common.util.ArrayUtils.contains;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnningproject.R;
import com.example.learnningproject.adapter.GenericListAdapter;
import com.google.android.gms.common.util.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectorFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private List<FormatItem> cameraList;
    CameraManager cameraManager;
    RecyclerView recyclerView;
    GenericListAdapter adapter;
    public SelectorFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SelectorFragment.
     */
    public static SelectorFragment newInstance(String param1, String param2) {
        SelectorFragment fragment = new SelectorFragment();
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
        cameraManager = (CameraManager) requireContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraList = enumerateCameras(cameraManager);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_selector, container, false);
        rootView.setTag("CAMERA_SELECTOR");
        recyclerView = rootView.findViewById(R.id.rv_camera);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GenericListAdapter(cameraList);
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter.setOnItemClickListener(new GenericListAdapter.ItemClickListener() {
            @Override
            public void onItemClicked(int position) {
                FormatItem item = cameraList.get(position);
                NavDirections directions = SelectorFragmentDirections.actionSelectorToCamera(item.cameraId, item.format);
                Navigation.findNavController(requireActivity(),R.id.fragment_container)
                        .navigate(directions);
            }

            @Override
            public void BindCallback(View view, List<Bitmap> data, int position) {

            }
        });
    }

    public String lensOrientationString(int value){
        String prefix = "Unknown";
        if(value == CameraCharacteristics.LENS_FACING_FRONT){
            prefix  = "FRONT";
        }else if(value == CameraCharacteristics.LENS_FACING_BACK){
            prefix = "BACK";
        }else if (value == CameraCharacteristics.LENS_FACING_EXTERNAL){
            prefix  = "EXTERNAL";
        }
        return prefix;
    }

    public List<FormatItem> enumerateCameras(CameraManager manager) throws CameraAccessException {
        List<FormatItem> items = new ArrayList<>();
        String[] cameraIds = manager.getCameraIdList();
        for (String id : cameraIds){
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
            String orientation = lensOrientationString(characteristics.get(CameraCharacteristics.LENS_FACING));
            int[] capabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
            int[] outputs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputFormats();
            if(ArrayUtils.contains(capabilities,CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE)){
                //所有的相机都必须支持JPEG格式
                items.add(new FormatItem(orientation+" JPEG "+ id,id, ImageFormat.JPEG));
                //输出支持raw功能的
                if(contains(capabilities,CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)
                && contains(outputs,ImageFormat.RAW_SENSOR)){
                    items.add(new FormatItem(orientation+" RAW "+id,id,ImageFormat.RAW_SENSOR));
                }
                if(contains(capabilities,CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT)
                && contains(outputs,ImageFormat.DEPTH_JPEG)){
                    items.add(new FormatItem(orientation + " DEPTH "+id,id,ImageFormat.DEPTH_JPEG));
                }
            }
        }
        return items;
    }
    public static class FormatItem {
        private String title;
        private String cameraId;
        private int format;

        public FormatItem(String title, String cameraId, int format) {
            this.title = title;
            this.cameraId = cameraId;
            this.format = format;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCameraId() {
            return cameraId;
        }

        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }

        public int getFormat() {
            return format;
        }

        public void setFormat(int format) {
            this.format = format;
        }
    }
}