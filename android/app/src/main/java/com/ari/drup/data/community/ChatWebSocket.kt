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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

class ChatWebSocket(
    private val token: String,
    private val roomId: String,
    private val viewModel: GroupChatViewModel
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // Use 0 for no timeout on read
        .pingInterval(20, TimeUnit.SECONDS)     // Keep the connection alive
        .build()
        private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        private var isConnecting = AtomicBoolean(false) // Prevents multiple connect calls
        private var retryDelayMs = 1000L
        private val maxRetryDelayMs = 16000L

    fun connect() {


        if (isConnecting.getAndSet(true)) {
            Log.d("ChatWebSocket", "Connection attempt already in progress.")
            return
        }
        val request = Request.Builder()
            .url(SERVER_URL) // declared in your ViewModel
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("ChatWebSocket", "Connected successfully.")
                isConnecting.set(false)
                retryDelayMs = 1000L // Reset delay on successful connection
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
                            viewModel.setChatLoading(false)
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
                isConnecting.set(false) // Allow another connect attempt
                webSocket = null

                // ✅ SOLUTION 2: Exponential backoff retry logic
                scope.launch {
                    Log.d("ChatWebSocket", "Retrying connection in ${retryDelayMs / 1000} seconds.")
                    delay(retryDelayMs)
                    retryDelayMs = (retryDelayMs * 2).coerceAtMost(maxRetryDelayMs)
                    connect() // Attempt to reconnect
                }
            }
            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                Log.d("ChatWebSocket", "Closing: $code / $reason")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d("ChatWebSocket", "Closed: $code / $reason")
                isConnecting.set(false)
                webSocket = null
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
        viewModel.setChatLoading(true)
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
        val sent = webSocket?.send(payload)

        if (sent != true) {
            Log.e("ChatWebSocket", "Failed to send message. Socket is not connected.")
            // You could add the message to a "pending" queue and try resending on reconnect
            // Or show an error to the user
        }
    }

    fun disconnect() {
        scope.coroutineContext.cancelChildren() // Cancel any pending retries
        webSocket?.close(1000, "User left")
        webSocket = null
    }
}

