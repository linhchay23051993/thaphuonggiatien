package com.linhchay.thaphuonggiatien.ui.temple

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TempleViewModel : ViewModel() {
    private val _temples = MutableLiveData<List<String>>().apply {
        value = listOf("Chùa Một Cột", "Chùa Hương", "Chùa Bái Đính", "Chùa Tam Chúc", "Đền Hùng")
    }
    val temples: LiveData<List<String>> = _temples
}