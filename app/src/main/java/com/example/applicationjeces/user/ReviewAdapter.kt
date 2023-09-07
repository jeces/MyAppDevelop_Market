import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.R
import com.example.applicationjeces.product.Response
import com.example.applicationjeces.user.Review
import com.google.firebase.firestore.DocumentSnapshot

class ReviewAdapter(private var reviews: List<DocumentSnapshot>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

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
        holder.userName.text = review.get("from").toString()
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
