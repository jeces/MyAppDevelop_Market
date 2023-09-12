package com.example.applicationjeces.user

import android.util.Log
import android.view.MenuItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.applicationjeces.R

class LoginViewModel() : ViewModel() {
    /* MutableLiveData를 은닉하기 */
    private var currentPage = MutableLiveData(PageDataLogin.LOGIN)
    /* 다른 클래스가 접근할 수 있는 데이터 */
    val currentPages: LiveData<PageDataLogin>
        get() = currentPage

    private val repository = LoginRepository()

    private val _signUpStatus = MutableLiveData<Boolean>()
    val signUpStatus: LiveData<Boolean> get() = _signUpStatus

    fun createUser(email: String, password: String, name: String) {
        repository.createUser(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                repository.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                    if (emailTask.isSuccessful) {
                        repository.addUserToFirestore(email, name)
                            .addOnSuccessListener { _signUpStatus.value = true }
                            .addOnFailureListener { _signUpStatus.value = false }
                    } else {
                        _signUpStatus.value = false
                    }
                }
            } else {
                _signUpStatus.value = false
            }
        }
    }

    /* PageNum에 따라 currentPages 변경 */
    fun setCurrentPage(item: MenuItem): Boolean {
        Log.d("체크6", "셋")
        val menuItemId = item.itemId
        val pageNum = getPageNum(menuItemId)
        changePageNum(pageNum)
        return true
    }

    /* BottomMunu 누르면 PageData에 저장 */
    fun getPageNum(menuItemId: Int): PageDataLogin {
        Log.d("체크4", menuItemId.toString())
        return when(menuItemId) {
            R.id.sign_in -> PageDataLogin.LOGIN
            R.id.sign_up -> PageDataLogin.JOIN
            else -> throw java.lang.IllegalArgumentException("Not found pageNum")
        }
    }

    /* 현재 FragnentType과 비교하여 같으면 return, 다르면 변경 */
    fun changePageNum(pageNum: PageDataLogin) {
        if(currentPages.value == pageNum) {
            Log.d("체크5", pageNum.toString() + " / " + currentPage.value)
            return
        } else {
            Log.d("체크7", pageNum.toString() + " / " + currentPage.value)
            currentPage.value = pageNum
        }
    }
}