package com.uts.socialuts.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.uts.socialuts.R
import com.uts.socialuts.adapters.ChatsAdapter
import com.uts.socialuts.models.Chat
import com.uts.socialuts.providers.AuthProvider
import com.uts.socialuts.providers.ChatsProvider

/**
 * A simple [Fragment] subclass.
 */
class ChatsFragment : Fragment() {
    var mAdapter: ChatsAdapter? = null
    var mRecyclerView: RecyclerView? = null
    var mView: View? = null
    var mChatsProvider: ChatsProvider? = null
    var mAuthProvider: AuthProvider? = null
    var mToolbar: Toolbar? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_chats, container, false)
        mRecyclerView = mView!!.findViewById(R.id.recyclerViewChats)
        mToolbar = mView!!.findViewById(R.id.toolbar)


        (activity as AppCompatActivity).supportActionBar?.title = "Chats"
        val linearLayoutManager = LinearLayoutManager(context)
        mRecyclerView?.layoutManager = linearLayoutManager
        mChatsProvider = ChatsProvider()
        mAuthProvider = AuthProvider()
        return mView
    }

    override fun onStart() {
        super.onStart()
        val query: Query? = mChatsProvider?.getAll(mAuthProvider?.getUid())
        val options: FirestoreRecyclerOptions<Chat?> = query?.let {
            FirestoreRecyclerOptions.Builder<Chat>()
                .setQuery(it, Chat::class.java)
                .build()
        } as FirestoreRecyclerOptions<Chat?>
        mAdapter = context?.let { ChatsAdapter(options, it) }
        mRecyclerView?.adapter = mAdapter
        mAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        mAdapter?.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mAdapter?.getListener() != null) {
            mAdapter!!.getListener()?.remove()
        }
        if (mAdapter?.getListenerLastMessage() != null) {
            mAdapter!!.getListenerLastMessage()?.remove()
        }
    }
}