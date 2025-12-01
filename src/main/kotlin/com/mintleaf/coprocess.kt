package com.mintleaf

import com.mintleaf.coprocess.CoProcess
import java.io.File
import java.io.InputStream
import java.io.OutputStream

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
): Int =
    CoProcess(
        command = command,
        directory = directory,
        input = input,
        output = output,
        error = error,
        onStarted = onStarted,
    )
