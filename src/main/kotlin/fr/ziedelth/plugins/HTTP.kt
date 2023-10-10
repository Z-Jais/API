package fr.ziedelth.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import fr.ziedelth.utils.Constant
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*

fun Application.configureHTTP() {
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }

    install(CORS) {
        HttpMethod.DefaultMethods.forEach { allowMethod(it) }
        allowHeader(HttpHeaders.Authorization)
        anyHost()
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    authentication {
        jwt("auth-jwt") {
            realm = Constant.jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(Constant.jwtSecret))
                    .withAudience(Constant.jwtAudience)
                    .withIssuer(Constant.jwtDomain)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(Constant.jwtAudience) && !credential.payload.getClaim("uuid")
                        ?.asString().isNullOrBlank()
                )
                    JWTPrincipal(credential.payload)
                else
                    null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }

    install(SwaggerUI) {
        swagger {
            swaggerUrl = "swagger"
            forwardRoot = true
        }
        info {
            title = "API"
            version = "latest"
            description = "API for testing and demonstration purposes."
        }
    }
}
