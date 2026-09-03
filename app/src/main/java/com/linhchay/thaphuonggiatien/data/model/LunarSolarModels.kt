package com.linhchay.thaphuonggiatien.data.model

import com.google.gson.annotations.SerializedName

data class LunarDateRequest(
    val day: Int,
    val month: Int,
    val year: Int,
    val isLeap: Boolean = false
)

data class SolarDateResponse(
    @SerializedName("day") val day: Int,
    @SerializedName("month") val month: Int,
    @SerializedName("year") val year: Int,
    @SerializedName("jd") val julianDay: Double? = null
)

data class HuyenMinhResponse(
    @SerializedName("nguon") val nguon: Nguon,
    @SerializedName("cau_tra_loi") val cauTraLoi: String,
    @SerializedName("duong_lich") val duongLich: String,
    @SerializedName("ngay") val ngay: Int,
    @SerializedName("thang") val thang: Int,
    @SerializedName("nam") val nam: Int,
    @SerializedName("thu") val thu: String,
    @SerializedName("iso") val iso: String,
    @SerializedName("am_lich") val amLich: AmLich,
    @SerializedName("thang_nhuan_cua_nam") val thangNhuanCuaNam: Int?,
    @SerializedName("doi_nguoc_lai") val doiNguocLai: AmLich
)

data class Nguon(
    @SerializedName("ten") val ten: String,
    @SerializedName("web") val web: String,
    @SerializedName("trang") val trang: String,
    @SerializedName("ghi_nguon") val ghiNguon: String,
    @SerializedName("giay_phep") val giayPhep: String,
    @SerializedName("truong_phai") val truongPhai: String,
    @SerializedName("ranh_gioi") val ranhGioi: String
)

data class AmLich(
    @SerializedName("ngay") val ngay: Int,
    @SerializedName("thang") val thang: Int,
    @SerializedName("nam") val nam: Int,
    @SerializedName("nhuan") val nhuan: Boolean
)
