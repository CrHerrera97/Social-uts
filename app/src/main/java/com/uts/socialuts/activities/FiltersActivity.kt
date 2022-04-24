package com.uts.socialuts.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.uts.socialuts.R
import com.uts.socialuts.adapters.PostsAdapter
import com.uts.socialuts.models.Post
import com.uts.socialuts.providers.AuthProvider
import com.uts.socialuts.providers.PostProvider
import com.uts.socialuts.utils.ViewedMessageHelper

class FiltersActivity : AppCompatActivity() {
    var mExtraCategory: String? = null
    var mAuthProvider: AuthProvider? = null
    var mRecyclerView: RecyclerView? = null
    var mPostProvider: PostProvider? = null
    var mPostsAdapter: PostsAdapter? = null
    var mTextViewNumberFilter: TextView? = null
    var mToolbar: Toolbar? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters)
        mRecyclerView = findViewById(R.id.recyclerViewFilter)
        mToolbar = findViewById(R.id.toolbar)
        mTextViewNumberFilter = findViewById(R.id.textViewNumberFilter)

        // setSupportActionBar(mToolbar)
        supportActionBar?.title = "Filtros"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mRecyclerView?.layoutManager = GridLayoutManager(this@FiltersActivity, 2)
        mExtraCategory = intent.getStringExtra("category")
        mAuthProvider = AuthProvider()
        mPostProvider = PostProvider()
    }

    override fun onStart() {
        super.onStart()
        val query: Query? = mPostProvider?.getPostByCategoryAndTimestamp(mExtraCategory)
        val options: FirestoreRecyclerOptions<Post?> = query?.let {
            FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(it, Post::class.java)
                .build()
        } as FirestoreRecyclerOptions<Post?>
        mPostsAdapter = PostsAdapter(options, this@FiltersActivity, mTextViewNumberFilter)
        mRecyclerView?.adapter = mPostsAdapter
        mPostsAdapter!!.startListening()
        ViewedMessageHelper.updateOnline(true, this@FiltersActivity)
    }

    override fun onStop() {
        super.onStop()
        mPostsAdapter?.stopListening()
    }

    protected override fun onPause() {
        super.onPause()
        ViewedMessageHelper.updateOnline(false, this@FiltersActivity)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            finish()
        }
        return true
    }
}