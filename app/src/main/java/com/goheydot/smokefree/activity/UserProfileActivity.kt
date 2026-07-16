package com.goheydot.smokefree.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.goheydot.smokefree.R
import java.io.File
import java.io.FileOutputStream

class UserProfileActivity : AppCompatActivity() {

    private lateinit var ivAvatar: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserDesc: TextView
    private lateinit var layoutBindWechat: LinearLayout
    private lateinit var layoutBindPhone: LinearLayout
    private lateinit var layoutBindEmail: LinearLayout
    private lateinit var btnLogout: Button

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
        private const val PERMISSION_READ_IMAGES = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_profile)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.profile_title)

        initViews()
        updateUI()
        setupListeners()
    }

    private fun initViews() {
        ivAvatar = findViewById(R.id.iv_profile_avatar)
        tvUserName = findViewById(R.id.tv_profile_name)
        tvUserDesc = findViewById(R.id.tv_profile_desc)
        layoutBindWechat = findViewById(R.id.layout_bind_wechat)
        layoutBindPhone = findViewById(R.id.layout_bind_phone)
        layoutBindEmail = findViewById(R.id.layout_bind_email)
        btnLogout = findViewById(R.id.btn_profile_logout)
    }

    private fun updateUI() {
        val prefs = getSharedPreferences("smokefree", 0)
        val phone = prefs.getString("phone", "") ?: ""
        val wechatName = prefs.getString("wechat_name", "") ?: ""
        val email = prefs.getString("email", "") ?: ""

        if (phone.isNotEmpty()) {
            tvUserName.text = phone.substring(0, 3) + "****" + phone.substring(7)
            tvUserDesc.text = getString(R.string.phone_login_label)
            layoutBindPhone.visibility = View.GONE
            layoutBindWechat.visibility = View.VISIBLE
            layoutBindEmail.visibility = View.VISIBLE
        } else if (wechatName.isNotEmpty()) {
            tvUserName.text = wechatName
            tvUserDesc.text = getString(R.string.wechat_login_label)
            layoutBindWechat.visibility = View.GONE
            layoutBindPhone.visibility = View.VISIBLE
            layoutBindEmail.visibility = View.VISIBLE
        } else {
            finish()
            return
        }

        if (email.isNotEmpty()) {
            layoutBindEmail.visibility = View.GONE
        }

        loadSavedAvatar()
    }

    private fun setupListeners() {
        ivAvatar.setOnClickListener {
            checkPermissionAndPickImage()
        }
        layoutBindWechat.setOnClickListener {
            getSharedPreferences("smokefree", 0).edit()
                .putString("wechat_name", getString(R.string.wechat_login_label))
                .apply()
            Toast.makeText(this, getString(R.string.toast_wechat_bound), Toast.LENGTH_SHORT).show()
            updateUI()
        }

        layoutBindPhone.setOnClickListener {
            getSharedPreferences("smokefree", 0).edit()
                .putString("phone", "13800001111")
                .apply()
            Toast.makeText(this, getString(R.string.toast_phone_bound), Toast.LENGTH_SHORT).show()
            updateUI()
        }

        layoutBindEmail.setOnClickListener {
            val supportEmail = getString(R.string.support_email)
            getSharedPreferences("smokefree", 0).edit()
                .putString("email", supportEmail)
                .apply()
            Toast.makeText(this, getString(R.string.toast_email_bound), Toast.LENGTH_SHORT).show()
            updateUI()
        }

        btnLogout.setOnClickListener {
            getSharedPreferences("smokefree", 0).edit()
                .putBoolean("is_logged_in", false)
                .putString("phone", "")
                .putString("wechat_name", "")
                .apply()
            Toast.makeText(this, getString(R.string.me_toast_logged_out), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun checkPermissionAndPickImage() {
        val permission = if (Build.VERSION_CODES.TIRAMISU <= Build.VERSION.SDK_INT)
            android.Manifest.permission.READ_MEDIA_IMAGES
        else
            android.Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_READ_IMAGES)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        @Suppress("DEPRECATION")
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_avatar)), PICK_IMAGE_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_READ_IMAGES && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImagePicker()
        } else {
            Toast.makeText(this, getString(R.string.toast_avatar_permission), Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated but needed for result")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            try {
                val inputStream = contentResolver.openInputStream(uri) ?: return
                val avatarDir = File(filesDir, "avatars")
                avatarDir.mkdirs()
                val avatarFile = File(avatarDir, "user_avatar.jpg")
                FileOutputStream(avatarFile).use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()

                getSharedPreferences("smokefree", 0).edit()
                    .putString("avatar_path", avatarFile.absolutePath)
                    .apply()

                ivAvatar.setImageBitmap(BitmapFactory.decodeFile(avatarFile.absolutePath))
                Toast.makeText(this, getString(R.string.toast_avatar_updated), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.toast_avatar_failed, e.message ?: ""), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSavedAvatar() {
        val path = getSharedPreferences("smokefree", 0).getString("avatar_path", null)
        if (!path.isNullOrEmpty()) {
            val file = File(path)
            if (file.exists()) {
                ivAvatar.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
            } else {
                getSharedPreferences("smokefree", 0).edit().remove("avatar_path").apply()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
