package com.example.testarmflow.CompClassesHome

class CalendarDays {
    private var date: String = ""
    private var title: String = ""
    private var description: String = ""
    private var uid: String = ""
    private var byUser: String = ""

    constructor()
    constructor(date: String, title: String, description: String, uid: String, byUser: String) {
        this.date = date
        this.title = title
        this.description = description
        this.uid = uid
        this.byUser = byUser
    }

    fun getDate(): String? {
        return date
    }

    fun getTitle(): String {
        return title
    }

    fun setDate(date: String){
        this.date = date
    }

    fun setTitle(title: String){
        this.title = title
    }

    fun getUID(): String {
        return uid
    }

    fun setUID(uid: String){
        this.uid = uid
    }

    fun getByUser(): String {
        return byUser
    }

    fun setByUser(byUser: String){
        this.byUser = byUser
    }

    fun getDescription(): String{
        return description
    }

    fun setDescription(description: String){
        this.description = description
    }

}