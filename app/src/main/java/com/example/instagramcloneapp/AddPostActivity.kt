package com.example.instagramcloneapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.instagramcloneapp.Fragments.HomeFragment
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_add_post.*

class AddPostActivity : AppCompatActivity() {

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storagePostPicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Posts Pictures")

        save_new_post_btn.setOnClickListener { uploadImage() }
        close_post_btn.setOnClickListener {
            finish()
        }
        CropImage.activity().setAspectRatio(1,1).start(this@AddPostActivity)
    }

    private fun uploadImage() {
        when {
            imageUri == null -> Toast.makeText(this,"Please select image",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(description_post.text.toString()) -> Toast.makeText(this,"Please write your description",Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding New Post")
                progressDialog.setMessage("Please wait, we are adding your picture...")
                progressDialog.show()

                val fileRef = storagePostPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")

                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            progressDialog.dismiss()
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener( OnCompleteListener<Uri> { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                        val postId = ref.push().key

                        val postMap = HashMap<String,Any>()
                        postMap["postid"] = postId!!
                        postMap["description"] = description_post.text.toString()
                        postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        postMap["postimage"] = myUrl

                        Log.d("AddPost","Addpost Now user is ${FirebaseAuth.getInstance().currentUser!!.uid}")

                        ref.child(postId).updateChildren(postMap)

                        Toast.makeText(this,"Post uploaded successfully",Toast.LENGTH_LONG).show()
                        val intent = Intent(this@AddPostActivity,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()

                    } else {
                        progressDialog.dismiss()
                    }
                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // データを取得する
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            image_post.setImageURI(imageUri)
        }
    }
}