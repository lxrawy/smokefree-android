package com.example.smokefree.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
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
import com.example.smokefree.R
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
        supportActionBar?.title = "账户详情"

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
            // 手机号登录 → 显示绑定微信和绑定邮箱
            tvUserName.text = phone.substring(0, 3) + "****" + phone.substring(7)
            tvUserDesc.text = "手机号登录"
            layoutBindPhone.visibility = View.GONE   // 已绑定手机号，隐藏
            layoutBindWechat.visibility = View.VISIBLE
            layoutBindEmail.visibility = View.VISIBLE
        } else if (wechatName.isNotEmpty()) {
            // 微信登录 → 显示绑定手机号和绑定邮箱
            tvUserName.text = wechatName
            tvUserDesc.text = "微信登录"
            layoutBindWechat.visibility = View.GONE    // 已绑定微信，隐藏
            layoutBindPhone.visibility = View.VISIBLE
            layoutBindEmail.visibility = View.VISIBLE
        } else {
            finish()
            return
        }

        // 邮箱已绑定则隐藏绑定邮箱选项
        if (email.isNotEmpty()) {
            layoutBindEmail.visibility = View.GONE
        }

        // 加载已保存的头像
        loadSavedAvatar()
    }

    private fun setupListeners() {
        // 点击头像 → 选择图片
        ivAvatar.setOnClickListener {
            checkPermissionAndPickImage()
        }
        // 绑定微信
        layoutBindWechat.setOnClickListener {
            val prefs = getSharedPreferences("smokefree", 0)
            prefs.edit()
                .putString("wechat_name", "微信用户")
                .apply()
            Toast.makeText(this, "✅ 微信绑定成功", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        // 绑定手机号（模拟：直接标记已绑定）
        layoutBindPhone.setOnClickListener {
            val prefs = getSharedPreferences("smokefree", 0)
            prefs.edit()
                .putString("phone", "13800001111")
                .apply()
            Toast.makeText(this, "✅ 手机号绑定成功", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        // 绑定邮箱（模拟）
        layoutBindEmail.setOnClickListener {
            val prefs = getSharedPreferences("smokefree", 0)
            prefs.edit()
                .putString("email", "user@example.com")
                .apply()
            Toast.makeText(this, "✅ 邮箱绑定成功", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        // 退出登录
        btnLogout.setOnClickListener {
            getSharedPreferences("smokefree", 0).edit()
                .putBoolean("is_logged_in", false)
                .putString("phone", "")
                .putString("wechat_name", "")
                .apply()
            Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // ==================== 头像相关 ====================

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
        startActivityForResult(Intent.createChooser(intent, "选择头像"), PICK_IMAGE_REQUEST)
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
            Toast.makeText(this, "需要相册权限才能更换头像", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated but needed for result")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            // 保存到应用私有目录（避免外部文件被删除后丢失）
            try {
                val inputStream = contentResolver.openInputStream(uri) ?: return
                val avatarDir = File(filesDir, "avatars")
                avatarDir.mkdirs()
                val avatarFile = File(avatarDir, "user_avatar.jpg")
                FileOutputStream(avatarFile).use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()

                // 保存路径到 SharedPreferences
                getSharedPreferences("smokefree", 0).edit()
                    .putString("avatar_path", avatarFile.absolutePath)
                    .apply()

                // 显示新头像
                ivAvatar.setImageBitmap(BitmapFactory.decodeFile(avatarFile.absolutePath))
                Toast.makeText(this, "头像更新成功", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "头像设置失败: ${e.message}", Toast.LENGTH_SHORT).show()
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
                // 文件不存在，清除无效路径
                getSharedPreferences("smokefree", 0).edit().remove("avatar_path").apply()
            }
        } else {
            // 无自定义头像，显示默认 emoji 占位（用 TextView 方式模拟）
            // ImageView 保持 placeholder 背景即可
        }
    }

    // ==================== 头像结束 ====================

    override fun onResume() {
        super.onResume()
        // 从绑定页面返回时刷新UI（模拟已绑定后隐藏对应项）
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
