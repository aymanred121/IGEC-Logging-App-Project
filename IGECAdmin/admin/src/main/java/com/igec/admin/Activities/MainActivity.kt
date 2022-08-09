package com.igec.admin.Activities

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.igec.admin.databinding.ActivityMainBinding
import com.igec.admin.Dialogs.ProjectFragmentDialog
import com.igec.admin.Fragments.*

import com.google.android.material.navigation.NavigationView
import com.igec.admin.R

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var lastTab: Int = R.id.nav_add_user
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_add_user, R.id.nav_add_project, R.id.nav_add_machine,
                R.id.nav_users, R.id.nav_projects, R.id.nav_machines,
                R.id.nav_vacation_requests, R.id.nav_vacations_log, R.id.nav_summary,
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)


        val navigationView = findViewById<NavigationView>(R.id.nav_view)


        val contact_info = navigationView.getHeaderView(0).findViewById<TextView>(R.id.contact_info)
        contact_info.setOnClickListener { view: View? ->
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://" + getString(R.string.nav_header_subtitle))
            )
            this@MainActivity.startActivity(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        AddProjectFragment.clearTeam()
        ProjectFragmentDialog.clearTeam()
    }
    override fun onResume() {
        super.onResume()
        isNetworkAvailable()
    }

    //Functions
    private fun getExternalStoragePerm() {
        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }

    private fun isNetworkAvailable() {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        val connected = activeNetworkInfo != null && activeNetworkInfo.isConnected
        if (!connected) {
            val intent = Intent(this@MainActivity, SplashScreen_InternetConnection::class.java)
            startActivity(intent)
            finish()
        }
    }
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.main_activity2, menu)
//        return true
//    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerClosed(drawerView: View) {
                // This method will be called after drawer animation finishes
                // Perform the fragment replacement
                if(lastTab != item.itemId) {
                    if (lastTab == R.id.nav_add_project) {
                        AddProjectFragment.clearTeam()
                    }
                    when (item.itemId) {
                        R.id.nav_add_user -> {
                            binding.toolbar.title = getString(R.string.add_user)
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.nav_host_fragment_content_main, AddUserFragment())
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_add_project -> {
                            binding.toolbar.title = getString(R.string.add_project)
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.nav_host_fragment_content_main, AddProjectFragment())
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_add_machine -> {
                            binding.toolbar.title = getString(R.string.add_machine)
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.nav_host_fragment_content_main, AddMachineFragment())
                                .addToBackStack(null)
                                .commit()
                        }

                        R.id.nav_users -> {
                            binding.toolbar.title = getString(R.string.users)
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.nav_host_fragment_content_main, UsersFragment())
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_projects -> {
                            binding.toolbar.title = getString(R.string.projects)
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.nav_host_fragment_content_main, ProjectsFragment())
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_machines -> {
                            binding.toolbar.title = getString(R.string.machines)
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.nav_host_fragment_content_main, MachinesFragment())
                                .addToBackStack(null)
                                .commit()
                        }

                        R.id.nav_vacation_requests -> {
                            binding.toolbar.title = getString(R.string.vacation_requests)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    VacationRequestsFragment()
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_vacations_log -> {
                            binding.toolbar.title = getString(R.string.vacations_log)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    VacationsLogFragment()
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_summary -> {
                            binding.toolbar.title = getString(R.string.summary)
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.nav_host_fragment_content_main, SummaryFragment())
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                    lastTab = item.itemId
                }
                // Cross fade back the content container and hide progress bar


                // Remove this listener so close by, for example, swiping do not call it again
                binding.drawerLayout.removeDrawerListener(this)
            }
        })

        // Closes the drawer, triggering the listener above
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        else
            finish()
    }
}