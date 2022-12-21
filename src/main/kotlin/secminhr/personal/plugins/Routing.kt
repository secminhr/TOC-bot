package secminhr.personal.plugins

import com.linecorp.bot.model.event.EventMode
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.parser.LineSignatureValidator
import com.linecorp.bot.parser.WebhookParser
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import secminhr.personal.*
import java.lang.Exception
import kotlin.text.toByteArray

@Serializable
data class WebhookEventObjet(val type: String, val replyToken: String? = null)

const val CHANNEL_SECRET = "CHANNEL_SECRET"

private val validator = LineSignatureValidator(System.getenv(CHANNEL_SECRET).toByteArray())
private val parser = WebhookParser(validator)

fun Application.configureRouting() {

    // Starting point for a Ktor app:
    routing {
        post("/") {
            val request = try {
                parser.handle(call.request.header("x-line-signature"), call.receive())
            } catch (e: Exception) {
                call.respondNullable(HttpStatusCode.Forbidden, "null")
                return@post
            }

            request.events
                .mapNotNull { it as? MessageEvent<*> }
                .filter { it.mode == EventMode.ACTIVE }
                .forEach { event ->
                    onMessage(event)
                    if (!servingMachines.containsKey(event.source.userId)) {
//                        servingMachines[event.source.userId] = createEchoMachine(event.source.userId)
                        servingMachines[event.source.userId] = TOCMachine(event.source.userId)
                    }
                    (event.message as? TextMessageContent)?.let {
                        servingMachines[event.source.userId]!!.transition(Event.ReceiveText(event.source.userId, it.text))
                    }
                }
        }
    }
}
