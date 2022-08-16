package com.igec.admin.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Pair
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.igec.common.CONSTANTS
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.QuerySnapshot
import com.igec.admin.databinding.ActivityLoginBinding
import com.igec.common.cryptography.RSAUtil
import com.igec.common.firebase.Employee
import java.lang.Exception
import java.util.*
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    private var views: ArrayList<Pair<TextInputLayout, TextInputEditText>>? = null
    private var binding: ActivityLoginBinding? = null
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
                    val intent = Intent(this@LoginActivity, InternetConnection::class.java)
                    startActivity(intent)
                    finish()
                }
            })
        }
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        initialize()
        // Listeners
        binding!!.emailEdit.addTextChangedListener(twEmail)
        binding!!.passwordEdit.addTextChangedListener(twPassword)
        binding!!.signInButton.setOnClickListener(clSignIn)
    }
    // Functions
    private fun initialize() {
        views = ArrayList()
        views!!.add(
            Pair(
                binding!!.emailLayout, binding!!.emailEdit
            )
        )
        views!!.add(
            Pair(
                binding!!.passwordLayout, binding!!.passwordEdit
            )
        )
    }

    private fun generateError(): Boolean {
        for (view in views!!) {
            // check if its missing error
            if (Objects.requireNonNull(view.second.text).toString().trim { it <= ' ' }
                    .isEmpty()) view.first.error = "Missing"
            // check for other errors generated via text watchers
            if (view.first.error != null) {
                return true
            }
        }
        return false
    }

    private fun validateInput(): Boolean {
        return !generateError()
    }
    private fun isPasswordRight(password: String): Boolean {
        try {
            val decryptedPassword = RSAUtil.decrypt(password, RSAUtil.privateKey)
            if (binding!!.passwordEdit.text != null && binding!!.passwordEdit.text.toString() != decryptedPassword) {
                binding!!.emailLayout.error = " "
                binding!!.passwordLayout.error = "Wrong E-mail or password"
                return false
            }
        } catch (e: Exception) {
            binding!!.emailLayout.error = " "
            binding!!.passwordLayout.error = "Wrong E-mail or password"
            return false
        }
        return true
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
            binding!!.emailLayout.isErrorEnabled = binding!!.emailLayout.error != null
        }
    }
    private val twPassword: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {
            // remove any error when changed
            binding!!.passwordLayout.error = null
            binding!!.passwordLayout.isErrorEnabled = false
        }
    }
    private val clSignIn = View.OnClickListener {
        if (!validateInput()) return@OnClickListener
        binding!!.signInButton.isEnabled = false
        CONSTANTS.EMPLOYEE_COL
            .whereEqualTo(
                "email",
                if (binding!!.emailEdit.text != null) binding!!.emailEdit.text.toString() else ""
            )
            .limit(1)
            .get().addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                if (queryDocumentSnapshots.size() == 0) {
                    binding!!.emailLayout.error = " "
                    binding!!.passwordLayout.error = "Wrong E-mail or password"
                    binding!!.signInButton.isEnabled = true
                    return@addOnSuccessListener
                }
                val d = queryDocumentSnapshots.documents[0]
                if (d.exists()) {
                    val currEmployee = d.toObject(Employee::class.java)
                    if (currEmployee != null && !isPasswordRight(currEmployee.password)) {
                        binding!!.signInButton.isEnabled = true
                        return@addOnSuccessListener
                    }
                    val intent: Intent
                    if (currEmployee != null && currEmployee.isAdmin) {
                        intent = Intent(this@LoginActivity, MainActivity::class.java)
                        val sharedPreferences = getSharedPreferences(CONSTANTS.IGEC, MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString(CONSTANTS.ID, currEmployee.id)
                        editor.apply()
                        startActivity(intent)
                        finish()
                    }
                }
            }
    }
}