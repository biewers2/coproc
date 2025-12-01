package com.biewers2.coprocess

import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.suspendCancellableCoroutine
import org.slf4j.LoggerFactory

internal object TransferIOStreams {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private const val DEFAULT_BUFFER_SIZE = 4096

    suspend operator fun invoke(
        input: InputStream,
        output: OutputStream,
        bufferSize: Int = DEFAULT_BUFFER_SIZE,
    ) {
        val buf = ByteArray(bufferSize)

        logger.trace("Starting IO transfer")
        do {
            when (val n = suspendIO { input.read(buf) }) {
                -1 -> break
                0 -> Unit
                else -> suspendIO { output.write(buf, 0, n) }
            }
        } while (true)
        logger.trace("IO transferring complete")
    }

    private suspend fun <T> suspendIO(block: () -> T): T = suspendCancellableCoroutine { cont ->
        Dispatchers.IO.asExecutor().execute {
            try {
                cont.resume(block())
            } catch (t: Throwable) {
                cont.resumeWithException(t)
            }
        }
    }
}
