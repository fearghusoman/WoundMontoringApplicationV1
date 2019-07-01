package com.example.woundmontoringapplicationv1;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class YourDataFragment extends Fragment {

    TextView reminderTV, historyTV, registeredDressingsTV;
    Button logout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_yourdata, container, false);

        logout = view.findViewById(R.id.button_logout);
        reminderTV = view.findViewById(R.id.reminders);
        historyTV = view.findViewById(R.id.historyTV);
        registeredDressingsTV = view.findViewById(R.id.registeredTV);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), LogoutActivity.class);
                startActivity(intent);
            }
        });

        reminderTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RemindersActivity.class);
                startActivity(intent);
            }
        });

        historyTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), HistoryActivity.class);
                startActivity(intent);
            }
        });

        registeredDressingsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RegisteredDressingsActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
