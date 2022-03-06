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
import com.example.igecuser.Fragments.VacationRequestFragment;
import com.example.igecuser.Fragments.VacationsLogFragment;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.google.android.material.navigation.NavigationView;

public class EmployeeDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Views
    private Toolbar vToolbar;
    private DrawerLayout vDrawerLayout;
    private NavigationView vNavigationView;

    // Vars
    private CheckInOutFragment checkInOutFragment;
    private VacationRequestFragment vacationRequest;
    private VacationsLogFragment vacationsLogFragment;
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
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, checkInOutFragment).commit();
            vNavigationView.setCheckedItem(R.id.item_CheckInOut);
        }


    }

    //Functions
    private void initialize() {
        vToolbar = findViewById(R.id.toolbar);
        vDrawerLayout = findViewById(R.id.drawer);
        vNavigationView = findViewById(R.id.navView);
        setSupportActionBar(vToolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, vDrawerLayout, vToolbar, R.string.openNavBar, R.string.closeNavBar);
        actionBarDrawerToggle.syncState();
        vNavigationView.setNavigationItemSelectedListener(this);

        vacationRequest = new VacationRequestFragment();
        checkInOutFragment = new CheckInOutFragment();
        vacationsLogFragment = new VacationsLogFragment(true);
        currEmployee = (Employee) getIntent().getSerializableExtra("emp");
        bundle = new Bundle();
        bundle.putSerializable("emp", currEmployee);
        vacationRequest.setArguments(bundle);
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
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, checkInOutFragment).commit();
                break;
            case R.id.item_VacationRequests:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, vacationRequest).commit();
                break;
            case R.id.item_VacationsLog:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, vacationsLogFragment).commit();
                break;
        }
        vDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

}