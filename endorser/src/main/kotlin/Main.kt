import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.util.concurrent.ConcurrentHashMap


fun main() {
    val keys = ConcurrentHashMap<String, Pair<String, String>>()

    embeddedServer(Netty, 8080) {
        routing {
            get("/keys/add/{keyName}/{publicKeyValue}/{privateKeyValue}") {
                println("Received request /keys/add")

                val keyName = call.parameters["keyName"]
                val publicKeyValue = call.parameters["publicKeyValue"]
                val privateKeyValue = call.parameters["privateKeyValue"]

                if (keyName.isNullOrEmpty() || publicKeyValue.isNullOrEmpty() || privateKeyValue.isNullOrEmpty()) {
                    call.respond(false)
                    throw RuntimeException("Empty key is submitted")
                }

                println("Received key: $keyName = ($publicKeyValue,$privateKeyValue)")

                keys[keyName] = Pair(publicKeyValue, privateKeyValue)
                call.respond(true)
            }

            get("/keys/get/{keyName}") {
                println("Received request /keys/get")
                val keyName = call.parameters["keyName"]

                if (keyName.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, false)
                    throw RuntimeException("Empty key is requested")
                }

                println("Received key: $keyName")
                val keyValue = keys[keyName]
                if (keyValue == null) {
                    call.respond(HttpStatusCode.BadRequest, false)
                    throw RuntimeException("No such key $keyName")
                }

                call.respond("${keyValue.first}:${keyValue.second}")
            }

            get("/keys/list") {
                println("Received request /keys/list")
                call.respond(keys.entries.joinToString(",") { "${it.key}=${it.value.first}:${it.value.second}" })
                println("Responded with $keys")
            }
        }
    }.start(true)
}