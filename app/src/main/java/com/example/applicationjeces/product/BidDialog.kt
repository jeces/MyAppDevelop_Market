package com.example.applicationjeces.product

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.applicationjeces.JecesViewModel
import com.example.applicationjeces.R
import kotlinx.android.synthetic.main.fragment_bid_dialog.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BidDialog.newInstance] factory method to
 * create an instance of this fragment.
 */
class BidDialog(var pId: String, var pName: String) : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var jecesViewModel: JecesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_bid_dialog, container, false)
//        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)


        jecesViewModel = ViewModelProvider(this)[JecesViewModel::class.java]

        view.btnBid.setOnClickListener {
            var bid_price = view.bid_price.text.toString()
            jecesViewModel.bidchange(pId, pName, bid_price)
        }

        view.btnPerch.setOnClickListener {

        }

        /**
         * 다이어로그 뷰 만들기 */
        // Inflate the layout for this fragment
        return view
    }
}