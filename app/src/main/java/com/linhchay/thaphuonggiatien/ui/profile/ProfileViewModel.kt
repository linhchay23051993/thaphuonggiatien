package com.linhchay.thaphuonggiatien.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Thông tin tài khoản cá nhân"
    }
    val text: LiveData<String> = _text
}