package com.uts.socialuts.activities

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.smarteist.autoimageslider.IndicatorAnimations
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import com.squareup.picasso.Picasso
import com.uts.socialuts.R
import com.uts.socialuts.adapters.CommentAdapter
import com.uts.socialuts.adapters.SliderAdapter
import com.uts.socialuts.models.Comment
import com.uts.socialuts.models.FCMBody
import com.uts.socialuts.models.FCMResponse
import com.uts.socialuts.models.SliderItem
import com.uts.socialuts.providers.*
import com.uts.socialuts.utils.RelativeTime
import com.uts.socialuts.utils.ViewedMessageHelper
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class PostDetailActivity : AppCompatActivity() {
    var mSliderView: SliderView? = null
    var mSliderAdapter: SliderAdapter? = null
    var mSliderItems: MutableList<SliderItem> = ArrayList<SliderItem>()
    var mPostProvider: PostProvider? = null
    var mUsersProvider: UsersProvider? = null
    var mCommentsProvider: CommentsProvider? = null
    var mAuthProvider: AuthProvider? = null
    var mLikesProvider: LikesProvider? = null
    var mNotificationProvider: NotificationProvider? = null
    var mTokenProvider: TokenProvider? = null
    var mAdapter: CommentAdapter? = null
    var mExtraPostId: String? = null
    var mTextViewTitle: TextView? = null
    var mTextViewDescription: TextView? = null
    var mTextViewUsername: TextView? = null
    var mTextViewPhone: TextView? = null
    var mTextViewNameCategory: TextView? = null
    var mTextViewRelativeTime: TextView? = null
    var mTextViewLikes: TextView? = null
    var mImageViewCategory: ImageView? = null
    var mCircleImageViewProfile: CircleImageView? = null
    var mButtonShowProfile: Button? = null
    var mFabComment: FloatingActionButton? = null
    var mRecyclerView: RecyclerView? = null
    var mToolbar: Toolbar? = null
    var mIdUser: String? = ""
    var mListener: ListenerRegistration? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)
        mSliderView = findViewById(R.id.imageSlider)
        mTextViewTitle = findViewById(R.id.textViewTitle)
        mTextViewDescription = findViewById(R.id.textViewDescription)
        mTextViewUsername = findViewById(R.id.textViewUsername)
        mTextViewPhone = findViewById(R.id.textViewPhone)
        mTextViewNameCategory = findViewById(R.id.textViewNameCategory)
        mTextViewRelativeTime = findViewById(R.id.textViewRelativeTime)
        mTextViewLikes = findViewById(R.id.textViewLikes)
        mImageViewCategory = findViewById(R.id.imageViewCategory)
        mCircleImageViewProfile = findViewById(R.id.circleImageProfile)
        mButtonShowProfile = findViewById(R.id.btnShowProfile)
        mFabComment = findViewById(R.id.fabComment)
        mRecyclerView = findViewById(R.id.recyclerViewComments)
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val linearLayoutManager = LinearLayoutManager(this@PostDetailActivity)
        mRecyclerView?.layoutManager = linearLayoutManager
        mPostProvider = PostProvider()
        mUsersProvider = UsersProvider()
        mCommentsProvider = CommentsProvider()
        mAuthProvider = AuthProvider()
        mLikesProvider = LikesProvider()
        mNotificationProvider = NotificationProvider()
        mTokenProvider = TokenProvider()
        mExtraPostId = intent.getStringExtra("id")
        mFabComment?.setOnClickListener { showDialogComment() }
        mButtonShowProfile!!.setOnClickListener { goToShowProfile() }
        getPost()
        getNumberLikes()
    }

    private fun getNumberLikes() {
        mListener = mLikesProvider?.getLikesByPost(mExtraPostId)
            ?.addSnapshotListener { queryDocumentSnapshots, e ->
                if (queryDocumentSnapshots != null) {
                    val numberLikes: Int = queryDocumentSnapshots.size()
                    if (numberLikes == 1) {
                        mTextViewLikes?.text = "$numberLikes Me gusta"
                    } else {
                        mTextViewLikes?.text = "$numberLikes Me gustas"
                    }
                }
            }
    }

    protected override fun onStart() {
        super.onStart()
        val query: Query? = mCommentsProvider?.getCommentsByPost(mExtraPostId)
        val options: FirestoreRecyclerOptions<Comment?> = query?.let {
            FirestoreRecyclerOptions.Builder<Comment>()
                .setQuery(it, Comment::class.java)
                .build()
        } as FirestoreRecyclerOptions<Comment?>
        mAdapter = CommentAdapter(options, this@PostDetailActivity)
        mRecyclerView?.adapter = mAdapter
        mAdapter?.startListening()
        ViewedMessageHelper.updateOnline(true, this@PostDetailActivity)
    }

    protected override fun onStop() {
        super.onStop()
        mAdapter?.stopListening()
    }

    protected override fun onPause() {
        super.onPause()
        ViewedMessageHelper.updateOnline(false, this@PostDetailActivity)
    }

    protected override fun onDestroy() {
        super.onDestroy()
        mListener?.remove()
    }

    private fun showDialogComment() {
        val alert = AlertDialog.Builder(this@PostDetailActivity)
        alert.setTitle("¡COMENTARIO!")
        alert.setMessage("Ingresa tu comentario")
        val editText = EditText(this@PostDetailActivity)
        editText.hint = "Texto"
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(36, 0, 36, 36)
        editText.layoutParams = params
        val container = RelativeLayout(this@PostDetailActivity)
        val relativeParams: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        container.layoutParams = relativeParams
        container.addView(editText)
        alert.setView(container)
        alert.setPositiveButton("OK"
        ) { dialogInterface, i ->
            val value: String = editText.text.toString()
            if (value.isNotEmpty()) {
                createComment(value)
            } else {
                Toast.makeText(
                    this@PostDetailActivity,
                    "Debe ingresar el comentario",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        alert.setNegativeButton("Cancelar"
        ) { dialogInterface, i -> }
        alert.show()
    }

    private fun createComment(value: String) {
        val comment = Comment()
        comment.comment = value
        comment.idPost = mExtraPostId
        comment.idUser = mAuthProvider?.getUid()
        comment.timestamp = Date().time
        mCommentsProvider?.create(comment)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                sendNotification(value)
                Toast.makeText(
                    this@PostDetailActivity,
                    "El comentario se creo correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@PostDetailActivity,
                    "No se pudo crear el comentario",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendNotification(comment: String) {
        if (mIdUser == null) {
            return
        }
        mTokenProvider?.getToken(mIdUser)
            ?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("token")) {
                        val token: String? = documentSnapshot.getString("token")
                        val data: MutableMap<String, String> = HashMap()
                        data["title"] = "NUEVO COMENTARIO"
                        data["body"] = comment
                        val body = FCMBody(token, "high", "4500s", data)
                        mNotificationProvider?.sendNotification(body)
                            ?.enqueue(object : Callback<FCMResponse?> {
                                override fun onResponse(
                                    call: Call<FCMResponse?>,
                                    response: Response<FCMResponse?>
                                ) {
                                    if (response.body() != null) {
                                        if (response.body()!!.success === 1) {
                                            Toast.makeText(
                                                this@PostDetailActivity,
                                                "La notificacion se envio correcatemente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@PostDetailActivity,
                                                "La notificacion no se pudo enviar",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            this@PostDetailActivity,
                                            "La notificacion no se pudo enviar",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onFailure(
                                    call: Call<FCMResponse?>,
                                    t: Throwable
                                ) {
                                }
                            })
                    }
                } else {
                    Toast.makeText(
                        this@PostDetailActivity,
                        "El token de notificaciones del usuario no existe",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun goToShowProfile() {
        if (mIdUser != "") {
            val intent = Intent(this@PostDetailActivity, UserProfileActivity::class.java)
            intent.putExtra("idUser", mIdUser)
            startActivity(intent)
        } else {
            Toast.makeText(this, "El id del usuario aun no se carga", Toast.LENGTH_SHORT).show()
        }
    }

    private fun instanceSlider() {
        mSliderAdapter = SliderAdapter(this@PostDetailActivity, mSliderItems)
        mSliderView?.setSliderAdapter(mSliderAdapter!!)
        mSliderView?.setIndicatorAnimation(IndicatorAnimations.THIN_WORM)
        mSliderView?.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
        mSliderView?.autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_RIGHT
        mSliderView?.indicatorSelectedColor = Color.WHITE
        mSliderView?.indicatorUnselectedColor = Color.GRAY
        mSliderView?.scrollTimeInSec = 5
        mSliderView?.isAutoCycle = true
        mSliderView?.startAutoCycle()
    }

    private fun getPost() {
        mPostProvider?.getPostById(mExtraPostId)
            ?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("image1")) {
                        val image1: String? = documentSnapshot.getString("image1")
                        val item = SliderItem()
                        item.imageUrl = image1
                        mSliderItems.add(item)
                    }
                    if (documentSnapshot.contains("image2")) {
                        val image2: String? = documentSnapshot.getString("image2")
                        val item = SliderItem()
                        item.imageUrl = image2
                        mSliderItems.add(item)
                    }
                    if (documentSnapshot.contains("title")) {
                        val title: String? = documentSnapshot.getString("title")
                        mTextViewTitle?.text = title?.uppercase(Locale.getDefault())
                    }
                    if (documentSnapshot.contains("description")) {
                        val description: String? = documentSnapshot.getString("description")
                        mTextViewDescription?.text = description
                    }
                    if (documentSnapshot.contains("category")) {
                        val category: String? = documentSnapshot.getString("category")
                        mTextViewNameCategory?.text = category
                        if (category == "DOCENTE") {
                            mImageViewCategory!!.setImageResource(R.drawable.icon_docente)
                        } else if (category == "DIRECTIVO") {
                            mImageViewCategory!!.setImageResource(R.drawable.icon_directivo)
                        } else if (category == "ALUMNO") {
                            mImageViewCategory!!.setImageResource(R.drawable.icon_alumno)
                        } else if (category == "OPERARIO") {
                            mImageViewCategory!!.setImageResource(R.drawable.icon_operario)
                        }
                    }
                    if (documentSnapshot.contains("idUser")) {
                        mIdUser = documentSnapshot.getString("idUser")
                        getUserInfo(mIdUser)
                    }
                    if (documentSnapshot.contains("timestamp")) {
                        val timestamp: Long? = documentSnapshot.getLong("timestamp")
                        val relativeTime: String? =
                            timestamp?.let { RelativeTime.getTimeAgo(it, this@PostDetailActivity) }
                        mTextViewRelativeTime?.text = relativeTime
                    }
                    instanceSlider()
                }
            }
    }

    private fun getUserInfo(idUser: String?) {
        mUsersProvider?.getUser(idUser)
            ?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.contains("username")) {
                            val username: String? = documentSnapshot.getString("username")
                            mTextViewUsername?.text = username
                        }
                        if (documentSnapshot.contains("phone")) {
                            val phone: String? = documentSnapshot.getString("phone")
                            mTextViewPhone?.text = phone
                        }
                        if (documentSnapshot.contains("image_profile")) {
                            val imageProfile: String? = documentSnapshot.getString("image_profile")
                            Picasso.with(this@PostDetailActivity).load(imageProfile)
                                .into(mCircleImageViewProfile)
                        }
                    }
                }
            }
    }
}