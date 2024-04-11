package com.example.testarmflow.CompClassesMap

class Events {
    private var uid: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var address: String = ""
    private var addressTwo: String = ""
    private var headLine: String = ""
    private var description: String = ""
    private var eventID: String = ""

    constructor()
    constructor(
        uid: String,
        latitude: Double,
        longitude: Double,
        address: String,
        addressTwo: String,
        headLine: String,
        description: String,
        eventID: String
    ) {
        this.uid = uid
        this.latitude = latitude
        this.longitude = longitude
        this.address = address
        this.addressTwo = addressTwo
        this.headLine = headLine
        this.description = description
        this.eventID = eventID
    }

    fun getUID(): String? {
        return uid
    }

    fun getLatitude(): Double {
        return latitude
    }

    fun getLongitude(): Double {
        return longitude
    }

    fun getAddress(): String? {
        return address
    }

    fun getAddressTwo(): String? {
        return addressTwo
    }

    fun getHeadLine(): String? {
        return headLine
    }

    fun getDescription(): String? {
        return description
    }

    fun getEventID(): String? {
        return eventID
    }

    fun setUID(uid: String?){
        this.uid = uid!!
    }

    fun setLatitude(latitude: Double){
        this.latitude = latitude!!
    }

    fun setLongitude(longitude: Double){
        this.longitude = longitude!!
    }

    fun setAddress(address: String?){
        this.address = address!!
    }

    fun setAddressTwo(addressTwo: String?){
        this.addressTwo = addressTwo!!
    }

    fun setHeadLine(headLine: String?){
        this.headLine = headLine!!
    }

    fun setDescription(description: String?){
        this.description = description!!
    }

    fun setEventID(eventID: String?){
        this.eventID = eventID!!
    }

}