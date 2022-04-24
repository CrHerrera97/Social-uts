package com.uts.socialuts.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.smarteist.autoimageslider.SliderViewAdapter
import com.squareup.picasso.Picasso
import com.uts.socialuts.R
import com.uts.socialuts.models.SliderItem
import java.util.ArrayList

class SliderAdapter(private val context: Context, sliderItems: List<SliderItem>) :
    SliderViewAdapter<SliderAdapter.SliderAdapterVH?>() {
    private var mSliderItems: List<SliderItem> = ArrayList<SliderItem>()
    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH {
        val inflate: View =
            LayoutInflater.from(parent.context).inflate(R.layout.slider_layout_item, null)
        return SliderAdapterVH(inflate)
    }

    override fun onBindViewHolder(viewHolder: SliderAdapterVH?, position: Int) {
        val sliderItem: SliderItem = mSliderItems[position]
        if (sliderItem.imageUrl != null) {
            if (sliderItem.imageUrl!!.isNotEmpty()) {
                Picasso.with(context).load(sliderItem.imageUrl)
                    .into(viewHolder?.imageViewSlider)
            }
        }
    }

    override fun getCount(): Int {
        //slider view count could be dynamic size
        return mSliderItems.size
    }

    inner class SliderAdapterVH(var itemView: View) : SliderViewAdapter.ViewHolder(itemView) {
        var imageViewSlider: ImageView = itemView.findViewById(R.id.imageViewSlider)

    }

    init {
        mSliderItems = sliderItems
    }
}