package com.uts.socialuts.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.*
import com.squareup.picasso.Picasso
import com.uts.socialuts.R
import com.uts.socialuts.adapters.MyPostsAdapter
import com.uts.socialuts.models.Post
import com.uts.socialuts.providers.AuthProvider
import com.uts.socialuts.providers.PostProvider
import com.uts.socialuts.providers.UsersProvider
import com.uts.socialuts.utils.ViewedMessageHelper
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class UserProfileActivity : AppCompatActivity() {
    var mLinearLayoutEditProfile: LinearLayout? = null
    var mTextViewUsername: TextView? = null
    var mTextViewPhone: TextView? = null
    var mTextViewEmail: TextView? = null
    var mTextViewPostNumber: TextView? = null
    var mTextViewPostExist: TextView? = null
    var mImageViewCover: ImageView? = null
    var mCircleImageProfile: CircleImageView? = null
    var mRecyclerView: RecyclerView? = null
    var mToolbar: Toolbar? = null
    var mFabChat: FloatingActionButton? = null
    var mUsersProvider: UsersProvider? = null
    var mAuthProvider: AuthProvider? = null
    var mPostProvider: PostProvider? = null
    var mExtraIdUser: String? = null
    var mAdapter: MyPostsAdapter? = null
    var mListener: ListenerRegistration? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        mLinearLayoutEditProfile = findViewById(R.id.linearLayoutEditProfile)
        mTextViewEmail = findViewById(R.id.textViewEmail)
        mTextViewUsername = findViewById(R.id.textViewUsername)
        mTextViewPhone = findViewById(R.id.textViewphone)
        mTextViewPostNumber = findViewById(R.id.textViewPostNumber)
        mTextViewPostExist = findViewById(R.id.textViewPostExist)
        mCircleImageProfile = findViewById(R.id.circleImageProfile)
        mImageViewCover = findViewById(R.id.imageViewCover)
        mRecyclerView = findViewById(R.id.recyclerViewMyPost)
        mFabChat = findViewById(R.id.fabChat)
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val linearLayoutManager = LinearLayoutManager(this@UserProfileActivity)
        mRecyclerView?.layoutManager = linearLayoutManager
        mUsersProvider = UsersProvider()
        mAuthProvider = AuthProvider()
        mPostProvider = PostProvider()
        mExtraIdUser = intent.getStringExtra("idUser")
        if (mAuthProvider!!.getUid().equals(mExtraIdUser)) {
            mFabChat?.isEnabled = false
        }
        mFabChat?.setOnClickListener { goToChatActivity() }
        getUser()
        getPostNumber()
        checkIfExistPost()
    }

    private fun goToChatActivity() {
        val intent = Intent(this@UserProfileActivity, ChatActivity::class.java)
        intent.putExtra("idUser1", mAuthProvider?.getUid())
        intent.putExtra("idUser2", mExtraIdUser)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        val query: Query? = mPostProvider?.getPostByUser(mExtraIdUser)
        val options: FirestoreRecyclerOptions<Post?> = query?.let {
            FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(it, Post::class.java)
                .build()
        } as FirestoreRecyclerOptions<Post?>
        mAdapter = MyPostsAdapter(options, this@UserProfileActivity)
        mRecyclerView?.adapter = mAdapter
        mAdapter!!.startListening()
        ViewedMessageHelper.updateOnline(true, this@UserProfileActivity)
    }

    override fun onStop() {
        super.onStop()
        mAdapter?.stopListening()
    }

    protected override fun onPause() {
        super.onPause()
        ViewedMessageHelper.updateOnline(false, this@UserProfileActivity)
    }

    protected override fun onDestroy() {
        super.onDestroy()
        mListener?.remove()
    }

    private fun checkIfExistPost() {
        mListener = mPostProvider?.getPostByUser(mExtraIdUser)
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

    private fun getPostNumber() {
        mPostProvider?.getPostByUser(mExtraIdUser)?.get()
            ?.addOnSuccessListener { queryDocumentSnapshots ->
                val numberPost: Int? = queryDocumentSnapshots?.size()
                mTextViewPostNumber?.text = numberPost.toString()
            }
    }

    private fun getUser() {
        mUsersProvider?.getUser(mExtraIdUser)
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
                            mTextViewUsername?.text = username?.uppercase(Locale.getDefault())
                        }
                        if (documentSnapshot.contains("image_profile")) {
                            val imageProfile: String? = documentSnapshot.getString("image_profile")
                            if (imageProfile != null) {
                                if (imageProfile.isNotEmpty()) {
                                    Picasso.with(this@UserProfileActivity).load(imageProfile)
                                        .into(mCircleImageProfile)
                                }
                            }
                        }
                        if (documentSnapshot.contains("image_cover")) {
                            val imageCover: String? = documentSnapshot.getString("image_cover")
                            if (imageCover != null) {
                                if (imageCover.isNotEmpty()) {
                                    Picasso.with(this@UserProfileActivity).load(imageCover)
                                        .into(mImageViewCover)
                                }
                            }
                        }
                    }
                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            finish()
        }
        return true
    }
}