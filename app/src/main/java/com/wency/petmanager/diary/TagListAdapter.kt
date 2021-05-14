package com.wency.petmanager.diary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.databinding.ItemTagAddBinding
import com.wency.petmanager.databinding.ItemTagExpendBinding
import com.wency.petmanager.databinding.ItemTagViewBinding

class TagListAdapter(private val tagList: MutableList<String>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val listSize = 4

    var list : MutableList<String?> = mutableListOf()
    var isListExpend = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when(viewType){
            ITEM_TYPE_TAG -> TagViewHolder(ItemTagViewBinding.inflate(layoutInflater, parent, false))
            ITEM_TYPE_ADD -> AddTagViewHolder(ItemTagAddBinding.inflate(layoutInflater, parent, false))
            ITEM_TYPE_EXPEND -> ExpendViewHolder(ItemTagExpendBinding.inflate(layoutInflater, parent, false))
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder){
            is TagViewHolder -> {holder.bind(list[position])}
        }
    }

    class TagViewHolder(val binding: ItemTagViewBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(tag: String?){
            tag?.let {

            }
        }
    }
    class AddTagViewHolder(val binding: ItemTagAddBinding): RecyclerView.ViewHolder(binding.root){

    }
    class ExpendViewHolder(val binding: ItemTagExpendBinding): RecyclerView.ViewHolder(binding.root){

    }

    override fun getItemCount(): Int {
        if (isListExpend){
            list.addAll(tagList)
            list.add("optionAdd")
        } else if (tagList.size >= listSize){
            list.addAll(tagList.subList(0,listSize-2))
            list.add(null)
        } else {
            list.add("optionAdd")
        }
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(list[position]){
            null -> ITEM_TYPE_EXPEND
            "optionAdd" -> ITEM_TYPE_ADD
            else -> ITEM_TYPE_TAG
        }
    }

    companion object {
        private const val ITEM_TYPE_ADD = 0x00
        private const val ITEM_TYPE_EXPEND = 0x01
        private const val ITEM_TYPE_TAG = 0x02
    }
}