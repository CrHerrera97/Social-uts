package com.uts.socialuts.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.uts.socialuts.R
import de.hdodenhof.circleimageview.CircleImageView

class TermsActivity : AppCompatActivity() {

    var mCircleImageBack: CircleImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)
        mCircleImageBack = findViewById(R.id.circleImageBack)

        mCircleImageBack!!.setOnClickListener { finish() }
    }
}