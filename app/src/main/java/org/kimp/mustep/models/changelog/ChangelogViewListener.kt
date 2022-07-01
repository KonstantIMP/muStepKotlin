package org.kimp.mustep.models.changelog

import android.app.Activity
import android.view.View
import com.synnapps.carouselview.ViewListener
import org.kimp.mustep.databinding.ViewChangeCardBinding
import org.kimp.mustep.domain.Change

class ChangelogViewListener(private val changes: List<Change>,
    private val owner: Activity) : ViewListener {
    override fun setViewForPosition(position: Int): View {
        val binding = ViewChangeCardBinding.inflate(
            owner.layoutInflater
        )

        binding.ccvDescMsg.text = changes[position].desc.getTranslatedValue()
        binding.ccvImage.setImageBitmap(
            changes[position].imageBitmap
        )

        return binding.root
    }
}