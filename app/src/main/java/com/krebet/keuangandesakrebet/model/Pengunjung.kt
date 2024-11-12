package com.krebet.keuangandesakrebet.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

@Suppress("SpellCheckingInspection" , "DEPRECATION")
data class Pengunjung (
    var id: String? = "" ,
    var nama: String? = "" ,
    val alamat: String? = "" ,
    val tanggal: Timestamp? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ,
        parcel.readString() ,
        parcel.readString() ,
        parcel.readParcelable(Timestamp::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel , flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nama)
        parcel.writeString(alamat)
        parcel.writeParcelable(tanggal , flags)
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