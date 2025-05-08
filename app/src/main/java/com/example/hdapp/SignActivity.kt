package com.example.hdapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.hdapp.databinding.ActivitySignBinding
import com.example.hdapp.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignActivity : AppCompatActivity() {

    private lateinit var userName: String
    private lateinit var userEmail: String
    private lateinit var userPassword: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient

    val binding by lazy {
        ActivitySignBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize all
        database = Firebase.database.reference
        auth = Firebase.auth

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        binding.haveaccountbtn.setOnClickListener {

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.signbutton.setOnClickListener {

            // Get test from editText filled
            userName = binding.editTextTextUser.text.toString()
            userEmail = binding.editTextTextEmailAddress.text.toString().trim()
            userPassword = binding.editTextTextPassword.text.toString().trim()

            if (userName.isBlank() || userEmail.isBlank() || userPassword.isBlank()) {

                Toast.makeText(this, "Please fill the Filled", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            saveUserData()
                            updateUi()
                            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(
                                this,
                                "Account create failed : ${task.exception}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        binding.googlebutton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }


    }

    // launcher for google sign-in
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

                if (task.isSuccessful) {
                    val account: GoogleSignInAccount? = task.result
                    val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                    auth.signInWithCredential(credential).addOnCompleteListener { tasek ->
                        if (tasek.isSuccessful) {
                            updateUi()
                            Toast.makeText(
                                this,
                                "Account sign-in successfully with Google",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Account creation failed : ${tasek.exception}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Account creation failed : ${task.exception}",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            } else {
                Toast.makeText(
                    this,
                    "Account creation failed",
                    Toast.LENGTH_SHORT
                ).show()

            }

        }

    private fun saveUserData() {
        userName = binding.editTextTextUser.text.toString()
        userPassword = binding.editTextTextPassword.text.toString().trim()
        userEmail = binding.editTextTextEmailAddress.text.toString().trim()

        val user = UserModel(userName, userEmail, userPassword)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Save data to firebase database
        database.child("Users").child(userId!!).setValue(user)

    }

    private fun updateUi() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}