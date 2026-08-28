package com.linhchay.thaphuonggiatien.ui.ancestor

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.linhchay.thaphuonggiatien.data.model.Prayer
import com.linhchay.thaphuonggiatien.ui.ancestor.adapter.PrayerAdapter
import com.linhchay.thaphuonggiatien.databinding.DialogPrayersBinding
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.linhchay.thaphuonggiatien.MainActivity
import com.linhchay.thaphuonggiatien.MainViewModel
import com.linhchay.thaphuonggiatien.R
import com.linhchay.thaphuonggiatien.data.model.AltarItem
import com.linhchay.thaphuonggiatien.databinding.FragmentAncestorBinding
import com.linhchay.thaphuonggiatien.ui.home.adapter.EventAdapter
import java.util.Random
import kotlin.math.max

class AncestorFragment : Fragment() {

    private var _binding: FragmentAncestorBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AncestorViewModel
    private val mainViewModel: MainViewModel by activityViewModels()
    private val smokeViews = mutableListOf<SmokeView>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(AncestorViewModel::class.java)
        _binding = FragmentAncestorBinding.inflate(inflater, container, false)

        setupGoldObserver()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        return binding.root
    }

    private fun setupGoldObserver() {
        mainViewModel.gold.observe(viewLifecycleOwner) { gold ->
            binding.layoutGold.txtGold.text = gold.toString()
        }
    }

    private fun observeViewModel() {
        viewModel.isBurning.observe(viewLifecycleOwner) { isBurning ->
            if (isBurning) {
                val burners = findBatHuongViews()
                if (burners.isNotEmpty()) {
                    burners.forEach { startSmokeEffect(it) }
                }
            } else {
                removeSmokeEffect()
            }
        }

        viewModel.placedItems.observe(viewLifecycleOwner) { items ->
            refreshAltarItems(items)
        }

        viewModel.isEditMode.observe(viewLifecycleOwner) { isEdit ->
            updateEditUi(isEdit)
        }
    }

    private fun updateEditUi(isEdit: Boolean) {
        binding.btnSave.visibility = if (isEdit) View.VISIBLE else View.GONE
        binding.btnCancelEdit.visibility = if (isEdit) View.VISIBLE else View.GONE
        binding.btnSapXep.visibility = if (isEdit) View.GONE else View.VISIBLE
        binding.btnMuaVatPham.visibility = View.VISIBLE
        binding.layoutActions.visibility = if (isEdit) View.GONE else View.VISIBLE
        refreshAltarItems(viewModel.placedItems.value ?: emptyList())
    }

    private fun refreshAltarItems(items: List<AltarItem>) {
        val childCount = binding.altarContainer.childCount
        if (childCount > 1) {
            binding.altarContainer.removeViews(1, childCount - 1)
        }
        val isEdit = viewModel.isEditMode.value ?: false
        items.forEach { item ->
            addPlacedItemView(item, isEdit)
        }
    }

    private fun addPlacedItemView(item: AltarItem, isEdit: Boolean) {
        val itemView = layoutInflater.inflate(R.layout.layout_altar_item_resizable, binding.altarContainer, false)
        val imgItem = itemView.findViewById<ImageView>(R.id.imgItem)
        val borderView = itemView.findViewById<View>(R.id.borderView)
        val btnDelete = itemView.findViewById<View>(R.id.btnDelete)
        val handleBR = itemView.findViewById<View>(R.id.handleBottomRight)

        imgItem.setImageResource(item.imageResId)
        
        val params = itemView.layoutParams
        params.width = item.width
        params.height = item.height
        itemView.layoutParams = params
        
        itemView.x = item.x
        itemView.y = item.y
        itemView.tag = item.id

        if (isEdit) {
            borderView.visibility = View.VISIBLE
            btnDelete.visibility = View.VISIBLE
            handleBR.visibility = View.VISIBLE
            setupResizeAndDrag(itemView, item)
        } else {
            borderView.visibility = View.GONE
            btnDelete.visibility = View.GONE
            handleBR.visibility = View.GONE
            itemView.setOnTouchListener(null)
        }

        binding.altarContainer.addView(itemView)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupResizeAndDrag(view: View, item: AltarItem) {
        val btnDelete = view.findViewById<View>(R.id.btnDelete)
        val handleBR = view.findViewById<View>(R.id.handleBottomRight)
        
        var dX = 0f
        var dY = 0f
        val minSize = 100

        // Drag logic (Main View)
        view.setOnTouchListener { v, event ->
            if (viewModel.isEditMode.value != true) return@setOnTouchListener false
            
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    v.x = event.rawX + dX
                    v.y = event.rawY + dY
                    item.x = v.x
                    item.y = v.y
                    true
                }
                MotionEvent.ACTION_UP -> {
                    v.performClick()
                    viewModel.updateItemPosition(item.id, item.x, item.y, v.width, v.height)
                    true
                }
                else -> false
            }
        }

        // Helper to update view size and item data
        fun updateSize(newX: Float, newY: Float, newW: Int, newH: Int) {
            view.x = newX
            view.y = newY
            val p = view.layoutParams
            p.width = newW
            p.height = newH
            view.layoutParams = p
            
            item.x = newX
            item.y = newY
            item.width = newW
            item.height = newH
        }

        // Delete Logic
        btnDelete.setOnClickListener {
            viewModel.removeItem(item.id)
        }

        // Bottom Right Resize
        handleBR.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = event.rawX
                    dY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - dX
                    val deltaY = event.rawY - dY
                    
                    val newW = max(minSize, view.width + deltaX.toInt())
                    val newH = max(minSize, view.height + deltaY.toInt())
                    
                    updateSize(view.x, view.y, newW, newH)
                    
                    dX = event.rawX
                    dY = event.rawY
                    true
                }
                MotionEvent.ACTION_UP -> {
                    viewModel.updateItemPosition(item.id, item.x, item.y, item.width, item.height)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        val adapter = EventAdapter()
        binding.rvAnniversaries.adapter = adapter
        viewModel.anniversaries.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
    }

    private fun setupClickListeners() {
        // Hàng nút chức năng chính (Bottom)
        binding.btnDangLe.setOnClickListener {
            showArrangeDialog(onlyOfferings = true)
        }
        binding.btnThapHuong.setOnClickListener {
            showIncenseDialog()
        }
        binding.btnBaiKhan.setOnClickListener {
            showPrayerDialog()
        }
        binding.btnAddEvent.setOnClickListener {
            showAddEventDialog()
        }

        // Các nút điều khiển sắp xếp (Top/Edit Mode)
        binding.btnMuaVatPham.setOnClickListener {
            showArrangeDialog(onlyOfferings = false)
        }
        binding.btnSapXep.setOnClickListener {
            enterEditMode()
        }
        binding.btnSave.setOnClickListener {
            val totalCost = viewModel.calculateNewItemsCost()
            val mainActivity = activity as? MainActivity
            if (mainActivity?.updateGold(-totalCost) == true) {
                viewModel.saveChanges()
                viewModel.setEditMode(false)
            } else {
                android.widget.Toast.makeText(requireContext(), "Bạn không đủ vàng để mua các vật phẩm mới!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnCancelEdit.setOnClickListener {
            viewModel.cancelChanges()
            viewModel.setEditMode(false)
        }
    }

    private fun enterEditMode() {
        viewModel.setEditMode(true)
    }

    private fun exitEditMode() {
        viewModel.setEditMode(false)
    }

    private fun showIncenseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_select_incense, null)
        val optionOne = dialogView.findViewById<View>(R.id.optionOneStick)
        val optionThree = dialogView.findViewById<View>(R.id.optionThreeSticks)
        val btnOk = dialogView.findViewById<View>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)
        
        var isThreeSticksSelected = false
        optionOne.isSelected = true 
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        optionOne.setOnClickListener {
            optionOne.isSelected = true
            optionThree.isSelected = false
            isThreeSticksSelected = false
        }
        
        optionThree.setOnClickListener {
            optionThree.isSelected = true
            optionOne.isSelected = false
            isThreeSticksSelected = true
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnOk.setOnClickListener {
            val price = if (isThreeSticksSelected) 10 else 5
            val mainActivity = activity as? MainActivity
            if (mainActivity?.updateGold(-price) == true) {
                startIncenseAnimation(isThreeSticksSelected)
                dialog.dismiss()
            } else {
                android.widget.Toast.makeText(requireContext(), "Bạn không đủ vàng!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        dialog.show()
    }

    private fun startIncenseAnimation(isThreeSticks: Boolean) {
        val burners = findBatHuongViews()
        
        if (burners.isEmpty()) {
            // Fallback: Bay vào giữa khu vực ban thờ nếu không có bát hương
            animateToPosition(
                binding.altarContainer.x + binding.altarContainer.width / 2f,
                binding.altarContainer.y + binding.altarContainer.height / 2f,
                isThreeSticks,
                true
            )
            return
        }

        burners.forEachIndexed { index, burner ->
            val location = IntArray(2)
            burner.getLocationInWindow(location)
            val rootLocation = IntArray(2)
            binding.root.getLocationInWindow(rootLocation)
            
            val sizeW = if (isThreeSticks) 60 else 30
            val targetX = (location[0] - rootLocation[0]).toFloat() + burner.width / 2f - sizeW / 2f
            val targetY = (location[1] - rootLocation[1]).toFloat() - 120 / 2f
            
            animateToPosition(targetX, targetY, isThreeSticks, index == 0)
        }
    }

    private fun animateToPosition(targetX: Float, targetY: Float, isThreeSticks: Boolean, shouldTriggerBurning: Boolean) {
        val incenseResId = if (isThreeSticks) R.drawable.ba_nen else R.drawable.mot_nen
        val incenseView = ImageView(requireContext())
        incenseView.setImageResource(incenseResId)
        val sizeW = if (isThreeSticks) 60 else 30
        val sizeH = 120
        incenseView.layoutParams = ViewGroup.LayoutParams(sizeW, sizeH)
        
        binding.root.addView(incenseView)
        incenseView.x = -sizeW.toFloat()
        incenseView.y = binding.root.height / 2f
        
        incenseView.animate()
            .x(targetX)
            .y(targetY)
            .alpha(0f)
            .setDuration(1950)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.root.removeView(incenseView)
                    if (shouldTriggerBurning) {
                        viewModel.startBurning(if (isThreeSticks) 3 else 1)
                    }
                }
            })
            .start()
    }

    private fun findBatHuongViews(): List<View> {
        val items = viewModel.placedItems.value ?: return emptyList()
        val batHuongItems = items.filter { it.type == "Bát hương" }
        val views = mutableListOf<View>()
        
        batHuongItems.forEach { item ->
            for (i in 0 until binding.altarContainer.childCount) {
                val child = binding.altarContainer.getChildAt(i)
                if (child.tag == item.id) {
                    views.add(child)
                }
            }
        }
        return views
    }

    private fun startSmokeEffect(batHuongView: View) {
        val smoke = SmokeView(requireContext())
        val smokeWidth = 300
        val smokeHeight = 600
        smoke.layoutParams = ViewGroup.LayoutParams(smokeWidth, smokeHeight)

        val offset24dp = 24 * resources.displayMetrics.density
        smoke.x = batHuongView.x + batHuongView.width / 2f - smokeWidth / 2f
        smoke.y = batHuongView.y - smokeHeight + 40f + offset24dp

        binding.altarContainer.addView(smoke)
        smokeViews.add(smoke)
    }

    private fun removeSmokeEffect() {
        smokeViews.forEach {
            binding.altarContainer.removeView(it)
        }
        smokeViews.clear()
    }

    /**
     * View tùy chỉnh để vẽ hiệu ứng khói sử dụng hệ thống hạt (Particle System)
     */
    private inner class SmokeView(context: Context) : View(context) {
        private val paint = Paint()
        private val MAX_PARTICLES = 40
        private val particles = Array(MAX_PARTICLES) { Particle() }
        private val random = Random()
        private var isRunning = false

        init {
            // Tắt tăng tốc phần cứng để BlurMaskFilter hoạt động ổn định
            setLayerType(LAYER_TYPE_SOFTWARE, null)
            paint.color = Color.WHITE
            paint.isAntiAlias = true
            // Tạo độ nhòe cho hạt khói mờ ảo hơn (25f)
            paint.maskFilter = BlurMaskFilter(25f, BlurMaskFilter.Blur.NORMAL)
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            isRunning = true
            postInvalidateOnAnimation()
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            isRunning = false
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            for (p in particles) {
                if (p.active) {
                    p.update()
                    if (p.alpha <= 0) {
                        p.active = false
                    } else {
                        paint.alpha = p.alpha.toInt()
                        canvas.drawCircle(p.x, p.y, p.radius, paint)
                    }
                } else if (random.nextFloat() < 0.05f) { // Tần suất sinh hạt tự nhiên
                    p.reset(width.toFloat(), height.toFloat())
                }
            }
            if (isRunning) {
                // Sử dụng postInvalidateOnAnimation để tối ưu hiệu suất
                postInvalidateOnAnimation()
            }
        }

        private inner class Particle {
            var x = 0f
            var y = 0f
            var radius = 0f
            var speedY = 0f
            var speedX = 0f
            var alpha = 0f
            var active = false

            fun reset(width: Float, height: Float) {
                // Hạt khói xuất phát ngẫu nhiên quanh tâm bát hương
                x = width / 2f + (random.nextFloat() - 0.5f) * (width * 0.4f)
                y = height * 0.9f
                radius = 3 + random.nextFloat() * 5
                speedY = 0.8f + random.nextFloat() * 1.2f
                speedX = (random.nextFloat() - 0.5f) * 0.3f
                alpha = 80 + random.nextFloat() * 60 // Khói nhạt
                active = true
            }

            fun update() {
                y -= speedY
                x += speedX
                alpha -= 0.7f // Tan biến chậm
                radius += 0.4f // Nở rộng khi bay lên
                speedX += (random.nextFloat() - 0.5f) * 0.05f // Lượn nhẹ
            }
        }
    }

    private fun showPrayerDialog() {
        // Hiển thị container bài khấn
        binding.layoutPrayerContainer.visibility = View.VISIBLE
        
        // Reset về trạng thái danh sách
        binding.rvPrayersList.visibility = View.VISIBLE
        binding.scrollPrayerContent.visibility = View.GONE
        binding.btnBackPrayer.visibility = View.GONE
        binding.txtPrayerHeader.text = "Bài Khấn"

        val prayerAdapter = PrayerAdapter { prayer ->
            // Khi chọn 1 bài: hiển thị nội dung, ẩn danh sách
            binding.rvPrayersList.visibility = View.GONE
            binding.scrollPrayerContent.visibility = View.VISIBLE
            binding.btnBackPrayer.visibility = View.VISIBLE
            binding.txtPrayerHeader.text = prayer.title
            binding.txtPrayerDetail.text = prayer.content
        }

        binding.rvPrayersList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = prayerAdapter
        }

        viewModel.prayers.observe(viewLifecycleOwner) {
            prayerAdapter.submitList(it)
        }

        binding.btnBackPrayer.setOnClickListener {
            // Quay lại danh sách bài khấn
            binding.rvPrayersList.visibility = View.VISIBLE
            binding.scrollPrayerContent.visibility = View.GONE
            binding.btnBackPrayer.visibility = View.GONE
            binding.txtPrayerHeader.text = "Bài Khấn"
        }

        binding.btnClosePrayer.setOnClickListener {
            binding.layoutPrayerContainer.visibility = View.GONE
        }
    }

    private fun showArrangeDialog(onlyOfferings: Boolean = false) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_altar_items, null)
        val tabLayout = dialogView.findViewById<TabLayout>(R.id.tabLayoutCategories)
        val layoutItemsContainer = dialogView.findViewById<LinearLayout>(R.id.layoutItemsContainer)
        val btnOk = dialogView.findViewById<View>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)

        var selectedResId: Int? = null
        var selectedCategory: String? = null
        var selectedPrice: Int = 0

        val categories = viewModel.getCategories(onlyOfferings)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        categories.forEach { (name, _) ->
            tabLayout.addTab(tabLayout.newTab().setText(name))
        }

        fun updateItems(position: Int) {
            layoutItemsContainer.removeAllViews()
            val images = categories[position].second
            val categoryName = categories[position].first
            val purchasedIds = viewModel.purchasedResIds.value ?: emptySet()

            images.forEachIndexed { index, resId ->
                val itemView = layoutInflater.inflate(R.layout.item_altar_selectable, layoutItemsContainer, false)
                val imgItem = itemView.findViewById<ImageView>(R.id.imgItem)
                val priceLayout = itemView.findViewById<View>(R.id.priceLayout)
                val txtPrice = itemView.findViewById<TextView>(R.id.txtPrice)
                val viewSelected = itemView.findViewById<View>(R.id.viewSelected)
                val isPurchased = resId in purchasedIds
                
                imgItem.setImageResource(resId)
                
                // Demo price: 20, 30, 50
                val price = if (isPurchased) 0 else when (index % 3) {
                    0 -> 20
                    1 -> 30
                    else -> 50
                }
                
                if (isPurchased) {
                    priceLayout.visibility = View.GONE
                } else {
                    priceLayout.visibility = View.VISIBLE
                    txtPrice.text = price.toString()
                }
                
                // Hiển thị highlight nếu đã chọn
                viewSelected.visibility = if (selectedResId == resId) View.VISIBLE else View.GONE

                itemView.setOnClickListener {
                    selectedResId = resId
                    selectedCategory = categoryName
                    selectedPrice = price
                    // Cập nhật lại UI để highlight
                    for (i in 0 until layoutItemsContainer.childCount) {
                        val child = layoutItemsContainer.getChildAt(i)
                        child.findViewById<View>(R.id.viewSelected).visibility = View.GONE
                    }
                    viewSelected.visibility = View.VISIBLE
                }
                layoutItemsContainer.addView(itemView)
            }
        }

        btnOk.setOnClickListener {
            selectedResId?.let { resId ->
                val newItem = AltarItem(
                    id = System.currentTimeMillis(),
                    type = selectedCategory ?: "",
                    imageResId = resId,
                    x = 300f,
                    y = 400f,
                    width = 250,
                    height = 250,
                    batHuongId = if (selectedCategory == "Bát hương") "batHuong_${System.currentTimeMillis()}" else null,
                    price = selectedPrice
                )
                viewModel.addAltarItem(newItem)
                dialog.dismiss()
                enterEditMode()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { updateItems(tab.position) }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        updateItems(0)
        dialog.show()
    }

    private fun showAddEventDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
        val edtName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtName)
        val edtLunarDate = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtLunarDate)
        val btnOk = dialogView.findViewById<View>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnOk.setOnClickListener {
            val name = edtName.text.toString()
            val lunarDate = edtLunarDate.text.toString()
            if (name.isNotEmpty() && lunarDate.isNotEmpty()) {
                viewModel.addEvent(name, lunarDate)
                dialog.dismiss()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeSmokeEffect()
        _binding = null
    }
}
