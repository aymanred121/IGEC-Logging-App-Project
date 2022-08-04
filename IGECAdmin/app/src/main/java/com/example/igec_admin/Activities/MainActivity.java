package com.example.igec_admin.Activities;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkRequest;

import com.example.igec_admin.Adatpers.ViewPagerAdapter;
import com.example.igec_admin.Dialogs.ProjectFragmentDialog;
import com.example.igec_admin.Fragments.AddMachineFragment;
import com.example.igec_admin.Fragments.AddProjectFragment;
import com.example.igec_admin.Fragments.AddUserFragment;
import com.example.igec_admin.Fragments.MachinesFragment;
import com.example.igec_admin.Fragments.ProjectsFragment;
import com.example.igec_admin.Fragments.UsersFragment;
import com.example.igec_admin.Fragments.VacationRequestsFragment;
import com.example.igec_admin.Fragments.VacationsLogFragment;
import com.example.igec_admin.R;
import com.example.igec_admin.Fragments.SummaryFragment;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout vDrawerLayout;
    private NavigationView vNavigationView;
    private ViewPager viewPager;
    private Toolbar vToolbar;

    // Vars
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private int selectedTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getExternalStoragePerm();
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
        isNetworkAvailable();
        initialize();

        // Listeners
        vDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        vDrawerLayout.addDrawerListener(drawerListener);
        viewPager.addOnPageChangeListener(viewPagerListener);


        NavigationView navigationView = findViewById(R.id.navView);


        TextView contact_info = navigationView.getHeaderView(0).findViewById(R.id.contact_info);
        contact_info.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + getString(R.string.nav_header_subtitle)));
            this.startActivity(intent);
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        AddProjectFragment.clearTeam();
        ProjectFragmentDialog.clearTeam();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isNetworkAvailable();
    }

    //Functions

    private void getExternalStoragePerm() {
        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
    }

    private void isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean connected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        if (!connected) {
            Intent intent = new Intent(MainActivity.this, SplashScreen_InternetConnection.class);
            startActivity(intent);
            finish();
        }
    }

    private void initialize() {
        // Views
        vToolbar = findViewById(R.id.toolbar);
        vDrawerLayout = findViewById(R.id.drawer);
        vNavigationView = findViewById(R.id.navView);
        viewPager = findViewById(R.id.fragment_container);

        setSupportActionBar(vToolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, vDrawerLayout, vToolbar, R.string.openNavBar, R.string.closeNavBar);
        actionBarDrawerToggle.syncState();
        vNavigationView.setNavigationItemSelectedListener(this);


        AddUserFragment addUserFragment = new AddUserFragment();
        AddProjectFragment addProjectFragment = new AddProjectFragment();
        AddMachineFragment addMachineFragment = new AddMachineFragment();
        UsersFragment usersFragment = new UsersFragment();
        ProjectsFragment projectsFragment = new ProjectsFragment();
        MachinesFragment machinesFragment = new MachinesFragment();
        SummaryFragment summaryFragment = new SummaryFragment();
        VacationRequestsFragment vacationRequestsFragment = new VacationRequestsFragment();
        VacationsLogFragment vacationsLogFragment = new VacationsLogFragment();

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(addUserFragment, getString(R.string.add_user));
        viewPagerAdapter.addFragment(addProjectFragment, getString(R.string.add_project));
        viewPagerAdapter.addFragment(addMachineFragment, getString(R.string.add_machine));
        viewPagerAdapter.addFragment(usersFragment, getString(R.string.users));
        viewPagerAdapter.addFragment(projectsFragment, getString(R.string.projects));
        viewPagerAdapter.addFragment(machinesFragment, getString(R.string.machines));
        viewPagerAdapter.addFragment(vacationRequestsFragment, getString(R.string.vacation_requests));
        viewPagerAdapter.addFragment(vacationsLogFragment, getString(R.string.vacations_log));
        viewPagerAdapter.addFragment(summaryFragment, getString(R.string.summary));

        viewPager.setAdapter(viewPagerAdapter);
        vNavigationView.getMenu().getItem(0).setChecked(true);


    }

    @Override
    public void onBackPressed() {
        if (vDrawerLayout.isDrawerOpen(GravityCompat.START))
            vDrawerLayout.closeDrawer(GravityCompat.START);
        else
            finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        vDrawerLayout.closeDrawer(GravityCompat.START);
        selectedTab = item.getItemId();
        return true;
    }

    private void changeTab(int itemId) {
        if (itemId == R.id.item_addUser)
            viewPager.setCurrentItem(0, true);
        if (itemId == R.id.item_addProject)
            viewPager.setCurrentItem(1, true);
        if (itemId == R.id.item_addMachine)
            viewPager.setCurrentItem(2, true);
        if (itemId == R.id.item_Users)
            viewPager.setCurrentItem(3, true);
        if (itemId == R.id.item_Projects)
            viewPager.setCurrentItem(4, true);
        if (itemId == R.id.item_Machines)
            viewPager.setCurrentItem(5, true);
        if (itemId == R.id.item_Vacation_Requests)
            viewPager.setCurrentItem(6, true);
        if (itemId == R.id.item_VacationsLog)
            viewPager.setCurrentItem(7, true);
        if (itemId == R.id.item_summary)
            viewPager.setCurrentItem(8, true);
    }

    private ViewPager.OnPageChangeListener viewPagerListener = new ViewPager.OnPageChangeListener() {
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
    private DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
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