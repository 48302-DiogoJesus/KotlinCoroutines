package KotlinCoroutines

import kotlinx.coroutines.*
import kotlin.coroutines.*

/**
 * Threads Problem
 *
 * - Take up too much memory
 * - Context Switch is costly
 * - [!] Hard to synchronize multiple Threads
 *   (ex: detect if any failed, waiting for a group of threads to finish requires using .join() which is ugly)
 *
 * - Solution: Coroutines
 * */

/**
 * Why Coroutines ?
 *
 * - Multiple things running without blocking Threads
 * - Can be suspended releasing the Thread that was executing them
 * - Concurrency Control provided: Easier to synchronize with the end of coroutines. A CoroutineScope only closes when
 *   all the inner coroutines are finished/closed
 */

/**
 * [CONCEPTS]
 *
 * - CoroutineScope: The scope of a coroutine
 * - CoroutineContext: The context of a Coroutine (Ex: The Dispatcher to use)
 * - Job: Represents a Coroutine (job.cancel() | job.join() | job.start() | job.isCompleted)
 *
 *   Notes:
 *       - When a coroutine is CANCELLED, all it's child are cancelled (receiving CancellationException)
 *       - When a coroutine is CANCELLED it's siblings are NOT cancelled
 *       - When a coroutine FAILS (with Exception != CancellationException) all it's siblings are cancelled
 *       - When all the children of a coroutine C1 are completed (normally | cancellation | exception[fail]), C1
 *         is also completed
 *
 * Functions involved:
 *
 *  [*] suspend fun
 *  [*] runBlocking                 => fun <R> runBlocking(CoroutineContext, suspend fun CoroutineScope.() -> R): R
 *  [*] coroutineScope              => suspend fun <R> coroutineScope(suspend fun CoroutineScope.() -> R): R
 *  [*] launch                      => fun CoroutineScope.launch(CoroutineContext, CoroutineStart, suspend CoroutineScope.() -> Unit): Job
 *  [*] suspendCoroutine            => suspend fun <T> suspendCoroutine((Continuation<T>) -> Unit): T
 *  [*] suspendCancellableCoroutine => suspend fun <T> suspendCancellableCoroutine((CancellableContinuation<T>) -> Unit): T
 *  [*] delay                       => suspend fun delay(time)
 *  [*] withContext                 => suspend fun <R> withContext(CoroutineContext, suspend fun CoroutineScope.() -> R): R
 * */


/**
 * Suspend Functions
 *
 * Functions that can be suspended. Other suspend functions can be called inside it
 * */
suspend fun thisIsASuspendFunction() {

    /**
     * runBlocking
     *
     * fun <R> runBlocking(CoroutineContext, suspend fun CoroutineScope.() -> R): R
     *
     * Creates a CoroutineScope. A CoroutineContext can be specified
     * Can be called from anywhere
     * */
    runBlocking {

        /**
         * coroutineScope
         *
         * suspend fun <R> coroutineScope(suspend fun CoroutineScope.() -> R): R
         *
         * Creates a CoroutineScope and suspends the invoking coroutine while the scope does not return
         * It's a suspend function, therefore it can be called from any suspend functions and coroutines
         * */
        val value: Int = coroutineScope<Int> {
            // ...
            /**
             * delay
             *
             * suspend fun delay(time)
             *
             * Suspends the invoking Coroutine for [time] amount to milliseconds
             * */
            delay(1000)
            // ...
            return@coroutineScope 2 // Could be Unit and not return a value
        }

        /**
         * withContext
         *
         * suspend fun <R> withContext(CoroutineContext, suspend fun CoroutineScope.() -> R): R
         *
         * Creates a CoroutineScope with CoroutineContext = CoroutineContext(1st param) + the context of the invoking
         * CoroutineScope (if it existed, since this can be called from outside a CoroutineScope)
         * */
        withContext(Dispatchers.IO) {
            delay(50)
        }

        /**
         * launch
         *
         * fun CoroutineScope.launch(CoroutineContext, CoroutineStart, suspend CoroutineScope.() -> Unit): Job
         *
         * Creates a new CoroutineScope without suspending the invoking coroutine.
         * It returns a Job that represents the Coroutine.
         * */
        val job = launch {

        }
    }
    /**
     *
     * suspendCoroutine
     *
     * suspend fun <T> suspendCoroutine((Continuation<T>) -> Unit): T
     *
     * Suspends the invoking coroutine until the Continuation is called.
     * This continuation is provided and can be called by any Thread at any time.
     *
     * Good Usage Example:
     * https://github.com/isel-leic-pc/s2122v-li41d/blob/main/jvm/src/main/kotlin/pt/isel/pc/coroutines/SuspendableMessageQueue.kt
     * */
    val v1: Int = suspendCoroutine<Int> { continuation: Continuation<Int> ->
        continuation.resume(2)
        try {
            // ...
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
    // Only called when the continuation of the suspendCoroutine above is called (until then THIS coroutine is suspended)
    println(v1)
    /**
     * suspendCancellableCoroutine
     *
     * suspend fun <T> suspendCancellableCoroutine((CancellableContinuation<T>) -> Unit): T
     *
     * Suspends the invoking coroutine until the Continuation is called.
     * This continuation is provided and can be called by any Thread at any time.
     *
     * The only difference from suspendCoroutine is that here we get a CancellableCoroutine<T> which, as suggested by the name,
     * can be cancelled. Cancelling it will propagate the CancellationException to the invoking coroutine, which will
     * cause it to be cancelled if this exception is not caught + ignored.
     * */
    val v2: Int = suspendCancellableCoroutine <Int> { continuation: CancellableContinuation<Int> ->
        continuation.resume(2)
        try {
            // ...
        } catch (e: Exception) {
            continuation.resumeWithException(e)
            // Difference from suspendCoroutine
            continuation.cancel(e)
        }
        // Difference from suspendCoroutine
        continuation.cancel()
    }
    // Only called when the continuation of the suspendCoroutine above is called (until then THIS coroutine is suspended)
    println(v2)
}