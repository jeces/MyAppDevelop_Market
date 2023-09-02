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
import com.example.applicationjeces.databinding.FragmentEditBinding
import com.example.applicationjeces.databinding.FragmentHomeBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.common.reflect.TypeToken
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
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
class EditFragment : Fragment(), CategoryBottomSheetFragment.CategoryListener {

    private var param1: String? = null
    private var param2: String? = null

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

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    override fun onCategorySelected(category: String) {
        // 카테고리 배열을 가져옵니다.
        val categories = resources.getStringArray(R.array.product_categories)
        // 선택된 카테고리의 인덱스를 찾습니다.
        val index = categories.indexOf(category)
        // 인덱스를 사용하여 Spinner의 값을 설정합니다.
//        binding.productCategorySpinner.setSelection(index)
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
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        val view = binding.root

        /**
        * 뷰모델 초기화 생성자
        **/
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]


        /**
         * 데이터
         */
        val pId = arguments?.getString("ID").toString()
        val pName = arguments?.getString("productName").toString()
        val productPrice = arguments?.getString("productPrice")
        val productDescription = arguments?.getString("productDescription")
        val productCount = arguments?.getString("productCount")
        val pChatCount = arguments?.getString("pChatCount")
        val pViewCount = arguments?.getString("pViewCount")
        val pHeartCount = arguments?.getString("pHeartCount")
        val tagsJson = arguments?.getString("tags")

        Log.d("afafafaf", tagsJson.toString())
        val gson = Gson()
        val tags: List<List<String>> = try {
            gson.fromJson(tagsJson, object : TypeToken<List<List<String>>>() {}.type)
        } catch (e: JsonSyntaxException) {
            Log.e("InfoFragment", "Failed to parse tags JSON", e)
            emptyList<List<String>>()
        }

        val tagsText = tags.flatten().joinToString(" ") { "#$it" }
//        binding.editTagInput.text = tagsText

        // 태그를 FlexboxLayout에 추가합니다.
        displayAllTags(tags, binding.editTagsContainer)

        binding.editProductName.setText(pName)
        binding.editProductPrice.setText(productPrice)
        binding.editProductDescription.setText(productDescription)





        firebaseStorage = FirebaseStorage.getInstance()
        productViewModel.whereMyUser("edit")

        setupRecyclerView()

        /**
         * 단위 입력(,)
         */
        binding.editProductPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing here
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Do nothing here
            }

            override fun afterTextChanged(s: Editable) {
                binding.editProductPrice.removeTextChangedListener(this)  // prevent infinite loop

                val str = s.toString().replace(",", "")
                if (str.isNotEmpty()) {
                    val formattedNumber = NumberFormat.getNumberInstance(Locale.US).format(str.toLong())
                    binding.editProductPrice.setText(formattedNumber)
                    binding.editProductPrice.setSelection(formattedNumber.length)  // move cursor to the end
                }
                binding.editProductPrice.addTextChangedListener(this)  // re-add to start listening again
            }
        })

        /**
         * 테그 입력
         * */
        // 텍스트 입력 완료 리스너에서 호출
        binding.editTagInput.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val tagText = v.text.toString().trim()
                if (tagText.isNotEmpty()) {
                    addTagToFlexbox(tagText, binding.editTagsContainer)  // 입력한 태그를 FlexboxLayout에 추가합니다.
                    v.text = ""  // 입력란을 비웁니다.
                }
                true
            } else {
                false
            }
        }

//        // Spinner 클릭시 BottomSheet 표시
//        binding.productCategorySpinner.setOnClickListener {
//            showCategoryBottomSheet()
//        }

        // Spinner 클릭 인터셉터에 BottomSheet 표시 리스너를 연결
        binding.editCategoryClickInterceptor.setOnClickListener {
            showCategoryBottomSheet()
        }

        return view
    }

    // 태그를 FlexboxLayout에 추가하는 함수
    private fun addTagToFlexbox(tag: String, flexboxLayout: FlexboxLayout) {
        val textView = TextView(context).apply {
            text = "#$tag"
            // 필요한 스타일이나 속성을 추가할 수 있습니다.
            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // 필요한 경우 마진 등을 설정할 수 있습니다.
                setMargins(8, 8, 8, 8)
            }
            this.layoutParams = layoutParams
            // 태그를 클릭하면 삭제합니다.
            setOnClickListener {
                flexboxLayout.removeView(this)
            }
        }
        flexboxLayout.addView(textView)
    }

    // 모든 태그들을 보여주는 함수
    private fun displayAllTags(tagsList: List<List<String>>, flexboxLayout: FlexboxLayout) {
        val tags = tagsList.flatten()
        flexboxLayout.removeAllViews()  // 기존의 뷰를 모두 제거합니다.
        for (tag in tags) {
            addTagToFlexbox(tag, flexboxLayout)
        }
    }

    private fun setupRecyclerView() {
        binding.editImgProfile.apply {
            adapter = this@EditFragment.adapter
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 5)

            // ItemTouchHelper 연결
            val itemTouchHelper = ItemTouchHelper(ItemMoveCallback(this@EditFragment.adapter))
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    fun registerProduct() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            funImageUpload()
        }
        insertProduct()
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

    private fun funImageUpload() {
        val productName = binding.editProductName.text.toString()
        val myId = productViewModel.thisUser
        imagelist.forEachIndexed { index, uri ->
            val imgFileName = "${myId}_${productName}_${index}_IMAGE_.png"
            Log.d("사진업로드", imgFileName)
            val storageRef = firebaseStorage?.reference?.child("$myId/$productName/")?.child(imgFileName)
            storageRef?.putFile(uri)
                ?.addOnSuccessListener { Toast.makeText(context, "ImageUploaded", Toast.LENGTH_SHORT).show() }
                ?.addOnFailureListener { /* Handle error */ }
        }
    }

    // 상품 아이디를 기반으로 데이터를 로드하는 함수
    private fun loadProductData(productId: String) {
        // 데이터베이스나 서버에서 상품 아이디를 사용하여 상품 정보를 로드합니다.
        // 로드한 데이터를 UI에 설정합니다.
    }

    private fun insertProduct() {
//        val productName = binding.productName.text.toString()
//        val productPrice = binding.productPrice.text.toString().replace(",", "")
//        val productDescription = binding.productDescription.text.toString()
//        val myId = productViewModel.thisUser
//        val nickName = productViewModel
//
//        if (productName.isNotEmpty() && productPrice.isNotEmpty()) {
//            imgFileName = if (targetImg) "basic_img.png" else "${myId}_${productName}_0_IMAGE_.png"
//            val product = Product(myId, productName, productPrice.toInt(), productDescription, imgCount, imgFileName, 0, 0, 0, "0", "0")
//            productViewModel.addProducts(product)
//
//            Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_LONG).show()
//
//            val mainActivityIntent = Intent(activity, MainActivity::class.java).apply {
//                putExtra("SELECT_HOME", true)
//            }
//            startActivity(mainActivityIntent)
//            activity?.finish()
//        } else {
//            Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_LONG).show()
//        }
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

