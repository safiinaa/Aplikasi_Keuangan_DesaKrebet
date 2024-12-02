package com.krebet.keuangandesakrebet.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

@Suppress("SpellCheckingInspection" , "DEPRECATION")
data class Pengunjung (
    var id: String? = "",
    var namaCp: String? = "",
    var namaInstansi: String? = "",
    val noTelp: String? = "",
    val alamat: String? = "",
    val tglTransaksi: Timestamp? = null,
    val tglKunjungan: Timestamp? = null,
    val dp: Float? = 0F
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ,
        parcel.readString() ,
        parcel.readString() ,
        parcel.readString() ,
        parcel.readString() ,
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readValue(Float::class.java.classLoader) as? Float
    )

    override fun writeToParcel(parcel: Parcel , flags: Int) {
        parcel.writeString(id)
        parcel.writeString(namaCp)
        parcel.writeString(namaInstansi)
        parcel.writeString(noTelp)
        parcel.writeString(alamat)
        parcel.writeParcelable(tglTransaksi , flags)
        parcel.writeParcelable(tglKunjungan , flags)
        parcel.writeValue(dp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Pengunjung> {
        override fun createFromParcel(parcel: Parcel): Pengunjung {
            return Pengunjung(parcel)
        }

        override fun newArray(size: Int): Array<Pengunjung?> {
            return arrayOfNulls(size)
        }
    }
}