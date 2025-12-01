package com.mintleaf.coprocess

import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

internal object CoProcess {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend operator fun invoke(
        command: List<String>,
        input: InputStream?,
        output: OutputStream?,
        error: OutputStream?,
        onStarted: suspend (Process) -> Unit,
    ): Int {
        val handle = ProcessBuilder(command).start()
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

            // Suspend while process is running
            logger.debug("Monitoring process")
            while (handle.isAlive) {
                // Terminate process if coroutine is canceled
                if (!isActive) {
                    logger.debug("Terminating process due to coroutine cancellation")
                    handle.destroy()
                    break
                }
                yield()
            }

            inputJob?.join()
            outputJob?.join()
            errorJob?.join()
        }

        return handle.exitValue().also { logger.debug("Process finished") }
    }
}
