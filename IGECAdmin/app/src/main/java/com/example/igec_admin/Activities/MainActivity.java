package com.example.igec_admin.Activities;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
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

import com.example.igec_admin.Adatpers.ViewPagerAdapter;
import com.example.igec_admin.Fragments.AddMachineFragment;
import com.example.igec_admin.Fragments.AddProjectFragment;
import com.example.igec_admin.Fragments.AddUserFragment;
import com.example.igec_admin.Fragments.MachinesFragment;
import com.example.igec_admin.Fragments.ProjectsFragment;
import com.example.igec_admin.Fragments.UsersFragment;
import com.example.igec_admin.R;
import com.example.igec_admin.Fragments.SummaryFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout vDrawerLayout;
    private NavigationView vNavigationView;
    private ViewPager viewPager;
    private Toolbar vToolbar;

    // Vars
    public static Context contextOfApplication;
    public static Context getContextOfApplication()
    {
        return contextOfApplication;
    }
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private int selectedTab = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= 30){
            if (!Environment.isExternalStorageManager()){
                Intent getpermission = new Intent();
                getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getpermission);
            }
        }
        super.onCreate(savedInstanceState);
        contextOfApplication = getApplicationContext();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        initialize();


        // Listeners
        vDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        vDrawerLayout.addDrawerListener(drawerListener);
        viewPager.addOnPageChangeListener(viewPagerListener);
    }

    //Functions
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


        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(addUserFragment, getString(R.string.add_user));
        viewPagerAdapter.addFragment(addProjectFragment, getString(R.string.add_project));
        viewPagerAdapter.addFragment(addMachineFragment, getString(R.string.add_machine));
        viewPagerAdapter.addFragment(usersFragment, getString(R.string.users));
        viewPagerAdapter.addFragment(projectsFragment, getString(R.string.projects));
        viewPagerAdapter.addFragment(machinesFragment, getString(R.string.machines));
        viewPagerAdapter.addFragment(summaryFragment, getString(R.string.summary));

        viewPager.setAdapter(viewPagerAdapter);
        vNavigationView.getMenu().getItem(0).setChecked(true);


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
    private void changeTab(int itemId)
    {
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
        if (itemId == R.id.item_summary)
            viewPager.setCurrentItem(6, true);
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