package com.example.myapplication.model

import android.os.Parcel
import android.os.Parcelable

data class ChatRoom(
    val username: String? = "",
    val message: String? = "",
    val conversationId: Int? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt() ?: 0
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeString(message)
        parcel.writeValue(conversationId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ChatRoom> {
        override fun createFromParcel(parcel: Parcel): ChatRoom {
            return ChatRoom(parcel)
        }

        override fun newArray(size: Int): Array<ChatRoom?> {
            return arrayOfNulls(size)
        }
    }
}
