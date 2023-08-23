import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * 리사이클러뷰 스와이프
 */
class SwipeCallback : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false // 아이템 이동은 처리하지 않습니다.
    }

    private var swipedPosition = -1
    private val optionButtonWidth = 200f
    private val swipeThreshold = optionButtonWidth // 스와이프의 임계치 설정

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // 스와이프한 항목의 위치를 얻습니다.
        swipedPosition = viewHolder.adapterPosition
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
        val itemView = viewHolder.itemView
        val paint = Paint()

        // 항목이 반대로 스와이프되어 복구될 때 아이템 뷰를 원래 위치로 되돌리는 로직
        if (!isCurrentlyActive && dX == 0f) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        // 항목 뷰를 움직이는 범위를 제한합니다.
        val newDX = when {
            dX > swipeThreshold -> swipeThreshold
            dX < -swipeThreshold -> -swipeThreshold
            else -> dX
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX > 0) { // 오른쪽 스와이프
                paint.color = Color.WHITE
                val background = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                c.drawRect(background, paint)

                paint.color = Color.BLACK
                paint.textSize = 40f
                val textWidth = paint.measureText("알림")
                c.drawText("알림", itemView.left.toFloat() + textWidth / 2, itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat()) / 2, paint)
            } else if (dX < 0) { // 왼쪽 스와이프
                paint.color = Color.WHITE
                val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                c.drawRect(background, paint)

                paint.color = Color.BLACK
                paint.textSize = 40f
                val textWidth = paint.measureText("나가기")
                c.drawText("나가기", itemView.right.toFloat() - textWidth * 1.5f, itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat()) / 2, paint)
            }
        }

        // 항목 뷰의 위치를 업데이트하는 코드
        super.onChildDraw(c, recyclerView, viewHolder, newDX, dY, actionState, isCurrentlyActive)
    }

    // 스와이프 후 항목이 원래 위치로 되돌아오게 설정
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        // 스와이프 애니메이션 종료 후 아이템 뷰를 원래 위치로 되돌리기
        viewHolder.itemView.translationX = 0f

        if (swipedPosition != -1) {
            // 여기서 해당 항목의 상태를 변경하거나 리사이클러뷰의 어댑터를 업데이트합니다.
            // 예: adapter.notifyItemChanged(swipedPosition)
            swipedPosition = -1
        }
    }
}