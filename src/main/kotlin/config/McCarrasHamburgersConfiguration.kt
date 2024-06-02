package config

data class McCarrasHamburgersConfiguration(
    val restaurantManagementThreadPoolSize: Int,
    val burgerProcessingTaskInstancesPerCycle: Int,
    val cyclePauseTimeSeconds: Int,
    val burgerProcessingTaskConfig: BurgerProcessingTaskConfig
    )

data class BurgerProcessingTaskConfig(
    val cookedBurgerLimit: Int,
    val completeOrdersLimit: Int,
    val operationalDelaySeconds: Int
)