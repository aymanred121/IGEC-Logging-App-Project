package com.example.igecuser.Activities;

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

import com.example.igecuser.Adapters.ViewPagerAdapter;
import com.example.igecuser.Fragments.ChangePasswordFragment;
import com.example.igecuser.Fragments.CheckInOutFragment;
import com.example.igecuser.Fragments.GrossSalaryFragment;
import com.example.igecuser.Fragments.ProjectSummaryFragment;
import com.example.igecuser.Fragments.SendTransferRequest;
import com.example.igecuser.Fragments.SendVacationRequestFragment;
import com.example.igecuser.Fragments.TransferRequestsFragment;
import com.example.igecuser.Fragments.VacationRequestsFragment;
import com.example.igecuser.Fragments.VacationsLogFragment;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.google.android.material.navigation.NavigationView;

public class ManagerDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar vToolbar;
    private ViewPager viewPager;
    private DrawerLayout vDrawerLayout;
    private NavigationView vNavigationView;
    // Vars
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Employee currManager;
    private int selectedTab = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);
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

    // Functions
    private void initialize() {
        currManager = (Employee) getIntent().getSerializableExtra("emp");


        // Views
        vToolbar = findViewById(R.id.toolbar);
        vDrawerLayout = findViewById(R.id.drawer);
        vNavigationView = findViewById(R.id.navView);
        viewPager = findViewById(R.id.fragment_container);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        CheckInOutFragment checkInOutFragment = new CheckInOutFragment(currManager);
        ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment(currManager);
        GrossSalaryFragment grossSalaryFragment = new GrossSalaryFragment(currManager.getId());
        SendVacationRequestFragment sendVacationRequestFragment = new SendVacationRequestFragment(currManager);
        VacationRequestsFragment vacationRequestsFragment = new VacationRequestsFragment(currManager);
        VacationsLogFragment vacationsLogFragment = new VacationsLogFragment(false, currManager);
        ProjectSummaryFragment projectSummaryFragment = new ProjectSummaryFragment(currManager);
        TransferRequestsFragment transferRequestsFragment = new TransferRequestsFragment(currManager);
        SendTransferRequest sendTransferRequest = new SendTransferRequest(currManager);


        viewPagerAdapter.addFragment(checkInOutFragment, getString(R.string.check_in_out));
        viewPagerAdapter.addFragment(changePasswordFragment, getString(R.string.change_password));
        viewPagerAdapter.addFragment(grossSalaryFragment, getString(R.string.gross_salary));
        viewPagerAdapter.addFragment(sendVacationRequestFragment, getString(R.string.send_vacation_request));
        viewPagerAdapter.addFragment(vacationRequestsFragment, getString(R.string.vacation_requests));
        viewPagerAdapter.addFragment(vacationsLogFragment, getString(R.string.vacations_log));
        viewPagerAdapter.addFragment(sendTransferRequest, getString(R.string.send_transfer_request));
        viewPagerAdapter.addFragment(transferRequestsFragment, getString(R.string.transfer_requests));
        viewPagerAdapter.addFragment(projectSummaryFragment, getString(R.string.project_summary));


        viewPager.setAdapter(viewPagerAdapter);
        vNavigationView.getMenu().getItem(0).setChecked(true);


        setSupportActionBar(vToolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, vDrawerLayout, vToolbar, R.string.openNavBar, R.string.closeNavBar);
        actionBarDrawerToggle.syncState();
        vNavigationView.setNavigationItemSelectedListener(this);

        TextView EmployeeName = vNavigationView.getHeaderView(0).findViewById(R.id.EmployeeName);
        TextView EmployeeID = vNavigationView.getHeaderView(0).findViewById(R.id.EmployeeID);

        EmployeeName.setText(String.format("%s %s", currManager.getFirstName(), currManager.getLastName()));
        EmployeeID.setText(String.format("Id: %s", currManager.getId()));

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
        if (itemId == R.id.nav_vacation_requests)
            viewPager.setCurrentItem(4, true);
        if (itemId == R.id.nav_vacations_log)
            viewPager.setCurrentItem(5, true);
        if (itemId == R.id.nav_send_transfer_request)
            viewPager.setCurrentItem(6, true);
        if (itemId == R.id.nav_transfer_requests)
            viewPager.setCurrentItem(7, true);
        if (itemId == R.id.nav_project_summary)
            viewPager.setCurrentItem(8, true);

    }

    public void validateDate(Context c) {
        if (Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) != 1) {
            Intent intent = new Intent(ManagerDashboard.this, SplashScreen_DateInaccurate.class);
            startActivity(intent);
            finish();
        }
    }
}