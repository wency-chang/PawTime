package com.wency.petmanager.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.databinding.DialogAddMemoBinding
import com.wency.petmanager.databinding.DialogAddTagBinding

class AddNewTagDialog(val listener: AddNewTagListener, val tagList: MutableList<String>): AppCompatDialogFragment() {
    private val checkRepeatSet: MutableSet<String> = tagList.toMutableSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AddContentDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DialogAddTagBinding.inflate(layoutInflater, container, false)
        binding.dialogFragment = this
        binding.enterTagConfirmButton.setOnClickListener {
            val tag: String = binding.newTagText.text.toString()
            checkRepeatSet.add(tag)
            if (tag.isNullOrEmpty()){
                Toast.makeText(context, "No Tag Enter", Toast.LENGTH_SHORT).show()
            } else if ( checkRepeatSet.size == tagList.size){
                Toast.makeText(context, "Tag Already Existed", Toast.LENGTH_SHORT).show()
            }
            else{
                listener.getTag(tag)
                Toast.makeText(context, "Add Tag", Toast.LENGTH_SHORT).show()
                dismiss()
            }

        }
        binding.enterTagCancelButton.setOnClickListener {
            dismiss()
        }
        return binding.root
    }
    interface AddNewTagListener{
        fun getTag(tag: String)
    }

}