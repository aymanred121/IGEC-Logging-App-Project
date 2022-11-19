package com.igec.user.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.igec.common.CONSTANTS
import com.igec.common.cryptography.RSAUtil
import com.igec.common.firebase.Employee
import com.igec.common.firebase.EmployeesGrossSalary
import com.igec.common.firebase.Project
import com.igec.user.CacheDirectory
import com.igec.user.databinding.ActivityLoginBinding
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    // Overrides
    private var binding: ActivityLoginBinding? = null

    @RequiresApi(api = Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(binding!!.root)
        //validateDate(this)
        initialize()
        // Listeners
        binding!!.emailEdit.addTextChangedListener(twEmail)
        binding!!.signInButton.setOnClickListener(clSignIn)
    }

    override fun onResume() {
        super.onResume()
        //validateDate(this)
    }

    private fun validateDate(c: Context) {
        if (Settings.Global.getInt(c.contentResolver, Settings.Global.AUTO_TIME, 0) != 1) {
            val intent = Intent(this@LoginActivity, DateInaccurate::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    // Functions
    private fun initialize() {
        locationPermissions()
        cameraPermission()
    }

    @AfterPermissionGranted(CONSTANTS.LOCATION_REQUEST_CODE)
    private fun locationPermissions(): Boolean {
        val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        return if (EasyPermissions.hasPermissions(this, *perms)) {
            true
        } else {
            EasyPermissions.requestPermissions(
                this,
                "We need location permissions in order to the app to functional correctly",
                CONSTANTS.LOCATION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            false
        }
    }

    @AfterPermissionGranted(CONSTANTS.CAMERA_REQUEST_CODE)
    private fun cameraPermission(): Boolean {
        val perms = arrayOf(Manifest.permission.CAMERA)
        return if (EasyPermissions.hasPermissions(this, *perms)) {
            true
        } else {
            EasyPermissions.requestPermissions(
                this, "We need camera permission in order to be able to scan the qr code",
                CONSTANTS.CAMERA_REQUEST_CODE, *perms
            )
            false
        }
    }

    private fun isPasswordRight(password: String): Boolean {
        try {
            val decryptedPassword = RSAUtil.decrypt(password, RSAUtil.privateKey)
            if (binding!!.passwordEdit.text != null && binding!!.passwordEdit.text.toString() != decryptedPassword) {
                Snackbar.make(
                    binding!!.root,
                    "please enter a valid email or password",
                    Snackbar.LENGTH_SHORT
                ).show()
                return false
            }
        } catch (e: Exception) {
            Snackbar.make(
                binding!!.root,
                "please enter a valid email or password",
                Snackbar.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private fun hideError(textInputLayout: TextInputLayout) {
        textInputLayout.isErrorEnabled = textInputLayout.error != null
    }

    // Listeners
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
                binding!!.emailLayout.error = "Wrong E-mail form"
            } else {
                binding!!.emailLayout.error = null
            }
            hideError(binding!!.emailLayout)
            binding!!.passwordLayout.error = null
            hideError(binding!!.passwordLayout)
        }
    }
    private val clSignIn = View.OnClickListener { /*
            *
            * wrong email or password -> show Snack bar with info [x]
            * Correct email and password:
            *           - locked -> this email is already in use [x]
            *           - unlocked -> save shared preference ,open suitable dashboard [x]
            *
            * */
        CONSTANTS.EMPLOYEE_COL
            .whereEqualTo(
                "email",
                if (binding!!.emailEdit.text != null) binding!!.emailEdit.text.toString() else ""
            )
            .limit(1)
            .get().addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                if (queryDocumentSnapshots.size() == 0) {
                    Snackbar.make(
                        binding!!.root,
                        "please enter a valid email or password",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return@addOnSuccessListener
                }
                val d = queryDocumentSnapshots.documents[0]
                if (d.exists()) {
                    val currEmployee = d.toObject(Employee::class.java)!!
                    if (!isPasswordRight(currEmployee.password)) {
                        return@addOnSuccessListener
                    }
                    if (currEmployee.isLocked) {
                        Snackbar.make(
                            binding!!.root,
                            "This email is already in use",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        return@addOnSuccessListener
                    }
                    currEmployee.isLocked = true
                    CONSTANTS.EMPLOYEE_COL.document(currEmployee.id)
                        .set(currEmployee, SetOptions.merge())
                        .addOnSuccessListener EmployeeColListener@{
                            CONSTANTS.PROJECT_COL.get().addOnSuccessListener { doc ->
                                val projects = doc.toObjects(Project::class.java)
                                val gson = Gson()
                                val json = gson.toJson(projects)
                                CacheDirectory.writeAllCachedText(this, "projects.json", json)
                                CONSTANTS.EMPLOYEE_GROSS_SALARY_COL.document(currEmployee.id).get().addOnSuccessListener{ doc ->
                                   val grossSalary = doc.toObject(EmployeesGrossSalary::class.java)
                                    CacheDirectory.writeAllCachedText(this, "baseAllowances.json", gson.toJson(grossSalary))

                                    val intent: Intent = when (currEmployee.managerID) {
                                        CONSTANTS.ADMIN -> {
                                            Intent(this@LoginActivity, MDashboard::class.java)
                                        }
                                        else -> {
                                            Intent(this@LoginActivity, EDashboard::class.java)
                                        }
                                    }
                                    intent.putExtra("user", currEmployee)
                                    val sharedPreferences =
                                        getSharedPreferences(CONSTANTS.IGEC, MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putBoolean(CONSTANTS.LOGGED, true)
                                    editor.putString(CONSTANTS.ID, currEmployee.id)
                                    editor.apply()
                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    startActivity(intent)
                                    finish()
                                }

                            }

                        }
                }
            }
    }
}