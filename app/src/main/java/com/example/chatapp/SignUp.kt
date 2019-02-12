package com.example.chatapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.*

class SignUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        alreadyaccount.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }

        btnSignUp.setOnClickListener {
            signup()
        }
        SelectPhotoBtnSignUp.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode== 0 && resultCode== Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selectPhoto_imageview_register.setImageBitmap(bitmap)
            SelectPhotoBtnSignUp.alpha = 0f

            /*val bitmapDrawable = BitmapDrawable(bitmap)
            SelectPhotoBtnSignUp.setBackgroundDrawable(bitmapDrawable)*/
        }
    }

    private fun signup() {
        val email = EmailSignUp.text.toString()
        val password = PasswordSignUp.text.toString()

        Log.d("SignUp", "Email is" + email)
        Log.d("SignUp", "Password:$password")

        if (email.isEmpty()|| password.isEmpty()) {
            Toast.makeText(this, "Please Insert Email and Password", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this,
            R.style.Theme_MaterialComponents_Light_Dialog)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Authenticating...")
        progressDialog.show()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{
                if (!it.isSuccessful){ return@addOnCompleteListener
                    val intent = Intent (this, SignUp::class.java)
                    startActivity(intent)
                }
                else
                    Log.d("SignUp", "Succesfully created user with uid: ${it.result.user.uid}")
                Toast.makeText(this, "Succesfully Create an Account", Toast.LENGTH_SHORT).show()
                val intent = Intent (this, SignIn::class.java)
                startActivity(intent)
                finish()
                uploadImage()
            }
            .addOnFailureListener{
                Log.d("SignUp", "Failed to create Account: ${it.message}")
                Toast.makeText(this, "Email/Password incorrect", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("SignUp", "Succesfully upload Image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("SignUp","File Location: $it")

                    saveUserToDatabase(it.toString())
                }

            }
    }

    private fun saveUserToDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid?:""
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")

        val user = User(uid,UsernameSignUp.text.toString(),EmailSignUp.text.toString(),PasswordSignUp.text.toString(),profileImageUrl)

        ref.setValue(user)

    }

}




