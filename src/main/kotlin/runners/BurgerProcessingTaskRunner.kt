package runners

import io.github.oshai.kotlinlogging.KotlinLogging
import models.Order
import services.OrderIdGenerationService
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class BurgerProcessingTaskRunner(
    private val taskId: String? = null,
    private val pendingOrders: ConcurrentLinkedQueue<Order>,
    private val completeOrders: ConcurrentLinkedQueue<Order>,
    private val cookedBurgers: AtomicInteger,
    private val dressedBurgers: AtomicInteger,
    private val customersQueuedCount: AtomicInteger,
    private val orders: Queue<Order>,
    private val orderIdGenerationService: OrderIdGenerationService,
    private val cookedBurgerLimit: Int = 5,
    private val completeOrdersLimit: Int = 15,
    private val operationalDelaySeconds: Int = 2
) : Runnable {

    private val tasks = mutableListOf(this::cookBurger, this::dressBurger, this::serveBurger, this::takeOrder)

    private val logger = KotlinLogging.logger {}

    override fun run() {
        var result = 0
        while (result == 0) {
            result = tasks.random()
                .let {
                    task ->
                    tasks.remove(task)
                    task.invoke()
                        .apply {
                            val status = if (this == 0) { "skipped"} else "complete"
                            logger.info { "model=burger_processing_task_runner, task=${task.name}, status=$status, metadata={thread_name=${Thread.currentThread().name}, task_id=$taskId}" }
                        }
                }
            Thread.sleep(operationalDelaySeconds*1000L)
        }
        return
    }

    private fun cookBurger(): Int {
        if(cookedBurgers.get() < cookedBurgerLimit) {
            cookedBurgers.incrementAndGet()
            return 1
        }
        return 0
    }

    @Synchronized
    private fun dressBurger(): Int {
        if (cookedBurgers.get() > 0) {
            dressedBurgers.incrementAndGet()
            cookedBurgers.decrementAndGet()
            return 1
        }
        return 0
    }

    private fun serveBurger(): Int {
        pendingOrders.peek()?.let {
            if(dressedBurgers.get() >= it.burgerQuantity) {
                dressedBurgers.decrementAndGet()
                if (it.drinkQuantity>=0) {
                    repeat(it.drinkQuantity) { pourDrink() }
                }
                val completeOrder = pendingOrders.remove()
                completeOrders.addAndManageCompleteOrders(completeOrder)
                return 1
            }
        }
        return 0
    }

    private fun pourDrink() {}


    private fun takeOrder() : Int {
        return if (customersQueuedCount.get() > 0) {
            orders.add(
                Order(
                    id = orderIdGenerationService.getId(),
                    burgerQuantity = Random.nextInt(1, 2),
                    drinkQuantity = Random.nextInt(0, 2)
                )
            )
            customersQueuedCount.decrementAndGet()
            1
        } else {
            0
        }
    }

    private fun ConcurrentLinkedQueue<Order>.addAndManageCompleteOrders(newCompleteOrder: Order) {
        if (this.size > completeOrdersLimit) {
            this.remove()
        }
        this.add(newCompleteOrder)
    }
}
