import io.github.oshai.kotlinlogging.KotlinLogging
import models.Order
import models.threads.GroupNamedThreadFactory
import runners.BurgerProcessingTaskRunner
import services.OrderIdGenerationService
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * McCarra's Burgers
 *
 *
 * McCarra's Burgers is a restaurant offering its customers meals which consist of least one burger and optionally a drink.
 * Customers order by shouting at servers behind the counter who always understand the order no matter the task the server is doing.
 *
 * The servers at McCarras have four tasks:
 *
 * 1. Take order
 * 1. Cook a burger
 * 2. Dress a burger
 * 3. Serve the burger
 * 4. Pour the drinks
 *
 * A burger has the following lifecycle:
 * cooked > dressed > served
 *
 * Drinks are poured at the time of serving the order
 *
 */

class Main {

    private val logger = KotlinLogging.logger {}

    private val cookedBurgersCount = AtomicInteger(0)
    private val dressedBurgersCount = AtomicInteger(0)
    private val customersQueuedCount = AtomicInteger(0)
    private val pendingOrders = ConcurrentLinkedQueue<Order>()
    private val completeOrders = ConcurrentLinkedQueue<Order>()

    private val orderIdGenerationService = OrderIdGenerationService()

    private fun burgerProcessingTaskRunner(taskId: String) = BurgerProcessingTaskRunner(
        taskId = taskId,
        pendingOrders = pendingOrders,
        completeOrders = completeOrders,
        cookedBurgers = cookedBurgersCount,
        dressedBurgers = dressedBurgersCount,
        customersQueuedCount = customersQueuedCount,
        orders = pendingOrders,
        orderIdGenerationService = orderIdGenerationService
    )

    private val restaurantManagementThreadPool = Executors.newFixedThreadPool(2, GroupNamedThreadFactory("restaurant-management"))

    fun run() {
        logger.info { "model=main, operation=run, status=started" }
        while (true) {

            val customersToGenerate = Random.nextInt(0,2)

            repeat(customersToGenerate) {
                customersQueuedCount.incrementAndGet()
            }

            logger.info { "model=customers_queued_count, customers_generated_count_on_current_pass=$customersToGenerate, total_count=$customersQueuedCount" }

            logger.info { "model=pending_orders, size=${pendingOrders.size}, pending_orders=$pendingOrders" }
            logger.info { "model=complete_orders, size=${completeOrders.size}, pending_orders=$completeOrders" }

            logger.info { "model=cooked_burgers, count=${cookedBurgersCount}" }
            logger.info { "model=dressed_burgers, count=${dressedBurgersCount}" }


            val burgerProcessingJobs =

                listOf(
                    restaurantManagementThreadPool.submit(burgerProcessingTaskRunner("task_1")),
                    restaurantManagementThreadPool.submit(burgerProcessingTaskRunner("task_2"))
                )

            while (!burgerProcessingJobs.all { it.isDone }) {
                // allow for a pause so that processing isn't too fast
                Thread.sleep(1*1000L)
            }
        }
    }
}


fun main() {
    Main().run()
}

