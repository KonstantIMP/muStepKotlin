package org.kimp.mustep.models

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso
import org.kimp.mustep.R
import org.kimp.mustep.databinding.ViewUniversityCardBinding
import org.kimp.mustep.domain.University
import org.kimp.mustep.utils.AppCache


class UniversitiesCardViewAdapter(
    private val universities: List<University>
) : RecyclerView.Adapter<UniversityCardViewHolder>() {
    lateinit var placeholder: Drawable

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UniversityCardViewHolder {
        return UniversityCardViewHolder(
            ViewUniversityCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ).root
        )
    }

    override fun onBindViewHolder(holder: UniversityCardViewHolder, position: Int) {
        holder.nameLabel!!.text = universities[position].name.getTranslatedValue()
        holder.descLabel!!.text = universities[position].address.getTranslatedValue()

        holder.officialChip!!.visibility = if (universities[position].official) View.VISIBLE else View.GONE

        Picasso.get()
            .load(
                AppCache.getCacheSupportUri(String.format(
                    "%s/head.png", universities[position].uid
                ), holder.nameLabel!!.context)
            ).placeholder(createPlaceholder(holder.headImage!!))
            .into(holder.headImage)
    }

    private fun createPlaceholder(parent: ShapeableImageView): Drawable {
        if (this::placeholder.isInitialized) return placeholder

        val typedValue = TypedValue()
        parent.context.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        val color = typedValue.data

        var drawablePlaceholder = ResourcesCompat.getDrawable(
            parent.context.resources, R.drawable.ic_downloading, parent.context.theme
        )!!
        DrawableCompat.setTint(drawablePlaceholder, color)

        return drawablePlaceholder
    }

    override fun getItemCount(): Int = universities.size
}