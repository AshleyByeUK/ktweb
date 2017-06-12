package uk.ashleybye.models

/**
 * Holds server configuration information.
 *
 * Data class to hold Vert.x server configuration information.
 *
 * @property port - the port to listen on
 * @property caching - enabled if true; disabled if false
 */
data class ServerConfig(val port: Int, val caching: Boolean)
