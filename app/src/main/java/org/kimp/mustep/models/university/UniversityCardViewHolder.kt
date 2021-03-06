package org.kimp.mustep.models.university

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import org.kimp.mustep.R

class UniversityCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var headImage: ShapeableImageView? = null
    var nameLabel: MaterialTextView? = null
    var descLabel: MaterialTextView? = null
    var startBtn: MaterialButton? = null
    var getBtn: MaterialButton? = null
    var officialChip: Chip? = null

    init {
        officialChip = itemView.findViewById(R.id.ucv_official_chip)
        headImage = itemView.findViewById(R.id.ucv_head_img)
        nameLabel = itemView.findViewById(R.id.ucv_name_msg)
        descLabel = itemView.findViewById(R.id.ucv_desc_msg)
        startBtn = itemView.findViewById(R.id.ucv_start_btn)
        getBtn = itemView.findViewById(R.id.ucv_get_btn)
    }
}
