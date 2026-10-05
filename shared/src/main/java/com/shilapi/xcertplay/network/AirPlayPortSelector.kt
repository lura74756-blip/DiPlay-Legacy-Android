package com.shilapi.xcertplay.network

import java.net.BindException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket

object AirPlayPortSelector {
    val FALLBACK_PORTS: IntRange = 7001..7010

    fun bind(
        address: InetAddress,
        preferredPort: Int,
        fallbackPorts: Iterable<Int> = FALLBACK_PORTS,
        onFallback: (busyPort: Int, boundPort: Int) -> Unit = { _, _ -> },
    ): ServerSocket {
        tryBind(address, preferredPort)?.let { return it }
        for (port in fallbackPorts) {
            if (port == preferredPort) continue
            tryBind(address, port)?.let { server ->
                onFallback(preferredPort, server.localPort)
                return server
            }
        }
        val server = bindPort(address, 0)
        onFallback(preferredPort, server.localPort)
        return server
    }

    private fun tryBind(address: InetAddress, port: Int): ServerSocket? = try {
        bindPort(address, port)
    } catch (_: BindException) {
        null
    }

    private fun bindPort(address: InetAddress, port: Int): ServerSocket {
        val server = ServerSocket()
        try {
            server.bind(InetSocketAddress(address, port))
            return server
        } catch (error: Throwable) {
            try { server.close() } catch (_: Throwable) {}
            throw error
        }
    }
}
