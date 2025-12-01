package com.mintleaf.coprocess

import com.mintleaf.singleThreadedContext
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.time.Duration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TransferIOStreamsTest :
    DescribeSpec({
        describe("transferring") {
            it("should transfer streams") {
                val expected = "this is my input"
                val inputStream = expected.byteInputStream()
                val outputStream = ByteArrayOutputStream()

                inputStream.use { input ->
                    outputStream.use { output -> TransferIOStreams(input, output) }
                }

                outputStream.toString() shouldBe expected
            }

            it("can be canceled").config(timeout = Duration.parse("3s")) {
                val input: InputStream = mockk { every { read(any()) } returns 0 }
                val output: OutputStream = mockk { every { write(any(), any(), any()) } just runs }

                val job = launch(singleThreadedContext) { TransferIOStreams(input, output) }

                job.cancelAndJoin()
            }

            it("propagates thrown exceptions") {
                val input: InputStream = mockk { every { read(any()) } throws IOException() }
                val output: OutputStream = mockk()

                shouldThrow<IOException> { TransferIOStreams(input, output) }
            }
        }
    })
