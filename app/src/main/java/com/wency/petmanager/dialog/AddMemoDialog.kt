package com.wency.petmanager.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import com.wency.petmanager.R
import com.wency.petmanager.databinding.DialogAddMemoBinding

class AddMemoDialog(private val listener: MemoDialogListener,
                    private val memoText: String = "",
                    private val position: Int? = null): AppCompatDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AddContentDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DialogAddMemoBinding.inflate(layoutInflater, container, false)
        binding.dialogFragment = this

        if (memoText.isNotEmpty()){
            binding.memoText = memoText
        }

        binding.memoAddConfirmButton.setOnClickListener {
            val memo: String = binding.memoAddContext.text.toString()
            if (memo.isEmpty()){
                Toast.makeText(context, this.getString(R.string.EMPTY_MEMO), Toast.LENGTH_SHORT).show()
            } else{
                listener.getMemo(memo, position)
                Toast.makeText(context, this.getString(R.string.MEMO_ADDED), Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
        binding.memoAddCancelButton.setOnClickListener {
            dismiss()
        }
        return binding.root
    }



    interface MemoDialogListener{
        fun getMemo(memo: String, position: Int?)
    }
}