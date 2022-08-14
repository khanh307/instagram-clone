package com.example.instagram

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_add_post.*
import java.io.File

class AddPostActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private var myUrl = ""
    private var storageProfilePicRef: StorageReference? = null

    private val CODE_IMG_GALLERY = 1
    private val SAMPLE_CROPPER_IMG_NAME = "SampleCropImg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Posts Pictures")
        save_new_post_btn.setOnClickListener { uploadImage() }
        startActivityForResult(Intent().setAction(Intent.ACTION_GET_CONTENT).setType("image/*"), CODE_IMG_GALLERY)
    }

    private fun uploadImage() {
        when{
            imageUri == null -> Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(description_post.text.toString()) -> Toast.makeText(this, "Please write description", Toast.LENGTH_SHORT).show()
            else ->{
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding new post")
                progressDialog.setMessage("Please wait, we are adding your post...")
                progressDialog.show()

                val fileRef = storageProfilePicRef!!.child(System.currentTimeMillis().toString() + ".jpg")

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
                        val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                        val postId = ref.push().key

                        val postMap = HashMap<String, Any>()
                        postMap["postid"] = postId!!
                        postMap["description"] = description_post.text.toString()
                        postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        postMap["postimage"] = myUrl

                        ref.child(postId).updateChildren(postMap)

                        Toast.makeText(this, "Post updated successfully.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@AddPostActivity, MainActivity::class.java)
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
                image_post.setImageURI(imageUri)
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

        var uCrop = UCrop.of(uri, Uri.fromFile(File(cacheDir, destinationFileName)))
            .withAspectRatio(1F, 1F)
            .withMaxResultSize(450, 450)
            .withOptions(options)
            .start(this)
    }
}