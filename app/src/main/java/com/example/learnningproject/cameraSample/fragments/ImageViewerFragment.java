package com.example.learnningproject.cameraSample.fragments;

import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.learnningproject.R;
import com.example.learnningproject.util.ExifUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ImageViewerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageViewerFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public ImageViewerFragment() {
    }
    private final String TAG = ImageViewerFragment.class.getSimpleName();

    /** These are the magic numbers used to separate the different JPG data chunks */
    private final List<Integer> JPEG_DELIMITER_BYTES = new ArrayList<>();


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ImageViewerFragment.
     */
    public static ImageViewerFragment newInstance(String param1, String param2) {
        ImageViewerFragment fragment = new ImageViewerFragment();
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
        for(int i = -1;i>=-39;i--){
            JPEG_DELIMITER_BYTES.add(i);
        }
        com.example.learnningproject.cameraSample.fragments.ImageViewerFragmentArgs args = new ImageViewerFragmentArgs();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        // Keep Bitmaps at less than 1 MP
        // 1MP
        int DOWNSAMPLE_SIZE = 1024;
        if(Math.max(options.outHeight, options.outWidth) > DOWNSAMPLE_SIZE){
            int scaleFactorX = options.outWidth / DOWNSAMPLE_SIZE +1;
            int scaleFactorY = options.outHeight / DOWNSAMPLE_SIZE +1;
            options.inSampleSize = Math.max(scaleFactorX,scaleFactorY);
        }
        boolean isDepth = args.getDepth();
        Matrix bitmapTransformation = new Matrix();
        bitmapTransformation.set(ExifUtils.decodeExifOrientation(args.getOrientation()));
        ImageView imageView = new ImageView(requireContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_viewer, container, false);
    }
    /**
     * Utility function used to find the markers indicating separation between JPEG data chunks
     */
    private int findNextJpegEndMarker(byte[] jpegBuffer,int start){
        // Sanitize input arguments
        int result = -1;
        if(start < 0) {
            Log.d(TAG, "findNextJpegEndMarker: Invalid start marker");
            return result;
        }
        if(jpegBuffer.length < start){
            Log.d(TAG, "findNextJpegEndMarker: Buffer size ("+jpegBuffer.length+") smaller than start marker ("+start+")");
            return result;
        }
        for(int i = start;i<jpegBuffer.length;i++){
            if(jpegBuffer[i] == JPEG_DELIMITER_BYTES.get(0) && jpegBuffer[i+1] == JPEG_DELIMITER_BYTES.get(1)){
                result = i+2;
            }
        }
        return result;
    }
}