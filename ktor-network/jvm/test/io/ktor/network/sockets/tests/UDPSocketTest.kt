/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets.tests

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.debug.junit4.*
import org.junit.*
import java.lang.IllegalStateException
import java.net.*
import kotlin.coroutines.*
import kotlin.io.use
import kotlin.test.*
import kotlin.test.Test

class UDPSocketTest : CoroutineScope {
    private val testJob = Job()
    private val selector = ActorSelectorManager(Dispatchers.Default + testJob)

    @get:Rule
    val timeout = CoroutinesTimeout(1000, cancelOnTimeout = true)

    override val coroutineContext: CoroutineContext
        get() = testJob

    @AfterTest
    fun tearDown() {
        testJob.cancel()
        selector.close()
    }

    @Test
    fun testBroadcastFails(): Unit = runBlocking {
        lateinit var socket: BoundDatagramSocket
        assertFailsWith<SocketException>("Permission denied") {
            socket =aSocket(selector)
                .udp()
                .bind()

            socket.use {
                val datagram = Datagram(
                    packet = buildPacket { writeText("0123456789") },
                    address = NetworkAddress("255.255.255.255", 56700)
                )

                it.send(datagram)
            }
        }

        socket.socketContext.join()
        assertTrue(socket.isClosed)
    }

    @Test
    fun testClose(): Unit = runBlocking {
        val socket = aSocket(selector)
            .udp()
            .bind()

        socket.close()

        socket.socketContext.join()
        assertTrue(socket.isClosed)
    }

    @Test
    fun testInvokeOnClose() = runBlocking {
        val socket: BoundDatagramSocket = aSocket(selector)
            .udp()
            .bind()

        var done = 0
        socket.outgoing.invokeOnClose {
            done += 1
        }

        assertFailsWith<IllegalStateException> {
            socket.outgoing.invokeOnClose {
                done += 2
            }
        }

        socket.close()
        socket.close()

        assertEquals(1, done)
        assertTrue(socket.isClosed)
    }

    @Test
    fun testOutgoingInvokeOnClose() = runBlocking {
        val socket: BoundDatagramSocket = aSocket(selector)
            .udp()
            .bind()

        var done = 0
        socket.outgoing.invokeOnClose {
            done += 1
            assertTrue(it is AssertionError)
        }

        socket.outgoing.close(AssertionError())

        assertEquals(1, done)
        assertTrue(socket.isClosed)
    }

    @Test
    fun testOutgoingInvokeOnCloseWithSocketClose() = runBlocking {
        val socket: BoundDatagramSocket = aSocket(selector)
            .udp()
            .bind()

        var done = 0
        socket.outgoing.invokeOnClose {
            done += 1
        }

        socket.close()

        assertEquals(1, done)

        socket.socketContext.join()
        assertTrue(socket.isClosed)
    }

    @Test
    fun testOutgoingInvokeOnClosed() = runBlocking {
        val socket: BoundDatagramSocket = aSocket(selector)
            .udp()
            .bind()

        socket.outgoing.close(AssertionError())

        var done = 0
        socket.outgoing.invokeOnClose {
            done += 1
            assertTrue(it is AssertionError)
        }

        assertEquals(1, done)

        socket.socketContext.join()
        assertTrue(socket.isClosed)
    }
}
