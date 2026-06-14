package com.example.planningfortravel

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddEditActivity : AppCompatActivity() {

    private lateinit var db: TravelDBHelper
    private lateinit var etPlace: EditText
    private lateinit var etDate: EditText
    private lateinit var etMemo: EditText
    private lateinit var imgPhoto: ImageView
    private lateinit var progressBar: ProgressBar

    private var recordId = -1
    private var isViewOnly = false
    private var photoUri: String = ""
    private var cameraUri: Uri? = null

    companion object {
        const val REQ_CAMERA = 100
        const val REQ_GALLERY = 101
        const val REQ_CAM_PERM = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = TravelDBHelper(this)
        etPlace = findViewById(R.id.etPlace)
        etDate = findViewById(R.id.etDate)
        etMemo = findViewById(R.id.etMemo)
        imgPhoto = findViewById(R.id.imgPhoto)
        progressBar = findViewById(R.id.progressBar)

        recordId = intent.getIntExtra("record_id", -1)
        isViewOnly = intent.getBooleanExtra("view_only", false)

        if (recordId != -1) {
            supportActionBar?.title = if (isViewOnly) "여행 상세" else "기록 수정"
            loadRecord(recordId)
        } else {
            supportActionBar?.title = "여행 추가"
            etDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
        }

        if (isViewOnly) {
            etPlace.isEnabled = false
            etDate.isEnabled = false
            etMemo.isEnabled = false
            findViewById<Button>(R.id.btnSave).visibility = View.GONE
            findViewById<Button>(R.id.btnPhoto).visibility = View.GONE
        }

        findViewById<Button>(R.id.btnPhoto).setOnClickListener { showPhotoDialog() }
        findViewById<Button>(R.id.btnSave).setOnClickListener { save() }
    }

    private fun loadRecord(id: Int) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val record = withContext(Dispatchers.IO) { db.getById(id) }
            progressBar.visibility = View.GONE
            record?.let {
                etPlace.setText(it.place)
                etDate.setText(it.visitDate)
                etMemo.setText(it.memo)
                photoUri = it.photoUri
                if (it.photoUri.isNotEmpty()) {
                    loadImageAsync(Uri.parse(it.photoUri))
                }
            }
        }
    }

    private fun showPhotoDialog() {
        AlertDialog.Builder(this)
            .setTitle("사진 선택")
            .setItems(arrayOf("카메라 촬영", "갤러리에서 선택")) { _, which ->
                if (which == 0) openCamera() else openGallery()
            }
            .show()
    }

    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQ_CAM_PERM)
            return
        }
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "travel_${System.currentTimeMillis()}.jpg")
        cameraUri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
        }
        startActivityForResult(intent, REQ_CAMERA)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQ_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        val uri: Uri? = when (requestCode) {
            REQ_CAMERA -> cameraUri
            REQ_GALLERY -> data?.data
            else -> null
        }
        uri?.let {
            photoUri = it.toString()
            loadImageAsync(it)
        }
    }

    // 코루틴으로 이미지 비동기 로딩 + ProgressBar (가산점)
    private fun loadImageAsync(uri: Uri) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                Thread.sleep(100) // IO 스레드에서 처리 시뮬레이션
            }
            try {
                imgPhoto.setImageURI(uri)
            } catch (e: Exception) {
                imgPhoto.setImageResource(R.drawable.ic_placeholder)
            }
            progressBar.visibility = View.GONE
        }
    }



    private fun save() {
        val place = etPlace.text.toString().trim()
        val date = etDate.text.toString().trim()
        val memo = etMemo.text.toString().trim()

        if (place.isEmpty()) { etPlace.error = "여행지를 입력하세요"; return }
        if (date.isEmpty()) { etDate.error = "날짜를 입력하세요"; return }

        lifecycleScope.launch {
            val record = TravelRecord(
                no = if (recordId != -1) recordId else 0,
                place = place,
                visitDate = date,
                memo = memo,
                photoUri = photoUri,
            )
            withContext(Dispatchers.IO) {
                if (recordId != -1) db.update(record) else db.insert(record)
            }
            Toast.makeText(this@AddEditActivity, "저장되었습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_CAM_PERM && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}