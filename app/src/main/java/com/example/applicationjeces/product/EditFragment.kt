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
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.FragmentAddBinding
import com.example.applicationjeces.databinding.FragmentEditBinding
import com.example.applicationjeces.databinding.FragmentHomeBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.common.reflect.TypeToken
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.File
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

    // RecyclerView에 표시된 현재 이미지들의 URI를 저장하기 위한 변수
    private val currentImageList = ArrayList<Uri>()

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

        loadProductImages(pId, binding.editProductName.text.toString()) // 상품 이미지 로드

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

        binding.editPhotoButton.setOnClickListener {
            openImagePicker()
        }

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
         * 수정완료
         */
        binding.editCompleteButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                funImageUpload()
                updateProduct(pName)
            } else {
                updateProduct(pName)
            }
        }

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

        loadProductImagesFromFirebaseStorage(pId, pName)

        return view
    }

    /**
     * 기존 폴더 삭제 */
    fun deleteImagesFromOldFolder(myId: String, oldProductName: String, onComplete: () -> Unit) {
        val oldDirectoryRef = firebaseStorage?.reference?.child("${myId}/${oldProductName}/")

        oldDirectoryRef?.listAll()?.addOnSuccessListener { listResult ->
            val deleteTasks = listResult.items.map { it.delete() }
            Tasks.whenAllSuccess<Void>(deleteTasks).addOnSuccessListener {
                onComplete()
            }
        }
    }

    /**
     * 이미지 로컬 저장
     */
    private fun loadProductImagesFromFirebaseStorage(myId: String, productName: String) {
        val directoryRef = firebaseStorage?.reference?.child("$myId/$productName/")

        directoryRef?.listAll()?.addOnSuccessListener { listResult ->
            val maxImageCount = listResult.items.size

            for (i in 0 until maxImageCount) {
                val imgFileName = "${myId}_${i}_IMAGE_.png"
                val storageRef = directoryRef.child(imgFileName)

                val localFile = File.createTempFile("images", "jpg")
                storageRef.getFile(localFile).addOnSuccessListener {
                    val localUri = Uri.fromFile(localFile)
                    imagelist.add(localUri)
                    adapter.notifyDataSetChanged()
                }.addOnFailureListener {
                    Log.e("LoadProductImages", "Error loading product image: $imgFileName")
                }
            }
        }?.addOnFailureListener {
            Log.e("LoadProductImages", "Error listing images in directory: $myId/$productName/")
        }
    }

    /**
     * 새로운 폴더
     */
    private fun uploadImagesToNewFolder(myId: String, newProductName: String, onComplete: () -> Unit) {
        val uploadTasks = mutableListOf<Task<Void>>()

        currentImageList.forEachIndexed { index, uri ->
            val imgFileName = "${myId}_${index}_IMAGE_.png"
            val newImageRef = firebaseStorage?.reference?.child("${myId}/${newProductName}/")?.child(imgFileName)

            val tcs = TaskCompletionSource<Void>() // 우리 자체적으로 Task<Void>를 만듭니다.

            newImageRef?.putFile(uri)?.addOnSuccessListener {
                Toast.makeText(requireContext(), "Image Uploaded", Toast.LENGTH_SHORT).show()
                tcs.setResult(null) // 작업이 성공적으로 완료될 때 setResult 호출
            }?.addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                tcs.setException(exception) // 작업이 실패할 때 setException 호출
            }

            uploadTasks.add(tcs.task)
        }

        // 모든 이미지 업로드 작업이 완료되면 onComplete 콜백을 호출
        Tasks.whenAllSuccess<Void>(uploadTasks).addOnSuccessListener {
            onComplete()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to upload all images", Toast.LENGTH_SHORT).show()
        }
    }



    private fun updateProduct(pName: String) {
        //RecyclerView에 표시된 이미지들의 URI를 가져옵니다.
        fetchCurrentImagesFromRecyclerView()

        val productName = binding.editProductName.text.toString()
        val productPrice = binding.editProductPrice.text.toString().replace(",", "")
        val productDescription = binding.editProductDescription.text.toString()
        val tags = getTagsFromContainer() // 태그 가져오기
        val category = getSelectedCategory() // 선택된 카테고리 가져오기
        val myId = productViewModel.thisUser

        // 기존 폴더의 이미지 삭제 후 새로운 폴더에 이미지 업로드
        deleteImagesFromOldFolder(myId, pName) {
            uploadImagesToNewFolder(myId, productName) {
                if (productName.isNotEmpty() && productPrice.isNotEmpty()) {
                    imgFileName = if (targetImg) "basic_img.png" else "${myId}_0_IMAGE_.png"
                    val updateProduct = UpdateProduct(myId, productName, productPrice.toInt(), productDescription, imgCount, imgFileName, tags, category)
                    productViewModel.updateProducts(updateProduct, pName) // 이 함수 내에서 Firestore에 저장되는 코드가 있어야 합니다.

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
        }
    }

    private fun getTagsFromContainer(): List<String> {
        val tags = mutableListOf<String>()
        for (i in 0 until binding.editTagsContainer.childCount) {
            val childView = binding.editTagsContainer.getChildAt(i)
            val tagView = childView.findViewById<TextView>(R.id.tag_name) // 이 ID는 실제 태그 TextView의 ID와 일치해야 합니다.
            tagView?.let {
                tags.add(it.text.toString().replace("#", ""))
            }
            Log.d("TAGS", "Tags: $tags")
        }
        return tags
    }

    private fun getSelectedCategory(): String {
        return binding.editProductCategorySpinner.selectedItem.toString()
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

    // 태그를 FlexboxLayout에 추가하는 함수
    private fun addTagToFlexbox(tag: String, flexboxLayout: FlexboxLayout) {
        // 태그 개수 제한 확인
        if (flexboxLayout.childCount >= 5) {
            Toast.makeText(context, "최대 5개의 태그만 추가할 수 있습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 태그 길이 제한 확인
        if (tag.length > 7) {
            Toast.makeText(context, "태그는 7자 이하로 입력해야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val tagView = LayoutInflater.from(context).inflate(R.layout.tag_item, binding.editTagsContainer, false)
        val tagName = tagView.findViewById<TextView>(R.id.tag_name)
        val deleteButton = tagView.findViewById<ImageView>(R.id.delete_button)
        val textView = TextView(context).apply {
            tagName.text = "#$tag"
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
        }
        deleteButton.setOnClickListener {
            flexboxLayout.removeView(tagView)
        }
        flexboxLayout.addView(tagView)
    }

    // 모든 태그들을 보여주는 함수
    private fun displayAllTags(tagsList: List<List<String>>, flexboxLayout: FlexboxLayout) {
        val tags = tagsList.flatten()
        flexboxLayout.removeAllViews()  // 기존의 뷰를 모두 제거합니다.
        for (tag in tags) {
            addTagToFlexbox(tag, flexboxLayout)
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

    // RecyclerView에 표시된 이미지들의 URI를 가져와 currentImageList에 저장하는 함수
    private fun fetchCurrentImagesFromRecyclerView() {
        currentImageList.clear()
        currentImageList.addAll(imagelist)
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

    private fun loadProductImages(productId: String, productName: String) {
        // 상품 이미지를 가져오기 위한 코드를 추가합니다.
        // 예:
         for (i in 0 until imgCount) {
             val imgFileName = "${productId}_${productName}_${i}_IMAGE_.png"
             val storageRef = firebaseStorage?.reference?.child("$productId/$productName/")?.child(imgFileName)
             storageRef?.downloadUrl?.addOnSuccessListener { uri ->
                 imagelist.add(uri)
                 adapter.notifyDataSetChanged()
             }
         }
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

