package com.example.applicationjeces.product

import CategoryBottomSheetFragment
import ItemMoveCallback
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.FragmentAddBinding
import com.example.applicationjeces.databinding.FragmentHomeBinding
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

/* Product 추가 Fragment  */
class AddFragment : Fragment(), CategoryBottomSheetFragment.CategoryListener {

    private var param1: String? = null
    private var param2: String? = null

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var viewProfile: View
    private val pickImageFromAlbum = 0
    private var firebaseStorage: FirebaseStorage? = null
    private var uriPhoto: Uri? = null
    private var imgFileName: String = ""
    private var imgCount = 0
    private var targetImg = false
    private val imagelist = ArrayList<Uri>()
    private val adapter = ProductImageRecyclerViewAdapter(imagelist, this)
    private lateinit var productViewModel: ProductViewModel

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    override fun onCategorySelected(category: String) {
        // 카테고리 배열을 가져옵니다.
        val categories = resources.getStringArray(R.array.product_categories)
        // 선택된 카테고리의 인덱스를 찾습니다.
        val index = categories.indexOf(category)
        // 인덱스를 사용하여 Spinner의 값을 설정합니다.
        binding.productCategorySpinner.setSelection(index)
    }

    private fun showCategoryBottomSheet() {
        CategoryBottomSheetFragment().show(childFragmentManager, "categoryBottomSheet")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /**
         * view 바인딩
         */
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val view = binding.root

        /**
        * 뷰모델 초기화 생성자
        **/
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        firebaseStorage = FirebaseStorage.getInstance()
        productViewModel.whereMyUser("add")

        setupRecyclerView()

        /**
         * 단위 입력(,)
         */
        binding.productPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing here
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Do nothing here
            }

            override fun afterTextChanged(s: Editable) {
                binding.productPrice.removeTextChangedListener(this)  // prevent infinite loop

                val str = s.toString().replace(",", "")
                if (str.isNotEmpty()) {
                    val formattedNumber = NumberFormat.getNumberInstance(Locale.US).format(str.toLong())
                    binding.productPrice.setText(formattedNumber)
                    binding.productPrice.setSelection(formattedNumber.length)  // move cursor to the end
                }
                binding.productPrice.addTextChangedListener(this)  // re-add to start listening again
            }
        })

