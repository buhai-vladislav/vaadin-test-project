package com.testtask.base

import org.slf4j.LoggerFactory

abstract class BaseService(
    private val clazz: Class<*>
) {
    protected val logger = LoggerFactory.getLogger(clazz)

    fun <T> executeWithLogging(operation: String, action: () -> T): T {
        logger.info("Starting execution of ${clazz.simpleName}")
        try {
            return action().also {
                logger.info("Finished execution of $operation")
            }
        } catch (e: Exception) {
            logger.error("Error during execution of $operation: ${e.message}", e)
            throw e
        }
    }
}