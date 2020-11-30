package com.prez.extension


fun String.last(size: Int): String = this.substring(this.length - size)

fun String.first(size: Int) = this.substring(0, size)

fun String.obfuscateEnd(offset: Int): String {
    val idx = if (offset > this.length / 2) this.length / 2 else offset
    return this.substring(0, idx) + "********************"
}

fun String.obfuscateBegin(size: Int): String {
    val idx = if (size > this.length / 2) this.length / 2 else size
    return "********************" + this.last(idx)
}

fun String.obfuscateEmail() = this.replaceBefore('@', this.substringBefore("@", "*******").first(3) + "*******", this)