//        // Spinner 클릭시 BottomSheet 표시
//        binding.productCategorySpinner.setOnClickListener {
//            showCategoryBottomSheet()
//        }

        // Spinner 클릭 인터셉터에 BottomSheet 표시 리스너를 연결
        binding.categoryClickInterceptor.setOnClickListener {
            showCategoryBottomSheet()
        }

        binding.addPhotoButton.setOnClickListener {
            openImagePicker()
        }

        /**
         * 테그 입력
         * */
        binding.tagInput.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val tagText = v.text.toString().trim()
                if (tagText.isNotEmpty()) {
                    addTagToContainer(tagText)
                    v.text = ""
                }
                true
            } else {
                false
            }
        }


        return view
    }

    private fun setupRecyclerView() {
        binding.imgProfile.apply {
            adapter = this@AddFragment.adapter
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 5)

            // ItemTouchHelper 연결
            val itemTouchHelper = ItemTouchHelper(ItemMoveCallback(this@AddFragment.adapter))
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    private fun addTagToContainer(tag: String) {
        // 태그의 길이를 확인
        if (tag.length > 7) {
            Toast.makeText(context, "태그는 7자 이하로 입력해야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 태그 개수 제한 확인
        if (binding.tagsContainer.childCount >= 5) {
            Toast.makeText(context, "최대 5개의 태그만 추가할 수 있습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val tagView = LayoutInflater.from(context).inflate(R.layout.tag_item, binding.tagsContainer, false)
        val tagName = tagView.findViewById<TextView>(R.id.tag_name)
        val deleteButton = tagView.findViewById<ImageView>(R.id.delete_button)

        tagName.text = "#$tag"
        deleteButton.setOnClickListener {
            binding.tagsContainer.removeView(tagView)
        }

        binding.tagsContainer.addView(tagView)
    }

    private fun getTagsFromContainer(): List<String> {
        val tags = mutableListOf<String>()
        for (i in 0 until binding.tagsContainer.childCount) {
            val childView = binding.tagsContainer.getChildAt(i)
            val tagView = childView.findViewById<TextView>(R.id.tag_name) // 이 ID는 실제 태그 TextView의 ID와 일치해야 합니다.
            tagView?.let {
                tags.add(it.text.toString().replace("#", ""))
            }
            Log.d("TAGS", "Tags: $tags")
        }
        return tags
    }

    private fun getSelectedCategory(): String {
        return binding.productCategorySpinner.selectedItem.toString()
    }

    fun registerProduct() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            funImageUpload()
        } else {
            insertProduct()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImageFromAlbum && resultCode == Activity.RESULT_OK) {
            imagelist.clear()
            data?.clipData?.let {
                // Multiple images
                imgCount = it.itemCount.takeIf { count -> count <= 10 } ?: return
                targetImg = (imgCount == 0)
                for (i in 0 until imgCount) {
                    imagelist.add(it.getItemAt(i).uri)
                }
            } ?: run {
                // Single image
                data?.data?.let { imagelist.add(it) }
            }
            adapter.notifyDataSetChanged()
        }
    }

//    private fun funImageUpload() {
//        val productName = binding.productName.text.toString()
//        val myId = productViewModel.thisUser
//        imagelist.forEachIndexed { index, uri ->
//            val imgFileName = "${myId}_${index}_IMAGE_.png"
//            Log.d("사진업로드", imgFileName)
//            val storageRef = firebaseStorage?.reference?.child("$myId/$productName/")?.child(imgFileName)
//            storageRef?.putFile(uri)
//                ?.addOnSuccessListener { if(isAdded) {
//                    Toast.makeText(requireContext(), "ImageUploaded", Toast.LENGTH_SHORT).show()
//                } }
//                ?.addOnFailureListener { exception -> Log.e("ImageUploadError", exception.message ?: "Unknown error") }
//        }
//    }

    private fun funImageUpload() {
        coroutineScope.launch {
            val productName = binding.productName.text.toString()
            val myId = productViewModel.thisUser

            val deferredList = mutableListOf<Deferred<Boolean>>()

            imagelist.forEachIndexed { index, uri ->
                val deferred = async(Dispatchers.IO) { uploadImage(index, productName, myId, uri) }
                deferredList.add(deferred)
            }

            val results = deferredList.awaitAll()
            if (results.all { it }) {
                // All images uploaded successfully
                insertProduct()
            } else {
                // Handle failure
                Toast.makeText(requireContext(), "Failed to upload some images.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun uploadImage(index: Int, productName: String, myId: String, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            val imgFileName = "${myId}_${index}_IMAGE_.png"
            val storageRef = firebaseStorage?.reference?.child("$myId/$productName/")?.child(imgFileName)
            try {
                storageRef?.putFile(uri)?.await()
                true
            } catch (e: Exception) {
                Log.e("ImageUploadError", "Failed to upload $imgFileName. Error: ${e.message}")
                false
            }
        }
    }

    private fun insertProduct() {
        val productName = binding.productName.text.toString()
        val productPrice = binding.productPrice.text.toString().replace(",", "")
        val productDescription = binding.productDescription.text.toString()
        val tags = getTagsFromContainer() // 태그 가져오기
        val category = getSelectedCategory() // 선택된 카테고리 가져오기
        val myId = productViewModel.thisUser

        if (productName.isNotEmpty() && productPrice.isNotEmpty()) {
            imgFileName = if (targetImg) "basic_img.png" else "${myId}_0_IMAGE_.png"
            val product = Product(myId, productName, productPrice.toInt(), productDescription, imgCount, imgFileName, 0, 0, 0, "0", "0", tags, category, "판매중") // tags 추가
            productViewModel.addProducts(product) // 이 함수 내에서 Firestore에 저장되는 코드가 있어야 합니다.

            Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_LONG).show()

            val mainActivityIntent = Intent(activity, MainActivity::class.java).apply {
                putExtra("SELECT_HOME", true)
            }
            startActivity(mainActivityIntent)
            activity?.finish()
        } else {
            Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_LONG).show()
        }
    }

    fun openImagePicker() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK).apply {
            data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
        }
        startActivityForResult(photoPickerIntent, pickImageFromAlbum)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

