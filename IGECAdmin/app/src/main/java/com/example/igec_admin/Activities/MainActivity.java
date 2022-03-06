package com.example.igec_admin.Activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Views
    private Toolbar vToolbar;
    private DrawerLayout vDrawerLayout;
    private NavigationView vNavigationView;

    // Vars
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private AddUserFragment adduserFragment;
    private AddProjectFragment addprojectFragment;
    private AddMachineFragment addmachineFragment;
    private SummaryFragment summaryFragment;
    private UsersFragment usersFragment;
    private ProjectsFragment projectsFragment;
    private MachinesFragment machinesFragment;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();


        // Listeners
        vDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, adduserFragment).commit();
            vNavigationView.setCheckedItem(R.id.item_addUser);
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
        adduserFragment = new AddUserFragment();
        addprojectFragment = new AddProjectFragment();
        addmachineFragment = new AddMachineFragment();
        summaryFragment = new SummaryFragment();
        machinesFragment = new MachinesFragment();
        projectsFragment = new ProjectsFragment();
        usersFragment = new UsersFragment();

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
            case R.id.item_addUser:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, adduserFragment).commit();
                break;
            case R.id.item_addProject:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, addprojectFragment).commit();
                break;
            case R.id.item_addMachine:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, addmachineFragment).commit();
                break;
            case R.id.item_summary:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, summaryFragment).commit();
                break;
            case R.id.item_Users:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, usersFragment).commit();
                break;
            case R.id.item_Projects:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, projectsFragment).commit();
                break;
            case R.id.item_Machines:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, machinesFragment).commit();
                break;
        }
        vDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}