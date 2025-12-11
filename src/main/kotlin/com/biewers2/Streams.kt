package com.biewers2

import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DEFAULT_BUFFER_SIZE = 8192
private const val DEFAULT_CHANNEL_CAPACITY = 100

suspend fun transfer(
    input: InputStream,
    output: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
) =
    withContext(Dispatchers.IO) {
        val buf = ByteArray(bufferSize)
        while (true) {
            currentCoroutineContext().ensureActive()
            when (val n = input.read(buf)) {
                -1 -> break
                0 -> Unit
                else -> output.write(buf, 0, n)
            }
        }
    }

suspend fun pipeline(
    input: InputStream,
    output: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    channelCapacity: Int = DEFAULT_CHANNEL_CAPACITY,
) =
    withContext(Dispatchers.IO) {
        val chan = Channel<ByteArray>(channelCapacity)

        launch {
            val buf = ByteArray(bufferSize)
            while (true) {
                when (val n = input.read(buf)) {
                    -1 -> break
                    0 -> Unit
                    else -> chan.send(buf.sliceArray(0 until n))
                }
            }
            chan.close()
        }

        for (bytes in chan) {
            output.write(bytes)
        }
    }
