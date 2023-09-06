import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import com.example.applicationjeces.R
import com.example.applicationjeces.databinding.LocationBottomSheetLayoutBinding
import com.example.applicationjeces.databinding.FragmentFilterSearchBinding
import com.example.applicationjeces.product.KakaoAPI
import com.example.applicationjeces.product.KakaoResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.location_bottom_sheet_layout.*
import kotlinx.android.synthetic.main.location_bottom_sheet_layout.view.*
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LocationBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: LocationBottomSheetLayoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView

    interface OnAddressSelectedListener {
        fun onAddressSelected(address: String)
    }

    private var listener: OnAddressSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is OnAddressSelectedListener) {
            listener = parentFragment as OnAddressSelectedListener
        } else {
            throw RuntimeException("The parent fragment must implement OnAddressSelectedListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LocationBottomSheetLayoutBinding.inflate(inflater, container, false)

        // Retrofit 인스턴스 생성
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(KakaoAPI::class.java)

        binding.confirmButton.setOnClickListener {
            val mapCenterPoint = mapView.mapCenterPoint
            val latitude = mapCenterPoint.mapPointGeoCoord.latitude
            val longitude = mapCenterPoint.mapPointGeoCoord.longitude

            api.getReverseGeo(longitude, latitude).enqueue(object : Callback<KakaoResponse> {
                override fun onResponse(call: Call<KakaoResponse>, response: Response<KakaoResponse>) {
                    val address = response.body()?.documents?.get(0)?.address?.address_name
                    listener?.onAddressSelected(address ?: "주소를 가져오지 못했습니다.")
                    Log.d("adadadad", "${mapCenterPoint}/${latitude}/${address}/")
//                    yourTextView.text = address ?: "주소를 가져오지 못했습니다."


                    /** 여기서 addfragment로 데이터 전달
                     *
                     */
                    dismiss()
                }

                override fun onFailure(call: Call<KakaoResponse>, t: Throwable) {
                    // 실패 처리
                }
            })
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = MapView(requireActivity())
        val mapContainer = binding.mapContainer

        mapContainer.addView(mapView)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1000
            )
            return
        }

        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        mapView.setShowCurrentLocationMarker(true)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1000) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
                mapView.setShowCurrentLocationMarker(true)
            } else {
                // 권한 거부 시 처리
            }
        }
    }
}
