import android.util.Log
import com.ari.drup.data.community.Chat
import com.ari.drup.data.community.Messages
import com.ari.drup.data.community.MessagesResponse
import com.ari.drup.viewmodels.GroupChatViewModel
import com.ari.drup.viewmodels.MainChatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import com.ari.drup.viewmodels.SERVER_URL
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import java.time.Instant

class ChatWebSocket(
    private val token: String,
    private val roomId: String,
    private val viewModel: GroupChatViewModel
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    fun connect() {
        val request = Request.Builder()
            .url(SERVER_URL) // declared in your ViewModel
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("ChatWebSocket", "Connected ✅")
                joinRoom()
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d("ChatWebSocket", "Message: $text")

                try {
                    val json = JSONObject(text)
                    when (json.getString("type")) {
                        "chat_history" -> {
                            Log.d("ChatWebSocket","load chat")
                            val messagesResponse = Gson().fromJson(json.toString(), MessagesResponse::class.java)
                            val chats = messagesResponse.messages // this is List<Chat>
                            val sortedChats = chats.messages.sortedBy { chat ->
                                try {
                                    Instant.parse(chat.timestamp)
                                } catch (e: Exception) {
                                    Instant.MIN
                                }
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                viewModel.setMessages(Messages(
                                    messages = sortedChats.reversed(),
                                    nextCursor = messagesResponse.messages.nextCursor
                                ))
                            }
                        }
                        "message" -> {
                            val chat = Gson().fromJson(json.toString(), Chat::class.java)

                            CoroutineScope(Dispatchers.Main).launch {
                                viewModel.addChat(chat)
                            }
                        }
                        "announcement"->{
                            Log.d("ChatWebSocket","Announcement")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChatWebSocket", "Parse error: ${e.message}")
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("ChatWebSocket", "Connection failed: ${t.message}")
            }
            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                Log.d("ChatWebSocket", "Closing: $code / $reason")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d("ChatWebSocket", "Closed: $code / $reason")
            }
        })
    }

    private fun joinRoom() {
        Log.d("ChatWebSocket","Sending for join with $roomId, $token")
        val joinPayload = """
            {
              "type": "join",
              "roomId": "$roomId",
              "token": "$token"
            }
        """.trimIndent()
        webSocket?.send(joinPayload)
        loadChat()
    }

    fun loadChat() {
        Log.d("ChatWebSocket","Sending for load chats")
        val payload = """{"type": "load_chat"}"""
        webSocket?.send(payload)
    }

    fun sendMessage(text: String, userId: String) {
        Log.d("ChatWebSocket","Sending text to server")
        val payload = """
            {
              "type": "message",
              "userId": "$userId",
              "text": "$text",
              "timestamp": ${System.currentTimeMillis()}
            }
        """.trimIndent()
        webSocket?.send(payload)
        loadChat()
    }

    fun disconnect() {
        webSocket?.close(1000, "User left")
    }
}

