package com.example.igecuser.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.igecuser.Fragments.CheckInOutFragment;
import com.example.igecuser.Fragments.VacationRequestsFragment;
import com.example.igecuser.Fragments.VacationsLogFragment;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.google.android.material.navigation.NavigationView;

public class ManagerDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Views
    private Toolbar vToolbar;
    private DrawerLayout vDrawerLayout;
    private NavigationView vNavigationView;

    // Vars
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private VacationRequestsFragment vacationRequestsFragment;
    private VacationsLogFragment vacationsLogFragment;
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
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, checkInOutFragment).commit();
            vNavigationView.setCheckedItem(R.id.item_CheckInOut);
        }


    }

    // Functions
    private void initialize() {
        vToolbar = findViewById(R.id.toolbar);
        vDrawerLayout = findViewById(R.id.drawer);
        vNavigationView = findViewById(R.id.navView);
        setSupportActionBar(vToolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, vDrawerLayout, vToolbar, R.string.openNavBar, R.string.closeNavBar);
        actionBarDrawerToggle.syncState();
        vNavigationView.setNavigationItemSelectedListener(this);

        vacationRequestsFragment = new VacationRequestsFragment();
        vacationsLogFragment = new VacationsLogFragment(false);
        checkInOutFragment = new CheckInOutFragment();

        currManager = (Employee) getIntent().getSerializableExtra("emp");
        bundle = new Bundle();
        bundle.putSerializable("emp", currManager);
        bundle.putSerializable("mgr", currManager);
        vacationRequestsFragment.setArguments(bundle);
        vacationsLogFragment.setArguments(bundle);
        checkInOutFragment.setArguments(bundle);
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
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, checkInOutFragment).commit();
                break;
            case R.id.item_VacationRequests:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, vacationRequestsFragment).commit();
                break;
            case R.id.item_VacationsLog:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, vacationsLogFragment).commit();
                break;
        }
        vDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}