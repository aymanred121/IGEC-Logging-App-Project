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
import com.igec.common.fragments.VacationRequestsFragment
import com.igec.common.fragments.VacationsLogFragment
import com.igec.user.R
import com.igec.user.channelName
import com.igec.user.databinding.ActivityMdashboardBinding
import com.igec.user.fragments.*


private var VACATION_REQUEST_NOTIFICATION_ID = 0
private var VACATION_STATUS_NOTIFICATION_ID = 0
private var TRANSFER_REQUEST_NOTIFICATION_ID = 0
private var TRANSFER_STATUS_NOTIFICATION_ID = 0

class MDashboard : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var currManager: Employee? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMdashboardBinding
    private lateinit var navController: NavController
    private var lastTab: Int = R.id.nav_check_in_out
    private lateinit var vacationStatusNotification: Notification
    private lateinit var vacationRequestNotification: Notification
    private lateinit var transferStatusNotification: Notification
    private lateinit var transferRequestNotification: Notification
    private lateinit var notificationManager: NotificationManagerCompat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMdashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        validateDate(this)
        currManager = intent.getSerializableExtra("user") as Employee?
        setSupportActionBar(binding.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.mobile_navigation_manager)
        val args = Bundle()
        args.putSerializable("user", currManager)
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

        "${currManager?.firstName} ${currManager?.lastName}".also { employeeName ->
            binding.navView.getHeaderView(
                0
            ).findViewById<TextView>(R.id.EmployeeName).text = employeeName
        }
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.EmployeeID).text =
            currManager?.id
        createNotificationChannel(
            VACATION_STATUS_CHANNEL_ID,
            R.string.vacation_status_channel_name,
            R.string.vacation_status_channel_description
        )
        createNotificationChannel(
            VACATION_REQUEST_CHANNEL_ID,
            R.string.vacation_request_channel_name,
            R.string.vacation_request_channel_description
        )
        createNotificationChannel(
            TRANSFER_STATUS_CHANNEL_ID,
            R.string.transfer_status_channel_name,
            R.string.manager_transfer_status_channel_description
        )
        createNotificationChannel(
            TRANSFER_REQUEST_CHANNEL_ID,
            R.string.transfer_request_channel_name,
            R.string.transfer_request_channel_description
        )

        notificationManager = NotificationManagerCompat.from(this)
        //get your pending request status
        CONSTANTS.VACATION_COL.whereEqualTo("employee.id", currManager!!.id)
            .whereEqualTo("vacationNotification", 0)
            .whereNotEqualTo("vacationStatus", 0)
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
                            vacationStatusNotification = setupNotification(
                                "Vacation Request Status",
                                msg,
                                R.drawable.ic_stat_name,
                                VACATION_STATUS_CHANNEL_ID
                            )
                            notificationManager.notify(
                                VACATION_STATUS_NOTIFICATION_ID++,
                                vacationStatusNotification
                            )
                            CONSTANTS.VACATION_COL.document(vacation.id)
                                .update("vacationNotification", 1)
                        }
                    }
                }
            }
        //get vacation requests from your employees
        CONSTANTS.VACATION_COL.whereEqualTo("manager.id", currManager!!.id)
            .whereEqualTo("vacationNotification", -1).addSnapshotListener { values, error ->
                run {
                    if (error != null) {
                        Log.w("error", error.toString())
                        return@run
                    }
                    values!!.documents.forEach { documentSnapshot ->
                        run {
                            val vacation = documentSnapshot.toObject(VacationRequest::class.java)
                            val msg =
                                "${vacation!!.employee.firstName} has requested ${vacation.days} days, starting from ${
                                    vacation.convertDateToString(
                                        vacation.startDate.time
                                    )
                                }"
                            vacationRequestNotification = setupNotification(
                                "New Vacation Request",
                                msg,
                                R.drawable.ic_stat_name,
                                VACATION_REQUEST_CHANNEL_ID
                            )
                            notificationManager.notify(
                                VACATION_REQUEST_NOTIFICATION_ID++,
                                vacationRequestNotification
                            )
                            CONSTANTS.VACATION_COL.document(vacation.id)
                                .update("vacationNotification", 0)
                        }
                    }
                }
            }


        //check if their is new transfer request
        currManager!!.projectIds.forEach { projectID ->
            CONSTANTS.TRANSFER_REQUESTS_COL.whereEqualTo("oldProjectId", projectID)
                .whereEqualTo("seenByOld", false)
                .addSnapshotListener { values, error ->
                    run {
                        if (error != null) {
                            Log.w("listen error", error.toString())
                            return@run
                        }
                        for (document in values!!.documents) {
                            val transferRequest = document.toObject(TransferRequests::class.java)
                            val msg =
                                "A Transfer Request for ${transferRequest!!.employee.firstName} to ${transferRequest.newProjectName}"
                            transferRequestNotification = setupNotification(
                                "New Transfer Request",
                                msg,
                                R.drawable.ic_stat_name,
                                TRANSFER_REQUEST_CHANNEL_ID
                            )

                            notificationManager.notify(
                                TRANSFER_REQUEST_NOTIFICATION_ID++,
                                transferRequestNotification
                            )

                            CONSTANTS.TRANSFER_REQUESTS_COL.document(document.id)
                                .update("seenByOld", true)
                        }
                    }
                }
            //check transfer request status
            CONSTANTS.TRANSFER_REQUESTS_COL.whereEqualTo("newProjectId", projectID)
                .whereEqualTo("seenByNew", false)
                .addSnapshotListener { values, error ->
                    run {
                        if (error != null) {
                            Log.w("listen error", error.toString())
                            return@run
                        }
                        for (document in values!!.documents) {
                            if ((document.toObject(TransferRequests::class.java)!!.transferStatus == -1))
                                continue
                            val msg =
                                if (document.toObject(TransferRequests::class.java)!!.transferStatus == 0)
                                    "Transfer Request for Employee ${
                                        document.toObject(
                                            TransferRequests::class.java
                                        )!!.employee.firstName
                                    } was rejected"
                                else
                                    "Transfer Request for Employee ${
                                        document.toObject(
                                            TransferRequests::class.java
                                        )!!.employee.firstName
                                    } was accepted"
                            transferStatusNotification = setupNotification(
                                "Transfer Request Status",
                                msg,
                                R.drawable.ic_stat_name,
                                TRANSFER_STATUS_CHANNEL_ID
                            )

                            notificationManager.notify(
                                TRANSFER_STATUS_NOTIFICATION_ID++,
                                transferStatusNotification
                            )

                            CONSTANTS.TRANSFER_REQUESTS_COL.document(document.id)
                                .update("seenByNew", true)
                        }
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
            val intent = Intent(this@MDashboard, DateInaccurate::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        else
            finish()
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
                                    CheckInOutFragment.newInstance(currManager)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_change_password -> {
                            binding.toolbar.title = getString(R.string.change_password)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    ChangePasswordFragment.newInstance(currManager)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_gross_salary -> {
                            binding.toolbar.title = getString(R.string.gross_salary)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    GrossSalaryFragment.newInstance(currManager!!.id)
                                )
                                .addToBackStack(null)
                                .commit()
                        }

                        R.id.nav_send_vacation_request -> {
                            binding.toolbar.title = getString(R.string.send_vacation_request)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    SendVacationRequestFragment.newInstance(currManager)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_vacation_requests -> {
                            binding.toolbar.title = getString(R.string.vacation_requests)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    VacationRequestsFragment.newInstance(currManager)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_vacations_log -> {
                            binding.toolbar.title = getString(R.string.vacations_log)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    VacationsLogFragment.newInstance(currManager)
                                )
                                .addToBackStack(null)
                                .commit()
                        }

                        R.id.nav_send_transfer_request -> {
                            binding.toolbar.title = getString(R.string.send_transfer_request)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    SendTransferRequest.newInstance(currManager)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_transfer_requests -> {
                            binding.toolbar.title = getString(R.string.transfer_requests)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    TransferRequestsFragment.newInstance(currManager)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_project_summary -> {
                            binding.toolbar.title = getString(R.string.project_summary)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    ProjectSummaryFragment.newInstance(currManager)
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
}