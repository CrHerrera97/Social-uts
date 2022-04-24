package com.uts.socialuts.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.Query
import com.mancj.materialsearchbar.MaterialSearchBar
import com.uts.socialuts.R
import com.uts.socialuts.activities.MainActivity
import com.uts.socialuts.activities.PostActivity
import com.uts.socialuts.adapters.PostsAdapter
import com.uts.socialuts.models.Post
import com.uts.socialuts.providers.AuthProvider
import com.uts.socialuts.providers.PostProvider
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment(), MaterialSearchBar.OnSearchActionListener {
    var mView: View? = null
    var mFab: FloatingActionButton? = null
    var mSearchBar: MaterialSearchBar? = null
    var mAuthProvider: AuthProvider? = null
    var mRecyclerView: RecyclerView? = null
    var mPostProvider: PostProvider? = null
    var mPostsAdapter: PostsAdapter? = null
    var mPostsAdapterSearch: PostsAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        mView = inflater.inflate(R.layout.fragment_home, container, false)
        mFab = mView!!.findViewById(R.id.fab)
        mRecyclerView = mView!!.findViewById(R.id.recyclerViewHome)
        mSearchBar = mView!!.findViewById(R.id.searchBar)
        val linearLayoutManager = LinearLayoutManager(context)
        mRecyclerView?.layoutManager = linearLayoutManager
        setHasOptionsMenu(true)
        mAuthProvider = AuthProvider()
        mPostProvider = PostProvider()
        mSearchBar?.setOnSearchActionListener(this)
        mSearchBar?.inflateMenu(R.menu.main_menu)
        mSearchBar?.menu?.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.itemLogout) {
                logout()
            }
            true
        }
        mFab?.setOnClickListener(View.OnClickListener { goToPost() })
        return mView
    }

    private fun searchByTitle(title: String) {
        val query: Query? = mPostProvider?.getPostByTitle(title)
        val options: FirestoreRecyclerOptions<Post?> = query?.let {
            FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(it, Post::class.java)
                .build()
        } as FirestoreRecyclerOptions<Post?>
        mPostsAdapterSearch = context?.let { PostsAdapter(options, it) }
        mPostsAdapterSearch?.notifyDataSetChanged()
        mRecyclerView?.adapter = mPostsAdapterSearch
        mPostsAdapterSearch?.startListening()
    }

    private fun getAllPost() {
        val query: Query? = mPostProvider?.getAll()
        val options: FirestoreRecyclerOptions<Post?> = query?.let {
            FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(it, Post::class.java)
                .build()
        } as FirestoreRecyclerOptions<Post?>
        mPostsAdapter = context?.let { PostsAdapter(options, it) }
        mPostsAdapter!!.notifyDataSetChanged()
        mRecyclerView?.adapter = mPostsAdapter
        mPostsAdapter!!.startListening()
    }

    override fun onStart() {
        super.onStart()
        getAllPost()
    }

    override fun onStop() {
        super.onStop()
        mPostsAdapter?.stopListening()
        mPostsAdapterSearch?.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPostsAdapter?.getListener() != null) {
            mPostsAdapter?.getListener()!!.remove()
        }
    }

    private fun goToPost() {
        val intent = Intent(context, PostActivity::class.java)
        startActivity(intent)
    }

    private fun logout() {
        mAuthProvider?.logout()
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onSearchStateChanged(enabled: Boolean) {
        if (!enabled) {
            getAllPost()
        }
    }

    override fun onSearchConfirmed(text: CharSequence) {
        searchByTitle(text.toString().lowercase(Locale.getDefault()))
    }

    override fun onButtonClicked(buttonCode: Int) {}
}