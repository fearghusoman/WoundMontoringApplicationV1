package com.example.woundmontoringapplicationv1;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MonitorFragment extends Fragment {

    Button btn1, btn2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_monitor, container, false);

        btn1 = view.findViewById(R.id.button2);
        btn2 = view.findViewById(R.id.button3);

        btn1.setOnClickListener(new View.OnClickListener() {
            /**
             *
             * @param v
             */
            @Override
            public void onClick(View v) {
                Intent intentCapture = new Intent(getContext(), CaptureImageActivity.class);
                startActivity(intentCapture);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            /**
             *
             * @param v
             */
            @Override
            public void onClick(View v) {
                Intent intentProcess = new Intent(getContext(), ProcessImageActivity.class);
                startActivity(intentProcess);
            }
        });

        return view;
    }
}
