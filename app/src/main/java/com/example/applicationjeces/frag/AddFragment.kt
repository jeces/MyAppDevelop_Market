package com.example.applicationjeces.frag

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
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
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.page.DataViewModel
import com.example.applicationjeces.page.PageData
import com.example.applicationjeces.product.Product
import com.example.applicationjeces.product.ProductViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
import java.text.SimpleDateFormat
import java.util.*

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

    /* ViewModel 이니셜라이즈 */
    private lateinit var productViewModel: ProductViewModel

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
        viewProfile = inflater.inflate(R.layout.fragment_add, container, false).rootView

        /* ViewModel provider를 실행 */
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        /* Initialize Firebase Storage */
        firebaseStorage = FirebaseStorage.getInstance()

        /* 업로드 버튼 누르면 */
        viewProfile!!.imgBtn.setOnClickListener {
            /* 앨범 오픈 */
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, pickImageFromAlbum)
        }

        /* 버튼 누르면 실행 */
        viewProfile!!.addBtn.setOnClickListener {
            insertProduct()
            if(ContextCompat.checkSelfPermission(viewProfile!!.context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                funImageUpload(viewProfile!!)
                Log.d("업로드3", "업로드3")
            }
            else {

            }
        }
        return viewProfile
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == pickImageFromAlbum) {
            Log.d("업로드", "업로드")
            if(resultCode == Activity.RESULT_OK) {
                /* 선택된 이미지 경로 */
                uriPhoto = data?.data
                imageView.setImageURI(uriPhoto)
            }
        }
    }

    private fun funImageUpload(view : View) {
        Log.d("업로드2", "업로드2")
        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imgFileName = "IMAGE_" + timeStamp + "_.png"
        var storageRef = firebaseStorage?.reference?.child("productimg")?.child(imgFileName)

        storageRef?.putFile(uriPhoto!!)?.addOnSuccessListener {
            Toast.makeText(view.context, "ImageUploiaded", Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertProduct() {
        val productName = productName.text.toString()
        val productPrice = productPrice.text.toString()


        /* 두 텍스트에 입력이 되었는지 */
        if(inputCheck(productName, productPrice)) {
            /* pk값이 자동이라도 넣어줌, Product에 저장 */
            val product = Product(0, productName, productPrice, "1", uriPhoto.toString())
            /* ViewModel에 addProduct를 해줌으로써 데이터베이스에 product값을 넣어줌 */
//            productViewModel.addProduct(product)
            productViewModel.addProducts(product)

            Log.d("뷰모델2", product.toString())
            /* 메시지 */
            Toast.makeText(requireContext(),"Successfully added!", Toast.LENGTH_LONG).show()
            /* 다시 homefragment로 돌려보냅니다. */
            Log.d("addfrag", pageViewModel.currentPages.value.toString())

            /* ViewModel 가지고와서 LiveData 넘기기[업데이트 됨] */
            val model: DataViewModel by activityViewModels()
            model.changePageNum(PageData.DETAIL)
            /* Navigation Bar Selected 넘겨야 됨[여기서부터해야함] */
            val mActivity = activity as MainActivity
            mActivity.bottomNavigationView.menu.findItem(R.id.detail).isChecked = true
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
