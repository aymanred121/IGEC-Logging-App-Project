package com.example.igecuser.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

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

    // Views
    private Toolbar vToolbar;
    private ViewPager viewPager;
    private DrawerLayout vDrawerLayout;
    private NavigationView vNavigationView;

    // Vars
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private VacationRequestsFragment vacationRequestsFragment;
    private VacationsLogFragment vacationsLogFragment ;
    private CheckInOutFragment checkInOutFragment;
    private Employee currManager;
    private Bundle bundle;

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
        currManager = (Employee) getIntent().getSerializableExtra("emp");


        vToolbar = findViewById(R.id.toolbar);
        vDrawerLayout = findViewById(R.id.drawer);
        vNavigationView = findViewById(R.id.navView);
        viewPager = findViewById(R.id.fragment_container);
        setSupportActionBar(vToolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, vDrawerLayout, vToolbar, R.string.openNavBar, R.string.closeNavBar);
        actionBarDrawerToggle.syncState();
        vNavigationView.setNavigationItemSelectedListener(this);

        checkInOutFragment = new CheckInOutFragment(currManager);
        vacationsLogFragment = new VacationsLogFragment(false, currManager);
        vacationRequestsFragment = new VacationRequestsFragment(currManager);

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
        switch (item.getItemId()) {
            case R.id.item_CheckInOut:
                viewPager.setCurrentItem(0);
                break;
            case R.id.item_VacationRequests:
                viewPager.setCurrentItem(1);
                break;
            case R.id.item_VacationsLog:
                viewPager.setCurrentItem(2);
                break;
        }
        vDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Listeners
}