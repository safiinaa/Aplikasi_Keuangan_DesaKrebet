package com.krebet.keuangandesakrebet.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

@Suppress("SpellCheckingInspection" , "DEPRECATION")
data class Transaksi (
    var idTransaksi: String? = "" ,
    val idPengunjung: String? = "" ,
    val tanggal: Timestamp? = null ,
    val nominal: Float? = 0F ,
    val qty: Float? = 0F ,
    val total: Float? = 0F ,
    val catatan: String? = "" ,
    val jenis: String? = "" ,
    var pengunjung: Pengunjung? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ,
        parcel.readString() ,
        parcel.readParcelable(Timestamp::class.java.classLoader) ,
        parcel.readValue(Float::class.java.classLoader) as? Float ,
        parcel.readValue(Float::class.java.classLoader) as? Float ,
        parcel.readValue(Float::class.java.classLoader) as? Float ,
        parcel.readString() ,
        parcel.readString() ,
        parcel.readParcelable(Pengunjung::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel , flags: Int) {
        parcel.writeString(idTransaksi)
        parcel.writeString(idPengunjung)
        parcel.writeParcelable(tanggal , flags)
        parcel.writeValue(nominal)
        parcel.writeValue(qty)
        parcel.writeValue(total)
        parcel.writeString(catatan)
        parcel.writeString(jenis)
        parcel.writeParcelable(pengunjung , flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Transaksi> {
        override fun createFromParcel(parcel: Parcel): Transaksi {
            return Transaksi(parcel)
        }

        override fun newArray(size: Int): Array<Transaksi?> {
            return arrayOfNulls(size)
        }
    }
}