package com.igec.admin.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.*
import com.igec.admin.R
import com.igec.admin.databinding.FragmentAddUserBinding
import com.igec.admin.fragments.UsersFragment
import com.igec.common.CONSTANTS
import com.igec.common.CONSTANTS.EMPLOYEE_GROSS_SALARY_COL
import com.igec.common.cryptography.RSAUtil
import com.igec.common.firebase.Allowance
import com.igec.common.firebase.Employee
import com.igec.common.firebase.EmployeeOverview
import com.igec.common.firebase.EmployeesGrossSalary
import com.igec.common.utilities.AllowancesEnum.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors


class UserFragmentDialog : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullscreenDialogTheme)
        parentFragmentManager.setFragmentResultListener(
            "allowances", this
        ) { _: String?, bundle: Bundle ->
            allowances = bundle.getParcelableArrayList("allowances")!!
        }
    }

    // Views
    private val vDatePickerBuilder = MaterialDatePicker.Builder.datePicker()
    private var vDatePicker: MaterialDatePicker<*>? = null
    private var views: ArrayList<Pair<TextInputLayout, EditText>>? = null

    //Var
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var allowances: ArrayList<Allowance>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var employee: Employee
    private var oldEmployeeOverviewData: EmployeeOverview? = null
    private val oldNetSalary = Allowance()
    private var hireDate: Long = 0
    private lateinit var year: String
    private lateinit var month: String
    private lateinit var day: String
    private var batch = db.batch()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val window = dialog.window
        if (window != null) {
            dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED)
        }
        return dialog
    }

    private lateinit var binding: FragmentAddUserBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        // Listeners
        binding.allowancesButton.setOnClickListener(oclAllowances)
        binding.emailEdit.addTextChangedListener(twEmail)
        vDatePicker!!.addOnPositiveButtonClickListener(pclDatePicker)
        binding.hireDateLayout.setEndIconOnClickListener(oclHireDate)
        binding.hireDateLayout.setErrorIconOnClickListener(oclHireDate)
        binding.updateButton.setOnClickListener(clUpdate)
        binding.deleteButton.setOnClickListener(clDelete)
        binding.unlockButton.setOnClickListener(clUnlock)
        binding.adminCheckbox.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            binding.deleteButton.isEnabled = !b
        }
        binding.passwordLayout.setEndIconOnClickListener(oclPasswordGenerate)
        for (v in views!!) {
            if (v.first !== binding.emailLayout) v.second.addTextChangedListener(object :
                TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence, i: Int, i1: Int, i2: Int
                ) {
                }

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {
                    v.first.error = null
                    v.first.isErrorEnabled = false
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val parent = parentFragmentManager.fragments.size - 1
        (parentFragmentManager.fragments[parent] as UsersFragment).setOpened(false)
    }

    override fun onResume() {
        super.onResume()
        val currencies = ArrayList<String>()
        currencies.add("EGP")
        currencies.add("SAR")
        val currenciesAdapter = ArrayAdapter(
            requireActivity(), R.layout.item_dropdown, currencies
        )
        binding.currencyAuto.setAdapter(currenciesAdapter)
    }

    // Functions
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initialize() {
        allowances = ArrayList()
        employee = requireArguments().getSerializable("employee") as Employee
        vDatePickerBuilder.setTitleText("Hire Date")
        vDatePicker = vDatePickerBuilder.build()
        binding.registerButton.visibility = View.GONE
        binding.allowancesButton.visibility = View.VISIBLE
        binding.deleteButton.visibility = View.VISIBLE
        binding.updateButton.visibility = View.VISIBLE
        binding.unlockButton.visibility = View.VISIBLE
        val state =
            if (employee.isLocked) requireActivity().getDrawable(R.drawable.ic_outline_lock_24) else requireActivity().getDrawable(
                R.drawable.ic_round_lock_open_24
            )
        binding.unlockButton.icon = state
        views = ArrayList()
        views!!.add(
            Pair(
                binding.firstNameLayout, binding.firstNameEdit
            )
        )
        views!!.add(
            Pair(
                binding.secondNameLayout, binding.secondNameEdit
            )
        )
        views!!.add(
            Pair(
                binding.emailLayout, binding.emailEdit
            )
        )
        views!!.add(
            Pair(
                binding.passwordLayout, binding.passwordEdit
            )
        )
        views!!.add(
            Pair(
                binding.phoneLayout, binding.phoneEdit
            )
        )
        views!!.add(
            Pair(
                binding.titleLayout, binding.titleEdit
            )
        )
        views!!.add(
            Pair(
                binding.salaryLayout, binding.salaryEdit
            )
        )
        views!!.add(
            Pair(
                binding.currencyLayout, binding.currencyAuto
            )
        )
        views!!.add(
            Pair(
                binding.insuranceNumberLayout, binding.insuranceNumberEdit
            )
        )
        views!!.add(
            Pair(
                binding.insuranceAmountLayout, binding.insuranceAmountEdit
            )
        )
        views!!.add(
            Pair(
                binding.areaLayout, binding.areaEdit
            )
        )
        views!!.add(
            Pair(
                binding.cityLayout, binding.cityEdit
            )
        )
        views!!.add(
            Pair(
                binding.streetLayout, binding.streetEdit
            )
        )
        views!!.add(
            Pair(
                binding.hireDateLayout, binding.hireDateEdit
            )
        )
        views!!.add(
            Pair(
                binding.nationalIdLayout, binding.nationalIdEdit
            )
        )
        binding.firstNameEdit.setText(employee.firstName)
        binding.secondNameEdit.setText(employee.lastName)
        binding.titleEdit.setText(employee.title)
        binding.areaEdit.setText(employee.area)
        binding.cityEdit.setText(employee.city)
        binding.streetEdit.setText(employee.street)
        binding.emailEdit.setText(employee.email)
        binding.salaryEdit.setText(employee.salary.toString())
        binding.currencyAuto.setText(employee.currency)
        binding.nationalIdEdit.setText(employee.ssn)
        binding.passwordEdit.setText(employee.decryptedPassword)
        binding.phoneEdit.setText(employee.phoneNumber)
        binding.adminCheckbox.isChecked = employee.isAdmin
        binding.managerCheckbox.isEnabled = employee.managerID == null
        binding.managerCheckbox.isChecked = employee.isManager
        binding.temporaryCheckbox.isChecked = employee.isTemporary
        binding.insuranceNumberEdit.setText(employee.insuranceNumber)
        binding.insuranceAmountEdit.setText(employee.insuranceAmount.toString())
        binding.hireDateEdit.setText(CONSTANTS.convertDateToString(employee.hireDate.time))
        hireDate = employee.hireDate.time
        vDatePickerBuilder.setTitleText("Hire Date")
        vDatePicker = vDatePickerBuilder.setSelection(hireDate).build()
        binding.passwordLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
        binding.passwordLayout.setEndIconDrawable(R.drawable.ic_baseline_autorenew_24)
        binding.passwordEdit.isEnabled = false
        binding.deleteButton.isEnabled = false
        // can't remove employee without having checkout all his machines
        CONSTANTS.MACHINE_EMPLOYEE_COL.whereEqualTo("employee.id", employee.id)
            .addSnapshotListener { docs: QuerySnapshot?, e: FirebaseFirestoreException? ->
                // no machines found = enabled X
                // a machine without a check-out = disabled
                // all machines have been checked-out = enabled
                if (e != null || docs!!.isEmpty) {
                    binding.deleteButton.isEnabled =
                        employee.managerID == null && !employee.isAdmin // not working
                    return@addSnapshotListener
                }
                for (doc in docs) {
                    if (doc["checkOut"] == null || (doc["checkOut"] as HashMap<*, *>?)!!.size == 0) {
                        binding.deleteButton.isEnabled = false
                        return@addSnapshotListener
                    }
                    binding.deleteButton.isEnabled =
                        employee.managerID == null && !employee.isAdmin // not working
                }
            }
    }

    private fun generateError(): Boolean {
        for (view in views!!) {
            if (view.second.text.toString().trim { it <= ' ' }.isEmpty()) {
                view.first.error = "Missing"
                return true
            }
            if (view.first.error != null) {
                return true
            }
        }
        val isNationalIdValid = binding.nationalIdEdit.text.toString().length == 14
        if (!isNationalIdValid) {
            binding.nationalIdLayout.error = "Must be 14 digits"
            return true
        }
        return false
    }

    private fun validateInputs(): Boolean {
        return !generateError()
    }

    // update data
    private suspend fun updateEmployee() {
        updateDate()
        // check if the new e-mail is taken
        if (isEmailTaken()) return
        // employeeOverview
        updateEmployeeOverview()
        // employees
        updateEmployeeData()
        // grossSalary
        val doc = EMPLOYEE_GROSS_SALARY_COL.document(employee.id).collection(year)
            .document(month).get().await()
        if (oldNetSalary.amount != binding.salaryEdit.text.toString()
                .toDouble() || oldNetSalary.currency == null //for current data as some employees don't have currency
            || oldNetSalary.currency != binding.currencyAuto.text.toString()
        ) {
            // remove old grossSalary
            if (!doc.exists()) {
                batch.update(
                    EMPLOYEE_GROSS_SALARY_COL.document(employee.id),
                    "allTypes",
                    FieldValue.arrayRemove(oldNetSalary)
                )
                // update netSalary
                oldNetSalary.currency = binding.currencyAuto.text.toString()
                oldNetSalary.amount = binding.salaryEdit.text.toString().toDouble()
                // update grossSalary
                batch.update(
                    EMPLOYEE_GROSS_SALARY_COL.document(employee.id),
                    "allTypes",
                    FieldValue.arrayUnion(oldNetSalary)
                )
            } else {
                batch.update(
                    EMPLOYEE_GROSS_SALARY_COL.document(employee.id),
                    "allTypes",
                    FieldValue.arrayRemove(oldNetSalary)
                )
                batch.update(
                    EMPLOYEE_GROSS_SALARY_COL.document(employee.id).collection(
                        year
                    ).document(month), "allTypes", FieldValue.arrayRemove(oldNetSalary)
                )

                // update netSalary
                oldNetSalary.currency = binding.currencyAuto.text.toString()
                oldNetSalary.amount = binding.salaryEdit.text.toString().toDouble()
                batch.update(
                    EMPLOYEE_GROSS_SALARY_COL.document(employee.id),
                    "allTypes",
                    FieldValue.arrayUnion(oldNetSalary)
                )
                batch.update(
                    EMPLOYEE_GROSS_SALARY_COL.document(employee.id).collection(
                        year
                    ).document(month), "allTypes", FieldValue.arrayUnion(oldNetSalary)
                )
            }

        }

        val oneTimeAllowances = ArrayList<Allowance>()
        val permanentAllowances = ArrayList<Allowance>()
        val value = EMPLOYEE_GROSS_SALARY_COL.document(employee.id).get().await()
        if (!value.exists()) return
        for (allowance in allowances) {
            if (allowance.type == BONUS.ordinal || allowance.type == RETENTION.ordinal) {
                oneTimeAllowances.add(allowance)
            } else {
                permanentAllowances.add(allowance)
            }
        }
        val employeesGrossSalary1 = value.toObject(
            EmployeesGrossSalary::class.java
        )!!
        employeesGrossSalary1.allTypes.removeIf { allowance: Allowance -> allowance.type != NETSALARY.ordinal && allowance.type != PROJECT.ordinal }
        employeesGrossSalary1.allTypes.addAll(permanentAllowances)
        EMPLOYEE_GROSS_SALARY_COL.document(employee.id)
            .update("allTypes", employeesGrossSalary1.allTypes)
        if (!doc.exists()) {
            //new month
            //add project allowances
            employeesGrossSalary1.baseAllowances =
                employeesGrossSalary1.allTypes.stream().filter { x: Allowance ->
                    x.projectId.trim { it <= ' ' }.isNotEmpty()
                }.collect(Collectors.toCollection { ArrayList() })
            employeesGrossSalary1.allTypes.removeIf { x: Allowance -> x.type != NETSALARY.ordinal }
            employeesGrossSalary1.allTypes.addAll(oneTimeAllowances)
            employeesGrossSalary1.baseAllowances.addAll(permanentAllowances)
            db.document(doc.reference.path)[employeesGrossSalary1] =
                SetOptions.mergeFields("allTypes", "baseAllowances")
            return
        }
        val employeesGrossSalary = doc.toObject(EmployeesGrossSalary::class.java)!!
        employeesGrossSalary.baseAllowances.removeIf { x: Allowance -> x.type != PROJECT.ordinal }
        employeesGrossSalary.baseAllowances.addAll(permanentAllowances)
        employeesGrossSalary.allTypes.removeIf { x: Allowance -> x.type == RETENTION.ordinal || x.type == BONUS.ordinal }
        employeesGrossSalary.allTypes.addAll(oneTimeAllowances)
        db.document(doc.reference.path).update(
            "allTypes",
            employeesGrossSalary.allTypes,
            "baseAllowances",
            employeesGrossSalary.baseAllowances
        ).addOnSuccessListener {
            oneTimeAllowances.clear()
            permanentAllowances.clear()
        }

    }

    private suspend fun isEmailTaken(): Boolean {
        val documents = CONSTANTS.EMPLOYEE_COL.whereEqualTo("email",
            binding.emailEdit.text.toString().trim { it <= ' ' }).whereNotEqualTo("id", employee.id)
            .get().await()
        if (documents.documents.size != 0) {
            binding.emailLayout.error = "This Email already exists"
            binding.updateButton.isEnabled = true
            return true
        }
        return false
    }

    private fun updateDate() {
        val calendar = Calendar.getInstance()
        year = calendar[Calendar.YEAR].toString()
        month = String.format("%02d", calendar[Calendar.MONTH] + 1)
        day = String.format("%02d", calendar[Calendar.DAY_OF_MONTH])
        if (day.toInt() > 25) {
            if (month.toInt() + 1 == 13) {
                month = "01"
                year = String.format("%d", year.toInt() + 1)
            } else {
                month = String.format("%02d", month.toInt() + 1)
            }
        }
    }

    private fun updateEmployeeOverview() {
        val updatedEmpOverviewMap: MutableMap<String, Any> = HashMap()
        val empInfo = ArrayList<Any>()
        empInfo.add(binding.firstNameEdit.text.toString())
        empInfo.add(binding.secondNameEdit.text.toString())
        empInfo.add(binding.titleEdit.text.toString())
        empInfo.add(employee.managerID)
        empInfo.add(object : HashMap<String?, Any?>() {
            init {
                put("pids", employee.projectIds)
            }
        })
        empInfo.add(employee.managerID != null)
        empInfo.add(binding.managerCheckbox.isChecked)
        updatedEmpOverviewMap[employee.id] = empInfo
        batch.update(CONSTANTS.EMPLOYEE_OVERVIEW_REF, updatedEmpOverviewMap)
    }

    private suspend fun updateMachineEmployee() {
        val documentSnapshots =
            CONSTANTS.MACHINE_EMPLOYEE_COL.whereEqualTo("employee.id", employee.id).get().await()
        for (d in documentSnapshots) {
            batch.update(
                CONSTANTS.MACHINE_EMPLOYEE_COL.document(d.id), "employee", employee
            )
        }
    }

    private suspend fun updateVacation() {
        val queryDocumentSnapshots =
            CONSTANTS.VACATION_COL.whereEqualTo("employee.id", employee.id).get().await()

        for (d in queryDocumentSnapshots) {
            batch.update(CONSTANTS.VACATION_COL.document(d.id), "employee", employee)
        }
        val queryDocumentSnapshot =
            CONSTANTS.VACATION_COL.whereEqualTo("manager.id", employee.id).get().await()

        for (d in queryDocumentSnapshot) {
            batch.update(CONSTANTS.VACATION_COL.document(d.id), "manager", employee)
        }
    }

    private fun updateProjects() {
        val tempEmp = EmployeeOverview(
            binding.firstNameEdit.text.toString(),
            binding.secondNameEdit.text.toString(),
            binding.titleEdit.text.toString(),
            employee.id,
            employee.projectIds,
            employee.projectIds.size != 0
        )
        tempEmp.managerID = employee.managerID
        if (employee.projectIds.size == 0) {
            return
        }
        // update employee data in project
        for (pid in employee.projectIds) {
            batch.update(
                CONSTANTS.PROJECT_COL.document(pid),
                "employees",
                FieldValue.arrayRemove(oldEmployeeOverviewData)
            )
            if (tempEmp.managerID == CONSTANTS.ADMIN) {
                batch.update(
                    CONSTANTS.PROJECT_COL.document(pid),
                    "managerName",
                    tempEmp.firstName + " " + tempEmp.lastName,
                    "employees",
                    FieldValue.arrayUnion(tempEmp)
                )
            } else {
                batch.update(
                    CONSTANTS.PROJECT_COL.document(pid), "employees", FieldValue.arrayUnion(tempEmp)
                )
            }
        }

    }

    private fun updateEmployeeData() {
        oldNetSalary.amount = employee.salary
        oldNetSalary.type = NETSALARY.ordinal
        oldNetSalary.currency = employee.currency
        oldNetSalary.name = "Net salary"
        oldEmployeeOverviewData = EmployeeOverview(
            employee.firstName,
            employee.lastName,
            employee.title,
            employee.id,
            employee.projectIds,
            employee.projectIds.size != 0
        )
        oldEmployeeOverviewData!!.isManager = binding.managerCheckbox.isChecked
        oldEmployeeOverviewData!!.managerID = employee.managerID
        employee.isAdmin = binding.adminCheckbox.isChecked
        employee.area = binding.areaEdit.text.toString()
        employee.city = binding.cityEdit.text.toString()
        employee.currency = binding.currencyAuto.text.toString()
        employee.email = binding.emailEdit.text.toString()
        employee.firstName = binding.firstNameEdit.text.toString()
        employee.hireDate = Date(hireDate)
        employee.insuranceAmount = binding.insuranceAmountEdit.text.toString().toDouble()
        employee.insuranceNumber = binding.insuranceNumberEdit.text.toString()
        employee.lastName = binding.secondNameEdit.text.toString()
        employee.overTime = binding.salaryEdit.text.toString().toDouble() / 30.0 / 10.0 * 1.5
        employee.password = encryptedPassword()
        employee.phoneNumber = binding.phoneEdit.text.toString()
        employee.salary = binding.salaryEdit.text.toString().toDouble()
        employee.ssn = binding.nationalIdEdit.text.toString()
        employee.street = binding.streetEdit.text.toString()
        employee.isTemporary = binding.temporaryCheckbox.isChecked
        employee.title = binding.titleEdit.text.toString()
        employee.isManager = binding.managerCheckbox.isChecked
        batch[CONSTANTS.EMPLOYEE_COL.document(employee.id), employee] = SetOptions.merge()
    }

    // delete
    private suspend fun deleteEmployee() {
        batch = db.batch()
        // employee
        batch.delete(CONSTANTS.EMPLOYEE_COL.document(employee.id))

        // employeeOverview
        batch.update(CONSTANTS.EMPLOYEE_OVERVIEW_REF, employee.id, FieldValue.delete())
        if (employee.projectIds.size != 0) for (pid in employee.projectIds) batch.update(
            CONSTANTS.PROJECT_COL.document(pid),
            "employees",
            FieldValue.arrayRemove(oldEmployeeOverviewData)
        )
        val documentQuery = CONSTANTS.VACATION_COL.whereEqualTo("employee.id", employee.id)
            .whereEqualTo("vacationStatus", CONSTANTS.PENDING).get().await()

        for (d in documentQuery) {
            batch.delete(CONSTANTS.VACATION_COL.document(d.id))
        }
        batch.commit().addOnSuccessListener {
            Snackbar.make(binding.root, "Deleted", Snackbar.LENGTH_SHORT).show()
            binding.deleteButton.isEnabled = true
            dismiss()
        }.addOnFailureListener { e: Exception ->
            Toast.makeText(
                activity, e.toString(), Toast.LENGTH_SHORT
            ).show()
        }

    }

    // helper functions
    private fun encryptedPassword(): String? {
        return try {
            Base64.getEncoder().encodeToString(
                RSAUtil.encrypt(
                    binding.passwordEdit.text.toString()
                )
            )
        } catch (e: Exception) {
            Log.e("error in encryption", e.toString())
            null
        }
    }

    // Listeners
    private val clUpdate = View.OnClickListener {
        if (!validateInputs()) return@OnClickListener
        binding.updateButton.isEnabled = false
        uiScope.launch {
            updateEmployee()
            updateMachineEmployee()
            updateProjects()
            updateVacation()
            batch.commit().addOnSuccessListener {
                binding.updateButton.isEnabled = true
                Snackbar.make(binding.root, "Updated", Snackbar.LENGTH_SHORT).show()
                dismiss()
            }.addOnFailureListener { e: Exception ->
                Toast.makeText(
                    activity, e.toString(), Toast.LENGTH_SHORT
                ).show()
            }.addOnCompleteListener { batch = db.batch() }
        }
    }
    private val clDelete = View.OnClickListener {
        binding.deleteButton.isEnabled = false
        val builder = MaterialAlertDialogBuilder(
            requireActivity()
        )
        builder.setTitle(getString(R.string.Delete)).setMessage(getString(R.string.AreUSure))
            .setNegativeButton(getString(R.string.no)) { _: DialogInterface?, _: Int ->
                binding.deleteButton.isEnabled = true
            }
            .setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, _: Int ->
                uiScope.launch {
                    deleteEmployee()
                    dialogInterface.dismiss()
                }
            }.show()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private val clUnlock = View.OnClickListener {
        if (!employee.isLocked) {
            Snackbar.make(binding.root, "E-mail is already unlocked", Snackbar.LENGTH_SHORT).show()
        } else {
            employee.isLocked = false
            binding.unlockButton.icon =
                requireActivity().getDrawable(R.drawable.ic_round_lock_open_24)
            CONSTANTS.EMPLOYEE_COL.document(employee.id)
                .set(employee, SetOptions.mergeFields("locked")).addOnSuccessListener {
                    Snackbar.make(
                        binding.root, "E-mail unlocked", Snackbar.LENGTH_SHORT
                    ).show()
                }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private val oclAllowances = View.OnClickListener { _: View? ->
        updateDate()
        uiScope.launch {
            val doc = EMPLOYEE_GROSS_SALARY_COL.document(employee.id).collection(year)
                .document(month).get().await()
            if (doc.exists()) {
                val employeesGrossSalary = doc.toObject(
                    EmployeesGrossSalary::class.java
                )!!
                // Adds only Bonuses and penalties
                // get (penalty || bonus )
                employeesGrossSalary.allTypes.stream()
                    .filter { allowance: Allowance -> allowance.type == RETENTION.ordinal || allowance.type == BONUS.ordinal }
                    .forEach { allowance: Allowance -> allowances.add(allowance) }
                //get baseAllowances
                employeesGrossSalary.baseAllowances.stream()
                    .filter { allowance: Allowance -> allowance.type != PROJECT.ordinal }
                    .forEach { allowance: Allowance -> allowances.add(allowance) }
            }
            val dialog = AddAllowanceDialog(
                allowances.clone() as ArrayList<Allowance>?, employee.salary, employee.currency
            )
            dialog.show(parentFragmentManager, "AllowancesDialog")
        }
    }
    private val oclHireDate = View.OnClickListener {
        if (!vDatePicker!!.isVisible) vDatePicker!!.show(
            requireFragmentManager(), "DATE_PICKER"
        )
    }
    private val pclDatePicker: MaterialPickerOnPositiveButtonClickListener<in Any> =
        MaterialPickerOnPositiveButtonClickListener<Any?> { selection ->
            binding.hireDateEdit.setText(CONSTANTS.convertDateToString(selection as Long))
            hireDate = selection
        }
    private val twEmail: TextWatcher = object : TextWatcher {
        private val mPattern =
            Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])")

        private fun isValid(s: CharSequence): Boolean {
            return s.toString() == "" || mPattern.matcher(s).matches()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            if (!isValid(s)) {
                binding.emailLayout.error = "Wrong E-mail form"
            } else {
                binding.emailLayout.error = null
            }
        }
    }
    private val oclPasswordGenerate = View.OnClickListener {
        binding.passwordEdit.setText("1234")
        binding.passwordEdit.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
    }

    companion object {
        @JvmStatic
        fun newInstance(employee: Employee?): UserFragmentDialog {
            val args = Bundle()
            args.putSerializable("employee", employee)
            val fragment = UserFragmentDialog()
            fragment.arguments = args
            return fragment
        }
    }
}