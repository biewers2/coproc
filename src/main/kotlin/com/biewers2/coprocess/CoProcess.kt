package com.biewers2.coprocess

import executor
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

internal object CoProcess {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend operator fun invoke(
        command: List<String>,
        directory: File,
        input: InputStream?,
        output: OutputStream?,
        error: OutputStream?,
        onStarted: suspend (Process) -> Unit,
    ): Int {
        val handle = ProcessBuilder(command).directory(directory).start()

        logger.info("Coprocess started (pid ${handle.pid()}): ${command.joinToString(" ")}")
        onStarted(handle)

        coroutineScope {
            val inputJob =
                input?.let {
                    launch { TransferIOStreams(input = it, output = handle.outputStream) }
                }
            val outputJob =
                output?.let {
                    launch { TransferIOStreams(input = handle.inputStream, output = it) }
                }
            val errorJob =
                error?.let { launch { TransferIOStreams(input = handle.errorStream, output = it) } }

            handle.join()
            inputJob?.join()
            outputJob?.join()
            errorJob?.join()
        }

        return handle.exitValue().also { logger.info("Coprocess completed") }
    }

    private suspend fun Process.join() {
        suspendCancellableCoroutine { cont ->
            cont.invokeOnCancellation {
                try {
                    logger.info("Coprocess terminating due to coroutine cancellation")
                    destroy()
                    waitFor()
                } catch (e: InterruptedException) {
                    logger.error("Coprocess termination interrupted", e)
                }
            }

            cont.executor.execute {
                try {
                    logger.info("Coprocess awaiting completion")
                    waitFor()
                    cont.resume(exitValue())
                    logger.info("Coprocess completed successfully")
                } catch (e: InterruptedException) {
                    logger.error("Coprocess waiting interrupted", e)
                    cont.resumeWithException(e)
                }
            }
        }
    }
}
