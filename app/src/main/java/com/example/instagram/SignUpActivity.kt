package com.example.instagram

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.sql.DatabaseMetaData

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signin_link_btn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        signup_btn.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        val fullName = fullname_signup.text.toString()
        val userName = username_signup.text.toString()
        val email = email_signup.text.toString()
        val password = password_signup.text.toString()

        when{
            TextUtils.isEmpty(fullName) -> Toast.makeText(this, "Full name is required", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(this, "User name is required", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()

             else ->{
                 val progressDialog = ProgressDialog(this@SignUpActivity)
                 progressDialog.setTitle("SignUp")
                 progressDialog.setMessage("Please wait, this may take a while...")
                 progressDialog.setCanceledOnTouchOutside(false)
                 progressDialog.show()

                 val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                 mAuth.createUserWithEmailAndPassword(email, password)
                     .addOnCompleteListener { task->
                         if(task.isSuccessful){

                             saveUserInfo(fullName, userName, email, progressDialog)

                         } else {
                             val message = task.exception!!.toString()
                             Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                             mAuth.signOut()
                             progressDialog.dismiss()
                         }
                     }
             }

        }
    }

    private fun saveUserInfo(fullName: String, userName: String, email: String, progressDialog: ProgressDialog) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName.toString()
        userMap["username"] = userName.toString().toLowerCase()
        userMap["email"] = email
        userMap["bio"] = "hey i am using KhanhTQ Instagram Clone App."
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/instagram-41619.appspot.com/o/Default%20Image%2Fprofile.png?alt=media&token=6affb077-13ad-4f16-b338-36306d48e5e2"

        usersRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener { task->
                if(task.isSuccessful){
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account has been created successfully.", Toast.LENGTH_SHORT).show()


                    FirebaseDatabase.getInstance().reference
                            .child("Follow").child(currentUserID)
                            .child("Following").child(currentUserID).setValue(true)



                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else{
                    val message = task.exception!!.toString()
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }

    }
}