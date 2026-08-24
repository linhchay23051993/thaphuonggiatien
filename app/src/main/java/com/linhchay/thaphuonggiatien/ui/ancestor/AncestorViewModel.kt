package com.linhchay.thaphuonggiatien.ui.ancestor

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AncestorViewModel : ViewModel() {

    private val _isBurning = MutableLiveData<Boolean>(false)
    val isBurning: LiveData<Boolean> = _isBurning

    private val _remainingTime = MutableLiveData<Long>(0L)
    val remainingTime: LiveData<Long> = _remainingTime

    private var timer: CountDownTimer? = null

    fun startBurning(durationMillis: Long) {
        if (_isBurning.value == true) return

        _isBurning.value = true
        _remainingTime.value = durationMillis

        timer?.cancel()
        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingTime.value = millisUntilFinished
            }

            override fun onFinish() {
                _isBurning.value = false
                _remainingTime.value = 0L
            }
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}