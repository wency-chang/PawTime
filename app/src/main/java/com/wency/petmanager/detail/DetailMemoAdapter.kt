package com.wency.petmanager.detail

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.create.events.adapter.TagListAdapter
import com.wency.petmanager.databinding.ItemDetailMemoListBinding

class DetailMemoAdapter(private val editable: Boolean):
    ListAdapter<String, RecyclerView.ViewHolder>(TagListAdapter.DiffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType){
            TagListAdapter.ITEM_TYPE_TAG -> {
                TagHolder(ItemDetailMemoListBinding.inflate(layoutInflater, parent, false))
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is TagHolder -> {
                Log.d("memo","${getItem(position)}")
                holder.bind(getItem(position))
                holder.bindClickable(editable)

            }
        }
    }

    class TagHolder(val binding: ItemDetailMemoListBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(memo: String){
            binding.memo = memo
            binding.executePendingBindings()
        }
        fun bindClickable(clickable: Boolean){
            binding.clickableMemoCard.isClickable = clickable
            binding.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)){

            else ->{
                TagListAdapter.ITEM_TYPE_TAG
            }

        }

    }
}