package com.uts.socialuts.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.squareup.picasso.Picasso
import com.uts.socialuts.R
import com.uts.socialuts.models.Comment
import com.uts.socialuts.providers.UsersProvider
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class CommentAdapter(options: FirestoreRecyclerOptions<Comment?>, var context: Context) :
    FirestoreRecyclerAdapter<Comment?, CommentAdapter.ViewHolder?>(options) {
    var mUsersProvider: UsersProvider = UsersProvider()
    protected override fun onBindViewHolder(holder: ViewHolder, position: Int, comment: Comment) {
        val document: DocumentSnapshot = snapshots.getSnapshot(position)
        val commentId: String = document.id
        val idUser: String? = document.getString("idUser")
        holder.textViewComment.text = comment.comment
        if (idUser != null) {
            getUserInfo(idUser, holder)
        }
    }

    private fun getUserInfo(idUser: String, holder: ViewHolder) {
        mUsersProvider.getUser(idUser)
            ?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.contains("username")) {
                            val username: String? = documentSnapshot.getString("username")
                            if (username != null) {
                                holder.textViewUsername.text = username.uppercase(Locale.getDefault())
                            }
                        }
                        if (documentSnapshot.contains("image_profile")) {
                            val imageProfile: String? = documentSnapshot.getString("image_profile")
                            if (imageProfile != null) {
                                if (imageProfile.isNotEmpty()) {
                                    Picasso.with(context).load(imageProfile)
                                        .into(holder.circleImageComment)
                                }
                            }
                        }
                    }
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview_comment, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textViewUsername: TextView = view.findViewById<TextView>(R.id.textViewUsername)
        var textViewComment: TextView = view.findViewById<TextView>(R.id.textViewComment)
        var circleImageComment: CircleImageView = view.findViewById(R.id.circleImageComment)
        var viewHolder: View = view

    }

}