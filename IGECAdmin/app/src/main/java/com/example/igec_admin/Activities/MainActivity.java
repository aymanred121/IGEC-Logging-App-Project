package com.example.igec_admin.Activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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

    // Vars
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        initialize();


        // Listeners
        vDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AddUserFragment()).commit();
            vNavigationView.setCheckedItem(R.id.item_addUser);
        }

    }


    //Functions
    private void initialize() {
        // Views
        Toolbar vToolbar = findViewById(R.id.toolbar);
        vDrawerLayout = findViewById(R.id.drawer);
        vNavigationView = findViewById(R.id.navView);
        setSupportActionBar(vToolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, vDrawerLayout, vToolbar, R.string.openNavBar, R.string.closeNavBar);
        actionBarDrawerToggle.syncState();
        vNavigationView.setNavigationItemSelectedListener(this);


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
        int itemId = item.getItemId();
        if (itemId == R.id.item_addUser)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AddUserFragment()).commit();
        if (itemId == R.id.item_addProject)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AddProjectFragment()).commit();
        if (itemId == R.id.item_addMachine)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AddMachineFragment()).commit();
        if (itemId == R.id.item_summary)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SummaryFragment()).commit();
        if (itemId == R.id.item_Users)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UsersFragment()).commit();
        if (itemId == R.id.item_Projects)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProjectsFragment()).commit();
        if (itemId == R.id.item_Machines)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MachinesFragment()).commit();

        vDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}