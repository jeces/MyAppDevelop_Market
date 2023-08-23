import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * 리사이클러뷰 스와이프
 */
class SwipeCallback : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false // 아이템 이동은 처리하지 않습니다.
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // 스와이프한 항목의 위치를 얻습니다.
        val position = viewHolder.adapterPosition

        if (direction == ItemTouchHelper.LEFT) {
            // 왼쪽으로 스와이프했을 때의 동작을 정의합니다.
            // 예를 들면, 옵션을 표시하도록 합니다.
            showOptionsForItem(position)
        }
    }

    private fun showOptionsForItem(position: Int) {
        // 항목에 대한 옵션을 표시하는 로직을 여기에 추가합니다.
        // 예: 대화상자 표시, 특정 뷰 가시성 변경 등
    }

    // 아이템 뷰가 고정되게 하려면 onChildDraw()를 오버라이드하여 아무 것도 하지 않도록 합니다.
    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // 오른쪽으로 스와이프할 때
            val itemView = viewHolder.itemView
            val paint = Paint()
            paint.color = Color.WHITE
            paint.textSize = 40f
            if (dX > 0) {

                // 박스 그리기
                val background = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                c.drawRect(background, paint)

                // "알림" 텍스트 그리기
                val textWidth = paint.measureText("알림")
                c.drawText("알림", itemView.left.toFloat() + textWidth / 2, itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat()) / 2, paint)
            } else if (dX < 0) { // 왼쪽으로 스와이프할 때
                // 왼쪽으로 스와이프할 때 "나가기" 텍스트와 박스를 그립니다.

                // 박스 그리기
                val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                c.drawRect(background, paint)

                // "나가기" 텍스트 그리기
                val textWidth = paint.measureText("나가기")
                c.drawText("나가기", itemView.right.toFloat() - textWidth * 1.5f, itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat()) / 2, paint)
            }
        }
    }
}