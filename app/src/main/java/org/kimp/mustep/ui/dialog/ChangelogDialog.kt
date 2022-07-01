package org.kimp.mustep.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import org.kimp.mustep.databinding.DialogChangelogBinding

class ChangelogDialog : DialogFragment() {
    lateinit var binding: DialogChangelogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isCancelable = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialog?.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )

        binding = DialogChangelogBinding.inflate(
            inflater, container, false
        )

        binding.adTitle.minWidth = requireContext().resources.displayMetrics.widthPixels * 90 / 100

        return binding.root
    }
}