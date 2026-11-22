package com.harshdeep.intelliops

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform