package com.example.applicationjeces.product

import ItemMoveCallback
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.applicationjeces.MainActivity
import com.example.applicationjeces.R
import com.example.applicationjeces.page.DataViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
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
    private lateinit var jecesViewModel: ProductViewModel
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
    ): View {
        viewProfile = inflater.inflate(R.layout.fragment_add, container, false)

        jecesViewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        firebaseStorage = FirebaseStorage.getInstance()
        jecesViewModel.whereMyUser("add")

        setupRecyclerView()

        return viewProfile
    }

    private fun setupRecyclerView() {
        viewProfile.img_profile.apply {
            adapter = this@AddFragment.adapter
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 5)

            // ItemTouchHelper 연결
            val itemTouchHelper = ItemTouchHelper(ItemMoveCallback(this@AddFragment.adapter))
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    fun registerProduct() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            funImageUpload(viewProfile)
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

    private fun funImageUpload(view: View) {
        val productName = view.productName.text.toString()
        val myId = jecesViewModel.thisUser
        imagelist.forEachIndexed { index, uri ->
            val imgFileName = "${myId}_${productName}_${index}_IMAGE_.png"
            Log.d("사진업로드", imgFileName)
            val storageRef = firebaseStorage?.reference?.child("$myId/$productName/")?.child(imgFileName)
            storageRef?.putFile(uri)
                ?.addOnSuccessListener { Toast.makeText(view.context, "ImageUploaded", Toast.LENGTH_SHORT).show() }
                ?.addOnFailureListener { /* Handle error */ }
        }
    }

    private fun insertProduct() {
        val productName = viewProfile.productName.text.toString()
        val productPrice = viewProfile.productPrice.text.toString()
        val productDescription = viewProfile.productDescription.text.toString()
        val myId = jecesViewModel.thisUser

        if (productName.isNotEmpty() && productPrice.isNotEmpty()) {
            imgFileName = if (targetImg) "basic_img.png" else "${myId}_${productName}_0_IMAGE_.png"
            val product = Product(myId, productName, productPrice, productDescription, imgCount, imgFileName, "0", "0", "0", "0")
            jecesViewModel.addProducts(product)

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

