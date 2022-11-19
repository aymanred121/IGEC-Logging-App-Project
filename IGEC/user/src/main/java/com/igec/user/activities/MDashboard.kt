package com.igec.user.activities

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.Network
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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.igec.common.CONSTANTS.*
import com.igec.common.firebase.*
import com.igec.common.fragments.VacationRequestsFragment
import com.igec.common.fragments.VacationsLogFragment
import com.igec.user.CacheDirectory
import com.igec.user.R
import com.igec.user.databinding.ActivityMdashboardBinding
import com.igec.user.fragments.*
import java.util.*


private var VACATION_REQUEST_NOTIFICATION_ID = 0
private var VACATION_STATUS_NOTIFICATION_ID = 0
private var TRANSFER_REQUEST_NOTIFICATION_ID = 0
private var TRANSFER_STATUS_NOTIFICATION_ID = 0

class MDashboard : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var employee: Employee? = null
    private lateinit var year: String
    private lateinit var month: String
    private lateinit var day: String
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMdashboardBinding
    private lateinit var navController: NavController
    private var lastTab: Int = R.id.nav_check_in_out
    private lateinit var vacationStatusNotification: Notification
    private lateinit var vacationRequestNotification: Notification
    private lateinit var transferStatusNotification: Notification
    private lateinit var transferRequestNotification: Notification
    private lateinit var notificationManager: NotificationManagerCompat
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMdashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //validateDate(this)
        employee = intent.getSerializableExtra("user") as Employee?
        setSupportActionBar(binding.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.mobile_navigation_manager)
        val args = Bundle()
        args.putSerializable("user", employee)
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

        "${employee?.firstName} ${employee?.lastName}".also { employeeName ->
            binding.navView.getHeaderView(
                0
            ).findViewById<TextView>(R.id.EmployeeName).text = employeeName
        }
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.EmployeeID).text =
            employee?.id
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
        val vacationRequests: MutableSet<String> = mutableSetOf()
        VACATION_COL.whereEqualTo("employee.id", employee!!.id)
            .whereEqualTo("vacationNotification", 0)
            .whereNotEqualTo("vacationStatus", PENDING)
            .addSnapshotListener { values, error ->
                run {
                    if (error != null) {
                        Log.w("error", error.toString())
                        return@run
                    }
                    values!!.documents.forEach { documentSnapshot ->

                        val vacation = documentSnapshot.toObject(VacationRequest::class.java)
                        if (vacationRequests.contains(vacation!!.id)) return@run
                        vacationRequests.add(vacation.id)
                        val msg: String = if (vacation.vacationStatus == REJECTED)
                            "your vacation request for ${vacation.requestedDaysString} days has been rejected"
                        else
                            "your vacation request for ${vacation.requestedDaysString} days has been accepted"
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
                        VACATION_COL.document(vacation.id)
                            .update("vacationNotification", 1)

                    }
                }
            }
        //get vacation requests from your employees
        val vacationRequestsFromEmployees: MutableSet<String> = mutableSetOf()
        VACATION_COL.whereEqualTo("manager.id", employee!!.id)
            .whereEqualTo("vacationNotification", -1).addSnapshotListener { values, error ->
                run {
                    if (error != null) {
                        Log.w("error", error.toString())
                        return@run
                    }
                    values!!.documents.forEach { documentSnapshot ->

                        val vacation = documentSnapshot.toObject(VacationRequest::class.java)
                        if (vacationRequestsFromEmployees.contains(vacation!!.id)) return@run
                        vacationRequestsFromEmployees.add(vacation.id)
                        val msg =
                            "${vacation.employee.firstName} has requested ${vacation.requestedDaysString} days, starting from ${
                                vacation.formattedStartDate()
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
                        VACATION_COL.document(vacation.id)
                            .update("vacationNotification", 0)

                    }
                }
            }


        //check if their is new transfer request
        val transferRequests: MutableSet<String> = mutableSetOf()
        employee!!.projectIds.forEach { projectID ->
            TRANSFER_REQUESTS_COL.whereEqualTo("oldProjectId", projectID)
                .whereEqualTo("seenByOld", false)
                .addSnapshotListener { values, error ->
                    run {
                        if (error != null) {
                            Log.w("listen error", error.toString())
                            return@run
                        }
                        for (document in values!!.documents) {
                            val transferRequest = document.toObject(TransferRequests::class.java)
                            if (transferRequests.contains(transferRequest!!.transferId)) return@run
                            transferRequests.add(transferRequest.transferId)
                            val msg =
                                "A Transfer Request for ${transferRequest.employee.firstName} to ${transferRequest.newProjectName}"
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

                            TRANSFER_REQUESTS_COL.document(document.id)
                                .update("seenByOld", true)
                        }
                    }
                }
            //check transfer request status
            val transferStatus: MutableSet<String> = mutableSetOf()
            TRANSFER_REQUESTS_COL.whereEqualTo("newProjectId", projectID)
                .whereEqualTo("seenByNew", false)
                .addSnapshotListener { values, error ->
                    run {
                        if (error != null) {
                            Log.w("listen error", error.toString())
                            return@run
                        }
                        for (document in values!!.documents) {
                            val transferRequest = document.toObject(TransferRequests::class.java)
                            if (transferStatus.contains(transferRequest!!.transferId)) return@run
                            transferStatus.add(transferRequest.transferId)
                            if (transferRequest.transferStatus == PENDING)
                                continue
                            val msg =
                                if (transferRequest.transferStatus == REJECTED)
                                    "Transfer Request for Employee ${
                                        transferRequest.employee.firstName
                                    } was rejected"
                                else
                                    "Transfer Request for Employee ${
                                        transferRequest.employee.firstName
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

                            TRANSFER_REQUESTS_COL.document(document.id)
                                .update("seenByNew", true)
                        }
                    }
                }
        }
        EMPLOYEE_COL.document(employee!!.id).addSnapshotListener { value, error ->
            if (error != null || value == null || !value.exists()) return@addSnapshotListener
            employee = value.toObject(Employee::class.java)
            val intent:Intent
            if(!employee!!.isLocked)
            {
                intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }
        updateSummaryCacheStatus()
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                updateSummaryChanges()
                updateBaseGrossSalaryCache()
                updateProjectsCache()
            }

            override fun onLost(network: Network) {

            }
        })
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
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
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
                                    CheckInOutFragment.newInstance(employee)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_change_password -> {
                            binding.toolbar.title = getString(R.string.change_password)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    ChangePasswordFragment.newInstance(employee)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_gross_salary -> {
                            binding.toolbar.title = getString(R.string.gross_salary)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    GrossSalaryFragment.newInstance(employee!!.id)
                                )
                                .addToBackStack(null)
                                .commit()
                        }

                        R.id.nav_send_vacation_request -> {
                            binding.toolbar.title = getString(R.string.send_vacation_request)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    SendVacationRequestFragment.newInstance(employee)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_vacation_requests -> {
                            binding.toolbar.title = getString(R.string.vacation_requests)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    VacationRequestsFragment.newInstance(employee)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_vacations_log -> {
                            binding.toolbar.title = getString(R.string.vacations_log)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    VacationsLogFragment.newInstance(employee)
                                )
                                .addToBackStack(null)
                                .commit()
                        }

                        R.id.nav_send_transfer_request -> {
                            binding.toolbar.title = getString(R.string.send_transfer_request)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    SendTransferRequest.newInstance(employee)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_transfer_requests -> {
                            binding.toolbar.title = getString(R.string.transfer_requests)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    TransferRequestsFragment.newInstance(employee)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                        R.id.nav_project_summary -> {
                            binding.toolbar.title = getString(R.string.project_summary)
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.nav_host_fragment_content_main,
                                    ProjectSummaryFragment.newInstance(employee)
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
    private fun updateSummaryCacheStatus() {
        val calendar = Calendar.getInstance()
        val cachedSummary = CacheDirectory.readAllCachedText(this, "summary.json")
        val summary: Summary = Gson().fromJson(cachedSummary, Summary::class.java) ?: return
        val checkInDate = ((summary.checkIn["Time"] as Map<*, *>)["seconds"] as Double).toLong()
        calendar.time = Date(checkInDate * 1000)
        summary.checkIn["Time"] = Timestamp(calendar.time)
        if (calendar.get(Calendar.DAY_OF_MONTH) != Calendar.getInstance()
                .get(Calendar.DAY_OF_MONTH)
        ) {
            CacheDirectory.writeAllCachedText(this, "summary.json", "")
        }
    }
    private fun updateSummaryChanges() {
        updateDate()
        val calendar = Calendar.getInstance()
        val cachedSummary = CacheDirectory.readAllCachedText(this, "summary.json")
        val cachedGrossSalary = CacheDirectory.readAllCachedText(this, "grossSalary.json")
        val summary: Summary = Gson().fromJson(cachedSummary, Summary::class.java) ?: return
        val employeeGrossSalary =
            Gson().fromJson(cachedGrossSalary, EmployeesGrossSalary::class.java)
        //fix Date in summary
        val checkInDate = ((summary.checkIn["Time"] as Map<*, *>)["seconds"] as Double).toLong()
        calendar.time = Date(checkInDate * 1000)
        summary.checkIn["Time"] = Timestamp(calendar.time)

        if (summary.checkOut != null) {
            val checkOutDate =
                ((summary.checkOut["Time"] as Map<*, *>)["seconds"] as Double).toLong()
            summary.checkOut["Time"] = Timestamp(Date(checkOutDate * 1000))
        }
        //check if the summary is for today
        if (calendar.get(Calendar.DAY_OF_MONTH) != Calendar.getInstance()
                .get(Calendar.DAY_OF_MONTH)
        ) return
        val path = employee!!.id
        SUMMARY_COL.document(path).collection("$year-$month").document(day).get()
            .addOnSuccessListener { document ->
                if (document.metadata.isFromCache) return@addOnSuccessListener
                SUMMARY_COL.document(path).collection("$year-$month")
                    .document(day)
                    .set(summary, SetOptions.merge())
                if (employeeGrossSalary != null) {
                    EMPLOYEE_GROSS_SALARY_COL.document(path).collection(year).document(month)
                        .set(employeeGrossSalary, SetOptions.merge()).addOnSuccessListener {
                            CacheDirectory.writeAllCachedText(this, "grossSalary.json", "")
                        }
                }
                if (summary.checkOut != null) {
                    for (pid in summary.projectIds) {
                        if (pid.value == CHECK_IN_FROM_SITE)
                            PROJECT_COL.document(pid.key).update("employeeWorkedTime." + employee!!.id, FieldValue.increment((summary.workingTime[pid.key]!!)))
                    }
                }
            }
    }

    private fun updateDate() {
        val calendar = java.util.Calendar.getInstance()
        year = calendar[java.util.Calendar.YEAR].toString()
        month = String.format("%02d", calendar[java.util.Calendar.MONTH] + 1)
        day = String.format("%02d", calendar[java.util.Calendar.DAY_OF_MONTH])
        if (day.toInt() > 25) {
            if (month.toInt() + 1 == 13) {
                month = "01"
                year = String.format("%d", year.toInt() + 1)
            } else {
                month = String.format("%02d", month.toInt() + 1)
            }
        }
    }

    private fun updateBaseGrossSalaryCache() {
        EMPLOYEE_GROSS_SALARY_COL.document(employee?.id!!).get().addOnSuccessListener { doc ->
            {
                if (doc.exists()) {
                    CacheDirectory.writeAllCachedText(
                        this,
                        "baseAllowances.json",
                        Gson().toJson(doc.toObject(EmployeesGrossSalary::class.java))
                    )

                }
            }
        }
    }

    private fun updateProjectsCache() {
        PROJECT_COL.get().addOnSuccessListener { docs ->
            val gson = Gson()
            CacheDirectory.writeAllCachedText(
                this,
                "projects.json",
                gson.toJson(docs.toObjects(Project::class.java))
            )
        }
    }
}