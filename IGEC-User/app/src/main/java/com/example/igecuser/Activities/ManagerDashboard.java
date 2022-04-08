package com.example.igecuser.Activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
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


        initialize();
        // Listeners
        vDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        vDrawerLayout.addDrawerListener(drawerListener);
        viewPager.addOnPageChangeListener(viewPagerListener);
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
        ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
        GrossSalaryFragment grossSalaryFragment = new GrossSalaryFragment();
        VacationRequestsFragment vacationRequestsFragment = new VacationRequestsFragment(currManager);
        VacationsLogFragment vacationsLogFragment = new VacationsLogFragment(false, currManager);
        ProjectSummaryFragment projectSummaryFragment = new ProjectSummaryFragment(currManager);


        viewPagerAdapter.addFragment(checkInOutFragment, getString(R.string.check_in_out));
        viewPagerAdapter.addFragment(changePasswordFragment, getString(R.string.change_password));
        viewPagerAdapter.addFragment(grossSalaryFragment, getString(R.string.gross_salary));
        viewPagerAdapter.addFragment(vacationRequestsFragment, getString(R.string.vacation_requests));
        viewPagerAdapter.addFragment(projectSummaryFragment, getString(R.string.ProjectSummary));
        viewPagerAdapter.addFragment(vacationsLogFragment, getString(R.string.vacations_log));

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
        if (itemId == R.id.item_CheckInOut)
            viewPager.setCurrentItem(0, true);
        if (itemId == R.id.item_ChangePassword)
            viewPager.setCurrentItem(1, true);
        if (itemId == R.id.item_GrossSalary)
            viewPager.setCurrentItem(2, true);
        if (itemId == R.id.item_Vacation_Requests)
            viewPager.setCurrentItem(3, true);
        if (itemId == R.id.item_ProjectSummary)
            viewPager.setCurrentItem(4, true);
        if (itemId == R.id.item_VacationsLog)
            viewPager.setCurrentItem(5, true);

    }
}