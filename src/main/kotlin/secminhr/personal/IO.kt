package secminhr.personal

import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.model.ReplyMessage
import com.linecorp.bot.model.action.MessageAction
import com.linecorp.bot.model.event.FollowEvent
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.MessageContent
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.model.message.quickreply.QuickReply
import com.linecorp.bot.model.message.quickreply.QuickReplyItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch

const val CHANNEL_ACCESS_TOKEN = "CHANNEL_ACCESS_TOKEN"
private val lineClient = LineMessagingClient.builder(System.getenv(CHANNEL_ACCESS_TOKEN)).build()

private suspend fun sendMessages(texts: List<String>, replyToken: String) {
    if (texts.size > 5) {
        throw IllegalArgumentException("reply texts size > 5")
    }
    val replyMessage = ReplyMessage(replyToken, texts.map(::TextMessage))

    lineClient.replyMessage(replyMessage).await()
}
private suspend fun sendMessages(messages: Map<String, List<String>>, replyToken: String) {
    if (messages.size > 5) {
        throw IllegalArgumentException("reply texts size > 5")
    }
    if (messages.any { it.value.size > 13 }) {
        throw java.lang.IllegalArgumentException("quick reply size > 13")
    }

    val textMessages = messages.map { (text, quickReply) ->
        val replies = quickReply.map {
            QuickReplyItem
                .builder()
                .action(MessageAction(it, it))
                .build()
        }
        if (replies.isEmpty()) {
            TextMessage(text)
        } else {
            TextMessage(text, QuickReply.items(replies))
        }
    }
    val replyMessage = ReplyMessage(replyToken, textMessages)
    lineClient.replyMessage(replyMessage).await()
}

private val lastReplyToken: MutableMap<String, String> = mutableMapOf()
fun <T: MessageContent> onMessage(event: MessageEvent<T>) {
    lastReplyToken[event.source.userId] = event.replyToken
}
fun onMessage(event: FollowEvent) {
    lastReplyToken[event.source.userId] = event.replyToken
}

private val lineResponseCoroutine = CoroutineScope(Dispatchers.IO + SupervisorJob())
fun replyMessageTo(userId: String, text: String, quickReply: List<String> = listOf()) = replyMessageTo(userId, listOf(text))
fun replyMessageTo(userId: String, vararg text: String) = replyMessageTo(userId, text.toList())
fun replyMessageTo(userId: String, text: List<String>) {
    lastReplyToken[userId]?.let {
        lineResponseCoroutine.launch {
            sendMessages(text, it)
        }
    }
}
fun replyMessageTo(userId: String, vararg message: Pair<String, List<String>>) = replyMessageTo(userId, message.toMap())
fun replyMessageTo(userId: String, message: Map<String, List<String>>) {
    lastReplyToken[userId]?.let {
        lineResponseCoroutine.launch {
            sendMessages(message, it)
        }
    }
}
