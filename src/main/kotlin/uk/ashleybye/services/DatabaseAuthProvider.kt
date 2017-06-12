package uk.ashleybye.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.zaxxer.hikari.HikariDataSource
import io.vertx.core.AsyncResult
import io.vertx.core.CompositeFuture
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.User
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.funktionale.either.eitherTry
import org.mindrot.jbcrypt.BCrypt
import uk.ashleybye.models.AuthInfo
import uk.ashleybye.models.DatabaseUser

/**
 * Authenticates users against a data source.
 *
 * Uses the given dataSource to authenticate users based on a username and password. User credentials
 * are mapped from JSON format provided by Vert.X to an instance of [AuthInfo] by the provided
 * jsonMapper.
 *
 * @property dataSource - the data source
 * @property jsonMapper - the JSON mapper
 */
class DatabaseAuthProvider(val dataSource: HikariDataSource, val jsonMapper: ObjectMapper) : AuthProvider {

    private val authenticationFailureMessage = "Invalid username and password combination"

    /**
     * See [AuthProvider](http://vertx.io/docs/apidocs/io/vertx/ext/auth/AuthProvider.html)
     */
    override fun authenticate(authInfoJson: JsonObject?, resultHandler: Handler<AsyncResult<User>>?) {
        val authInfo = jsonMapper.readValue(authInfoJson?.encode(), AuthInfo::class.java)
        val userTry = eitherTry {
            using(sessionOf(dataSource)) { session ->
                val query = queryOf("SELECT * FROM users WHERE user_code = ?", authInfo.username)
                val maybeUser = query.map { DatabaseUser.fromDb(it) }.asSingle.runWithSession(session)
                maybeUser ?: throw Exception(authenticationFailureMessage)
            }
        }

        userTry.fold({ exception ->
            val result = CompositeFuture.factory.failedFuture<User>(exception)
            resultHandler?.handle(result)
        }, { dbUser ->
            val isValid = BCrypt.checkpw(authInfo.password, dbUser.passwordHash)
            val result = if (isValid) {
                CompositeFuture.factory.succeededFuture(dbUser as User)
            } else {
                CompositeFuture.factory.failureFuture(authenticationFailureMessage)
            }
            resultHandler?.handle(result)
        })
    }
}
