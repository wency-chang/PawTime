package com.wency.petmanager.profile.record

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.RecordDocument
import com.wency.petmanager.data.Records
import com.wency.petmanager.databinding.ItemRecordListBinding
import com.wency.petmanager.profile.TimeFormat

class RecordListAdapter(val onClickListener: OnClickListener): ListAdapter<RecordDocument, RecordListAdapter.RecordListHolder> (DiffCallback) {
    class RecordListHolder(val binding: ItemRecordListBinding): RecyclerView.ViewHolder(binding.root){
        val addButton = binding.recordAddButton
        val detailButton = binding.recordChardButton

        fun bind(recordDocument: RecordDocument){
            binding.recordDocument = recordDocument
            binding.executePendingBindings()
        }

        fun bindData(data: String, lastData: String){
            binding.currentData = data
            binding.lastData = lastData
            binding.executePendingBindings()
        }


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordListHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return RecordListHolder(ItemRecordListBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecordListHolder, position: Int) {
        val recordDocument = getItem(position)
        Log.d("Record","recordDocument: $recordDocument")
        holder.bind(recordDocument)
        val recordData = mutableListOf<Records>()
        recordDocument.recordData.forEach{(key, value): Map.Entry<String, Double> ->
            recordData.add(Records(TimeFormat.dateFormat.parse(key), value))
        }
        recordData.sortBy {
            it.recordDate
        }

        if (recordData.size > 1){
            holder.bindData(recordData.last().recordNumber.toString(), recordData[recordData.lastIndex-1].recordNumber.toString())
        } else if (recordData.size == 1) {
            holder.bindData(recordData.last().recordNumber.toString(), "")
        }

        holder.addButton.setOnClickListener {
            onClickListener.onClick(recordDocument, true)
        }
        holder.detailButton.setOnClickListener {
            onClickListener.onClick(recordDocument, false)
        }

    }
    class OnClickListener(val clickListener:(recordDocument: RecordDocument, new: Boolean)-> Unit){
        fun onClick(recordDocument: RecordDocument, new: Boolean) = clickListener(recordDocument, new)
    }



    companion object DiffCallback: DiffUtil.ItemCallback<RecordDocument>(){
        override fun areItemsTheSame(oldItem: RecordDocument, newItem: RecordDocument): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: RecordDocument, newItem: RecordDocument): Boolean {
            return oldItem.recordData == newItem.recordData
        }

    }



}