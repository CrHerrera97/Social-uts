package com.uts.socialuts.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.squareup.picasso.Picasso
import com.uts.socialuts.R
import com.uts.socialuts.activities.PostDetailActivity
import com.uts.socialuts.models.Like
import com.uts.socialuts.models.Post
import com.uts.socialuts.providers.AuthProvider
import com.uts.socialuts.providers.LikesProvider
import com.uts.socialuts.providers.UsersProvider
import java.util.*

class PostsAdapter : FirestoreRecyclerAdapter<Post?, PostsAdapter.ViewHolder?> {
    var context: Context
    var mUsersProvider: UsersProvider
    var mLikesProvider: LikesProvider
    var mAuthProvider: AuthProvider
    var mTextViewNumberFilter: TextView? = null
    var mListener: ListenerRegistration? = null

    constructor(options: FirestoreRecyclerOptions<Post?>, context: Context) : super(options) {
        this.context = context
        mUsersProvider = UsersProvider()
        mLikesProvider = LikesProvider()
        mAuthProvider = AuthProvider()
    }

    constructor(
        options: FirestoreRecyclerOptions<Post?>,
        context: Context,
        textView: TextView?
    ) : super(options) {
        this.context = context
        mUsersProvider = UsersProvider()
        mLikesProvider = LikesProvider()
        mAuthProvider = AuthProvider()
        mTextViewNumberFilter = textView
    }

    protected override fun onBindViewHolder(holder: ViewHolder, position: Int, post: Post) {
        val document: DocumentSnapshot = snapshots.getSnapshot(position)
        val postId: String = document.id
        if (mTextViewNumberFilter != null) {
            val numberFilter: Int = snapshots.size
            mTextViewNumberFilter!!.text = numberFilter.toString()
        }
        holder.textViewTitle.text = post.title?.uppercase(Locale.getDefault())
        holder.textViewDescription.text = post.description
        if (post.image1 != null) {
            if (post.image1!!.isNotEmpty()) {
                Picasso.with(context).load(post.image1).into(holder.imageViewPost)
            }
        }
        holder.viewHolder.setOnClickListener {
            val intent = Intent(context, PostDetailActivity::class.java)
            intent.putExtra("id", postId)
            context.startActivity(intent)
        }
        holder.imageViewLike.setOnClickListener {
            val like = Like()
            like.idUser = mAuthProvider.getUid()
            like.idPost = postId
            like.timestamp = Date().time
            like(like, holder)
        }
        post.idUser?.let { getUserInfo(it, holder) }
        getNumberLikesByPost(postId, holder)
        mAuthProvider.getUid()?.let { checkIfExistLike(postId, it, holder) }
    }

    private fun getNumberLikesByPost(idPost: String, holder: ViewHolder) {
        mListener = mLikesProvider.getLikesByPost(idPost)
            .addSnapshotListener { queryDocumentSnapshots, e ->
                if (queryDocumentSnapshots != null) {
                    val numberLikes: Int = queryDocumentSnapshots.size()
                    holder.textViewLikes.text = "$numberLikes Me gustas"
                }
            }
    }

    private fun like(like: Like, holder: ViewHolder) {
        mLikesProvider.getLikeByPostAndUser(like.idPost, mAuthProvider.getUid()).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val numberDocuments: Int = queryDocumentSnapshots.size()
                if (numberDocuments > 0) {
                    val idLike: String = queryDocumentSnapshots.documents[0].id
                    holder.imageViewLike.setImageResource(R.drawable.icon_like_grey)
                    mLikesProvider.delete(idLike)
                } else {
                    holder.imageViewLike.setImageResource(R.drawable.icon_like_blue)
                    mLikesProvider.create(like)
                }
            }
    }

    private fun checkIfExistLike(idPost: String, idUser: String, holder: ViewHolder) {
        mLikesProvider.getLikeByPostAndUser(idPost, idUser).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val numberDocuments: Int = queryDocumentSnapshots.size()
                if (numberDocuments > 0) {
                    holder.imageViewLike.setImageResource(R.drawable.icon_like_blue)
                } else {
                    holder.imageViewLike.setImageResource(R.drawable.icon_like_grey)
                }
            }
    }

    private fun getUserInfo(idUser: String, holder: ViewHolder) {
        mUsersProvider.getUser(idUser)
            ?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.contains("username")) {
                            val username: String? = documentSnapshot.getString("username")
                            holder.textViewUsername.text =
                                ("BY: " + username?.uppercase(Locale.getDefault()))
                        }
                    }
                }
            }
    }

    fun getListener(): ListenerRegistration? {
        return mListener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.cardview_post, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textViewTitle: TextView = view.findViewById<TextView>(R.id.textViewTitlePostCard)
        var textViewDescription: TextView = view.findViewById<TextView>(R.id.textViewDescriptionPostCard)
        var textViewUsername: TextView = view.findViewById<TextView>(R.id.textViewUsernamePostCard)
        var textViewLikes: TextView = view.findViewById<TextView>(R.id.textViewLikes)
        var imageViewPost: ImageView = view.findViewById(R.id.imageViewPostCard)
        var imageViewLike: ImageView = view.findViewById(R.id.imageViewLike)
        var viewHolder: View = view
    }
}