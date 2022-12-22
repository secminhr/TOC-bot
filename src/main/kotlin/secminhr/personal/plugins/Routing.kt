package secminhr.personal.plugins

import com.linecorp.bot.model.event.EventMode
import com.linecorp.bot.model.event.FollowEvent
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

const val WelcomeMessage = """
Welcome to bot-bot.
This bot allows you to create a state machine and run it directly in your chat room!
Available commands are provided to you with the little babble at the bottom of the chat.
"""

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
                .mapNotNull { it as? FollowEvent }
                .forEach {
                    onMessage(it)
                    if (!servingMachines.containsKey(it.source.userId)) {
                        servingMachines[it.source.userId] = TOCMachine(it.source.userId)
                    }
                    replyMessageTo(it.source.userId, "")
                }

            request.events
                .mapNotNull { it as? MessageEvent<*> }
                .filter { it.mode == EventMode.ACTIVE }
                .forEach { event ->
                    onMessage(event)
                    if (!servingMachines.containsKey(event.source.userId)) {
                        servingMachines[event.source.userId] = TOCMachine(event.source.userId)
                    }
                    (event.message as? TextMessageContent)?.let {
                        servingMachines[event.source.userId]!!.transition(Event.ReceiveText(event.source.userId, it.text))
                    }
                }
        }
    }
}
