package com.mintleaf.coprocess

import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
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
            when (val n = withBlockingIO { input.read(buf) }) {
                -1 -> break
                0 -> Unit
                else -> withBlockingIO { output.write(buf, 0, n) }
            }
        } while (true)
        logger.trace("IO transferring complete")
    }

    private suspend fun <T> withBlockingIO(block: () -> T): T =
        suspendCancellableCoroutine { cont ->
            (cont.context[ExecutorCoroutineDispatcher] ?: Dispatchers.IO)
                .also { logger.trace("Using executor ${it::class.java.name}") }
                .asExecutor()
                .execute {
                    try {
                        cont.resume(block())
                    } catch (t: Throwable) {
                        cont.resumeWithException(t)
                    }
                }
        }
}
