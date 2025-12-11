package com.biewers2

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger by lazy {
    LoggerFactory.getLogger(object {}.javaClass.packageName + ".coprocess")
}

/**
 * Run a suspendable [Process].
 *
 * @param command program and its arguments
 * @param input the input to redirect to the process' stdin
 * @param output where to redirect the process' stdout
 * @param error where to redirect the process' stdin
 */
suspend fun coprocess(
    vararg command: String,
    directory: File = File("."),
    input: InputStream? = null,
    output: OutputStream? = null,
    error: OutputStream? = null,
    onStarted: suspend (Process) -> Unit = {},
): Int = coprocess(command.asList(), directory, input, output, error, onStarted)

suspend fun coprocess(
    command: List<String>,
    directory: File = File("."),
    input: InputStream? = null,
    output: OutputStream? = null,
    error: OutputStream? = null,
    onStarted: suspend (Process) -> Unit = {},
): Int {
    val handle = ProcessBuilder(command).directory(directory).start()
    try {
        logger.debug("Coprocess started (pid ${handle.pid()}): ${command.joinToString(" ")}")
        onStarted(handle)

        withContext(Dispatchers.IO) {
            input?.let { launch { transfer(input = it, output = handle.outputStream) } }
            output?.let { launch { transfer(input = handle.inputStream, output = it) } }
            error?.let { launch { transfer(input = handle.errorStream, output = it) } }

            logger.debug("Coprocess awaiting completion")
            handle.waitFor()
            logger.debug("Coprocess completed successfully")
        }

        return handle.exitValue().also { logger.debug("Coprocess completed") }
    } catch (e: CancellationException) {
        try {
            logger.debug("Coprocess terminating due to coroutine cancellation")
            handle.destroy()
            handle.waitFor()
            return handle.exitValue()
        } catch (e: InterruptedException) {
            logger.error("Coprocess termination interrupted", e)
        }
        throw e
    }
}
