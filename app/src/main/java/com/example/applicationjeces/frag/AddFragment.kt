package com.example.applicationjeces.frag

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.page.DataViewModel
import com.example.applicationjeces.page.PageData
import com.example.applicationjeces.product.Product
import com.example.applicationjeces.product.ProductImageRecyclerViewAdapter
import com.example.applicationjeces.JecesViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
import java.text.SimpleDateFormat
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
class AddFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    /* firebase storage */
    private var viewProfile : View? = null
    var pickImageFromAlbum = 0
    var firebaseStorage : FirebaseStorage? = null
    var uriPhoto : Uri? = null
    var imgFileName : String = ""

    /* 이미지 개수 */
    var imgCount: Int = 0

    /* 이미지 없을 때 기본화면 띄어주려고 */
    var targetImg: Boolean = false

    /* 이미지 리스트 */
    var imagelist = ArrayList<Uri>()

    /* 이미지 어뎁터 */
    private val adapter = ProductImageRecyclerViewAdapter(imagelist, this@AddFragment)

    /* ViewModel 이니셜라이즈 */
    private lateinit var jecesViewModel: JecesViewModel

    /* ViewPage */
    private val pageViewModel by viewModels<DataViewModel>()

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
    ): View? {
        /* ADD Fragment 불러옴 */
        viewProfile = inflater.inflate(R.layout.fragment_add, container, false)

        /* ViewModel provider를 실행 */
        jecesViewModel = ViewModelProvider(this)[JecesViewModel::class.java]

        /* Initialize Firebase Storage */
        firebaseStorage = FirebaseStorage.getInstance()

        /* 업로드 버튼 누르면 */
        viewProfile!!.imgBtn.setOnClickListener {
            /* 앨범 오픈 */
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            photoPickerIntent.action = Intent.ACTION_GET_CONTENT
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, pickImageFromAlbum)
        }

        /* 등록버튼 누르면 */
        viewProfile!!.addBtn.setOnClickListener {
            if(ContextCompat.checkSelfPermission(viewProfile!!.context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                funImageUpload(viewProfile!!)
            }
            insertProduct()
        }
        
        /* 이미지 리사이클러뷰 어뎁터 장착 */
        val recyclerView = viewProfile!!.img_profile
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        
        return viewProfile
    }

    /* 다중이미지 업로드 참고 https://stickode.tistory.com/116 */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == pickImageFromAlbum) {
            if(resultCode == Activity.RESULT_OK) {
                imagelist.clear()
                /* 사진을 여러개 선택한 경우 */
                if(data?.clipData != null) {
                    imgCount = data.clipData!!.itemCount
                    if(imgCount > 10) {
                        Toast.makeText(requireContext(),"사진을 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show()
                        return
                    } else if(imgCount == 0) {
                        /* 이미지가 없을 때 */
                        targetImg = true
                    }
                    for(i in 0 until imgCount) {
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        imagelist.add(imageUri)
                    }
                }
                /* 단일 선택인 경우 */
                else {
                    data?.data?.let { uri ->
                        val imageUri : Uri? = data?.data
                        if(imageUri != null) {
                            imagelist.add(imageUri)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    /* 이미지 업로드 */
    private fun funImageUpload(view : View) {
        val productName = productName.text.toString()
        /* 다중이미지 저장 */
        var count = 0

        for (i in imagelist) {
            var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            imgFileName = jecesViewModel.thisUser + "_" + productName + "_" + count + "_IMAGE_.png"
            var storageRef = firebaseStorage!!.reference.child("productimg/").child(imgFileName)
            storageRef.putFile(i).
            addOnSuccessListener {
                Toast.makeText(view.context, "ImageUploaded", Toast.LENGTH_SHORT).show()
            }.
            addOnFailureListener {

            }
            count++
        }
    }

    private fun insertProduct() {
        val productName = productName.text.toString()
        val productPrice = productPrice.text.toString()
        val productDescription = productDescription.text.toString()

        /* 두 텍스트에 입력이 되었는지 */
        if(inputCheck(productName, productPrice)) {
            /* pk값이 자동이라도 넣어줌, Product에 저장 */
            if(targetImg == true) {
                /* 이미지가 없을 때 */
                imgFileName = "basic_img.png"
            } else {
                /* 이미지가 있을 때 */
                imgFileName = jecesViewModel.thisUser + "_" + productName + "_0_IMAGE_.png"
            }


            val product = Product(jecesViewModel.thisUser.toString(), productName, productPrice, productDescription, imgCount, imgFileName, "0", "0", "0")
            /* ViewModel에 addProduct를 해줌으로써 데이터베이스에 product값을 넣어줌 */
            jecesViewModel.addProducts(product)

            /* 메시지 */
            Toast.makeText(requireContext(),"Successfully added!", Toast.LENGTH_LONG).show()
            /* 다시 homefragment로 돌려보냅니다. */
            Log.d("addfrag", pageViewModel.currentPages.value.toString())

            /* ViewModel 가지고와서 LiveData 넘기기[업데이트 됨] */
            val model: DataViewModel by activityViewModels()
            model.changePageNum(PageData.HOME)
            /* Navigation Bar Selected 넘겨야 됨[여기서부터해야함] */
            val mActivity = activity as MainActivity
            mActivity.bottomNavigationView.menu.findItem(R.id.home).isChecked = true
        } else {
            /* 비어있다면 */
            Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_LONG).show()
        }
    }

    /* Product 텍스트가 비어있는지 체크 */
    private fun inputCheck(productName: String, productPrice: String): Boolean {
        return !(TextUtils.isEmpty(productName)&&TextUtils.isEmpty(productPrice))
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddFragment.
         */
        // TODO: Rename and change types and number of parameters
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
