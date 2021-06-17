package com.wency.petmanager.home.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.databinding.SubItemMemoTextBinding

class MemoAdapter(val memoList: List<String>):RecyclerView.Adapter<MemoAdapter.MemoListHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoListHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MemoListHolder(
            SubItemMemoTextBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: MemoListHolder, position: Int) {
        holder.bind(memoList[position])
    }

    override fun getItemCount(): Int {
        return memoList.size
    }

    class MemoListHolder(val binding: SubItemMemoTextBinding): RecyclerView.ViewHolder(binding.root){

       fun bind(memo: String){
           binding.memo = memo
           binding.executePendingBindings()
       }
    }
}