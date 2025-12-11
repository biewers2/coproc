package com.biewers2

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CoprocessTest :
    DescribeSpec({
        fun isAlive(pid: Long) = ProcessHandle.of(pid).let { it.isPresent && it.get().isAlive }

        describe("coprocess") {
            it("waits for process to finish") {
                val message = "hello, world"
                val output = ByteArrayOutputStream()
                val error = ByteArrayOutputStream()

                var pid: Long? = null
                val exitCode =
                    coprocess("echo", message, output = output, error = error) { pid = it.pid() }

                exitCode shouldBe 0
                output.toString() shouldBe "$message\n"
                error.toString() shouldBe ""

                pid.shouldNotBeNull()
                isAlive(pid) shouldBe false
            }

            it("can be canceled") {
                val output = ByteArrayOutputStream()
                val error = ByteArrayOutputStream()

                var pid: Long? = null
                val exitCode =
                    launch(singleThreadedContext) {
                        coprocess("tail", "-f", "/dev/null", output = output, error = error) {
                            pid = it.pid()
                        }
                    }

                exitCode.cancelAndJoin()
                exitCode.isCancelled shouldBe true

                output.toString() shouldBe ""
                error.toString() shouldBe ""

                pid.shouldNotBeNull()
                isAlive(pid) shouldBe false
            }
        }
    })
