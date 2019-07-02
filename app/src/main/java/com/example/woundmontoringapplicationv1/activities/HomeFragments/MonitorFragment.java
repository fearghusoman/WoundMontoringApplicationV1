package com.example.woundmontoringapplicationv1.activities.HomeFragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.woundmontoringapplicationv1.R;
import com.example.woundmontoringapplicationv1.activities.ImageProcessingActivities.CaptureImageActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MonitorFragment extends Fragment {

    FloatingActionButton btn1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_monitor, container, false);

        btn1 = view.findViewById(R.id.button2);

        btn1.setOnClickListener(new View.OnClickListener() {
            /**
             *
             * @param v
             */
            @Override
            public void onClick(View v) {
                Intent intentCapture = new Intent(getContext(), CaptureImageActivity.class);
                intentCapture.putExtra("CALLING_ACTIVITY", "ProcessNewImage");
                startActivity(intentCapture);
            }
        });

        return view;
    }
}
