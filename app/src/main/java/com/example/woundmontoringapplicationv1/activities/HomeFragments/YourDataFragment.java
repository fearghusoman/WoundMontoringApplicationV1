package com.example.woundmontoringapplicationv1.activities.HomeFragments;

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

import com.example.woundmontoringapplicationv1.R;
import com.example.woundmontoringapplicationv1.activities.MainActivities.HistoryActivity;
import com.example.woundmontoringapplicationv1.activities.LoginAndRegisterActivities.LogoutActivity;
import com.example.woundmontoringapplicationv1.activities.MainActivities.PersonalDetailsDataActivity;
import com.example.woundmontoringapplicationv1.activities.MainActivities.RegisteredDressingsActivity;
import com.example.woundmontoringapplicationv1.activities.MainActivities.RemindersActivity;

/**
 * This activity acts as a menu. It contains links to the various activities
 * where the user can view their details.
 * The logout button appears at the bottom of this activity.
 */
public class YourDataFragment extends Fragment {

    TextView reminderTV, historyTV, registeredDressingsTV, personalDetails;
    Button logout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_yourdata, container, false);

        logout = view.findViewById(R.id.button_logout);
        reminderTV = view.findViewById(R.id.reminders);
        historyTV = view.findViewById(R.id.historyTV);
        registeredDressingsTV = view.findViewById(R.id.registeredTV);
        personalDetails = view.findViewById(R.id.personalDetails);

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

        personalDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PersonalDetailsDataActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
