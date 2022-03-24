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
import android.widget.TextView;


import com.example.igecuser.Adapters.ViewPagerAdapter;
import com.example.igecuser.Fragments.ChangePasswordFragment;
import com.example.igecuser.Fragments.CheckInOutFragment;
import com.example.igecuser.Fragments.GrossSalaryFragment;
import com.example.igecuser.Fragments.VacationRequestFragment;
import com.example.igecuser.Fragments.VacationsLogFragment;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.google.android.material.navigation.NavigationView;

public class EmployeeDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ViewPager viewPager;
    private DrawerLayout vDrawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    // Overrides
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);


        initialize();


        // Listeners
        vDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        viewPager.setOnTouchListener((v, event) -> true);

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
            viewPager.setCurrentItem(0);
        else if (R.id.item_VacationRequests == item.getItemId())
            viewPager.setCurrentItem(1);
        else if (R.id.item_VacationsLog == item.getItemId())
            viewPager.setCurrentItem(2);
        else if (R.id.item_ChangePassword == item.getItemId())
            viewPager.setCurrentItem(3);
        else if (R.id.item_GrossSalary == item.getItemId())
            viewPager.setCurrentItem(4);
        vDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Functions
    private void initialize() {

        Employee currEmployee = (Employee) getIntent().getSerializableExtra("emp");

        // Views
        Toolbar vToolbar = findViewById(R.id.toolbar);


        vDrawerLayout = findViewById(R.id.drawer);
        NavigationView vNavigationView = findViewById(R.id.navView);
        viewPager = findViewById(R.id.fragment_container);
        setSupportActionBar(vToolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, vDrawerLayout, vToolbar, R.string.openNavBar, R.string.closeNavBar);
        actionBarDrawerToggle.syncState();
        vNavigationView.setNavigationItemSelectedListener(this);


        TextView EmployeeName = vNavigationView.getHeaderView(0).findViewById(R.id.EmployeeName);
        TextView EmployeeID = vNavigationView.getHeaderView(0).findViewById(R.id.EmployeeID);

        EmployeeName.setText(String.format("%s %s", currEmployee.getFirstName(), currEmployee.getLastName()));
        EmployeeID.setText(String.format("Id: %s", currEmployee.getId()));

        //TODO: might be removed
        viewPager.setOffscreenPageLimit(2);
        // Vars
        CheckInOutFragment checkInOutFragment = new CheckInOutFragment(currEmployee);
        VacationRequestFragment vacationRequestFragment = new VacationRequestFragment(currEmployee);
        VacationsLogFragment vacationsLogFragment = new VacationsLogFragment(true, currEmployee);
        ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
        GrossSalaryFragment grossSalaryFragment = new GrossSalaryFragment();

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(checkInOutFragment, getString(R.string.check_in_out));
        viewPagerAdapter.addFragment(vacationRequestFragment, getString(R.string.vacation_request));
        viewPagerAdapter.addFragment(vacationsLogFragment, getString(R.string.vacations_log));
        viewPagerAdapter.addFragment(changePasswordFragment, getString(R.string.change_password));
        viewPagerAdapter.addFragment(grossSalaryFragment, getString(R.string.gross_salary));
        viewPager.setAdapter(viewPagerAdapter);
    }

    //Listeners

}