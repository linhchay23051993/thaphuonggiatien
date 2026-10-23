package com.linhchay.thaphuonggiatien.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.linhchay.thaphuonggiatien.R
import com.linhchay.thaphuonggiatien.databinding.FragmentAllEventsBinding
import com.linhchay.thaphuonggiatien.data.model.Event
import com.linhchay.thaphuonggiatien.ui.ancestor.AncestorViewModel
import com.linhchay.thaphuonggiatien.ui.home.adapter.EventAdapter
import com.linhchay.thaphuonggiatien.utils.DateUtils
import com.linhchay.thaphuonggiatien.utils.ViewUtils

class AllEventsFragment : Fragment() {

    private var _binding: FragmentAllEventsBinding? = null
    private val binding get() = _binding!!

    private lateinit var allEventsViewModel: AllEventsViewModel
    private lateinit var ancestorViewModel: AncestorViewModel

    private lateinit var eventAdapter: EventAdapter
    private var allEventsList: List<Event> = emptyList()
    private var currentTabPosition: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllEventsBinding.inflate(inflater, container, false)
        allEventsViewModel = ViewModelProvider(this).get(AllEventsViewModel::class.java)
        ancestorViewModel = ViewModelProvider(this).get(AncestorViewModel::class.java)

        ViewUtils.applyStatusBarMargin(binding.layoutHeader)

