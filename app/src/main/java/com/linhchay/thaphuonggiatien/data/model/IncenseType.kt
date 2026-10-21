package com.linhchay.thaphuonggiatien.data.model

import com.linhchay.thaphuonggiatien.R

data class IncenseType(
    val id: Int,
    val name: String,
    val imageResId: Int,
    val price: Int,
    val sticksCount: Int,
    val description: String = ""
) {
    companion object {
        val DEFAULT_LIST = listOf(
            IncenseType(1, "1 nén thường", R.drawable.nen_huong_1, 5, 1, "Thành tâm kính cáo tổ tiên"),
            IncenseType(2, "1 nén cao cấp", R.drawable.nen_huong_2, 10, 1, "Chiêu tài tụ lộc, bình an"),
            IncenseType(3, "3 nén thường", R.drawable.nen_huong_3, 15, 3, "Tam bảo chứng giám lòng thành"),
            IncenseType(4, "3 nén cao cấp", R.drawable.nen_huong_4, 20, 3, "Đại cát đại lợi, hanh thông")
        )
    }
}
