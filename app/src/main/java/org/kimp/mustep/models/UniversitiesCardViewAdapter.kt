package org.kimp.mustep.models

import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import org.kimp.mustep.R
import org.kimp.mustep.databinding.ViewUniversityCardBinding
import org.kimp.mustep.domain.University
import org.kimp.mustep.domain.User
import org.kimp.mustep.ui.activity.EventsActivity
import org.kimp.mustep.ui.activity.TravelActivity
import org.kimp.mustep.ui.dialog.AuthDialog
import org.kimp.mustep.utils.AppCache
import org.kimp.mustep.utils.service.BackgroundDownloadingService
import org.kimp.mustep.utils.service.DOWNLOADING_SERVICE_MSG_QUEUE


class UniversitiesCardViewAdapter(
    private val universities: List<University>,
    private val owner: AppCompatActivity
) : RecyclerView.Adapter<UniversityCardViewHolder>() {
    private var mService: Messenger? = null
    private var mBound = false

    fun bindToService() {
        val intent = Intent(owner, BackgroundDownloadingService::class.java)
        intent.type = "remote"

        owner.bindService(
            intent, connection, BIND_AUTO_CREATE
        )
    }

    fun unbindFromService() {
        owner.unbindService(connection)
        mBound = false;
    }

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
            ).placeholder(R.drawable.ic_downloading)
            .into(holder.headImage)

        holder.startBtn!!.setOnClickListener {
            val mapIntent = Intent(it.context, TravelActivity::class.java)
            mapIntent.putExtra("university", universities[position])
            owner.startActivity(mapIntent)
        }

        holder.officialChip!!.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser == null) {
                val tempUni = universities[position]
                Snackbar.make(
                    owner, holder.officialChip!!, owner.resources.getString(R.string.ucv_login_require), Snackbar.LENGTH_LONG
                ).setAction(R.string.ucv_auth) {
                    val dialog = AuthDialog(owner)
                    dialog.setOnAuthListener(object : AuthDialog.OnAuthCompleted {
                        override fun authCompleted(user: User) {
                            val eventsIntent = Intent(owner, EventsActivity::class.java)
                            eventsIntent.putExtra("university", tempUni)
                            owner.startActivity(eventsIntent)
                        }
                    })
                    dialog.show()
                }.show()

                return@setOnClickListener
            }

            val eventsIntent = Intent(it.context, EventsActivity::class.java)
            eventsIntent.putExtra("university", universities[position])
            owner.startActivity(eventsIntent)
        }

        if (AppCache.isUniversityCached(universities[position].uid, owner)) {
            holder.getBtn!!.icon = ResourcesCompat.getDrawable(
                holder.getBtn!!.context.resources,
                R.drawable.ic_done,
                holder.getBtn!!.context.theme
            );
            holder.getBtn!!.setText(R.string.ucv_state_downloaded);
        } else {
            holder.getBtn!!.setOnClickListener {
                it.isEnabled = false

                if (mBound) {
                    val message = Message()
                    message.what = DOWNLOADING_SERVICE_MSG_QUEUE

                    Bundle().apply {
                        this.putParcelable("data", universities[position])
                        message.data = this
                    }

                    try {
                        mService?.send(message)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    Snackbar.make(
                        it, R.string.cache_warn, Snackbar.LENGTH_LONG
                    ).show();
                }
            }
        }
    }

    override fun getItemCount(): Int = universities.size

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            mService = Messenger(service)
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }
}