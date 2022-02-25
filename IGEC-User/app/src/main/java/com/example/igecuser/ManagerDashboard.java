package com.example.igecuser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.example.igecuser.Fragments.VacationRequests;
import com.example.igecuser.Fragments.VacationsLog;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;

public class ManagerDashboard extends AppCompatActivity {

    // Views
    ViewPager viewPager;
    TabLayout tabLayout;

    // Vars
    VacationRequests vacationRequests;
    VacationsLog vacationsLog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        vacationRequests = new VacationRequests();
        vacationsLog = new VacationsLog();

        tabLayout.setupWithViewPager(viewPager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(vacationRequests, getString(R.string.vacation_requests));
        viewPagerAdapter.addFragment(vacationsLog, getString(R.string.vacations_log));
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_baseline_mail_24);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_baseline_history_24);

        BadgeDrawable badgeDrawable = tabLayout.getTabAt(0).getOrCreateBadge();
        badgeDrawable.setVisible(true);
        badgeDrawable.setNumber(10);


    }
}