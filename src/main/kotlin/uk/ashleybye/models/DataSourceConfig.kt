package uk.ashleybye.models

/**
 * Holds data source configuration information.
 *
 * Data class to hold data source configuration information.
 *
 * @property username - the database username
 * @property password - the database password
 * @property jdbcUrl - the JDBC connection url
 */
data class DataSourceConfig(val username: String, val password: String, val jdbcUrl: String)
