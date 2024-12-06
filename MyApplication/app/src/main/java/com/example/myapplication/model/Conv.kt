package com.example.myapplication.model

import android.os.Parcel
import android.os.Parcelable

data class Conv(
    val id: Int? = null,
    val admin: Int? = null,
    val chatMessages: List<ChatMessage>? = emptyList(),
    val participants: List<Participant>? = emptyList(),
    val lastMessageTime: String? = null,
    val creationTime: String? = null,
    val privateChat: Boolean? = true,
    val status: String? = "ACTIVE",

    val content: String? = "",
    val isFromOpponent: Boolean,
    val sender: Int? = null,
    val senderName: String? = null,
    val conversationId: Int? = null,
)

data class ChatMessage(
    val content: String? = "",
    val contentType: String? = "TEXT",
    val creationTime: Long? = null,
    val conversationId: Int? = null,
    val sender: Int? = null,
    val id: Int? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(content)
        parcel.writeString(contentType)
        parcel.writeLong(creationTime ?: 0)
        parcel.writeInt(conversationId ?: 0)
        parcel.writeInt(sender ?: 0)
        parcel.writeInt(id ?: 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ChatMessage> {
        override fun createFromParcel(parcel: Parcel): ChatMessage {
            return ChatMessage(parcel)
        }

        override fun newArray(size: Int): Array<ChatMessage?> {
            return arrayOfNulls(size)
        }
    }
}

data class Participant(
    val conversationDisplayName: String? = "",
    val userId: Int? = null,
    val conversationId: Int? = null,
    val participantName: String? = null,
    val lastView: Long? = null,
    val profilePhoto: String? = null,
)