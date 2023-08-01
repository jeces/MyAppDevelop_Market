package com.example.applicationjeces.loading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.Response
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.http.GET

class SplashViewModel : ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    init {
        loadData()
    }

//    interface ApiService {
//        @GET("checkNetworkConnection")
//        suspend fun checkNetworkConnection(): Response<Unit>
//    }

    private fun loadData() = viewModelScope.launch {
        _loading.value = true
        delay(3000)  // 데이터 로딩을 시뮬레이션합니다. 실제로는 여기에 데이터 로딩 코드가 들어가야 합니다.
        _loading.value = false
    }
}
