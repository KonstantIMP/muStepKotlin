package org.kimp.mustep.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.GsonBuilder
import io.getstream.avatarview.coil.loadImage
import java.io.File
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kimp.mustep.R
import org.kimp.mustep.databinding.ActivityPreferencesBinding
import org.kimp.mustep.domain.User
import org.kimp.mustep.rest.MuStepServiceBuilder
import org.kimp.mustep.ui.dialog.AuthDialog
import org.kimp.mustep.ui.fragment.PreferencesFragment
import org.kimp.mustep.utils.AppCache
import org.kimp.mustep.utils.PreferencesData
import org.kimp.mustep.utils.StorageCredentials
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PreferencesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreferencesBinding
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.paPreferencesFragment.id, PreferencesFragment())
                .commit()
        }

        connectHandlers()
        setSignState()
        checkLoggedIn()
    }

    private fun setSignState() {
        binding.paSignedInLayout.visibility = if (FirebaseAuth.getInstance().currentUser == null) View.GONE else View.VISIBLE
        binding.paSignedOutLayout.visibility = if (FirebaseAuth.getInstance().currentUser == null) View.VISIBLE else View.GONE
    }

    private fun checkLoggedIn() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            MuStepServiceBuilder.build()
                .getUser(FirebaseAuth.getInstance().currentUser!!.uid)
                .enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        loadUserData(response.body()!!)
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        try {
                            loadUserData(
                                GsonBuilder().create()
                                    .fromJson(getSharedPreferences(PreferencesData.BASE_PREFERENCES_NAME, MODE_PRIVATE)
                                        .getString("user", ""), User::class.java)
                            )
                        } catch (e: Exception) {
                            Snackbar.make(
                                this@PreferencesActivity, binding.root,
                                String.format("%s %s and %s", getString(R.string.error_preview), t.localizedMessage, e.localizedMessage),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                )
        }
    }

    private fun loadUserData(user: User) {
        currentUser = user

        binding.paEmailMsg.text = user.email
        binding.paNameMsg.text = String.format("%s %s", user.name, user.surname)

        val nameParts = (user.name + " " + user.surname).split("\\s*")
        val nameInitials = StringBuilder()
        for (part in nameParts) if (part.isNotEmpty()) nameInitials.append(part[0])

        binding.paAvatarview.avatarInitials = nameInitials.toString().uppercase()

        if (user.avatar.isNotEmpty()) {
            binding.paAvatarview.loadImage(
                AppCache.getCacheSupportUri(String.format("users/%s", user.avatar), this),
                onSuccess = { _, _ ->
                    binding.paAvatarview.avatarInitials = ""
                    binding.paAvatarview.avatarBorderWidth = 0
                }
            )
        }
    }

    private fun connectHandlers() {
        binding.paAuthBtn.setOnClickListener {
            val dialog = AuthDialog(this)
            dialog.setOnAuthListener(object : AuthDialog.OnAuthCompleted {
                override fun authCompleted(user: User) {
                    Snackbar.make(
                        this@PreferencesActivity, binding.root,
                        getString(R.string.pa_auth_success), Snackbar.LENGTH_LONG
                    ).show()
                    loadUserData(user)
                    setSignState()
                }

            })
            dialog.show()
        }

        binding.paLogoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val pref = getSharedPreferences(PreferencesData.BASE_PREFERENCES_NAME, MODE_PRIVATE)
            val editor = pref.edit()
            editor.remove("user")
            editor.apply()

            setSignState()
        }

        binding.paAvatarview.setOnClickListener {
            val intent = Intent()

            intent.action = Intent.ACTION_PICK
            intent.type = "image/*"

            avatarResultLauncher.launch(intent)
        }
    }

    private val avatarResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != RESULT_OK) return@registerForActivityResult

        lifecycleScope.launch(Dispatchers.IO) {
            val uri = it!!.data!!.data!!
            val inputStream = contentResolver.openInputStream(uri)

            val s3Client = AmazonS3Client(
                StorageCredentials(), Region.getRegion(
                    Regions.US_WEST_2
                ))
            s3Client.endpoint = "storage.yandexcloud.net"

            val avatarName = String.format("%s.%s", currentUser.uid,
                SimpleDateFormat("ddMMyyyyhhmmss", Locale.getDefault()).format(Date()))

            s3Client.putObject("mustep", String.format("users/%s", avatarName), inputStream, ObjectMetadata())

            if (MuStepServiceBuilder.build()
                .updateAvatar(currentUser.uid, avatarName)
                .execute().body()!!) {
                currentUser.avatar = avatarName

                runOnUiThread {
                    loadUserData(currentUser)
                }
            }
        }
    }
}
