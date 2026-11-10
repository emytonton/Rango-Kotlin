package com.rango.delivery.data.connection

import kotlinx.coroutines.flow.StateFlow

interface ConnectivityMonitor {
    val online: StateFlow<Boolean>
}
