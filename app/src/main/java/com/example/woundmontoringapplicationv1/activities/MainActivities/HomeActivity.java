package com.example.woundmontoringapplicationv1.activities.MainActivities;

import androidx.annotation.NonNull;

import com.example.woundmontoringapplicationv1.activities.HomeFragments.HomeFragment;
import com.example.woundmontoringapplicationv1.activities.HomeFragments.MonitorFragment;
import com.example.woundmontoringapplicationv1.R;
import com.example.woundmontoringapplicationv1.activities.HomeFragments.YourDataFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Bundle bundle;

    View homeView, monitorView, yourDataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        homeView = findViewById(R.id.homeview);
        monitorView = findViewById(R.id.monitorview);
        yourDataView = findViewById(R.id.yourdataview);

        bundle = getIntent().getExtras();

        if(bundle != null){
            if(bundle.getString("message").equalsIgnoreCase("yourdatafragment")){
                getSupportFragmentManager().beginTransaction().replace(R.id.constraint_container_home,
                        new YourDataFragment()).commit();
                homeView.setVisibility(View.INVISIBLE);
                monitorView.setVisibility(View.INVISIBLE);
                yourDataView.setVisibility(View.VISIBLE);
            }
        }else{
            getSupportFragmentManager().beginTransaction().replace(R.id.constraint_container_home,
                    new HomeFragment()).commit();
            homeView.setVisibility(View.VISIBLE);
            monitorView.setVisibility(View.INVISIBLE);
            yourDataView.setVisibility(View.INVISIBLE);
        }
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
                            homeView.setVisibility(View.VISIBLE);
                            monitorView.setVisibility(View.INVISIBLE);
                            yourDataView.setVisibility(View.INVISIBLE);
                            break;
                        case R.id.second_item:
                            selectedFragment = new MonitorFragment();
                            monitorView.setVisibility(View.VISIBLE);
                            yourDataView.setVisibility(View.INVISIBLE);
                            homeView.setVisibility(View.INVISIBLE);
                            break;
                        case R.id.third_item:
                            selectedFragment = new YourDataFragment();
                            homeView.setVisibility(View.INVISIBLE);
                            monitorView.setVisibility(View.INVISIBLE);
                            yourDataView.setVisibility(View.VISIBLE);
                            break;

                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.constraint_container_home,
                            selectedFragment).commit();

                    return true;
                }
            };

    /**
     * override the onBackPressed method to disallow the back button
     */
    @Override
    public void onBackPressed() {

        if(homeView.getVisibility() == View.VISIBLE){
            moveTaskToBack(false);
        }
        else{
            moveTaskToBack(true);
        }

    }
}
