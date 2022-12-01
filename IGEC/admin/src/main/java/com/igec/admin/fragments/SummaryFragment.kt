package com.igec.admin.fragments

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.igec.admin.adapters.EmployeeAdapter
import com.igec.admin.databinding.FragmentSummaryBinding
import com.igec.admin.dialogs.MonthSummaryDialog
import com.igec.common.CONSTANTS
import com.igec.common.firebase.*
import com.igec.common.utilities.AllowancesEnum
import com.igec.common.utilities.CsvWriter
import com.igec.common.utilities.LocationDetails
import com.igec.common.utilities.WorkingDay
import com.whiteelephant.monthpicker.MonthPickerDialog
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import java.util.stream.IntStream

class SummaryFragment : Fragment() {
    // Vars
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.IO + job)
    private var adapter: EmployeeAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var year: String? = null
    private var month: String? = null
    private var prevMonth: String? = null
    private var prevYear: String? = null
    private var employees: ArrayList<EmployeeOverview>? = null
    private var opened = false
    private val EIGHT_HOURS: Long = 28800
    private var csvWriter: CsvWriter? = null
    private lateinit var dataRow: Array<String>
    private lateinit var alertDialog: AlertDialog
    private var dataRowSize = 0
    val selected = Calendar.getInstance()
    fun setOpened(opened: Boolean) {
        this.opened = opened
    }

    private var binding: FragmentSummaryBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
        binding = null

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        binding!!.monthLayout.setEndIconOnClickListener(oclMonthPicker)
        binding!!.monthLayout.setErrorIconOnClickListener(oclMonthPicker)
        adapter!!.setOnItemClickListener(oclEmployee)
        binding!!.createFab.setOnClickListener(oclCSV)
        binding!!.allFab.setOnClickListener(oclAll)
    }

    // Functions
    private fun initialize() {
        employees = ArrayList()
        binding!!.recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(activity)
        adapter = EmployeeAdapter(employees, EmployeeAdapter.Type.none)
        binding!!.recyclerView.layoutManager = layoutManager
        binding!!.recyclerView.adapter = adapter
        val builder = MaterialAlertDialogBuilder(
            requireActivity()
        )
        builder.setTitle("Creating CSV")
            .setMessage("creating CSV file, please wait...")
            .setCancelable(false)
        alertDialog = builder.create()
        getEmployees()
    }

    private suspend fun UpdateCSV(workingDays: ArrayList<WorkingDay>, empName: String, id: String) {


        val holidaysDoc = CONSTANTS.HOLIDAYS_COL.document(CONSTANTS.HOLIDAYS).get().await()
        val vacationsDoc = CONSTANTS.VACATION_COL
            .whereEqualTo("vacationStatus", 1)
            .whereEqualTo("employee.id", id).get().await()


        dataRow = Array(dataRowSize) { "" }
        dataRow[0] = empName
        for (w in workingDays) {
            dataRow[w.day.toInt()] = w.hours.toString()
        }
        IntStream.range(1, dataRow.size).filter { i: Int -> dataRow[i] == "" }
            .forEach { i: Int -> dataRow[i] = CONSTANTS.ABSENT }
        val vacationRequests = ArrayList<VacationRequest>()
        val holidays = ArrayList<Holiday>()
        addFridays()
        addHolidays(holidaysDoc, holidays)
        addVacations(vacationsDoc, vacationRequests)
        var temp = ArrayList<String>()
        temp.add(dataRow[0])
        temp.addAll(Arrays.asList(*dataRow).subList(26, dataRow.size))
        temp.addAll(Arrays.asList(*dataRow).subList(1, 26))
        dataRow = temp.toTypedArray()
        csvWriter!!.addDataRow(*dataRow)
        dataRow = Array(dataRowSize) { "" }
        dataRow[0] = "project name"
        for (w in workingDays) {
            dataRow[w.day.toInt()] = w.projectName.toString()
        }
        IntStream.range(1, dataRow.size).filter { i: Int -> dataRow[i] == "" }
            .forEach { i: Int -> dataRow[i] = "---" }
        temp = ArrayList()
        temp.add(dataRow[0])
        temp.addAll(Arrays.asList(*dataRow).subList(26, dataRow.size))
        temp.addAll(Arrays.asList(*dataRow).subList(1, 26))
        dataRow = temp.toTypedArray()
        csvWriter!!.addDataRow(*dataRow)

    }

    private fun addVacations(
        queryDocumentSnapshots: QuerySnapshot,
        vacationRequests: ArrayList<VacationRequest>
    ) {
        if (queryDocumentSnapshots.documents.size != 0) {
            for (d in queryDocumentSnapshots) {
                vacationRequests.add(d.toObject(VacationRequest::class.java))
            }
            val it = Calendar.getInstance()
            val end = Calendar.getInstance()
            for (v in vacationRequests) {
                val vacationLabels = Array<String>(v.requestedDays.toInt()) { "" }
                addVacationLabels(v.vacationDays, vacationLabels, "vacation")
                addVacationLabels(v.sickDays, vacationLabels, "sick leave")
                addVacationLabels(v.unpaidDays, vacationLabels, "unpaid")
                var labelIndex = 0

                // acceptable bounds
                val thisMonthCalendar = Calendar.getInstance()
                val prevMonthCalendar = Calendar.getInstance()
                if (month!!.toInt() == 1) {
                    thisMonthCalendar[year!!.toInt(), 0] = 25
                    prevMonthCalendar[prevYear!!.toInt(), 11] = 26
                } else {
                    thisMonthCalendar[year!!.toInt(), month!!.toInt() - 1] = 25
                    prevMonthCalendar[year!!.toInt(), prevMonth!!.toInt() - 1] = 26
                }
                val vacationStart = Calendar.getInstance()
                vacationStart.time = v.startDate
                val vacationEnd = Calendar.getInstance()
                vacationEnd.time = v.endDate
                // exits in the acceptable bounds
                // vacations that start and end on that range 26/prev - 25/this
                if (isEqualOrLater(
                        vacationStart,
                        prevMonthCalendar
                    ) && isEqualOrEarlier(vacationEnd, thisMonthCalendar)
                ) {
                    it.time = v.startDate
                    end.time = v.endDate
                    while (isEqualOrEarlier(it, end)) {
                        // skip if it's friday
                        val index = it[Calendar.DAY_OF_MONTH].toString().toInt()
                        if (it[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY) {
                            it.add(Calendar.DAY_OF_MONTH, 1)
                            continue
                        }
                        dataRow[index] = vacationLabels[labelIndex]
                        labelIndex++
                        it.add(Calendar.DAY_OF_MONTH, 1)
                    }
                } else if (isEqualOrEarlier(vacationStart, prevMonthCalendar) && isEqualOrEarlier(
                        vacationEnd,
                        thisMonthCalendar
                    )
                ) {
                    // cut the vacation to the acceptable bounds
                    it.time = prevMonthCalendar.time
                    // add difference between prev month and vacation start to the label index
                    labelIndex += (prevMonthCalendar.timeInMillis - vacationStart.timeInMillis).toInt() / 86400000
                    end.time = v.endDate
                    while (isEqualOrEarlier(it, end)) {
                        // skip if it's friday
                        val index = it[Calendar.DAY_OF_MONTH].toString().toInt()
                        if (it[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY) {
                            it.add(Calendar.DAY_OF_MONTH, 1)
                            continue
                        }
                        dataRow[index] = vacationLabels[labelIndex]
                        labelIndex++
                        it.add(Calendar.DAY_OF_MONTH, 1)
                    }
                } else if (isEqualOrEarlier(vacationStart, thisMonthCalendar) && isEqualOrLater(
                        vacationEnd,
                        thisMonthCalendar
                    )
                ) {
                    it.time = v.startDate
                    end.time = thisMonthCalendar.time
                    while (isEqualOrEarlier(it, end)) {
                        // skip if it's friday
                        val index = it[Calendar.DAY_OF_MONTH].toString().toInt()
                        if (it[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY) {
                            it.add(Calendar.DAY_OF_MONTH, 1)
                            continue
                        }
                        dataRow[index] = vacationLabels[labelIndex]
                        labelIndex++
                        it.add(Calendar.DAY_OF_MONTH, 1)
                    }
                }
            }
        }
    }

    private fun addHolidays(doc: DocumentSnapshot, holidays: ArrayList<Holiday>) {
        holidays.clear()
        prevYear = (year!!.toInt() - 1).toString()
        prevMonth = (month!!.toInt() - 1).toString()
        if (doc.exists() && (doc.contains(year!!) || doc.contains(prevYear!!))) {
            if (doc.data!![year] != null) {
                // loop over hashmap
                for (o in (doc.data!![year] as ArrayList<*>?)!!) {
                    holidays.add(Holiday(o as HashMap<*, *>))
                }
                if (doc.data!![prevYear] != null) {
                    for (o in (doc.data!![prevYear] as ArrayList<*>?)!!) {
                        holidays.add(Holiday(o as HashMap<*, *>))
                    }
                }
                for (i in 1 until dataRow.size) {
                    val thisMonthCalendar = Calendar.getInstance()
                    val prevMonthCalendar = Calendar.getInstance()
                    if (month!!.toInt() == 1) {
                        thisMonthCalendar[year!!.toInt(), 0] = i
                        prevMonthCalendar[prevYear!!.toInt(), 11] = i
                    } else {
                        thisMonthCalendar[year!!.toInt(), month!!.toInt() - 1] = i
                        prevMonthCalendar[year!!.toInt(), prevMonth!!.toInt() - 1] = i
                    }
                    if (i < 26) {
                        if (isHoliday(holidays, thisMonthCalendar)) {
                            if (dataRow[i] == CONSTANTS.ABSENT || dataRow[i] == "Home") {
                                dataRow[i] = "holiday"
                            } else {
                                dataRow[i] = dataRow[i] + " (holiday)"
                            }
                        }
                    } else {
                        if (isHoliday(holidays, prevMonthCalendar)) {
                            if (dataRow[i] == CONSTANTS.ABSENT || dataRow[i] == "Home" || dataRow[i] == "off") {
                                dataRow[i] = "holiday"
                            } else {
                                dataRow[i] = dataRow[i] + " (holiday)"
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isHoliday(holidays: ArrayList<Holiday>, day: Calendar): Boolean {
        var isHoliday = false
        for (holiday in holidays) {
            if (isEqualOrLater(day, holiday.startCalendar) && isEqualOrEarlier(
                    day,
                    holiday.endCalendar
                )
            ) {
                isHoliday = true
            }
        }
        return isHoliday
    }

    private fun isEqualOrLater(first: Calendar, second: Calendar): Boolean {
        if (first[Calendar.YEAR] > second[Calendar.YEAR]) return true else if (first[Calendar.YEAR] == second[Calendar.YEAR]) {
            if (first[Calendar.MONTH] > second[Calendar.MONTH]) return true else if (first[Calendar.MONTH] == second[Calendar.MONTH]) {
                return first[Calendar.DAY_OF_MONTH] >= second[Calendar.DAY_OF_MONTH]
            }
        }
        return false
    }

    private fun isEqualOrEarlier(first: Calendar, second: Calendar): Boolean {
        if (first[Calendar.YEAR] < second[Calendar.YEAR]) return true else if (first[Calendar.YEAR] == second[Calendar.YEAR]) {
            if (first[Calendar.MONTH] < second[Calendar.MONTH]) return true else if (first[Calendar.MONTH] == second[Calendar.MONTH]) {
                return first[Calendar.DAY_OF_MONTH] <= second[Calendar.DAY_OF_MONTH]
            }
        }
        return false
    }

    private fun addFridays() {
        val it = Calendar.getInstance()
        // set it to be the start of the month
        it[year!!.toInt(), month!!.toInt() - 1] = 1
        for (i in 1 until dataRow.size) {
            if (it[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY) {
                if (dataRow[i] == CONSTANTS.ABSENT || dataRow[i] == "Home") {
                    dataRow[i] = "off"
                } else {
                    dataRow[i] = dataRow[i] + " (off)"
                }
            }
            it.add(Calendar.DAY_OF_MONTH, 1)
            if (it[Calendar.DAY_OF_MONTH] == 26) {
                if (month == "1") it[prevYear!!.toInt(), month!!.toInt() - 1] =
                    1 else it[year!!.toInt(), prevMonth!!.toInt() - 1] = 26
            }
        }
    }

    private fun addVacationLabels(days: Int, vacationLabels: Array<String>, label: String) {
        var index = 0
        for (i in vacationLabels.indices) {
            if (vacationLabels[i] != "") index++ else break
        }
        for (i in index until index + days) {
            vacationLabels[i] = label
        }
    }

    private suspend fun loadWorkingDays(employee: EmployeeOverview, empName: String) {
        val workingDays = ArrayList<WorkingDay>()
        val summaryDoc = CONSTANTS.SUMMARY_COL.document(employee.id).collection("$year-$month")
            .get().await()

        if (summaryDoc.size() == 0) {
            UpdateCSV(workingDays, empName, employee.id)
            return
        }
        for (q in summaryDoc) {
            val day = q.id
            val summary = q.toObject(
                Summary::class.java
            )
            if (summary.checkOut == null) {
                if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == day.toInt()) {
                    Snackbar.make(
                        binding!!.root,
                        "this Employee is still working",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    // continue;
                }
                /*
                * if the employee forgot to checkout
                * check the time spent on all projects
                * add the remainder of 8 hrs to the lastProjectId
                * set checkOut to checkIn
                */
                summary.checkOut = summary.checkIn
                var workingTimeInSeconds =
                    if (summary.workingTime != null) summary.workingTime.values.stream()
                        .mapToLong { v: Long -> v }
                        .sum() else 0
                workingTimeInSeconds =
                    if (EIGHT_HOURS - workingTimeInSeconds < 0) 0 else EIGHT_HOURS - workingTimeInSeconds
                if (summary.workingTime != null) {
                    summary.workingTime.merge(
                        summary.lastProjectId,
                        workingTimeInSeconds
                    ) { a: Long, b: Long -> a + b }
                } else {
                    summary.workingTime = HashMap()
                    summary.workingTime[summary.lastProjectId] = workingTimeInSeconds
                }
            }

            for ((key, value) in summary.projectIds) {
                val projectsDoc = CONSTANTS.PROJECT_COL.document(key).get().await()
                var project: Project? = Project()
                if (!projectsDoc.exists()) {
                    if (key != "HOME") return
                    project!!.name = "Home"
                    project.reference = ""
                    project.locationArea = ""
                    project.locationCity = ""
                    project.locationStreet = ""
                } else {
                    project = projectsDoc.toObject(Project::class.java)
                }
                val hours =
                    if (summary.workingTime == null || !summary.workingTime.containsKey(
                            key
                        )
                    ) 0.0 else summary.workingTime[key]!! / 3600.0
                val checkInGeoHash = summary.checkIn["geohash"] as String?
                val checkInLat = summary.checkIn["lat"] as Double
                val checkInLng = summary.checkIn["lng"] as Double
                val checkOutGeoHash = summary.checkOut["geohash"] as String?
                val checkOutLat = summary.checkOut["lat"] as Double
                val checkOutLng = summary.checkOut["lng"] as Double
                val checkInLocation =
                    LocationDetails(checkInGeoHash, checkInLat, checkInLng)
                val checkOutLocation =
                    LocationDetails(checkOutGeoHash, checkOutLat, checkOutLng)
                val projectLocation = String.format(
                    "%s, %s, %s",
                    project!!.locationCity,
                    project.locationArea,
                    project.locationStreet
                )
                val projectName: String = if (summary.projectIds[key] == "office") {
                    "office"
                } else project.name
                workingDays.add(
                    WorkingDay(
                        day,
                        month,
                        year,
                        hours,
                        empName,
                        checkInLocation,
                        checkOutLocation,
                        projectName,
                        project.reference,
                        projectLocation,
                        summary.projectIds[key]
                    )
                )
                if (summaryDoc.documents.lastIndexOf(q) == summaryDoc.documents.size - 1) {
                    //sort working days by date
                    workingDays.sortWith { o1: WorkingDay, o2: WorkingDay ->
                        val o1Day = o1.day.toInt()
                        val o2Day = o2.day.toInt()
                        o1Day - o2Day
                    }
                    UpdateCSV(
                        workingDays.clone() as ArrayList<WorkingDay>,
                        empName,
                        employee.id
                    )
                }
            }
        }

    }

    private fun openMonthSummaryDialog(position: Int) {
        val employee = employees!![position]
        val workingDays = ArrayList<WorkingDay>()
        val empName = employee.firstName + " " + employee.lastName
        CONSTANTS.SUMMARY_COL.document(employee.id).collection("$year-$month")
            .get().addOnSuccessListener { docs: QuerySnapshot ->
                if (docs.size() == 0) {
                    Snackbar.make(binding!!.root, "No Work is registered", Snackbar.LENGTH_SHORT)
                        .show()
                    return@addOnSuccessListener
                }
                for (q in docs) {
                    val day = q.id
                    val summary = q.toObject(
                        Summary::class.java
                    )
                    if (summary.checkOut == null) {
                        if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == day.toInt()) {
                            Snackbar.make(
                                binding!!.root,
                                "this Employee is still working",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            // continue;
                        }
                        /*
                             * if the employee forgot to checkout
                             * check the time spent on all projects
                             * add the remainder of 8 hrs to the lastProjectId
                             * set checkOut to checkIn
                             */summary.checkOut = summary.checkIn
                        var workingTimeInSeconds =
                            if (summary.workingTime != null) summary.workingTime.values.stream()
                                .mapToLong { v: Long -> v }
                                .sum() else 0
                        workingTimeInSeconds =
                            if (EIGHT_HOURS - workingTimeInSeconds < 0) 0 else EIGHT_HOURS - workingTimeInSeconds
                        if (summary.workingTime != null) {
                            summary.workingTime.merge(
                                summary.lastProjectId,
                                workingTimeInSeconds
                            ) { a: Long, b: Long -> a + b }
                        } else {
                            summary.workingTime = HashMap()
                            summary.workingTime[summary.lastProjectId] = workingTimeInSeconds
                        }
                    }
                    summary.projectIds.keys.forEach(Consumer { pid: String ->
                        CONSTANTS.PROJECT_COL.document(pid).get()
                            .addOnSuccessListener PROJECT@{ doc: DocumentSnapshot ->
                                var project: Project? = Project()
                                if (!doc.exists()) {
                                    if (pid != "HOME") return@PROJECT
                                    project!!.name = "Home"
                                    project.reference = ""
                                    project.locationArea = ""
                                    project.locationCity = ""
                                    project.locationStreet = ""
                                } else {
                                    project = doc.toObject(Project::class.java)
                                }
                                val hours =
                                    if (summary.workingTime == null || !summary.workingTime.containsKey(
                                            pid
                                        )
                                    ) 0.0 else summary.workingTime[pid]!! / 3600.0
                                val checkInGeoHash = summary.checkIn["geohash"] as String?
                                val checkInLat = summary.checkIn["lat"] as Double
                                val checkInLng = summary.checkIn["lng"] as Double
                                val checkOutGeoHash = summary.checkOut["geohash"] as String?
                                val checkOutLat = summary.checkOut["lat"] as Double
                                val checkOutLng = summary.checkOut["lng"] as Double
                                val checkInLocation =
                                    LocationDetails(checkInGeoHash, checkInLat, checkInLng)
                                val checkOutLocation =
                                    LocationDetails(checkOutGeoHash, checkOutLat, checkOutLng)
                                val projectLocation = String.format(
                                    "%s, %s, %s",
                                    project!!.locationCity,
                                    project.locationArea,
                                    project.locationStreet
                                )
                                if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] != day.toInt()) workingDays.add(
                                    WorkingDay(
                                        day,
                                        month,
                                        year,
                                        hours,
                                        empName,
                                        checkInLocation,
                                        checkOutLocation,
                                        project.name,
                                        project.reference,
                                        projectLocation,
                                        summary.projectIds[pid]
                                    )
                                )
                                if (docs.documents.lastIndexOf(q) == docs.documents.size - 1) {
                                    if (opened) return@PROJECT
                                    opened = true
                                    //sort working days by date
                                    workingDays.sortWith { o1: WorkingDay, o2: WorkingDay ->
                                        val o1Day = o1.day.toInt()
                                        val o2Day = o2.day.toInt()
                                        o1Day - o2Day
                                    }
                                    val monthSummaryDialog = MonthSummaryDialog(workingDays)
                                    monthSummaryDialog.show(parentFragmentManager, "")
                                }
                            }
                    })
                }
            }
    }

    fun getEmployees() {
        CONSTANTS.EMPLOYEE_OVERVIEW_REF.addSnapshotListener { documentSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
            val empMap: Map<String, ArrayList<String>>?
            if (e != null) {
                Log.w(ContentValues.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                empMap = documentSnapshot.data as Map<String, ArrayList<String>>?
                retrieveEmployees(empMap)
            } else {
                return@addSnapshotListener
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun retrieveEmployees(empMap: Map<String, ArrayList<String>>?) {
        employees!!.clear()
        for (key in empMap!!.keys) {
            val firstName = empMap[key]!![0]
            val lastName = empMap[key]!![1]
            val title = empMap[key]!![2]
            employees!!.add(EmployeeOverview(firstName, lastName, title, key))
        }
        employees!!.sortWith(Comparator.comparing { obj: EmployeeOverview -> obj.id })
        adapter!!.employeeOverviewsList = employees
        adapter!!.notifyDataSetChanged()
    }

    private val oclMonthPicker = View.OnClickListener { v: View? ->
        val today = Calendar.getInstance()
        val builder = MonthPickerDialog.Builder(
            activity,
            { sMonth: Int, selectedYear: Int ->
                val selectedMonth = sMonth + 1
                binding!!.monthLayout.error = null
                binding!!.monthLayout.isErrorEnabled = false
                binding!!.monthEdit.setText(String.format("%d/%d", selectedMonth, selectedYear))
                year = String.format("%d", selectedYear)
                month = String.format("%02d", selectedMonth)
                if (selectedMonth - 1 == 0) {
                    prevMonth = "12"
                    prevYear = String.format("%d", selectedYear - 1)
                } else {
                    prevMonth = String.format("%02d", selectedMonth - 1)
                    prevYear = year
                }
                selected[selectedYear, selectedMonth - 1] = 1
            }, today[Calendar.YEAR], today[Calendar.MONTH]
        )
        builder.setActivatedMonth(selected[Calendar.MONTH])
            .setActivatedYear(selected[Calendar.YEAR])
            .setMaxYear(today[Calendar.YEAR])
            .setTitle("Select Month")
            .build().show()
    }
    private val oclAll = View.OnClickListener { v: View? ->
        if (binding!!.monthEdit.text.toString().isEmpty()) {
            binding!!.monthLayout.error = "Please select a month"
        } else {
            alertDialog.show()
            uiScope.launch {
                val header = StringJoiner(",")
                header.add("Day")
                var yearNumber = year!!.toInt()
                var monthNumber = month!!.toInt() - 1
                val calendar = Calendar.getInstance()
                //check if month is february using calendar
                if (monthNumber == 0) {
                    monthNumber = 12
                    yearNumber--
                }
                calendar[yearNumber, monthNumber - 1] = 1
                dataRowSize = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) + 1
                for (i in 26 until dataRowSize) {
                    header.add(String.format("%d", i))
                }
                for (i in 1..25) {
                    header.add(String.format("%d", i))
                }
                dataRow = Array(dataRowSize) { "" }
                csvWriter = CsvWriter(*header.toString().split(",").toTypedArray())
                for (emp in employees!!) {
                    val empName = emp.firstName + " " + emp.lastName
                    dataRow[0] = empName
                    loadWorkingDays(emp, empName)
                }
                try {
                    csvWriter!!.build("all_emp-$year-$month")
                    Snackbar.make(binding!!.root, "CSV file created", Snackbar.LENGTH_SHORT)
                        .show()
                    alertDialog.dismiss()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
    private val oclCSV = View.OnClickListener { v: View? ->
        alertDialog.show()
        if (binding!!.monthEdit.text.toString().isEmpty()) {
            binding!!.monthLayout.error = "Please select a month"
        } else {
            uiScope.launch(Dispatchers.IO)
            {

                val header = arrayOf(
                    "Name",
                    "Basic",
                    "over time",
                    "Cuts",
                    "Transportation",
                    "accommodation",
                    "site",
                    "remote",
                    "food",
                    "other",
                    "personal",
                    "Next month",
                    "current month",
                    "previous month"
                )
                val csvWriter = CsvWriter(*header)
                month = String.format(Locale.getDefault(), "%02d", month!!.toInt())
                val employees = CONSTANTS.EMPLOYEE_COL
                    .get().await()
                for (i in 0 until employees.documents.size) {
                    val emp = employees.documents[i].toObject(Employee::class.java)!!
                    val prevGrossSalaryDoc = CONSTANTS.EMPLOYEE_GROSS_SALARY_COL
                        .document(emp.id)
                        .collection(prevYear!!)
                        .document(prevMonth!!)
                        .get()
                        .await()
                    val currentGrossSalaryDoc = CONSTANTS.EMPLOYEE_GROSS_SALARY_COL
                        .document(emp.id)
                        .collection(year!!)
                        .document(month!!)
                        .get()
                        .await()
                    var cuts = 0.0
                    var transportation = 0.0
                    var accommodation = 0.0
                    var site = 0.0
                    var remote = 0.0
                    var food = 0.0
                    var other = 0.0
                    var overTime = 0.0
                    var personal = 0.0
                    var previousMonth = 0.0
                    if (currentGrossSalaryDoc.exists()) {
                        val currentGrossSalary =
                            currentGrossSalaryDoc.toObject(EmployeesGrossSalary::class.java)!!

                        for (allowance in currentGrossSalary.allTypes) {
                            if (allowance.type != AllowancesEnum.NETSALARY.ordinal) {
                                if (allowance.name.trim { it <= ' ' }
                                        .equals(
                                            "Transportation",
                                            ignoreCase = true
                                        )) {
                                    transportation += allowance.amount
                                } else if (allowance.name.trim { it <= ' ' }
                                        .equals(
                                            "accommodation",
                                            ignoreCase = true
                                        )) {
                                    accommodation += allowance.amount
                                } else if (allowance.name.trim { it <= ' ' }
                                        .equals("site", ignoreCase = true)) {
                                    site += allowance.amount
                                } else if (allowance.name.trim { it <= ' ' }
                                        .equals("remote", ignoreCase = true)) {
                                    remote += allowance.amount
                                } else if (allowance.name.trim { it <= ' ' }
                                        .equals("food", ignoreCase = true)) {
                                    food += allowance.amount
                                } else if (allowance.type == AllowancesEnum.RETENTION.ordinal) {
                                    cuts += allowance.amount
                                } else if (allowance.type == AllowancesEnum.BONUS.ordinal) {
                                    personal += allowance.amount
                                } else if (allowance.type == AllowancesEnum.OVERTIME.ordinal) {
                                    overTime += allowance.amount
                                } else {
                                    other += allowance.amount
                                }
                            }
                        }
                    }
                    val nextMonth: Double =
                        other + personal + accommodation + site + remote + food
                    val currentMonth: Double =
                        transportation + emp.salary + cuts + overTime

                    if (!prevGrossSalaryDoc.exists()) previousMonth = 0.0 else {
                        for (allowance in prevGrossSalaryDoc.toObject(
                            EmployeesGrossSalary::class.java
                        )!!.allTypes) {
                            if (allowance.type != AllowancesEnum.NETSALARY.ordinal) {
                                if (allowance.name.trim { it <= ' ' }
                                        .equals(
                                            "Transportation",
                                            ignoreCase = true
                                        )
                                ) continue
                                previousMonth += if (allowance.type == AllowancesEnum.RETENTION.ordinal) {
                                    continue
                                } else if (allowance.type == AllowancesEnum.OVERTIME.ordinal) {
                                    continue
                                } else {
                                    allowance.amount
                                }
                            }
                        }
                    }
                    csvWriter.addDataRow(
                        String.format(
                            "%s %s",
                            emp.firstName,
                            emp.lastName
                        ),
                        emp.salary.toString(),
                        overTime.toString(),
                        cuts.toString(),
                        transportation.toString(),
                        accommodation.toString(),
                        site.toString(),
                        remote.toString(),
                        food.toString(),
                        other.toString(),
                        personal.toString(),
                        nextMonth.toString(),
                        currentMonth.toString(),
                        previousMonth.toString()
                    )
                }
                createCSV(csvWriter)
            }
        }
    }


    private fun createCSV(csvWriter: CsvWriter) {
        try {
            csvWriter.build("$year-$month")
            Snackbar.make(
                binding!!.root,
                "CSV file created",
                Snackbar.LENGTH_SHORT
            ).show()
            alertDialog.dismiss()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private val oclEmployee = EmployeeAdapter.OnItemClickListener { position ->
        val today = Calendar.getInstance()
        @SuppressLint("DefaultLocale") val builder = MonthPickerDialog.Builder(
            activity,
            { sMonth: Int, selectedYear: Int ->
                val selectedMonth = sMonth + 1
                binding!!.monthLayout.error = null
                binding!!.monthLayout.isErrorEnabled = false
                binding!!.monthEdit.setText(String.format("%d/%d", selectedMonth, selectedYear))
                val selectedDate = binding!!.monthEdit.text.toString().split("/").toTypedArray()
                year = selectedDate[1]
                month = selectedDate[0]
                month = String.format("%02d", month!!.toInt())
                if (month!!.toInt() - 1 < 1) {
                    prevMonth = "12"
                    prevYear = (year!!.toInt() - 1).toString()
                } else {
                    prevMonth = (month!!.toInt() - 1).toString()
                    prevYear = year
                }
                prevMonth = String.format("%02d", prevMonth!!.toInt())
                openMonthSummaryDialog(position)
            }, today[Calendar.YEAR], today[Calendar.MONTH]
        )
        val monthPickerDialog = builder.setActivatedMonth(today[Calendar.MONTH])
            .setMinYear(today[Calendar.YEAR] - 1)
            .setActivatedYear(today[Calendar.YEAR])
            .setMaxYear(today[Calendar.YEAR] + 1)
            .setTitle("Select Month")
            .build()
        if (binding!!.monthEdit.text.toString().isEmpty()) monthPickerDialog.show() else {
            val selectedDate = binding!!.monthEdit.text.toString().split("/").toTypedArray()
            year = selectedDate[1]
            month = selectedDate[0]
            month = String.format("%02d", month!!.toInt())
            openMonthSummaryDialog(position)
        }
    }
}