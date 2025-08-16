package com.krebet.keuangandesakrebet.ui.laporan

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.FragmentRiwayatTransaksiBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import com.krebet.keuangandesakrebet.model.Transaksi
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("SetTextI18n")
class RiwayatTransaksiFragment : Fragment() {

    private var _binding : FragmentRiwayatTransaksiBinding? = null
    private val binding get() = _binding!!

    private var idPengunjung: String? = null
    private var selectedVisitor: String? = null
    private var alamat: String? = null
    private var tglAwal: Date? = null
    private var tglAkhir: Date? = null
    private var totalPengeluaran: Float? = 0F
    private var totalPemasukan: Float? = 0F
    private lateinit var visitors: List<Pengunjung>
    private var transaksiPengeluaran = mutableListOf<Transaksi>()
    private var filterPengeluaranByVisitor = mutableListOf<Transaksi>()

    private lateinit var listener: ListenerRegistration
    private val db = FirebaseFirestore.getInstance()

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater , container: ViewGroup? ,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiwayatTransaksiBinding.inflate(inflater, container, false)

        onBackPressed()

        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showToast("Izin diberikan")
                createAndSaveReport()
            } else {
                showToast("Izin ditolak, mohon berikan izin")
            }
        }

        binding.apply {
            etNamaInstansi.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence? , start: Int , count: Int , after: Int) {}

                override fun onTextChanged(s: CharSequence? , start: Int , before: Int , count: Int) {
                    val query = s.toString().trim()
                    if (query.isNotEmpty()) {
                        alamat = null
                        searchVisitor(query)
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            etNamaInstansi.setOnItemClickListener { parent , _ , position , _ ->
                selectedVisitor = parent.getItemAtPosition(position).toString()
                val visitorData = visitors.find { it.namaInstansi == selectedVisitor }
                visitorData.let {
                    idPengunjung = it?.id
                    alamat = it?.alamat
                }
                etNamaInstansi.dismissDropDown()
                etNamaInstansi.clearFocus()
            }

            btnTanggal.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTheme(R.style.ThemeMaterialCalendar)
                    .setTitleText("Pilih Rentang Tanggal")
                    .setSelection(androidx.core.util.Pair(null , null))
                    .build()
                datePicker.show(parentFragmentManager , "DatePicker")
                datePicker.isCancelable = false
                datePicker.addOnPositiveButtonClickListener {
                    tglAwal = Date(it.first)
                    tglAkhir = Date(it.second)

                    val sdf = SimpleDateFormat("dd-MM-yyyy" , Locale("id", "ID"))
                    btnTanggal.text = "${sdf.format(tglAwal!!)} - ${sdf.format(tglAkhir!!)}"
                }
            }

            btnSimpan.setOnClickListener {
                if (selectedVisitor == null) {
                    showToast("Nama instansi tidak boleh kosong")
                } else if (alamat == null) {
                    showToast("Nama instansi tidak ditemukan")
                } else if (tglAwal == null) {
                    showToast("Tanggal tidak boleh kosong")
                } else {
                    showLoading()

                    db.collection("pengeluaran")
                        .whereGreaterThanOrEqualTo("tglTransaksi", tglAwal!!)
                        .whereLessThanOrEqualTo("tglTransaksi", tglAkhir!!)
                        .get()
                        .addOnSuccessListener { documents ->
                            transaksiPengeluaran.clear()
                            filterPengeluaranByVisitor.clear()
                            totalPengeluaran = 0F

                            for (document in documents) {
                                val dataPengeluaran = document.toObject(Transaksi::class.java)
                                transaksiPengeluaran.add(dataPengeluaran)
                            }

                            db.collection("pemasukan")
                                .whereGreaterThanOrEqualTo("tglTransaksi", tglAwal!!)
                                .whereLessThanOrEqualTo("tglTransaksi", tglAkhir!!)
                                .get()
                                .addOnSuccessListener { snapshot ->
                                    val pemasukan = mutableListOf<Transaksi>()
                                    pemasukan.clear()
                                    totalPemasukan = 0F

                                    for (data in snapshot) {
                                        val dataPemasukan = data.toObject(Transaksi::class.java)
                                        pemasukan.add(dataPemasukan)
                                    }

                                    val filterPengeluaran = transaksiPengeluaran.filter { it.idPengunjung == idPengunjung }
                                    filterPengeluaran.forEach {
                                        totalPengeluaran = totalPengeluaran!! + (it.total ?: 0F)
                                        filterPengeluaranByVisitor.add(it)
                                    }

                                    val filterPemasukan = pemasukan.filter { it.idPengunjung == idPengunjung }
                                    filterPemasukan.forEach {
                                        totalPemasukan = totalPemasukan!! + (it.total ?: 0F)
                                    }


                                    if (filterPengeluaran.isEmpty()) {
                                        showToast("Transaksi pengeluaran instansi tersebut pada tanggal dipilih tidak ditemukan")
                                        dismissLoading()
                                    } else {
                                        checkAndRequestPermission()
                                    }

                                }.addOnFailureListener {
                                    showToast("Terjadi kesalahan")
                                    dismissLoading()
                                }
                        }.addOnFailureListener {
                            showToast("Terjadi kesalahan")
                            dismissLoading()
                        }
                }
            }

            btnKembali.setOnClickListener {
                setBackState()
            }
        }
    }

    private fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createAndSaveReport()
        } else if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            createAndSaveReport()
        } else {
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun createAndSaveReport() {
        val width = 1920
        val rowHeight = 80 // Tinggi setiap baris
        val marginTop = 400 // Margin atas untuk judul, detail toko, dan informasi tambahan
        val marginBottom = 730 // Margin bawah untuk tanda tangan
        val height = marginTop + (filterPengeluaranByVisitor.size * rowHeight) + marginBottom // Total tinggi canvas

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // Background putih
        canvas.drawColor(Color.WHITE)

        // Judul di tengah atas
        paint.color = Color.BLACK
        paint.textSize = 60f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("LAPORAN KEUANGAN", width / 2f, 100f, paint)

        // Load logo dari drawable
        val companyLogoDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_logo_large)
        val logoBitmap = (companyLogoDrawable as BitmapDrawable).bitmap

        // Tempatkan logo di atas kiri
        val logoHeight = 230
        val logoWidth = (logoBitmap.width.toFloat() * (logoHeight / logoBitmap.height.toFloat())).toInt() // Maintain aspect ratio
        val logoRect = Rect(20, 0, 0 + logoWidth, 0 + logoHeight)
        canvas.drawBitmap(logoBitmap, null, logoRect, paint)

        // Nama di kiri bawah judul
        paint.textSize = 40f
        paint.textAlign = Paint.Align.LEFT
        paint.isFakeBoldText = true
        canvas.drawText("DESA WISATA KREBET", 50f, 220f, paint)

        // Alamat dan email di bawah nama
        paint.isFakeBoldText = false
        canvas.drawText("Krebet, Sendangsari, Pajangan, Bantul", 50f, 280f, paint)
        canvas.drawText("Yogyakarta", 50f, 330f, paint)
        canvas.drawText("Email : pdwkrebet@gmail.com", 50f, 380f, paint)

        // Tempat dan tanggal di kanan bawah judul
        paint.textAlign = Paint.Align.RIGHT
        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        canvas.drawText("Yogyakarta, $currentDate", width - 50f, 220f, paint)

        // Menghitung lebar teks kota dan tanggal
        val textWidth = paint.measureText("Yogyakarta, $currentDate")

        // Posisi "Kepada Yth," dan "visitor" dimulai dari bawah teks Yogyakarta
        val startX = width - 50f - textWidth

        // Gunakan TextPaint untuk StaticLayout
        val textPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 40f
            isAntiAlias = true
            typeface = paint.typeface
        }
        // Gabungkan teks multi-baris
        val fullText = "Kepada Yth,\n$selectedVisitor"

