package com.uts.socialuts.fragments

import com.uts.socialuts.R
import android.content.Intent
import android.view.ViewGroup
import android.view.LayoutInflater
import android.os.Bundle
import androidx.cardview.widget.CardView
import com.uts.socialuts.activities.FiltersActivity
import android.view.View
import androidx.fragment.app.Fragment

/**
 * A simple [Fragment] subclass.
 */
class FiltersFragment : Fragment() {
    var mView: View? = null
    var mCardViewDocente: CardView? = null
    var mCardViewDirectivo: CardView? = null
    var mCardViewOperario: CardView? = null
    var mCardViewAlumno: CardView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_filters, container, false)
        mCardViewDocente = mView?.findViewById(R.id.cardViewDocente)
        mCardViewDirectivo = mView?.findViewById(R.id.cardViewDirectivo)
        mCardViewOperario = mView?.findViewById(R.id.cardViewOperario)
        mCardViewAlumno = mView?.findViewById(R.id.cardViewAlumno)
        mCardViewDocente?.setOnClickListener { goToFilterActivity("DOCENTE") }
        mCardViewDirectivo?.setOnClickListener { goToFilterActivity("DIRECTIVO") }
        mCardViewOperario?.setOnClickListener { goToFilterActivity("OPERARIO") }
        mCardViewAlumno?.setOnClickListener { goToFilterActivity("ALUMNO") }
        return mView
    }

    private fun goToFilterActivity(category: String) {
        val intent = Intent(context, FiltersActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }
}