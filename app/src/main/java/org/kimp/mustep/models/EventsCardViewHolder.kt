package org.kimp.mustep.models

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import io.getstream.avatarview.AvatarView
import org.kimp.mustep.R

class EventsCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var eventNameMsg: MaterialTextView? = null
    var eventUsersChip: Chip? = null
    var eventDateChip: Chip? = null
    var eventGuideAvatar: AvatarView? = null
    var eventGuideName: MaterialTextView? = null
    var eventRuSupported: ShapeableImageView? = null
    var eventZhSupported: ShapeableImageView? = null
    var eventEnSupported: ShapeableImageView? = null
    var ecvRegisterButton: MaterialButton? = null

    init {
        eventNameMsg = itemView.findViewById(R.id.ecv_event_name_msg)
        eventUsersChip = itemView.findViewById(R.id.ecv_event_users_chip)
        eventDateChip = itemView.findViewById(R.id.ecv_date_chip)
        eventGuideAvatar = itemView.findViewById(R.id.ecv_guide_avatar)
        eventGuideName = itemView.findViewById(R.id.ecv_guide_name_msg)
        eventRuSupported = itemView.findViewById(R.id.ecv_ru_img)
        eventZhSupported = itemView.findViewById(R.id.ecv_zh_img)
        eventEnSupported = itemView.findViewById(R.id.ecv_en_img)
        ecvRegisterButton = itemView.findViewById(R.id.ecv_register_btn)
    }
}
