package uk.ashleybye.verticles

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zaxxer.hikari.HikariDataSource
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.*
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.ext.web.templ.ThymeleafTemplateEngine
import nl.komponents.kovenant.functional.bind
import nl.komponents.kovenant.functional.map
import uk.ashleybye.models.ApiKeys
import uk.ashleybye.models.DataSourceConfig
import uk.ashleybye.models.ServerConfig
import uk.ashleybye.models.SunWeatherInfo
import uk.ashleybye.services.DatabaseAuthProvider
import uk.ashleybye.services.MigrationService
import uk.ashleybye.services.SunService
import uk.ashleybye.services.WeatherService
import uy.klutter.vertx.VertxInit

/**
 * Application entry point.
 */
class MainVerticle : AbstractVerticle() {

    private var maybeDatSource: HikariDataSource? = null

    /**
     * Starts the Vert.x server.
     */
    override fun start(startFuture: Future<Void>?) {

        /* ---------- Initialise Vert.x. ---------- */
        VertxInit.ensure()
        val server = vertx.createHttpServer()
        val router = Router.router(vertx)
        val logger = LoggerFactory.getLogger("VertxServer")
        val templateEngine = ThymeleafTemplateEngine.create()

        /* ---------- Load Configuration Parameters. ---------- */
        val jsonMapper = jacksonObjectMapper()

        // Server configuration.
        val serverConfig = jsonMapper.readValue(config()
                .getJsonObject("server").encode(), ServerConfig::class.java)
        val serverPort = serverConfig.port
        val enableCaching = serverConfig.caching

        // API Keys.
        val apiKeys = jsonMapper.readValue(config()
                .getJsonObject("apiKeys").encode(), ApiKeys::class.java)
        val openWeatherMapAppId = apiKeys.openWeatherMapAppId

        // Data source configuration and migrations.
        val dataSourceConfig = jsonMapper.readValue(config()
                .getJsonObject("dataSource").encode(), DataSourceConfig::class.java)
        val dataSource = initDataSource(dataSourceConfig)
        val migrationService = MigrationService(dataSource)
        val migrationResult = migrationService.migrate()

        migrationResult.fold({ exception ->
            logger.fatal("Exception occurred while performing migration", exception)
            vertx.close()
        }, { result ->
            logger.debug("Migration successful or not needed")
        })

        /* ---------- Services. ---------- */
        val sunService = SunService()
        val weatherService = WeatherService(openWeatherMapAppId)
        val authProvider = DatabaseAuthProvider(dataSource, jsonMapper) // Not dataSource.get() as per page 95.

        /* ---------- Handlers. ---------- */
        val staticHandler = StaticHandler.create().setWebRoot("public")
                .setCachingEnabled(enableCaching)

        /* ---------- Routing. ---------- */
        // Session - must be prior to any authentication configuration.
        router.route().handler(CookieHandler.create())
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)))
        router.route().handler(UserSessionHandler.create(authProvider))

        // Authentication.
        router.route("/hidden/*").handler(RedirectAuthHandler.create(authProvider, "/login"))
        router.route("/login").handler(BodyHandler.create())
        router.post("/login").handler(FormLoginHandler.create(authProvider)) // Post, not route!

        router.route("/public/*").handler(staticHandler)

        // Rendering. Private method instead, passing templateEngine?
        fun renderTemplate(routingContext: RoutingContext, template: String) {
            templateEngine.render(routingContext, template, { handler ->
                val response = routingContext.response()
                if (handler.failed()) {
                    logger.error("Template rendering failed", handler.cause())
                    response.setStatusCode(500).end()
                } else {
                    response.end(handler.result())
                }
            })
        }

        // Non-API Routes.
        router.get("/home").handler { routingContext ->
            renderTemplate(routingContext, "public/templates/index.html")
        }

        router.get("/login").handler { routingContext ->
            renderTemplate(routingContext, "public/templates/login.html")
        }

        router.get("/hidden/admin").handler { routingContext ->
            val username = routingContext.user().principal().getString("username")
            renderTemplate(routingContext.put("username", username), "public/templates/admin.html")
        }

        // API Routes.
        router.get("/api/data").handler { routingContext ->
            val lat = -33.8830
            val lon = 151.2167
            val sunInfoPromise = sunService.getSunInfo(lat, lon)
            val temperaturePromise = weatherService.getTemperature(lat, lon)
            val sunWeatherInfoPromise = sunInfoPromise.bind { sunInfo ->
                temperaturePromise.map { temp -> SunWeatherInfo(sunInfo, temp) }
            }

            sunWeatherInfoPromise.success { info ->
                val json = jsonMapper.writeValueAsString(info)
                val response = routingContext.response()
                response.end(json)
            }
        }

        /* ---------- Server config. ---------- */
        server.requestHandler { router.accept(it) }.listen(serverPort, { handler ->
            if (!handler.succeeded()) {
                logger.error("Failed to listen on port 8080")
            }
        })
    }

    /**
     * Clean up when server stops.
     */
    override fun stop(stopFuture: Future<Void>?) {
        maybeDatSource?.close()
    }

    /**
     * Helper function for initialising data source with connection pooling (HikariCP).
     */
    private fun initDataSource(config: DataSourceConfig): HikariDataSource {
        val hikariDs = HikariDataSource()
        hikariDs.username = config.username
        hikariDs.password = config.password
        hikariDs.jdbcUrl = config.jdbcUrl

        maybeDatSource = hikariDs

        return hikariDs
    }
}
