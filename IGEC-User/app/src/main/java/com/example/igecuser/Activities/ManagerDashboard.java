package com.example.igecuser.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.igecuser.Fragments.ChangePasswordFragment;
import com.example.igecuser.Fragments.CheckInOutFragment;
import com.example.igecuser.Fragments.GrossSalaryFragment;
import com.example.igecuser.Fragments.VacationRequestsFragment;
import com.example.igecuser.Fragments.VacationsLogFragment;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.google.android.material.navigation.NavigationView;

public class ManagerDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout vDrawerLayout;
    private NavigationView vNavigationView;
    // Vars
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Employee currManager;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);


        initialize();
        // Listeners
        vDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CheckInOutFragment(currManager)).commit();
            vNavigationView.setCheckedItem(R.id.item_CheckInOut);
        }
    }

    // Functions
    private void initialize() {
        currManager = (Employee) getIntent().getSerializableExtra("emp");


        // Views
        Toolbar vToolbar = findViewById(R.id.toolbar);
        vDrawerLayout = findViewById(R.id.drawer);
        vNavigationView = findViewById(R.id.navView);
        setSupportActionBar(vToolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, vDrawerLayout, vToolbar, R.string.openNavBar, R.string.closeNavBar);
        actionBarDrawerToggle.syncState();
        vNavigationView.setNavigationItemSelectedListener(this);

        TextView EmployeeName = vNavigationView.getHeaderView(0).findViewById(R.id.EmployeeName);
        TextView EmployeeID = vNavigationView.getHeaderView(0).findViewById(R.id.EmployeeID);

        EmployeeName.setText(String.format("%s %s", currManager.getFirstName(), currManager.getLastName()));
        EmployeeID.setText(String.format("Id: %s", currManager.getId()));

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
        if (R.id.item_CheckInOut == item.getItemId())
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CheckInOutFragment(currManager)).commit();
        else if (R.id.item_Vacation_Requests == item.getItemId())
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VacationRequestsFragment(currManager)).commit();
        else if (R.id.item_VacationsLog == item.getItemId())
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VacationsLogFragment(true,currManager)).commit();
        else if (R.id.item_ChangePassword == item.getItemId())
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ChangePasswordFragment()).commit();
        else if (R.id.item_GrossSalary == item.getItemId())
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new GrossSalaryFragment()).commit();
        vDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Listeners
}