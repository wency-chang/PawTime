package com.wency.petmanager.detail

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.create.events.adapter.TagListAdapter
import com.wency.petmanager.databinding.ItemDetailMemoListBinding

class DetailMemoAdapter(val editable: LiveData<Boolean>,
                        val lifecycleOwner: LifecycleOwner,
                        private val onClickListener: OnClickListener):
    ListAdapter<String, DetailMemoAdapter.MemoHolder>(TagListAdapter.DiffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MemoHolder(ItemDetailMemoListBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: MemoHolder, position: Int) {



        holder.cancelButton.setOnClickListener {
            onClickListener.onClick(false, position, "")
            notifyDataSetChanged()
        }
        holder.memoText.setOnClickListener {
            onClickListener.onClick(true, position, getItem(position))
        }
        editable.value?.let { holder.bind(getItem(position), it, this) }
    }

    class MemoHolder(val binding: ItemDetailMemoListBinding): RecyclerView.ViewHolder(binding.root){
        val memoText = binding.memoTextEdit
        val cancelButton = binding.memoCancelButton


        fun bind(memo: String, editable: Boolean, adapter: DetailMemoAdapter){

            if (editable){
                cancelButton.visibility = View.VISIBLE
            } else {
                cancelButton.visibility = View.GONE

            }
            binding.editable = editable

            binding.memo = memo
            binding.clickableMemoCard.visibility = View.VISIBLE
            binding.memoTextEdit.visibility = View.VISIBLE

            binding.executePendingBindings()
        }
    }

    class OnClickListener(val clickListener:(add: Boolean, position: Int?, memo: String)-> Unit){
        fun onClick(add: Boolean, position: Int?, memo: String) = clickListener(add, position, memo)
    }


}