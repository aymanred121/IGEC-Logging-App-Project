package com.igec.user.activities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FieldValue
import com.igec.common.CONSTANTS
import com.igec.common.CONSTANTS.*
import com.igec.common.firebase.Employee
import com.igec.common.firebase.TransferRequests
import com.igec.common.firebase.VacationRequest
import com.igec.common.fragments.VacationsLogFragment
import com.igec.user.R
import com.igec.user.databinding.ActivityEdashboardBinding
import com.igec.user.fragments.ChangePasswordFragment
import com.igec.user.fragments.CheckInOutFragment
import com.igec.user.fragments.GrossSalaryFragment
import com.igec.user.fragments.SendVacationRequestFragment

private var VACATION_NOTIFICATION_ID = 0
private var TRANSFER_NOTIFICATION_ID = 0

class EDashboard : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var currEmployee: Employee? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityEdashboardBinding
    private lateinit var navController: NavController
    private var lastTab: Int = R.id.nav_check_in_out
    private lateinit var vacationNotification: Notification
    private lateinit var transferNotification: Notification
    private lateinit var notificationManager: NotificationManagerCompat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEdashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        validateDate(this)
        currEmployee = intent.getSerializableExtra("user") as Employee?
        setSupportActionBar(binding.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.mobile_navigation_user)
        val args = Bundle()
        args.putSerializable("user", currEmployee)
        navController.setGraph(graph, args)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_check_in_out, R.id.nav_change_password, R.id.nav_gross_salary,
                R.id.nav_send_vacation_request, R.id.nav_vacations_log
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)

        "${currEmployee?.firstName} ${currEmployee?.lastName}".also { employeeName ->
            binding.navView.getHeaderView(
                0
            ).findViewById<TextView>(R.id.EmployeeName).text = employeeName
        }
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.EmployeeID).text =
            currEmployee?.id

        createNotificationChannel(
            VACATION_STATUS_CHANNEL_ID,
            R.string.vacation_status_channel_name,
            R.string.vacation_status_channel_description
        )
        createNotificationChannel(
            TRANSFER_STATUS_CHANNEL_ID,
            R.string.transfer_status_channel_name,
            R.string.employee_transfer_status_channel_description
        )

        notificationManager = NotificationManagerCompat.from(this)

        CONSTANTS.VACATION_COL.whereEqualTo("employee.id", currEmployee!!.id)
            .whereEqualTo("vacationNotification", 0)
            .whereNotEqualTo("vacationStatus", PENDING)
            .addSnapshotListener { values, error ->
                run {
                    if (error != null) {
                        Log.w("error", error.toString())
                        return@run
                    }
                    values!!.documents.forEach { documentSnapshot ->
                        run {
                            val vacation = documentSnapshot.toObject(VacationRequest::class.java)
                            val msg: String = if (vacation!!.vacationStatus == -1)
                                "your vacation request for ${vacation.days} days has been rejected"
                            else
                                "your vacation request for ${vacation.days} days has been accepted"
                            vacationNotification = setupNotification(
                                "Vacation Request Status",
                                msg,
                                R.drawable.ic_stat_name,
                                VACATION_STATUS_CHANNEL_ID
                            )
                            notificationManager.notify(
                                VACATION_NOTIFICATION_ID++,
                                vacationNotification
                            )
                            CONSTANTS.VACATION_COL.document(vacation.id)
                                .update("vacationNotification", 1)
                        }
                    }
                }
            }
        //check transfer request
        CONSTANTS.TRANSFER_REQUESTS_COL.whereEqualTo("employee.id", currEmployee!!.id)
            .whereEqualTo("seenByEmp", false)
            .addSnapshotListener { values, error ->
                run {
                    if (error != null) {
                        Log.w("listen error", error.toString())
                        return@run
                    }
                    for (document in values!!.documents) {
                        if (document.toObject(TransferRequests::class.java)!!.transferStatus == 0) {
                            //rejected
                            CONSTANTS.TRANSFER_REQUESTS_COL.document(document.id)
                                .update("transferNotification", FieldValue.increment(1))
                            continue
                        }
                        if (document.toObject(TransferRequests::class.java)!!.transferStatus == -1) {
                            //pending
                            continue
                        }
                        val msg =
                            "You have been transferred to project ${
                                document.toObject(
                                    TransferRequests::class.java
                                )!!.newProjectName
                            }"
                        transferNotification = setupNotification(
                            "Project Transfer",
                            msg,
                            R.drawable.ic_stat_name,
                            TRANSFER_STATUS_CHANNEL_ID
                        )

                        notificationManager.notify(
                            TRANSFER_NOTIFICATION_ID++,
                            transferNotification
                        )

                        CONSTANTS.TRANSFER_REQUESTS_COL.document(document.id)
                            .update("seenByEmp", true)
                    }
                }
            }
    }

    private fun createNotificationChannel(CHANNEL_ID: String, channelName: Int, channelDesc: Int) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = getString(channelName)
        val descriptionText = getString(channelDesc)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            enableLights(true)
            lightColor = Color.GREEN
            setSound(alarmSound, null)
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun setupNotification(
        title: String,
        content: String,
        icon: Int,
        CHANNEL_ID: String
    ): Notification {
        val intent = Intent(this, LauncherActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
    }


    private fun validateDate(c: Context) {
        if (Settings.Global.getInt(c.contentResolver, Settings.Global.AUTO_TIME, 0) != 1) {
            val intent = Intent(this@EDashboard, DateInaccurate::class.java)
            startActivity(intent)
            finish()
        }
    }

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
                    when (item.itemId) {
                        R.id.nav_check_in_out -> {
                            binding.toolbar.title = getString(R.string.check_in_out)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    CheckInOutFragment.newInstance(currEmployee)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_change_password -> {
                            binding.toolbar.title = getString(R.string.change_password)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    ChangePasswordFragment.newInstance(currEmployee)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_gross_salary -> {
                            binding.toolbar.title = getString(R.string.gross_salary)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    GrossSalaryFragment.newInstance(currEmployee!!.id)
                                )
                                .addToBackStack(null)
                                .commit()
                        }

                        R.id.nav_send_vacation_request -> {
                            binding.toolbar.title = getString(R.string.send_vacation_request)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    SendVacationRequestFragment.newInstance(currEmployee)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_vacations_log -> {
                            binding.toolbar.title = getString(R.string.vacations_log)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    VacationsLogFragment.newInstance(currEmployee)
                                )
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