// Hitung lebar maksimum teks (dari startX sampai batas kanan canvas)
        val maxTextWidth = (width - startX - 50).toInt()

// Gunakan StaticLayout untuk teks multi-baris
        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder
                .obtain(fullText, 0, fullText.length, textPaint, maxTextWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(fullText, textPaint, maxTextWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false)
        }

// Gambar teks di posisi tertentu
        val yKepadaYth = 250f
        canvas.save()
        canvas.translate(startX, yKepadaYth)
        staticLayout.draw(canvas)
        canvas.restore()
        // Kepada Yth
       /* paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Kepada Yth,", startX, 280f, paint)
        canvas.drawText("$selectedVisitor", startX, 330f, paint)*/


        // Header Table
        paint.textSize = 40f
        paint.textAlign = Paint.Align.LEFT
        paint.isFakeBoldText = true
        var yPosition = 500f
        canvas.drawText("No", 50f, yPosition, paint)
        canvas.drawText("Keterangan", 170f, yPosition, paint)

        // Ubah alignment untuk kolom-kolom ini menjadi kanan
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Banyaknya", width - 750f, yPosition, paint)
        canvas.drawText("Harga Satuan", width - 400f, yPosition, paint)
        canvas.drawText("Sub Total", width - 50f, yPosition, paint)
        paint.isFakeBoldText = false

        // Garis header
        paint.strokeWidth = 3f
        canvas.drawLine(50f, yPosition + 20f, width.toFloat() - 50f, yPosition + 20f, paint)

        // Data Rows
        yPosition += 70f
        for ((index, data) in filterPengeluaranByVisitor.withIndex()) {
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText((index + 1).toString(), 50f, yPosition, paint)
            canvas.drawText(data.catatan!!, 170f, yPosition, paint)

            // Ubah alignment menjadi kanan untuk data pemasukan, pengeluaran, dan saldo
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(data.qty!!.toInt().toString(), width - 750f, yPosition, paint)
            canvas.drawText(formatRupiah(data.nominal!!), width - 400f, yPosition, paint)
            canvas.drawText(formatRupiah(data.total!!), width - 50f, yPosition, paint)

            // Garis dibawah setiap baris
            canvas.drawLine(50f, yPosition + 20f, width.toFloat() - 50f, yPosition + 20f, paint)
            yPosition += rowHeight
        }

        // Total Table
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER

        canvas.drawText("Total Pengeluaran", width / 2f, yPosition, paint)
        canvas.drawLine(50f, yPosition + 20f, width.toFloat() - 50f, yPosition + 20f, paint)    //Garis bawah

        canvas.drawText("Pemasukan", width / 2f, yPosition + 70f, paint)
        canvas.drawLine(50f, yPosition + 90f, width.toFloat() - 50f, yPosition + 90f, paint)    //Garis bawah

        canvas.drawText("Sisa", width / 2f, yPosition + 140f, paint)
        canvas.drawLine(50f, yPosition + 160f, width.toFloat() - 50f, yPosition + 160f, paint)    //Garis bawah

        val sisa = totalPemasukan!! - totalPengeluaran!!
        // Ubah alignment untuk kolom-kolom ini menjadi kanan
        paint.textAlign = Paint.Align.RIGHT
        paint.isFakeBoldText = true
        canvas.drawText(formatRupiah(totalPengeluaran!!), width - 50f, yPosition, paint)
        canvas.drawText(formatRupiah(totalPemasukan!!), width - 50f, yPosition + 70f, paint)
        canvas.drawText(formatRupiah(sisa), width - 50f, yPosition + 140f, paint)
        paint.isFakeBoldText = false

        // Jarak untuk "Hormat Kami"
        yPosition += 250f
        paint.textAlign = Paint.Align.RIGHT // Align "Hormat Kami" ke kanan
        canvas.drawText("Hormat Kami,", width - 100f, yPosition, paint)

        // Memuat gambar tanda tangan dari drawable
        val signatureBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_tanda_tangan)

        // Menyesuaikan ukuran gambar tanda tangan
        yPosition -= 30f
        val signatureWidth = 300
        val signatureHeight = (signatureBitmap.height * (signatureWidth / signatureBitmap.width.toFloat())).toInt() // Menjaga aspek rasio
        val signatureRect = Rect(width - 50 - signatureWidth, yPosition.toInt(), width - 50, yPosition.toInt() + signatureHeight)

        // Gambar tanda tangan
        canvas.drawBitmap(signatureBitmap, null, signatureRect, paint)

        // Nama Pengurus
        paint.isFakeBoldText = true
        yPosition += signatureHeight - 20f // Menambahkan jarak setelah tanda tangan
        canvas.drawText("(Agus Jati Kumara)", canvas.width - 50f, yPosition, paint) // Nama pemilik toko juga sejajar kanan

        val sdf = SimpleDateFormat("dd-MM-yyyy" , Locale("id", "ID"))
        val fileName = "Laporan Keuangan $selectedVisitor ${sdf.format(tglAwal!!)} - ${sdf.format(tglAkhir!!)}.jpg"
        // Simpan bitmap ke folder yang dapat diakses oleh galeri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LaporanKeuangan")
            }

            val resolver = requireContext().contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                try {
                    val outputStream = resolver.openOutputStream(uri)
                    outputStream?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        showToast("Gambar berhasil disimpan")
                        dismissLoading()
                    }
                } catch (e: Exception) {
                    showToast("Gagal menyimpan gambar")
                    dismissLoading()
                }
            } else {
                showToast("Gagal menyimpan gambar")
                dismissLoading()
            }
        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/LaporanKeuangan") // Direktori Pictures
            val file = File(picturesDir, fileName)

            try {
                // Pastikan direktori tujuan ada
                if (!picturesDir.exists()) {
                    picturesDir.mkdirs()
                }

                // Simpan bitmap ke file
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    showToast("Gambar disimpan di: ${file.absolutePath}")

                    // Notify media scanner untuk memperbarui galeri
                    MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
                    binding.loading.isVisible = false
                }
            } catch (e: Exception) {
                Log.e("simpan gambar", e.message.toString())
                showToast("Gagal menyimpan gambar")
                dismissLoading()
            }
        }
    }

    private fun searchVisitor(query: String) {
        val docRef = db.collection("pengunjung")
            .orderBy("namaInstansi")
            .startAt(query)
            .endAt(query + "\uf8ff")
        listener = docRef.addSnapshotListener { value, error ->
            if (error != null) {
                showToast("Terjadi kesalahan, silahkan ulangi kembali")
                return@addSnapshotListener
            }

            if (value != null) {
                visitors = value.mapNotNull {
                    val visitor = it.toObject(Pengunjung::class.java)
                    visitor.copy(id = it.id)
                }.distinctBy { it.namaInstansi }

                if (isAdded) {
                    val adapter = ArrayAdapter<String>(requireContext() , R.layout.list_item , visitors.map { it.namaInstansi })
                    binding.etNamaInstansi.setAdapter(adapter)
                    binding.etNamaInstansi.showDropDown()
                }
            } else {
                showToast("Nama tidak ditemukan")
            }
        }
    }

    private fun formatRupiah(total: Float): String {
        val formatRp = DecimalFormat("Rp ###,###,###").format(total)
        return formatRp
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setBackState()
            }
        })
    }

    private fun setBackState() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, LaporanFragment())
            .commit()
    }

    private fun showLoading() {
        if (isAdded) {
            binding.loading.isVisible = true
        }
    }

    private fun dismissLoading() {
        if (isAdded) {
            binding.loading.isVisible = false
        }
    }

    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        if (::listener.isInitialized) {
            listener.remove()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::listener.isInitialized) {
            listener.remove()
        }
        _binding = null
    }
}