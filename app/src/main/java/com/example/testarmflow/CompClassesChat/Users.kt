package com.example.testarmflow.CompClassesChat

class Users {
    private var uid: String = ""
    private var nickname: String = ""
    private var nicknameLC: String = ""
    private var avatarSrc: String = ""
    private var status: String = ""
    private var nofityTime: String = ""

    constructor()
    constructor(uid: String, nickname: String, nicknameLC: String, avatarSrc: String, status: String, notifyTime: String) {
        this.uid = uid
        this.nickname = nickname
        this.nicknameLC = nicknameLC
        this.avatarSrc = avatarSrc
        this.status = status
        this.nofityTime = notifyTime
    }
    fun getUID(): String? {
        return uid
    }

    fun getNickName(): String? {
        return nickname
    }

    fun getNickNameLC(): String? {
        return nicknameLC
    }

    fun getAvatarSrc(): String? {
        return avatarSrc
    }

    fun getStatus(): String? {
        return status
    }

    fun getNotifyTime(): String? {
        return nofityTime
    }

    fun setNotifyTime(notifyTime: String) {
        this.nofityTime = notifyTime
    }
}