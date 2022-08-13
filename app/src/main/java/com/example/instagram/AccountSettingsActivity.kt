package com.example.instagram

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.example.instagram.Model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.io.File
import kotlin.coroutines.Continuation

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private val CODE_IMG_GALLERY = 1
    private val SAMPLE_CROPPER_IMG_NAME = "SampleCropImg"
    private var imageUri: Uri? = null
    private var myUrl = ""
    private var storageProfilePicRef: StorageReference? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        logout_btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        change_image_text_btn.setOnClickListener {
            checker = "clicked"
            startActivityForResult(Intent().setAction(Intent.ACTION_GET_CONTENT).setType("image/*"), CODE_IMG_GALLERY)

        }

        save_infor_profile_btn.setOnClickListener {
            if(checker == "clicked"){
                uploadImageAndUpdateInfo()
            } else{
                updateUserInfoOnly()
            }
        }
        userInfo()
    }

    private fun uploadImageAndUpdateInfo() {


        when{
            TextUtils.isEmpty(full_name_profile_frag.text.toString()) -> Toast.makeText(this, "Please write full name", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(user_name_profile_frag.text.toString()) -> Toast.makeText(this, "Please write user name", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(bio_profile_frag.text.toString()) -> Toast.makeText(this, "Please write user name", Toast.LENGTH_SHORT).show()
            imageUri == null -> Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT).show()

            else ->{
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, we are updating your profile...")
                progressDialog.show()

                val fileRef = storageProfilePicRef!!.child(firebaseUser!!.uid+".jpg")
                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if(task.isSuccessful){
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener ( OnCompleteListener<Uri>{ task->
                    if(task.isSuccessful){
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()
                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] = full_name_profile_frag.text.toString().toLowerCase()
                        userMap["username"] = user_name_profile_frag.text.toString().toLowerCase()
                        userMap["bio"] = bio_profile_frag.text.toString()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(this, "Account Information has been updated successfully.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    } else {
                        progressDialog.dismiss()
                    }

                } )
            }


        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CODE_IMG_GALLERY && resultCode == RESULT_OK){
            var imageUri: Uri = data!!.data!!
            if(imageUri != null){
                startCrop(imageUri)
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK){
            var imageResult = UCrop.getOutput(data!!)
            if(imageResult != null){
                imageUri = imageResult
                profile_image_view_profile_frag.setImageURI(imageUri)
            }
        }
    }

    private fun startCrop(@NonNull uri: Uri){
        var destinationFileName = SAMPLE_CROPPER_IMG_NAME
        destinationFileName += ".jpg"

        var options: UCrop.Options = UCrop.Options()
        options.setCompressionQuality(70)
        options.setHideBottomControls(false)
        options.setFreeStyleCropEnabled(false)
        options.setCircleDimmedLayer(true)

        var uCrop = UCrop.of(uri, Uri.fromFile(File(cacheDir, destinationFileName)))
            .withAspectRatio(1F, 1F)
            .withMaxResultSize(450, 450)
            .withOptions(options)
            .start(this)
    }


    private fun updateUserInfoOnly() {
        when{
            TextUtils.isEmpty(full_name_profile_frag.text.toString()) -> Toast.makeText(this, "Please write full name", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(user_name_profile_frag.text.toString()) -> Toast.makeText(this, "Please write user name", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(bio_profile_frag.text.toString()) -> Toast.makeText(this, "Please write user name", Toast.LENGTH_SHORT).show()
          else-> {

              val userRef = FirebaseDatabase.getInstance().reference.child("Users")

              val userMap = HashMap<String, Any>()
              userMap["fullname"] = full_name_profile_frag.text.toString()
              userMap["username"] = user_name_profile_frag.text.toString().toLowerCase()
              userMap["bio"] = bio_profile_frag.text.toString()

              userRef.child(firebaseUser.uid).updateChildren(userMap)

              Toast.makeText(this, "Account Information has been updated successfully.", Toast.LENGTH_SHORT).show()
              val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
              intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
              startActivity(intent)
              finish()
          }
        }
    }

    private fun userInfo(){
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)

        userRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    var user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profile_image_view_profile_frag)
                    user_name_profile_frag.setText(user!!.getUsername())
                    full_name_profile_frag.setText(user!!.getFullname())
                    bio_profile_frag.setText(user!!.getBio())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }



}