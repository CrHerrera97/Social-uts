package com.uts.socialuts.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.uts.socialuts.R
import com.uts.socialuts.models.Message
import com.uts.socialuts.providers.AuthProvider
import com.uts.socialuts.providers.UsersProvider
import com.uts.socialuts.utils.RelativeTime

class MessagesAdapter(options: FirestoreRecyclerOptions<Message?>, var context: Context) :
    FirestoreRecyclerAdapter<Message?, MessagesAdapter.ViewHolder?>(options) {
    var mUsersProvider: UsersProvider = UsersProvider()
    var mAuthProvider: AuthProvider = AuthProvider()
    protected override fun onBindViewHolder(holder: ViewHolder, position: Int, message: Message) {
        val document: DocumentSnapshot = snapshots.getSnapshot(position)
        val messageId: String = document.id
        holder.textViewMessage.text = message.message
        val relativeTime: String = RelativeTime.timeFormatAMPM(message.timestamp, context)
        holder.textViewDate.setText(relativeTime)
        if (message.idSender.equals(mAuthProvider.getUid())) {
            val params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            params.setMargins(150, 0, 0, 0)
            holder.linearLayoutMessage.layoutParams = params
            holder.linearLayoutMessage.setPadding(30, 20, 0, 20)
            holder.linearLayoutMessage.background = context.resources.getDrawable(R.drawable.rounded_linear_layout)
            holder.imageViewViewed.visibility = View.VISIBLE
            holder.textViewMessage.setTextColor(Color.WHITE)
            holder.textViewDate.setTextColor(Color.LTGRAY)
        } else {
            val params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            params.setMargins(0, 0, 150, 0)
            holder.linearLayoutMessage.layoutParams = params
            holder.linearLayoutMessage.setPadding(30, 20, 30, 20)
            holder.linearLayoutMessage.background = context.resources.getDrawable(R.drawable.rounded_linear_layout_grey)
            holder.imageViewViewed.visibility = View.GONE
            holder.textViewMessage.setTextColor(Color.DKGRAY)
            holder.textViewDate.setTextColor(Color.LTGRAY)
        }
        if (message.isViewed) {
            holder.imageViewViewed.setImageResource(R.drawable.icon_check_blue_light)
        } else {
            holder.imageViewViewed.setImageResource(R.drawable.icon_check_grey)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview_message, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textViewMessage: TextView = view.findViewById<TextView>(R.id.textViewMessage)
        var textViewDate: TextView = view.findViewById<TextView>(R.id.textViewDateMessage)
        var imageViewViewed: ImageView = view.findViewById(R.id.imageViewViewedMessage)
        var linearLayoutMessage: LinearLayout = view.findViewById<LinearLayout>(R.id.linearLayoutMessage)
        var viewHolder: View = view
    }

}