        setupTabs()
        setupRecyclerView()
        setupClickListeners()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val initialTab = savedInstanceState?.getInt(KEY_TAB_POSITION) ?: 0
        selectTab(initialTab)
    }

    private fun setupTabs() {
        binding.tabAll.setOnClickListener { selectTab(0) }
        binding.tabUpcoming.setOnClickListener { selectTab(1) }
        binding.tabPassed.setOnClickListener { selectTab(2) }
    }

    private fun selectTab(position: Int) {
        currentTabPosition = position

        val activeBg = R.drawable.bg_tab_pill_active
        val inactiveBg = R.drawable.bg_tab_pill_inactive
        val activeTextColor = Color.WHITE
        val inactiveTextColor = Color.parseColor("#4A2810")
        val activeIconColor = Color.WHITE
        val inactiveIconColor = Color.parseColor("#4A2810")

        // Tab 1: Tất cả
        val isAll = position == 0
        binding.tabAll.setBackgroundResource(if (isAll) activeBg else inactiveBg)
        binding.txtTabAll.setTextColor(if (isAll) activeTextColor else inactiveTextColor)
        binding.imgTabAll.setColorFilter(if (isAll) activeIconColor else inactiveIconColor)

        // Tab 2: Sắp tới
        val isUpcoming = position == 1
        binding.tabUpcoming.setBackgroundResource(if (isUpcoming) activeBg else inactiveBg)
        binding.txtTabUpcoming.setTextColor(if (isUpcoming) activeTextColor else inactiveTextColor)
        binding.imgTabUpcoming.setColorFilter(if (isUpcoming) activeIconColor else inactiveIconColor)

        // Tab 3: Đã qua
        val isPassed = position == 2
        binding.tabPassed.setBackgroundResource(if (isPassed) activeBg else inactiveBg)
        binding.txtTabPassed.setTextColor(if (isPassed) activeTextColor else inactiveTextColor)
        binding.imgTabPassed.setColorFilter(if (isPassed) activeIconColor else inactiveIconColor)

        updateDisplayList()
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(
            showActions = true,
            onEditClick = { event ->
                showAddEventDialog(event)
            },
            onDeleteClick = { event ->
                showConfirmDialog(
                    title = "Xác nhận xoá",
                    message = "Bạn có chắc chắn muốn xoá \"${event.name}\"?"
                ) {
                    ancestorViewModel.deleteEvent(event.id)
                }
            }
        )
        binding.rvAllEvents.adapter = eventAdapter

        allEventsViewModel.allEvents.observe(viewLifecycleOwner) { events ->
            allEventsList = events
            updateDisplayList()
        }
    }

    private fun updateDisplayList() {
        val filteredList = when (currentTabPosition) {
            1 -> allEventsList.filter { !isPassed(it) } // Sắp đến (hôm nay hoặc tương lai)
            2 -> allEventsList.filter { isPassed(it) }.sortedByDescending { it.eventDate } // Đã qua
            else -> allEventsList // Tất cả
        }

        eventAdapter.submitList(filteredList)

        val isEmpty = filteredList.isEmpty()
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvAllEvents.visibility = if (isEmpty) View.GONE else View.VISIBLE

        when (currentTabPosition) {
            1 -> {
                binding.txtEmptyTitle.text = "Không có sự kiện sắp tới"
                binding.txtEmptyDesc.text = "Nhấn + để thêm ngày lễ mới"
            }
            2 -> {
                binding.txtEmptyTitle.text = "Chưa có sự kiện nào đã qua"
                binding.txtEmptyDesc.text = "Các sự kiện đã qua sẽ xuất hiện ở đây"
            }
            else -> {
                binding.txtEmptyTitle.text = "Chưa có sự kiện nào"
                binding.txtEmptyDesc.text = "Nhấn + để thêm ngày lễ của bạn"
            }
        }
    }

    private fun isPassed(event: Event): Boolean {
        return DateUtils.getRemainingDaysDescription(event.eventDate) == "Đã qua"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_TAB_POSITION, currentTabPosition)
    }

    companion object {
        private const val KEY_TAB_POSITION = "key_tab_position"
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnAddEvent.setOnClickListener {
            showAddEventDialog(null)
        }
    }

    private fun showAddEventDialog(event: Event? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
        val tilName = dialogView.findViewById<TextInputLayout>(R.id.tilName)
        val tilLunarDate = dialogView.findViewById<TextInputLayout>(R.id.tilLunarDate)
        val edtName = dialogView.findViewById<TextInputEditText>(R.id.edtName)
        val edtLunarDate = dialogView.findViewById<TextInputEditText>(R.id.edtLunarDate)
        val btnOk = dialogView.findViewById<View>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)
        val txtTitle = dialogView.findViewById<TextView>(R.id.txtTitle)

        txtTitle.text = if (event == null) "Thêm Ngày Lễ / Ngày Giỗ" else "Sửa Ngày Lễ / Ngày Giỗ"
        event?.let {
            edtName.setText(it.name)
            val dateOnly = it.lunarDate.split(" ").firstOrNull()?.split("/")?.take(2)?.joinToString("/")
            edtLunarDate.setText(dateOnly ?: "")
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnOk.setOnClickListener {
            val name = edtName.text.toString().trim()
            val lunarDate = edtLunarDate.text.toString().trim()

            var isValid = true

            if (name.isEmpty()) {
                tilName.error = "Vui lòng nhập tên sự kiện"
                isValid = false
            } else {
                tilName.error = null
            }

            val dateRegex = Regex("""^\d{1,2}[/-]\d{1,2}$""")
            if (lunarDate.isEmpty()) {
                tilLunarDate.error = "Vui lòng nhập ngày âm lịch"
                isValid = false
            } else if (!dateRegex.matches(lunarDate)) {
                tilLunarDate.error = "Định dạng không đúng (VD: 15/7 hoặc 15-7)"
                isValid = false
            } else {
                val parts = lunarDate.split("/", "-")
                val day = parts[0].toIntOrNull() ?: 0
                val month = parts[1].toIntOrNull() ?: 0

                if (day < 1 || day > 30 || month < 1 || month > 12) {
                    tilLunarDate.error = "Ngày hoặc tháng không hợp lệ (Âm lịch)"
                    isValid = false
                } else {
                    tilLunarDate.error = null
                }
            }

            if (isValid) {
                if (event == null) {
                    ancestorViewModel.addEvent(name, lunarDate)
                } else {
                    ancestorViewModel.updateEvent(event.id, name, lunarDate)
                }
                dialog.dismiss()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showConfirmDialog(title: String, message: String, onConfirm: () -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm, null)
        val txtTitle = dialogView.findViewById<TextView>(R.id.txtTitle)
        val txtMessage = dialogView.findViewById<TextView>(R.id.txtMessage)
        val btnConfirm = dialogView.findViewById<View>(R.id.btnConfirm)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)

        txtTitle.text = title
        txtMessage.text = message

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnConfirm.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
