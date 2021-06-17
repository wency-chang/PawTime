package com.wency.petmanager.create.events.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.wency.petmanager.create.events.DiaryCreateViewModel
import com.wency.petmanager.databinding.ItemTagAddBinding
import com.wency.petmanager.databinding.ItemTagCloseBinding
import com.wency.petmanager.databinding.ItemTagExtendBinding
import com.wency.petmanager.databinding.ItemTagViewBinding


class TagListAdapter(private val onClickListener: OnClickListener):
    ListAdapter<String, RecyclerView.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when(viewType){
            ITEM_TYPE_TAG -> TagViewHolder(
                ItemTagViewBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            ITEM_TYPE_ADD -> AddTagViewHolder(
                ItemTagAddBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            ITEM_TYPE_MORE -> ExtendTagViewHolder(
                ItemTagExtendBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            ITEM_TYPE_CLOSE -> CloseTagViewHolder(
                ItemTagCloseBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val layoutParams = holder.itemView.layoutParams

        if (layoutParams is FlexboxLayoutManager.LayoutParams) {
            val flexboxLp: FlexboxLayoutManager.LayoutParams = FlexboxLayoutManager.LayoutParams(holder.itemView.layoutParams)
            flexboxLp.flexGrow = 1.0f
        }

        when (holder){
            is TagViewHolder -> {
                val tag = getItem(position)
                holder.bind(tag)
                holder.chipView.setOnClickListener {
                    onClickListener.onClick(ITEM_TYPE_TAG, tag, holder.chipView.isChecked)
                }
            }
            is AddTagViewHolder -> {

                holder.itemView.setOnClickListener {
                    onClickListener.onClick(ITEM_TYPE_ADD,"", false)
                }

            }
            is ExtendTagViewHolder -> {
                holder.itemView.setOnClickListener {
                    onClickListener.onClick(ITEM_TYPE_MORE,"", false)
                }
            }
            is CloseTagViewHolder -> {
                holder.itemView.setOnClickListener {
                    onClickListener.onClick(ITEM_TYPE_CLOSE, "", false)
                }
            }
        }
    }

    class TagViewHolder(val binding: ItemTagViewBinding):RecyclerView.ViewHolder(binding.root){

        val chipView = binding.chipView
        fun bind(tag: String?){
            tag?.let {
                binding.tag = tag
                binding.executePendingBindings()
            }
        }
    }
    class AddTagViewHolder(val binding: ItemTagAddBinding): RecyclerView.ViewHolder(binding.root)

    class ExtendTagViewHolder(val binding: ItemTagExtendBinding): RecyclerView.ViewHolder(binding.root)

    class CloseTagViewHolder(val binding: ItemTagCloseBinding): RecyclerView.ViewHolder(binding.root)

    class OnClickListener(
        val clickListener: (itemType: Int, itemString: String, itemStatus: Boolean)-> Unit){
        fun onClick(itemType: Int, itemString: String, itemStatus: Boolean) =
            clickListener(itemType, itemString, itemStatus)
    }


    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            DiaryCreateViewModel.ADD_TAG_STRING -> ITEM_TYPE_ADD
            DiaryCreateViewModel.EXTEND_TAG_STRING -> ITEM_TYPE_MORE
            DiaryCreateViewModel.CLOSE_TAG_STRING -> ITEM_TYPE_CLOSE
            else -> ITEM_TYPE_TAG
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
        const val ITEM_TYPE_ADD = 0x00
        const val ITEM_TYPE_TAG = 0x01
        const val ITEM_TYPE_MORE = 0x02
        const val ITEM_TYPE_CLOSE = 0x03
    }

}
