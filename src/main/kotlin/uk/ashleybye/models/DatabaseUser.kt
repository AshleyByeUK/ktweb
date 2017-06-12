package uk.ashleybye.models

import io.vertx.core.AsyncResult
import io.vertx.core.CompositeFuture
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AbstractUser
import io.vertx.ext.auth.AuthProvider
import kotliquery.Row
import java.util.*

/**
 * Holds information about a user from a database.
 *
 * See [AbstractUser](http://vertx.io/docs/apidocs/io/vertx/ext/auth/AbstractUser)
 *
 * @property id - the Unique User ID
 * @property username - the username
 * @property passwordHash - the hashed password
 */
class DatabaseUser(val id: UUID, val username: String, val passwordHash: String) : AbstractUser() {

    companion object {
        /**
         * Gets a DatabaseUser from the given row.
         *
         * @param row - the database row
         */
        fun fromDb(row: Row): DatabaseUser {
            return DatabaseUser(UUID.fromString(row.string("user_id")),
                    row.string("user_code"),
                    row.string("password"))
        }
    }

    /**
     * Abstract method from [AbstractUser](http://vertx.io/docs/apidocs/io/vertx/ext/auth/AbstractUser),
     * which determines whether the DatabaseUser is permitted. No explanation is given in API docs
     * as to what this actually does or what the permission string is used for. Check Vert.x
     * documentation instead.
     */
    override fun doIsPermitted(permission: String?, resultHandler: Handler<AsyncResult<Boolean>>?) {
        val result = CompositeFuture.factory.succeededFuture(true)
        resultHandler?.handle(result)
    }

    override fun setAuthProvider(authProvider: AuthProvider?) {
        // Not required: typically used for reattaching a User to an AuthProvider after it has been
        // de-serialised.
    }

    override fun principal(): JsonObject {
        return JsonObject().put("username", username)
    }

}
