package com.uts.socialuts.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.*
import com.squareup.picasso.Picasso
import com.uts.socialuts.R
import com.uts.socialuts.activities.EditProfileActivity
import com.uts.socialuts.adapters.MyPostsAdapter
import com.uts.socialuts.models.Post
import com.uts.socialuts.providers.AuthProvider
import com.uts.socialuts.providers.PostProvider
import com.uts.socialuts.providers.UsersProvider
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {
    var mView: View? = null
    var mLinearLayoutEditProfile: LinearLayout? = null
    var mTextViewUsername: TextView? = null
    var mTextViewPhone: TextView? = null
    var mTextViewEmail: TextView? = null
    var mTextViewPostNumber: TextView? = null
    var mTextViewPostExist: TextView? = null
    var mImageViewCover: ImageView? = null
    var mCircleImageProfile: CircleImageView? = null
    var mRecyclerView: RecyclerView? = null
    var mUsersProvider: UsersProvider? = null
    var mAuthProvider: AuthProvider? = null
    var mPostProvider: PostProvider? = null
    var mAdapter: MyPostsAdapter? = null
    var mListener: ListenerRegistration? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_profile, container, false)
        mLinearLayoutEditProfile = mView?.findViewById(R.id.linearLayoutEditProfile)
        mTextViewEmail = mView?.findViewById(R.id.textViewEmail)
        mTextViewUsername = mView?.findViewById(R.id.textViewUsername)
        mTextViewPhone = mView?.findViewById(R.id.textViewphone)
        mTextViewPostNumber = mView?.findViewById(R.id.textViewPostNumber)
        mTextViewPostExist = mView?.findViewById(R.id.textViewPostExist)
        mCircleImageProfile = mView!!.findViewById(R.id.circleImageProfile)
        mImageViewCover = mView!!.findViewById(R.id.imageViewCover)
        mRecyclerView = mView!!.findViewById(R.id.recyclerViewMyPost)
        val linearLayoutManager = LinearLayoutManager(context)
        mRecyclerView?.layoutManager = linearLayoutManager
        mLinearLayoutEditProfile?.setOnClickListener { goToEditProfile() }
        mUsersProvider = UsersProvider()
        mAuthProvider = AuthProvider()
        mPostProvider = PostProvider()
        getUser()
        getPostNumber()
        checkIfExistPost()
        return mView
    }

    private fun checkIfExistPost() {
        mListener = mPostProvider?.getPostByUser(mAuthProvider?.getUid())
            ?.addSnapshotListener { queryDocumentSnapshots, e ->
                if (queryDocumentSnapshots != null) {
                    val numberPost: Int = queryDocumentSnapshots.size()
                    if (numberPost > 0) {
                        mTextViewPostExist?.text = "Publicaciones"
                        mTextViewPostExist?.setTextColor(Color.RED)
                    } else {
                        mTextViewPostExist?.text = "No hay publicaciones"
                        mTextViewPostExist?.setTextColor(Color.GRAY)
                    }
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val query: Query? = mPostProvider?.getPostByUser(mAuthProvider?.getUid())
        val options: FirestoreRecyclerOptions<Post?> = query?.let {
            FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(it, Post::class.java)
                .build()
        } as FirestoreRecyclerOptions<Post?>
        mAdapter = context?.let { MyPostsAdapter(options, it) }
        mRecyclerView?.adapter = mAdapter
        mAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        mAdapter?.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        mListener?.remove()
    }

    private fun goToEditProfile() {
        val intent = Intent(context, EditProfileActivity::class.java)
        startActivity(intent)
    }

    private fun getPostNumber() {
        mPostProvider?.getPostByUser(mAuthProvider?.getUid())?.get()
            ?.addOnSuccessListener { queryDocumentSnapshots ->
                val numberPost: Int? = queryDocumentSnapshots?.size()
                mTextViewPostNumber?.text = numberPost.toString()
            }
    }

    private fun getUser() {
        mUsersProvider?.getUser(mAuthProvider?.getUid())
            ?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.contains("email")) {
                            val email: String? = documentSnapshot.getString("email")
                            mTextViewEmail?.text = email
                        }
                        if (documentSnapshot.contains("phone")) {
                            val phone: String? = documentSnapshot.getString("phone")
                            mTextViewPhone?.text = phone
                        }
                        if (documentSnapshot.contains("username")) {
                            val username: String? = documentSnapshot.getString("username")
                            if (username != null) {
                                mTextViewUsername?.text = username.uppercase(Locale.getDefault())
                            }
                        }
                        if (documentSnapshot.contains("image_profile")) {
                            val imageProfile: String? = documentSnapshot.getString("image_profile")
                            if (imageProfile != null) {
                                if (imageProfile.isNotEmpty()) {
                                    Picasso.with(context).load(imageProfile)
                                        .into(mCircleImageProfile)
                                }
                            }
                        }
                        if (documentSnapshot.contains("image_cover")) {
                            val imageCover: String? = documentSnapshot.getString("image_cover")
                            if (imageCover != null) {
                                if (imageCover.isNotEmpty()) {
                                    Picasso.with(context).load(imageCover).into(mImageViewCover)
                                }
                            }
                        }
                    }
                }
            }
    }
}