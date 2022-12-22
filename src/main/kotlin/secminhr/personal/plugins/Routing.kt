package secminhr.personal.plugins

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
import secminhr.personal.*
import java.lang.Exception
import kotlin.text.toByteArray

const val CHANNEL_SECRET = "CHANNEL_SECRET"

private val validator = LineSignatureValidator(System.getenv(CHANNEL_SECRET).toByteArray())
private val parser = WebhookParser(validator)

const val welcomeMessage = """
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
                .filterNotNull()
                .forEach {
                    when (it) {
                        is FollowEvent -> {
                            onMessage(it)
                            if (!servingMachines.containsKey(it.source.userId)) {
                                servingMachines[it.source.userId] = TOCMachine(it.source.userId)
                            }
                            if (userCustomMachines.containsKey(it.source.userId)) {
                                replyMessageTo(it.source.userId, welcomeMessage to quickReplies("new", "edit", "run", "help"))
                            } else {
                                replyMessageTo(it.source.userId, welcomeMessage to quickReplies("new", "help"))
                            }
                        }
                        is MessageEvent<*> -> {
                            onMessage(it)
                            if (!servingMachines.containsKey(it.source.userId)) {
                                servingMachines[it.source.userId] = TOCMachine(it.source.userId)
                            }
                            (it.message as? TextMessageContent)?.let { message ->
                                servingMachines[it.source.userId]!!.transition(Event.ReceiveText(it.source.userId, message.text))
                            }
                        }
                    }
                }
        }
    }
}
