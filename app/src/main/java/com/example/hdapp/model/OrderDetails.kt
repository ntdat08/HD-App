package com.example.hdapp.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class OrderDetails : Serializable, Parcelable {

    var userId: String? = null
    var userName: String? = null
    var foodItemName: ArrayList<String>? = null
    var foodItemImage: ArrayList<String>? = null
    var foodItemPrice: ArrayList<String>? = null
    var foodItemQuantities: ArrayList<Int>? = null
    var address: String? = null
    var totalPrice: String? = null
    var phoneNumber: String? = null
    var orderAccepted: Boolean = false
    var paymentReceived: Boolean = false
    var paymentMethod: String? = null
    var itemPushKey: String? = null
    var currentTime: Long = 0

    constructor()

    constructor(parcel: Parcel) : this() {
        userId = parcel.readString()
        userName = parcel.readString()
        address = parcel.readString()
        totalPrice = parcel.readString()
        phoneNumber = parcel.readString()
        orderAccepted = parcel.readByte() != 0.toByte()
        paymentReceived = parcel.readByte() != 0.toByte()
        paymentMethod = parcel.readString()
        itemPushKey = parcel.readString()
        currentTime = parcel.readLong()
        foodItemName = parcel.createStringArrayList()
        foodItemImage = parcel.createStringArrayList()
        foodItemPrice = parcel.createStringArrayList()
        foodItemQuantities = parcel.createIntArray()?.toCollection(ArrayList())
    }

    constructor(
        userId: String,
        names: String,
        foodItemName: ArrayList<String>,
        foodItemImage: ArrayList<String>,
        foodItemPrice: ArrayList<String>,
        foodItemQuantities: ArrayList<Int>,
        address: String,
        numbers: String,
        time: Long,
        itemPushKey: String?,
        orderAccepted: Boolean,
        paymentReceived: Boolean,
        totalPrice: String?,
        paymentMethod: String?
    ) : this() {
        this.userId = userId
        this.userName = names
        this.foodItemName = foodItemName
        this.foodItemImage = foodItemImage
        this.foodItemPrice = foodItemPrice
        this.foodItemQuantities = foodItemQuantities
        this.address = address
        this.phoneNumber = numbers
        this.currentTime = time
        this.itemPushKey = itemPushKey
        this.orderAccepted = orderAccepted
        this.paymentReceived = paymentReceived
        this.totalPrice = totalPrice
        this.paymentMethod = paymentMethod
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(address)
        parcel.writeString(totalPrice)
        parcel.writeString(phoneNumber)
        parcel.writeByte(if (orderAccepted) 1 else 0)
        parcel.writeByte(if (paymentReceived) 1 else 0)
        parcel.writeString(paymentMethod)
        parcel.writeString(itemPushKey)
        parcel.writeLong(currentTime)
        parcel.writeStringList(foodItemName)
        parcel.writeStringList(foodItemImage)
        parcel.writeStringList(foodItemPrice)
        parcel.writeIntArray(foodItemQuantities?.toIntArray())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OrderDetails> {
        override fun createFromParcel(parcel: Parcel): OrderDetails {
            return OrderDetails(parcel)
        }

        override fun newArray(size: Int): Array<OrderDetails?> {
            return arrayOfNulls(size)
        }
    }
}