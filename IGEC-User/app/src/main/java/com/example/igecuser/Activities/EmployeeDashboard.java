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
import android.view.MotionEvent;
import android.view.View;

import com.example.igecuser.Adapters.ViewPagerAdapter;
import com.example.igecuser.Fragments.CheckInOutFragment;
import com.example.igecuser.Fragments.VacationRequestFragment;
import com.example.igecuser.Fragments.VacationsLogFragment;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.google.android.material.navigation.NavigationView;

public class EmployeeDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Views
    private Toolbar vToolbar;
    private ViewPager viewPager;
    private DrawerLayout vDrawerLayout;
    private NavigationView vNavigationView;

    // Vars
    private CheckInOutFragment checkInOutFragment = new CheckInOutFragment();
    private VacationRequestFragment vacationRequestFragment = new VacationRequestFragment();
    private VacationsLogFragment vacationsLogFragment = new VacationsLogFragment(true);
    private Employee currEmployee;
    private Bundle bundle;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);


        initialize();


        // Listeners
        vDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        viewPager.setOnTouchListener((v, event) -> true);

    }

    //Functions
    private void initialize() {
        vToolbar = findViewById(R.id.toolbar);
        vDrawerLayout = findViewById(R.id.drawer);
        vNavigationView = findViewById(R.id.navView);
        viewPager = findViewById(R.id.fragment_container);
        setSupportActionBar(vToolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, vDrawerLayout, vToolbar, R.string.openNavBar, R.string.closeNavBar);
        actionBarDrawerToggle.syncState();
        vNavigationView.setNavigationItemSelectedListener(this);



        viewPager.setOffscreenPageLimit(2);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(checkInOutFragment, getString(R.string.check_in_out));
        viewPagerAdapter.addFragment(vacationRequestFragment, getString(R.string.vacation_request));
        viewPagerAdapter.addFragment(vacationsLogFragment, getString(R.string.vacations_log));
        viewPager.setAdapter(viewPagerAdapter);

        currEmployee = (Employee) getIntent().getSerializableExtra("emp");
        bundle = new Bundle();
        bundle.putSerializable("emp", currEmployee);
        vacationRequestFragment.setArguments(bundle);
        vacationsLogFragment.setArguments(bundle);
        checkInOutFragment.setArguments(bundle);
        vacationsLogFragment.setArguments(bundle);
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

}