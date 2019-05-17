package com.example.woundmontoringapplicationv1;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.constraint_container_home,
                new HomeFragment()).commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                /**
                 *
                 * @param menuItem
                 * @return
                 */
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;

                    switch(menuItem.getItemId()){
                        case R.id.first_item:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.second_item:
                            selectedFragment = new MonitorFragment();
                            break;
                        case R.id.third_item:
                            selectedFragment = new YourDataFragment();
                            break;

                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.constraint_container_home,
                            selectedFragment).commit();

                    return true;
                }
            };
}
