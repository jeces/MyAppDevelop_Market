package com.example.applicationjeces.user

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.ActivityEditProfileBinding
import com.example.applicationjeces.page.PageData
import com.example.applicationjeces.product.ProductViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var productViewModel: ProductViewModel
    private val IMAGE_REQUEST_CODE = 1001
    lateinit var selectedImageUri: Uri

    var firebaseStorage : FirebaseStorage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)
        binding.apply {
            productViewModel = productViewModel
            lifecycleOwner = this@EditProfileActivity
        }

        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        /* Initialize Firebase Storage */
        firebaseStorage = FirebaseStorage.getInstance()

        /**
         * 프로필 이미지 등록
         */
        binding.profileImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                chooseImage()
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), IMAGE_REQUEST_CODE)
                } else {
                    // Older versions of Android (before Marshmallow) will not request permissions at runtime
                    chooseImage()
                }
            }
        }

        /**
         * 프로필 SAVE
         */
        binding.saveButton.setOnClickListener {
            // 선택한 이미지가 없으면 Firebase에 업로드하는 동작은 생략하며, 기존 이미지가 그대로 유지됩니다.
            if (::selectedImageUri.isInitialized) {
                ImageUpload(binding.profileImage, selectedImageUri)
            }

            val mainActivity = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                putExtra("myinfo", PageData.MY)
            }
            /* 애니메이션 적용 */
            startActivity(mainActivity)
            overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left)
            finish()

            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        }


        changePasswordButton.setOnClickListener {
            // 여기에 패스워드 변경 화면으로 이동하는 코드를 작성
        }
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data!!
            if (selectedImageUri != null) {
                binding.profileImage.setImageURI(selectedImageUri)
            }
        }
    }

    /* 이미지 업로드 */
    private fun ImageUpload(view : View, imageUri: Uri) {
        val myId = productViewModel.thisUser.toString()
        var imgFileName = myId + "_Profil_IMAGE_.png"
        var storageRef = firebaseStorage!!.reference.child("${myId}/profil/").child(imgFileName)

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                Toast.makeText(view.context, "ImageUploaded", Toast.LENGTH_SHORT).show()
                binding.profileImage.setImageURI(imageUri)
            }
            .addOnFailureListener {
                Toast.makeText(view.context, "ImageUpload failed", Toast.LENGTH_SHORT).show()
            }
    }
}
