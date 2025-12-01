import java.util.concurrent.Executor
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asExecutor

val CancellableContinuation<*>.executor: Executor
    get() = (context[ExecutorCoroutineDispatcher] ?: Dispatchers.IO).asExecutor()
