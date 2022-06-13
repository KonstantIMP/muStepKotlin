package org.kimp.mustep.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.GsonBuilder
import org.kimp.mustep.R
import org.kimp.mustep.databinding.DialogAuthBinding
import org.kimp.mustep.domain.User
import org.kimp.mustep.rest.MuStepServiceBuilder
import org.kimp.mustep.utils.PreferencesData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthDialog(context: Context) : Dialog(context) {
    private lateinit var binding: DialogAuthBinding

    private var callbackListener: OnAuthCompleted? = null
    private var isSignUp = true

    interface OnAuthCompleted {
        fun authCompleted(user: User)
    }

    fun setOnAuthListener(listener: OnAuthCompleted) {
        callbackListener = listener
    }

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

        connectHandlers()
    }

    private fun connectHandlers() {
        binding.adAuthTypeMsg.setOnClickListener { changeAuthType() }
        binding.adAuthBtn.setOnClickListener { processAuth() }
    }

    private fun changeAuthType() {
        binding.adMoverTv.minHeight = binding.adPassIl.height
        binding.adMoverTv.maxHeight = binding.adPassIl.height

        binding.adAuthTypeMsg.text = if (isSignUp) context.resources.getString(R.string.ad_do_not_have_account) else
            context.resources.getString(R.string.ad_has_account)

        val fadeSlideAnimation = AnimationUtils.loadAnimation(
            context, if (isSignUp) R.anim.fade_slide_out else R.anim.fade_slide_in
        )

        val slideAnimation = AnimationUtils.loadAnimation(
            context, if (isSignUp) R.anim.slide_out else R.anim.slide_in
        )

        fadeSlideAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) { }

            override fun onAnimationEnd(p0: Animation?) {
                binding.adNameIl.visibility = if (isSignUp) View.GONE
                    else View.VISIBLE

                isSignUp = !isSignUp
            }

            override fun onAnimationRepeat(p0: Animation?) { }
        })

        binding.adNameIl.startAnimation(fadeSlideAnimation)

        if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.adMoverTv.startAnimation(slideAnimation)
            binding.adEmailIl.startAnimation(slideAnimation)
            binding.adPassIl.startAnimation(slideAnimation)
        }
    }

    private fun processAuth() {
        var incorrectInput = false

        if (binding.adEmailIt.text.toString().isEmpty()) {
            binding.adEmailIl.error = context.getString(R.string.pa_entry_edit)
            incorrectInput = true
        }

        if (binding.adPassIt.text.toString().isEmpty()) {
            binding.adPassIl.error = context.getString(R.string.pa_entry_edit)
            incorrectInput = true
        }

        if (binding.adNameIt.text.toString().isEmpty() && isSignUp) {
            binding.adNameIl.error = context.getString(R.string.pa_entry_edit)
            incorrectInput = true
        }

        if (incorrectInput) return
        setLayoutState(false)

        if (isSignUp) processSignUp()
        else processLogIn()
    }

    private fun processSignUp() {
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(
                binding.adEmailIt.text.toString().trim(),
                binding.adPassIt.text.toString()
            ).addOnFailureListener {
                Snackbar.make(
                    context, binding.root,
                    String.format("%s %s", context.getString(R.string.error_preview), it.localizedMessage), Snackbar.LENGTH_LONG
                ).show()
                setLayoutState(true)
            }.addOnSuccessListener {
                val newUser = User()
                newUser.uid = it.user!!.uid
                newUser.email = it.user!!.email!!
                newUser.name = binding.adNameIt.text.toString().trim()

                MuStepServiceBuilder.build()
                    .newUser(newUser)
                    .enqueue(
                        object: Callback<User> {
                            override fun onResponse(call: Call<User>, response: Response<User>) {
                                completeAuth(response.body()!!)
                                this@AuthDialog.dismiss()
                            }

                            override fun onFailure(call: Call<User>, t: Throwable) {
                                Snackbar.make(
                                    context, binding.root,
                                    String.format("%s %s", context.getString(R.string.error_preview), t.localizedMessage), Snackbar.LENGTH_LONG
                                ).show()
                                setLayoutState(true)
                            }
                        }
                    )
            }
    }

    private fun processLogIn() {
        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(
                binding.adEmailIt.text.toString().trim(),
                binding.adPassIt.text.toString()
            ).addOnFailureListener {
                Snackbar.make(
                    context, binding.root,
                    String.format("%s %s", context.getString(R.string.error_preview), it.localizedMessage), Snackbar.LENGTH_LONG
                ).show()
                setLayoutState(true)
            }.addOnSuccessListener {
                MuStepServiceBuilder.build()
                    .getUser(it.user!!.uid)
                    .enqueue(
                        object: Callback<User> {
                            override fun onResponse(call: Call<User>, response: Response<User>) {
                                completeAuth(response.body()!!)
                                this@AuthDialog.dismiss()
                            }

                            override fun onFailure(call: Call<User>, t: Throwable) {
                                Snackbar.make(
                                    context, binding.root,
                                    String.format("%s %s", context.getString(R.string.error_preview), t.localizedMessage), Snackbar.LENGTH_LONG
                                ).show()
                                setLayoutState(true)
                            }
                        }
                    )
            }
    }

    private fun completeAuth(user: User) {
        val pref = context.getSharedPreferences(PreferencesData.BASE_PREFERENCES_NAME, MODE_PRIVATE)

        val editor = pref.edit()
        editor.putString("user", GsonBuilder().create().toJson(user))
        editor.apply()

        callbackListener?.authCompleted(user)
    }

    private fun setLayoutState(state: Boolean, v: View = binding.root) {
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) {
                setLayoutState(state, v.getChildAt(i))
            }
        }
        v.isEnabled = state
    }
}
