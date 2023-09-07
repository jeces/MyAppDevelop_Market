import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.R
import com.example.applicationjeces.product.ProductViewModel
import com.example.applicationjeces.product.Response
import com.example.applicationjeces.user.Review
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class ReviewAdapter(private var reviews: List<DocumentSnapshot>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private val firestore: FirebaseFirestore? = null

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.review_user_name)
        val ratingBar: RatingBar = view.findViewById(R.id.review_rating_bar)
        val reviewContent: TextView = view.findViewById(R.id.review_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        firestore?.collection("UserInfo")
            ?.whereEqualTo("id", review.get("from").toString())
            ?.get()
            ?.addOnSuccessListener { querySnapshot ->
                // 여기에서 우리는 첫 번째 문서만 사용하겠습니다.
                // (실제로는 여러 문서가 반환될 수 있으므로 원하는 방식으로 처리해야 합니다.)
                val document = querySnapshot.documents.firstOrNull()
                if (document != null) {
                    holder.userName.text = document.getString("name")
                }
            }
        holder.ratingBar.rating = review.get("rating").toString().toFloat()
        holder.reviewContent.text = review.get("content").toString()
    }

    override fun getItemCount() = reviews.size

    @SuppressLint("NotifyDataSetChanged")
    fun setData(review: List<DocumentSnapshot>) {
        reviews = review
        Log.d("dkfflwksk", reviews.toString())
        /* 변경 알림 */
        notifyDataSetChanged()
    }
}
