package com.wency.petmanager.detail

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.create.events.DiaryCreateViewModel
import com.wency.petmanager.create.events.adapter.TagListAdapter
import com.wency.petmanager.databinding.ItemTagViewBinding
import kotlinx.android.synthetic.main.item_tag_view.view.*

class DetailTagListAdapter(private val editable: LiveData<Boolean>,
                           private val selectedList: MutableList<String>,
                            private val onClickListener: OnClickListener):
    ListAdapter<String, DetailTagListAdapter.TagHolder>(TagListAdapter.DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TagHolder(ItemTagViewBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: TagHolder, position: Int) {

        val tag = getItem(position)
        holder.bind(tag)
        editable.value?.let { holder.bindClickable(it) }
        if (editable.value == true){
            holder.bindSelected(selectedList.contains(tag))
        } else {
            holder.bindSelected(false)
        }

        holder.tag.setOnClickListener {

            onClickListener.onClick(it.chipView.isChecked, it.chipView.text as String)

        }
    }

    class TagHolder(val binding: ItemTagViewBinding): RecyclerView.ViewHolder(binding.root){
        val tag = binding.chipView

        fun bind(tag: String){
            binding.tag = tag
            binding.executePendingBindings()
        }
        fun bindClickable(clickable: Boolean){
            tag.isCheckable = clickable
            tag.isClickable = clickable
            binding.executePendingBindings()
        }

        fun bindSelected(selected: Boolean){
            binding.chipView.isChecked = selected
            binding.executePendingBindings()
        }
    }

    class OnClickListener(val clickListener:(add: Boolean, tag: String)-> Unit){
        fun onClick(add: Boolean, tag: String) = clickListener(add, tag)
    }


}