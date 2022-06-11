package org.kimp.mustep.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import org.kimp.mustep.databinding.DialogAuthBinding

class AuthDialog(context: Context) : Dialog(context) {
    private lateinit var binding: DialogAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)

        window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )

        binding = DialogAuthBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        binding.root.minWidth = context.resources.displayMetrics.widthPixels * 9 / 10
        binding.root.minHeight = context.resources.displayMetrics.heightPixels * 9 / 10
    }
}
