package com.uts.socialuts.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.squareup.picasso.Picasso
import com.uts.socialuts.R
import com.uts.socialuts.activities.PostDetailActivity
import com.uts.socialuts.models.Post
import com.uts.socialuts.providers.AuthProvider
import com.uts.socialuts.providers.LikesProvider
import com.uts.socialuts.providers.PostProvider
import com.uts.socialuts.providers.UsersProvider
import com.uts.socialuts.utils.RelativeTime
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class MyPostsAdapter(options: FirestoreRecyclerOptions<Post?>, var context: Context) :
    FirestoreRecyclerAdapter<Post?, MyPostsAdapter.ViewHolder?>(options) {
    var mUsersProvider: UsersProvider = UsersProvider()
    var mLikesProvider: LikesProvider = LikesProvider()
    var mAuthProvider: AuthProvider = AuthProvider()
    var mPostProvider: PostProvider = PostProvider()
    protected override fun onBindViewHolder(holder: ViewHolder, position: Int, post: Post) {
        val document: DocumentSnapshot = snapshots.getSnapshot(position)
        val postId: String = document.id
        val relativeTime: String = RelativeTime.getTimeAgo(post.timestamp, context)
        holder.textViewRelativeTime.text = relativeTime
        holder.textViewTitle.text = post.title?.uppercase(Locale.getDefault())
        if (post.idUser.equals(mAuthProvider.getUid())) {
            holder.imageViewDelete.visibility = View.VISIBLE
        } else {
            holder.imageViewDelete.visibility = View.GONE
        }
        if (post.image1 != null) {
            if (post.image1!!.isNotEmpty()) {
                Picasso.with(context).load(post.image1).into(holder.circleImagePost)
            }
        }
        holder.viewHolder.setOnClickListener {
            val intent = Intent(context, PostDetailActivity::class.java)
            intent.putExtra("id", postId)
            context.startActivity(intent)
        }
        holder.imageViewDelete.setOnClickListener { showConfirmDelete(postId) }
    }

    private fun showConfirmDelete(postId: String) {
        AlertDialog.Builder(context)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Eliminar publicación")
            .setMessage("¿Estas seguro de realizar esta accion?")
            .setPositiveButton("SI"
            ) { dialogInterface, i -> deletePost(postId) }
            .setNegativeButton("NO", null)
            .show()
    }

    private fun deletePost(postId: String) {
        mPostProvider.delete(postId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "El post se elimino correctamente", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(context, "No se pudo eliminar el post", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview_my_post, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textViewTitle: TextView = view.findViewById<TextView>(R.id.textViewTitleMyPost)
        var textViewRelativeTime: TextView = view.findViewById<TextView>(R.id.textViewRelativeTimeMyPost)
        var circleImagePost: CircleImageView = view.findViewById(R.id.circleImageMyPost)
        var imageViewDelete: ImageView = view.findViewById(R.id.imageViewDeleteMyPost)
        var viewHolder: View = view
    }

}