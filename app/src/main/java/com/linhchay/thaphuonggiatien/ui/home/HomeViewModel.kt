package com.linhchay.thaphuonggiatien.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.linhchay.thaphuonggiatien.data.model.Event

class HomeViewModel : ViewModel() {
    private val _events = MutableLiveData<List<Event>>().apply {
        value = listOf(
            Event(1, "Giỗ ông nội", "23/08/2026", "05/07 Âm lịch"),
            Event(2, "Lễ Vu Lan", "26/08/2026", "15/07 Âm lịch"),
            Event(3, "Giỗ bà ngoại", "10/09/2026", "23/07 Âm lịch")
        )
    }
    val events: LiveData<List<Event>> = _events
}