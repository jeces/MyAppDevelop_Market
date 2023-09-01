import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.view.LayoutInflater
import com.example.applicationjeces.R

class ProductDetailBottomSheet(context: Context) : BottomSheetDialog(context) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.detail_bottom_sheet, null)
        setContentView(view)

        // 초기화 코드 또는 view에 이벤트 리스너 추가 등을 여기에 작성할 수 있습니다.
    }

    // 필요한 메서드 또는 기능을 추가적으로 구현할 수 있습니다.
}
