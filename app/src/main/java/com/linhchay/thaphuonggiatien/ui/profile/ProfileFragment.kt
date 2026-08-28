package com.linhchay.thaphuonggiatien.ui.profile

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.linhchay.thaphuonggiatien.databinding.FragmentProfileBinding
import com.linhchay.thaphuonggiatien.utils.ViewUtils
import java.io.File
import java.io.FileOutputStream
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREFS_NAME = "user_profile"
        private const val KEY_NAME = "name"
        private const val KEY_GENDER = "gender"
        private const val KEY_BIRTHDATE = "birthdate"
        private const val KEY_AVATAR_URI = "avatar_uri"
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val localUri = copyUriToInternalStorage(it)
            if (localUri != null) {
                selectedImageUri = localUri
                binding.imgAvatar.setImageURI(localUri)
            }
        }
    }

    private fun copyUriToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val file = File(requireContext().filesDir, "profile_avatar.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        
        ViewUtils.applyStatusBarMargin(binding.root)

        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        setupUI()
        loadProfileData()
        return binding.root
    }

    private fun loadProfileData() {
        val name = sharedPreferences.getString(KEY_NAME, "")
        val gender = sharedPreferences.getInt(KEY_GENDER, -1)
        val birthDate = sharedPreferences.getString(KEY_BIRTHDATE, "")
        val avatarUriString = sharedPreferences.getString(KEY_AVATAR_URI, null)

        binding.edtName.setText(name)
        binding.txtProfileName.text = if (name.isNullOrEmpty()) "Người dùng" else name
        
        when (gender) {
            0 -> binding.rbMale.isChecked = true
            1 -> binding.rbFemale.isChecked = true
            else -> binding.rgGender.clearCheck()
        }

        if (!birthDate.isNullOrEmpty()) {
            binding.txtBirthDate.text = birthDate
            updateDerivedInfo(birthDate)
        } else {
            binding.txtBirthDate.text = ""
            binding.txtAge.text = "--"
            binding.txtZodiac.text = "--"
            binding.txtFate.text = "--"
        }

        if (avatarUriString != null) {
            try {
                val uri = Uri.parse(avatarUriString)
                // Kiểm tra quyền truy cập (đặc biệt cho các URI content:// cũ)
                if (uri.scheme == "content") {
                    requireContext().contentResolver.openInputStream(uri)?.close()
                }
                binding.imgAvatar.setImageURI(uri)
                selectedImageUri = uri
            } catch (e: Exception) {
                // Nếu không có quyền truy cập, quay về ảnh mặc định
                binding.imgAvatar.setImageResource(android.R.drawable.sym_def_app_icon)
                selectedImageUri = null
                sharedPreferences.edit().remove(KEY_AVATAR_URI).apply()
            }
        } else {
            binding.imgAvatar.setImageResource(android.R.drawable.sym_def_app_icon)
            selectedImageUri = null
        }
    }

    private fun updateDerivedInfo(birthDate: String) {
        try {
            val parts = birthDate.split("/")
            if (parts.size == 3) {
                val day = parts[0].toInt()
                val month = parts[1].toInt()
                val year = parts[2].toInt()
                
                val calendar = Calendar.getInstance()
                val age = calendar.get(Calendar.YEAR) - year
                binding.txtAge.text = age.toString()
                
                binding.txtZodiac.text = getZodiacSign(day, month)
                binding.txtFate.text = getFate(year)
            }
        } catch (e: Exception) {
            binding.txtAge.text = "--"
            binding.txtZodiac.text = "--"
            binding.txtFate.text = "--"
        }
    }

    private fun getZodiacSign(day: Int, month: Int): String {
        return when (month) {
            1 -> if (day < 20) "Ma Kết" else "Bảo Bình"
            2 -> if (day < 19) "Bảo Bình" else "Song Ngư"
            3 -> if (day < 21) "Song Ngư" else "Bạch Dương"
            4 -> if (day < 20) "Bạch Dương" else "Kim Ngưu"
            5 -> if (day < 21) "Kim Ngưu" else "Song Tử"
            6 -> if (day < 21) "Song Tử" else "Cự Giải"
            7 -> if (day < 23) "Cự Giải" else "Sư Tử"
            8 -> if (day < 23) "Sư Tử" else "Xử Nữ"
            9 -> if (day < 23) "Xử Nữ" else "Thiên Bình"
            10 -> if (day < 23) "Thiên Bình" else "Bọ Cạp"
            11 -> if (day < 22) "Bọ Cạp" else "Nhân Mã"
            12 -> if (day < 22) "Nhân Mã" else "Ma Kết"
            else -> "--"
        }
    }

    private fun getFate(year: Int): String {
        // Can: Giáp, Ất = 1; Bính, Đinh = 2; Mậu, Kỷ = 3; Canh, Tân = 4; Nhâm, Quý = 5
        val canValue = when (year % 10) {
            4, 5 -> 1 // Giáp, Ất
            6, 7 -> 2 // Bính, Đinh
            8, 9 -> 3 // Mậu, Kỷ
            0, 1 -> 4 // Canh, Tân
            2, 3 -> 5 // Nhâm, Quý
            else -> 0
        }

        // Chi: Tý, Sửu, Ngọ, Mùi = 0; Dần, Mão, Thân, Dậu = 1; Thìn, Tỵ, Tuất, Hợi = 2
        val chiValue = when (year % 12) {
            4, 5, 10, 11 -> 0 // Tý, Sửu, Ngọ, Mùi
            6, 7, 0, 1 -> 1   // Dần, Mão, Thân, Dậu
            8, 9, 2, 3 -> 2   // Thìn, Tỵ, Tuất, Hợi
            else -> 0
        }

        var fateValue = canValue + chiValue
        if (fateValue > 5) fateValue -= 5

        return when (fateValue) {
            1 -> "Kim"
            2 -> "Thủy"
            3 -> "Hỏa"
            4 -> "Thổ"
            5 -> "Mộc"
            else -> "--"
        }
    }

    private fun setupUI() {
        binding.btnChangeAvatar.setOnClickListener {
            if (binding.btnSaveProfile.isEnabled) {
                pickImage.launch("image/*")
            }
        }

        binding.txtBirthDate.setOnClickListener {
            if (binding.btnSaveProfile.isEnabled) {
                showDatePicker()
            }
        }

        binding.btnEditProfile.setOnClickListener {
            if (binding.btnSaveProfile.isEnabled) {
                loadProfileData()
                setEditMode(false)
            } else {
                setEditMode(true)
            }
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }
    }

    private fun setEditMode(isEdit: Boolean) {
        binding.edtName.isEnabled = isEdit
        binding.rbMale.isEnabled = isEdit
        binding.rbFemale.isEnabled = isEdit
        binding.txtBirthDate.isClickable = isEdit
        binding.btnChangeAvatar.visibility = if (isEdit) View.VISIBLE else View.GONE
        
        binding.btnSaveProfile.isEnabled = isEdit
        binding.btnEditProfile.text = if (isEdit) "Hủy" else "Chỉnh sửa"
        
        if (isEdit) {
            binding.edtName.requestFocus()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val birthDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            binding.txtBirthDate.text = birthDate
            updateDerivedInfo(birthDate)
        }, year, month, day).show()
    }

    private fun saveProfile() {
        val name = binding.edtName.text.toString()
        if (name.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = if (binding.rbMale.isChecked) 0 else if (binding.rbFemale.isChecked) 1 else -1
        val birthDate = binding.txtBirthDate.text.toString()

        with(sharedPreferences.edit()) {
            putString(KEY_NAME, name)
            putInt(KEY_GENDER, gender)
            putString(KEY_BIRTHDATE, birthDate)
            putString(KEY_AVATAR_URI, selectedImageUri?.toString())
            apply()
        }
        
        binding.txtProfileName.text = name
        setEditMode(false)
        Toast.makeText(context, "Đã lưu thông tin", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}