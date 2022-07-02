package org.kimp.mustep.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import org.kimp.mustep.databinding.DialogChangelogBinding
import org.kimp.mustep.models.changelog.ChangelogViewListener
import org.kimp.mustep.models.changelog.ChangelogViewModel

class ChangelogDialog : DialogFragment() {
    lateinit var binding: DialogChangelogBinding

    lateinit var viewModel: ChangelogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[ChangelogViewModel::class.java]
        isCancelable = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogChangelogBinding.inflate(inflater, container, false)

        binding.root.minWidth = requireContext().resources.displayMetrics.widthPixels * 9 / 10
        binding.root.minHeight = requireContext().resources.displayMetrics.heightPixels * 9 / 10

        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )

        viewModel.getChanges().observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe

            val viewListener = ChangelogViewListener(it, requireActivity())
            binding.adChangesCarousel.setViewListener(viewListener)
            binding.adChangesCarousel.pageCount = it.size
        }

        binding.adCloseBtn.setOnClickListener { this.dismiss() }

        return binding.root
    }
}
