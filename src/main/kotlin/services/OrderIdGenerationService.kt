package services

import java.util.concurrent.atomic.AtomicInteger

class OrderIdGenerationService(
    private val initalId: AtomicInteger = AtomicInteger(0)
) {
    @Synchronized
    fun getId(): String = initalId.incrementAndGet().toString().padStart(3, '0')
}