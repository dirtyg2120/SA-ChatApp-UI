package com.example.myapplication.model

import android.os.Parcel
import android.os.Parcelable

data class ChatRoom(
    val username: String? = "",
    val lastMessage: String? = "",
    val conversationId: Int? = null,
    val messages: List<ChatMessage> = emptyList(),
    val userId: Int? = null,
    val lastMessageTime: String? = "",
    val avatarUrl: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt() ?: 0,
        parcel.createTypedArrayList(ChatMessage.CREATOR) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeString(lastMessage)
        parcel.writeValue(conversationId)
        parcel.writeTypedList(messages)
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
