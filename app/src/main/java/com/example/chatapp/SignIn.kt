package com.example.chatapp

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignIn : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        SignUp.setOnClickListener {
            val intent = Intent (this,com.example.chatapp.SignUp::class.java)
            startActivity(intent)
        }

        btnSignIn.setOnClickListener {
            val email = inputEmailSignIn.text.toString()
            val password = inputPasswordSignIn.text.toString()
            if (email.isEmpty()|| password.isEmpty()) {
                Toast.makeText(this, "Please Insert Email and Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val progressDialog = ProgressDialog(this,
                R.style.Theme_MaterialComponents_Light_Dialog)
            progressDialog.isIndeterminate = true
            progressDialog.setMessage("Authenticating...")
            progressDialog.show()
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{

                    if (!it.isSuccessful){
                        val intent = Intent (this, SignIn::class.java)
                        startActivity(intent)
                        progressDialog.hide()
                        return@addOnCompleteListener
                    }
                    else
                        Toast.makeText(this, "Succesfully Login", Toast.LENGTH_SHORT).show()
                    val intent = Intent (this, Dashboard::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK  .or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    progressDialog.hide()
                    finish()
                }
                .addOnFailureListener{
                    Log.d("Main", "Failed Login: ${it.message}")
                    Toast.makeText(this, "Email/Password incorrect", Toast.LENGTH_SHORT).show()

                }
        }

    }
}
