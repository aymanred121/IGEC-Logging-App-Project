package com.example.igecuser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.example.igecuser.Fragments.CheckInOut;
import com.example.igecuser.Fragments.VacationRequest;
import com.google.android.material.tabs.TabLayout;

public class EmployeeDashboard extends AppCompatActivity {

    // Views
    ViewPager viewPager;
    TabLayout tabLayout;

    // Vars
    CheckInOut checkInOut;
    VacationRequest vacationRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        vacationRequest = new VacationRequest();
        checkInOut = new CheckInOut();

        tabLayout.setupWithViewPager(viewPager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(checkInOut, getString(R.string.check_in_out));
        viewPagerAdapter.addFragment(vacationRequest, getString(R.string.vacation_request));
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_baseline_access_time_24);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_baseline_near_me_24);

    }


}