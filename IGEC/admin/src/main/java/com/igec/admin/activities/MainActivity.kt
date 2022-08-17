package com.igec.admin.activities

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.igec.admin.dialogs.ProjectFragmentDialog
import com.igec.admin.fragments.*
import com.igec.admin.R
import com.igec.admin.databinding.ActivityMainBinding
import com.igec.common.fragments.VacationRequestsFragment
import com.igec.common.fragments.VacationsLogFragment


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var lastTab: Int = R.id.nav_add_user
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.let {
            it.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    //take action when network connection is gained
                }
                override fun onLost(network: Network) {
                    //take action when network connection is lost
                    val intent = Intent(this@MainActivity, InternetConnection::class.java)
                    startActivity(intent)
                    finish()
                }
            })
        }
        if (!checkStoragePermission())
            requestStoragePermission()
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


        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.contact_info)
            .setOnClickListener { view: View? ->
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

    //Functions
    private fun checkStoragePermission(): Boolean {
        return if (SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val result =
                ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
            val result1 =
                ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
            result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data =
                    Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivityForResult(intent, 2296)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivity(intent)
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(
                this,
                arrayOf(WRITE_EXTERNAL_STORAGE),
                123
            )
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
                if (lastTab != item.itemId) {
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
                                    VacationRequestsFragment.newInstance(null)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_vacations_log -> {
                            binding.toolbar.title = getString(R.string.vacations_log)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    VacationsLogFragment.newInstance(null)
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