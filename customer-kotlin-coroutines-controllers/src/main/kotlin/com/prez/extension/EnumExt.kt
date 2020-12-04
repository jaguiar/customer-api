package com.prez.extension

inline fun <reified T : Enum<T>> enumContains(name: String?): Boolean {
    return name?.let { enumValues<T>().any { it.name == name } } ?: false
}