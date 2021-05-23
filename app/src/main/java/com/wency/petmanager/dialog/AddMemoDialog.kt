package com.wency.petmanager.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.databinding.DialogAddMemoBinding
import java.lang.ClassCastException
import java.lang.RuntimeException

class AddMemoDialog(val listener: MemoDialogListener): AppCompatDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AddContentDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DialogAddMemoBinding.inflate(layoutInflater, container, false)
        binding.dialogFragment = this
        binding.memoAddConfirmButton.setOnClickListener {
            val memo: String = binding.memoAddContext.text.toString()
            if (memo.isNullOrEmpty()){
                Toast.makeText(context, "No Memo", Toast.LENGTH_SHORT).show()
            } else{
                listener.getMemo(memo)
                Toast.makeText(context, "Add Memo", Toast.LENGTH_SHORT).show()
                dismiss()
            }

        }
        binding.memoAddCancelButton.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)



    }


    interface MemoDialogListener{
        fun getMemo(memo: String)
    }
}