package eu.lauwenmark.amanogawa.server

import eu.lauwenmark.amanogawa.server.network.startNetwork
import mu.KotlinLogging

fun main(args: Array<String>) {
    val logger = KotlinLogging.logger {  }
    logger.info {"Amanogawa server v0.1."}
    startNetwork(2044)
}