package com.example.igecuser.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.igecuser.Adapters.ViewPagerAdapter;
import com.example.igecuser.Fragments.CheckInOutFragment;
import com.example.igecuser.Fragments.VacationRequestsFragment;
import com.example.igecuser.Fragments.VacationsLogFragment;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.google.android.material.navigation.NavigationView;

public class ManagerDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ViewPager viewPager;
    private DrawerLayout vDrawerLayout;

    // Vars
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);


        initialize();

        // Listeners
        vDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        viewPager.setOnTouchListener((v, event) -> true);

    }

    // Functions
    private void initialize() {
        Employee currManager = (Employee) getIntent().getSerializableExtra("emp");


        // Views
        Toolbar vToolbar = findViewById(R.id.toolbar);
        vDrawerLayout = findViewById(R.id.drawer);
        NavigationView vNavigationView = findViewById(R.id.navView);
        viewPager = findViewById(R.id.fragment_container);
        setSupportActionBar(vToolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, vDrawerLayout, vToolbar, R.string.openNavBar, R.string.closeNavBar);
        actionBarDrawerToggle.syncState();
        vNavigationView.setNavigationItemSelectedListener(this);

        CheckInOutFragment checkInOutFragment = new CheckInOutFragment(currManager);
        VacationsLogFragment vacationsLogFragment = new VacationsLogFragment(false, currManager);
        VacationRequestsFragment vacationRequestsFragment = new VacationRequestsFragment(currManager);

        viewPager.setOffscreenPageLimit(2);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(checkInOutFragment, getString(R.string.check_in_out));
        viewPagerAdapter.addFragment(vacationRequestsFragment, getString(R.string.vacation_request));
        viewPagerAdapter.addFragment(vacationsLogFragment, getString(R.string.vacations_log));
        viewPager.setAdapter(viewPagerAdapter);

    }

    @Override
    public void onBackPressed() {
        if (vDrawerLayout.isDrawerOpen(GravityCompat.START))
            vDrawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.item_CheckInOut) {
            viewPager.setCurrentItem(0);
        } else if (itemId == R.id.item_VacationRequests) {
            viewPager.setCurrentItem(1);
        } else if (itemId == R.id.item_VacationsLog) {
            viewPager.setCurrentItem(2);
        }
        vDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Listeners
}