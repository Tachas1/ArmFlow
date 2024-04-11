package com.example.testarmflow.CompClassesExerc

class Exercises {
    private var description: String = ""
    private var media1: String = ""
    private var media2: String = ""
    private var name: String = ""
    private var tips: String = ""
    private var source: String = ""
    private var id: String = ""

    constructor()
    constructor(description: String, media1: String, media2: String, name: String, tips: String, source: String, id: String) {
        this.description = description
        this.media1 = media1
        this.media2 = media2
        this.name = name
        this.tips = tips
        this.source = source
        this.id = source
    }
    fun getDescription(): String? {
        return description
    }

    fun getMedia1(): String? {
        return media1
    }

    fun getMedia2(): String? {
        return media2
    }

    fun getName(): String? {
        return name
    }

    fun getTips(): String? {
        return tips
    }

    fun getSource(): String? {
        return source
    }

    fun getID(): String? {
        return id
    }
}