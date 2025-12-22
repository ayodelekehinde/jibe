package com.cherrio.jibe.network

data class Device(
    val name: String,
    val ip: String,
    val port: Int,
    val type: Type = Type.UNKNOWN
){
    val isEmpty = type == Type.UNKNOWN
    enum class Type {
        MOBILE, DESKTOP, UNKNOWN;

        companion object {
            fun toType(name: String): Type {
                return when (name) {
                    MOBILE.name -> MOBILE
                    DESKTOP.name -> DESKTOP
                    else -> UNKNOWN
                }
            }
        }
    }

    override fun toString(): String {
        return "$name,$ip,$port,${type.name}"
    }

    companion object {
        val EMPTY = Device("","", 0, Type.UNKNOWN)
    }
}