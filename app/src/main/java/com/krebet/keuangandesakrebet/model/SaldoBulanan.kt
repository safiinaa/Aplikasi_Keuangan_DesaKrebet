package com.krebet.keuangandesakrebet.model

import android.os.Parcel
import android.os.Parcelable

data class SaldoBulanan(
    val bulan: String? = "" ,
    val saldo: Double? = 0.0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ,
        parcel.readValue(Double::class.java.classLoader) as? Double
    )

    override fun writeToParcel(parcel: Parcel , flags: Int) {
        parcel.writeString(bulan)
        parcel.writeValue(saldo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SaldoBulanan> {
        override fun createFromParcel(parcel: Parcel): SaldoBulanan {
            return SaldoBulanan(parcel)
        }

        override fun newArray(size: Int): Array<SaldoBulanan?> {
            return arrayOfNulls(size)
        }
    }
}
