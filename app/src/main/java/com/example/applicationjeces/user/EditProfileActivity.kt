package com.example.applicationjeces.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import android.widget.ImageView
import com.example.applicationjeces.R
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profileImage.setOnClickListener {
            // 여기에 프로필 이미지를 변경하는 코드를 작성
        }

        changePasswordButton.setOnClickListener {
            // 여기에 패스워드 변경 화면으로 이동하는 코드를 작성
        }

        saveButton.setOnClickListener {
            // 여기에 사용자 정보를 업데이트하는 코드를 작성
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        }
    }
}
