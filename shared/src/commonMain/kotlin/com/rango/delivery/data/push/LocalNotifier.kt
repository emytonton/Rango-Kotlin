package com.rango.delivery.data.push

interface LocalNotifier {
    fun show(id: Long, title: String, body: String)
}
