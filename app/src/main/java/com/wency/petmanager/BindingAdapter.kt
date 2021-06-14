package com.wency.petmanager

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingConversion
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wency.petmanager.create.events.adapter.*
import com.wency.petmanager.create.pet.CategoryAdapter
import com.wency.petmanager.data.*
import com.wency.petmanager.home.adapter.PetHeaderAdapter
import com.wency.petmanager.home.adapter.TimeLineMainAdapter
import com.wency.petmanager.detail.DetailMemoAdapter
import com.wency.petmanager.detail.DetailTagListAdapter
import com.wency.petmanager.detail.UserHeaderAdapter
import com.wency.petmanager.dialog.mission.MissionAdapter
import com.wency.petmanager.friend.FriendChooseAdapter
import com.wency.petmanager.friend.InviteListAdapter
import com.wency.petmanager.friend.FriendGridListAdapter
import com.wency.petmanager.friend.FriendPetListAdapter
import com.wency.petmanager.home.adapter.TagQueryAdapter
import com.wency.petmanager.memory.apater.MemoryListAdapter
import com.wency.petmanager.network.LoadApiStatus
import com.wency.petmanager.profile.CoverPhotoAdapter
import com.wency.petmanager.profile.record.RecordListAdapter

@BindingAdapter("HomeAPIStatus")
fun bindStatus(statusImageView: ImageView, status: LoadApiStatus?){
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
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_paw_time__ui__06))
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
            .centerCrop()
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_paw_time__ui__06))
//                    .error(R.drawable.ic_placeholder))
            .into(imgView)
    }
}

@BindingAdapter("imageUriCircle")
fun bindImageCircle(imgView: ImageView, imgUri: Uri?) {
    imgUri?.let {
        val imgUri = imgUri.path
        Glide.with(imgView.context)
            .load(imgUri)
            .circleCrop()
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_paw_time__ui__06))
//            .apply(
//                RequestOptions()
//                    .placeholder(R.drawable.image_placeholder)
//                    .error(R.drawable.image_strikethrough))
            .into(imgView)

    }
}

@BindingAdapter("imageUrlTest")
fun bindImageTest(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = it.toUri().buildUpon().build()
        Glide.with(imgView.context)
            .load(imgUri)
            .fitCenter()
            .centerCrop()
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_paw_time__ui__06))
//                    .error(R.drawable.ic_placeholder))
            .into(imgView)
    }
}


@BindingAdapter("petPhotoIconAdapter")
fun petHeaderBindRecyclerView(recyclerView: RecyclerView, data: List<Pet>?) {
    data?.let {
        recyclerView.adapter?.apply {
            when(this){
                is PetHeaderAdapter -> submitList(it)
                is FriendPetListAdapter -> submitList(it)
                is com.wency.petmanager.detail.PetHeaderAdapter -> submitList(it)
                is MemoryListAdapter -> submitList(it)
            }
        }
    }
}


@BindingAdapter("petSelectorAdapter")
fun petSelectorBindRecyclerView(recyclerView: RecyclerView, data: List<PetSelector>?) {
    data?.let {
        recyclerView.adapter?.apply {
            when(this){

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
    data?.let {
        recyclerView.adapter?.apply {

            when(this){
                is TagListAdapter -> submitList(it)
                is MemoListAdapter -> submitList(it)
                is PhotoListAdapter -> submitList(it)
                is CategoryAdapter -> submitList(it)
                is CoverPhotoAdapter -> submitList(it)
                is DetailTagListAdapter -> submitList(it)
                is DetailMemoAdapter -> submitList(it)
                is com.wency.petmanager.detail.PhotoListAdapter -> submitList(it)
                is TagQueryAdapter -> submitList(it)
            }
        }
    }
}

@BindingAdapter("userAdapter")
fun userBindRecyclerView(recyclerView: RecyclerView, data: List<UserInfo>?) {
    data?.let {
        recyclerView.adapter?.apply {
            when(this){
                is UserListAdapter -> submitList(it)
                is InviteListAdapter -> submitList(it)
                is FriendGridListAdapter -> submitList(it)
                is FriendChooseAdapter -> submitList(it)
                is UserHeaderAdapter -> submitList(it)

            }
        }
    }
}

@BindingAdapter("missionGroupAdapter")
fun missionBindRecyclerView(recyclerView: RecyclerView, data: List<MissionGroup>?) {
    data?.let {
        recyclerView.adapter?.apply {
            when(this){
                is MissionAdapter -> submitList(it)
            }
        }
    }
}

@BindingAdapter("recordDocumentAdapter")
fun recordListBindRecyclerView(recyclerView: RecyclerView, data: List<RecordDocument>?) {
    data?.let {
        recyclerView.adapter?.apply {
            when(this){
                is RecordListAdapter -> submitList(it)
            }
        }
    }
}








