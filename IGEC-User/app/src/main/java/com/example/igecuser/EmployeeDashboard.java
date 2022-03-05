package com.example.igecuser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.igecuser.Fragments.CheckInOut;
import com.example.igecuser.Fragments.VacationRequest;
import com.example.igecuser.Fragments.VacationsLog;
import com.example.igecuser.fireBase.Employee;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

public class EmployeeDashboard extends AppCompatActivity {

    // Views
    ViewPager viewPager;
    TabLayout tabLayout;
    MaterialButton sendRequestButton;
    // Vars
    CheckInOut checkInOut;
    VacationRequest vacationRequest;
    VacationsLog vacationsLog;
    Employee currEmployee;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        vacationRequest = new VacationRequest();
        checkInOut = new CheckInOut();
        vacationsLog = new VacationsLog(true);
        currEmployee = (Employee) getIntent().getSerializableExtra("emp");
        bundle = new Bundle();
        bundle.putSerializable("emp", currEmployee);
        vacationRequest.setArguments(bundle);
        vacationsLog.setArguments(bundle);
        checkInOut.setArguments(bundle);
        // TODO: vacationsLog doesn't update correctly
        vacationsLog.setArguments(bundle);
        tabLayout.setupWithViewPager(viewPager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(checkInOut, getString(R.string.check_in_out));
        viewPagerAdapter.addFragment(vacationRequest, getString(R.string.vacation_request));
        viewPagerAdapter.addFragment(vacationsLog, getString(R.string.vacations_log));
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_baseline_access_time_24);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_baseline_near_me_24);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_baseline_history_24);

    }
    //listener


}