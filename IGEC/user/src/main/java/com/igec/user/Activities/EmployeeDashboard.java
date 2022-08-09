package com.igec.user.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.igec.user.Adapters.ViewPagerAdapter;
import com.igec.user.Fragments.ChangePasswordFragment;
import com.igec.user.Fragments.CheckInOutFragment;
import com.igec.user.Fragments.GrossSalaryFragment;
import com.igec.user.Fragments.SendVacationRequestFragment;
import com.igec.user.Fragments.VacationsLogFragment;
import com.igec.user.R;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.google.android.material.navigation.NavigationView;

public class EmployeeDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar vToolbar;
    private DrawerLayout vDrawerLayout;
    private ViewPager viewPager;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView vNavigationView;
    private Employee currEmployee;
    private EmployeesGrossSalary employeesGrossSalary;

    private int selectedTab = 0;
    // Overrides
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);
        validateDate(this);
        initialize();
        // Listeners
        vDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        vDrawerLayout.addDrawerListener(drawerListener);
        viewPager.addOnPageChangeListener(viewPagerListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        validateDate(this);
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
        vDrawerLayout.closeDrawer(GravityCompat.START);
        selectedTab = item.getItemId();
        return true;
    }

    // Functions
    private void initialize() {

//        currEmployee = (Employee) getIntent().getSerializableExtra("emp");
//
//        // Views
//        vToolbar = findViewById(R.id.toolbar);
//        vDrawerLayout = findViewById(R.id.drawer);
//        vNavigationView = findViewById(R.id.navView);
//        viewPager = findViewById(R.id.fragment_container);
//
//        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
//        CheckInOutFragment checkInOutFragment = new CheckInOutFragment(currEmployee);
//        ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment(currEmployee);
//        GrossSalaryFragment grossSalaryFragment = new GrossSalaryFragment(currEmployee.getId());
//        SendVacationRequestFragment sendVacationRequestFragment = new SendVacationRequestFragment(currEmployee);
//        VacationsLogFragment vacationsLogFragment = new VacationsLogFragment(true, currEmployee);
//        viewPagerAdapter.addFragment(checkInOutFragment, getString(R.string.check_in_out));
//        viewPagerAdapter.addFragment(changePasswordFragment, getString(R.string.change_password));
//        viewPagerAdapter.addFragment(grossSalaryFragment, getString(R.string.gross_salary));
//        viewPagerAdapter.addFragment(sendVacationRequestFragment, getString(R.string.send_vacation_request));
//        viewPagerAdapter.addFragment(vacationsLogFragment, getString(R.string.vacations_log));
//
//        viewPager.setAdapter(viewPagerAdapter);
//        vNavigationView.getMenu().getItem(0).setChecked(true);
//
//
//        setSupportActionBar(vToolbar);
//        actionBarDrawerToggle = new ActionBarDrawerToggle(this, vDrawerLayout, vToolbar, R.string.openNavBar, R.string.closeNavBar);
//        actionBarDrawerToggle.syncState();
//        vNavigationView.setNavigationItemSelectedListener(this);
//
//
//        TextView EmployeeName = vNavigationView.getHeaderView(0).findViewById(R.id.EmployeeName);
//        TextView EmployeeID = vNavigationView.getHeaderView(0).findViewById(R.id.EmployeeID);
//
//        EmployeeName.setText(String.format("%s %s", currEmployee.getFirstName(), currEmployee.getLastName()));
//        EmployeeID.setText(String.format("Id: %s", currEmployee.getId()));
    }

    private void changeTab(int itemId) {
        if (itemId == R.id.nav_check_in_out)
            viewPager.setCurrentItem(0, true);
        if (itemId == R.id.nav_change_password)
            viewPager.setCurrentItem(1, true);
        if (itemId == R.id.nav_gross_salary)
            viewPager.setCurrentItem(2, true);
        if (itemId == R.id.nav_send_vacation_request)
            viewPager.setCurrentItem(3, true);
        if (itemId == R.id.nav_vacations_log)
            viewPager.setCurrentItem(4, true);
    }

    public void validateDate(Context c) {
        if (Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) != 1) {
            Intent intent = new Intent(EmployeeDashboard.this, SplashScreen_DateInaccurate.class);
            startActivity(intent);
            finish();
        }
    }
    //Listeners
    private final ViewPager.OnPageChangeListener viewPagerListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            vNavigationView.getMenu().getItem(position).setChecked(true);
            vToolbar.setTitle(vNavigationView.getMenu().getItem(position).getTitle());
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
    private final DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
            changeTab(selectedTab);
        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };
}