package models.threads

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class GroupNamedThreadFactory(private val groupName: String): ThreadFactory {
    private val counter = AtomicInteger(0)

    override fun newThread(r: Runnable): Thread {
        return Thread(null, r, "$groupName-thread-${counter.incrementAndGet()}")
    }
}