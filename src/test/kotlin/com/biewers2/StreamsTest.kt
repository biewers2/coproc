package com.biewers2

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
class StreamsTest :
    DescribeSpec({
        describe("transfer") {
            it("should transfer streams") {
                val expected = "this is my input"
                val inputStream = expected.byteInputStream()
                val outputStream = ByteArrayOutputStream()

                transfer(inputStream, outputStream)

                outputStream.toString() shouldBe expected
            }

            it("can be canceled").config(timeout = Duration.Companion.parse("3s")) {
                val input: InputStream = mockk { every { read(any()) } returns 0 }
                val output: OutputStream = mockk { every { write(any(), any(), any()) } just runs }

                val job = launch(singleThreadedContext) { transfer(input, output) }

                job.cancelAndJoin()
            }

            it("propagates thrown exceptions") {
                val input: InputStream = mockk { every { read(any()) } throws IOException() }
                val output: OutputStream = mockk()

                shouldThrow<IOException> { transfer(input, output) }
            }
        }

        describe("pipeline") {
            it("should transfer streams") {
                val expected = "this is my input"
                val inputStream = expected.byteInputStream()
                val outputStream = ByteArrayOutputStream()

                pipeline(inputStream, outputStream)

                outputStream.toString() shouldBe expected
            }

            it("can be canceled").config(timeout = Duration.Companion.parse("3s")) {
                val input: InputStream = mockk { every { read(any()) } returns 0 }
                val output: OutputStream = mockk { every { write(any(), any(), any()) } just runs }

                val job = launch(singleThreadedContext) { pipeline(input, output) }

                job.cancelAndJoin()
            }

            it("propagates thrown exceptions") {
                val input: InputStream = mockk { every { read(any()) } throws IOException() }
                val output: OutputStream = mockk()

                shouldThrow<IOException> { pipeline(input, output) }
            }
        }
    })
