package com.prez.exception

class NotFoundException(private val id: String, private val elementName: String) : RuntimeException() {
    override val message: String
        get() = localizedMessage

    override fun getLocalizedMessage(): String {
        return "No result for the given $elementName id=$id"
    }
}