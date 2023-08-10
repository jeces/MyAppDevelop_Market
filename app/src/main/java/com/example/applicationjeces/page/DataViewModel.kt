package com.example.applicationjeces.page

import android.util.Log
import android.view.MenuItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.applicationjeces.R

class DataViewModel() : ViewModel() {
    /* MutableLiveData를 은닉하기 */
    private var currentPage = MutableLiveData(PageData.HOME)
    /* 다른 클래스가 접근할 수 있는 데이터 */
    val currentPages: LiveData<PageData>
        get() = currentPage

    init {
        Log.d("여기 들어오니?", "몇번")
    }

    /* BottomMunu를 눌렀을 때 xml에서 불러짐. PageNum에 따라 currentPages 변경 */
    fun setCurrentPage(item: MenuItem): Boolean {
        val menuItemId = item.itemId
        val pageNum = getPageNum(menuItemId)
        changePageNum(pageNum)
        return true
    }

    /* BottomMunu 누르면 PageData에 저장 */
    fun getPageNum(menuItemId: Int): PageData {
        Log.d("체크4", menuItemId.toString())
        return when(menuItemId) {
            R.id.home -> PageData.HOME
            R.id.add -> PageData.ADD
            R.id.search -> PageData.SEARCH
            R.id.chatroom -> PageData.CHATROOM
            R.id.my -> PageData.MY
            else -> throw java.lang.IllegalArgumentException("Not found pageNum")
        }
    }

    /* 현재 FragnentType과 비교하여 같으면 return, 다르면 변경 */
    fun changePageNum(pageNum: PageData) {
        if(currentPages.value == pageNum) {
            return
        } else {
            currentPage.value = pageNum
        }
    }

    /* */
}