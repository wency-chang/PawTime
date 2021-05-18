package com.wency.petmanager

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingConversion
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.TimelineItem
import com.wency.petmanager.diary.PetSelectorAdapter
import com.wency.petmanager.diary.TagListAdapter
import com.wency.petmanager.diary.TagListAdapter2
import com.wency.petmanager.home.PetHeaderAdapter
import com.wency.petmanager.home.TimeLineMainAdapter
import com.wency.petmanager.network.LoadApiStatus

@BindingAdapter("HomeAPIStatus")
fun bindStatus (statusImageView: ImageView, status : LoadApiStatus?){
    when (status){
        LoadApiStatus.LOADING -> {
            statusImageView.visibility = View.VISIBLE

        }
        LoadApiStatus.ERROR -> {
            statusImageView.visibility = View.VISIBLE


        }
        LoadApiStatus.DONE -> {
            statusImageView.visibility = View.GONE
        }
    }
}

@BindingConversion
fun convertStringToColorDrawable(color: String): ColorDrawable {
    return ColorDrawable(Color.parseColor(color))
}

@BindingAdapter("imageUrlCircle")
fun bindImageCircle(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(imgView.context)
            .load(imgUri)
            .circleCrop()
//            .apply(
//                RequestOptions()
//                    .placeholder(R.drawable.image_placeholder)
//                    .error(R.drawable.image_strikethrough))
            .into(imgView)

    }
}
@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = it.toUri().buildUpon().build()
        Glide.with(imgView.context)
            .load(imgUri)
//            .apply(
//                RequestOptions()
//                    .placeholder(R.drawable.ic_placeholder)
//                    .error(R.drawable.ic_placeholder))
            .into(imgView)
    }
}


@BindingAdapter("petPhotoIconAdapter")
fun petOptionBindRecyclerView(recyclerView: RecyclerView, data: List<Pet>?) {
    data?.let {
        recyclerView.adapter?.apply {
            when(this){
                is PetHeaderAdapter -> submitList(it)
                is PetSelectorAdapter -> submitList(it)
            }
        }
    }
}

@BindingAdapter("timelineAdapter")
fun timelineBindRecyclerView(recyclerView: RecyclerView, data: List<TimelineItem>?) {
    val adapter = recyclerView.adapter as TimeLineMainAdapter
    adapter.submitList(data)
}
@BindingAdapter("tagSelectorAdapter")
fun tagOptionBindRecyclerView(recyclerView: RecyclerView, data: List<String>?) {
    Log.d("tagListDebug","binding adapter called")
    data?.let {
        recyclerView.adapter?.apply {
            when(this){
                is TagListAdapter -> submitList(it)
                is TagListAdapter2 -> submitList(it)
            }
        }
    }
}






