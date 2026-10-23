package com.mindrill.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChatMessage(val text: String, val mine: Boolean)

data class Conversation(
    val id: String,
    val name: String,
    val productName: String?,
    val messages: List<ChatMessage>,
    val lastTime: String,
    val unread: Int
)

fun sampleConversations(): List<Conversation> = listOf(
    Conversation(
        "Ali Ibrahim", "Ali Ibrahim", "Metal Detector CTX3030",
        listOf(
            ChatMessage("Hello, I am interested!", true),
            ChatMessage("Hi! Yes, it is available. Want to see photos?", false),
            ChatMessage("Yes, please", true),
            ChatMessage("Sent 3 photos. Price is negotiable.", false)
        ), "9:30", 2
    ),
    Conversation(
        "Rahim Traders", "Rahim Traders", "Vibrating Sluice Box",
        listOf(ChatMessage("Can you do 400 USD?", true), ChatMessage("I can offer 450 USD.", false)),
        "Mon", 0
    ),
    Conversation(
        "Sylhet Gear Market", "Sylhet Gear Market", "Conveyor Belt 10 m",
        listOf(ChatMessage("How long is delivery?", true), ChatMessage("Delivery takes 3 days.", false)),
        "Sun", 1
    ),
    Conversation(
        "Karim Drilling Co.", "Karim Drilling Co.", "Diamond Core Drill Rig",
        listOf(ChatMessage("Can we meet tomorrow?", true), ChatMessage("Thanks, see you tomorrow!", false)),
        "Fri", 0
    ),
    Conversation(
        "Nasrin Supplies", "Nasrin Supplies", "Helmet with Headlamp",
        listOf(ChatMessage("Can you send photos?", true), ChatMessage("Photos sent.", false)),
        "Thu", 0
    )
)

@Composable
fun ChatsTab(
    modifier: Modifier = Modifier,
    conversations: List<Conversation>,
    openId: String?,
    onOpen: (String?) -> Unit,
    onSend: (String, String) -> Unit
) {
    val open = conversations.firstOrNull { it.id == openId }
    if (open != null) {
        BackHandler { onOpen(null) }
        ConversationScreen(
            modifier = modifier,
            conversation = open,
            onBack = { onOpen(null) },
            onSend = { text -> onSend(open.id, text) }
        )
    } else {
        ChatList(modifier = modifier, conversations = conversations, onOpen = { onOpen(it) })
    }
}

@Composable
private fun Avatar(name: String, size: Int = 48) {
    val bg = if (name.length % 2 == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val fg = if (name.length % 2 == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape).background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(name.first().toString(), color = fg, fontWeight = FontWeight.Bold, fontSize = (size / 2.5).sp)
    }
}

@Composable
private fun ChatList(modifier: Modifier, conversations: List<Conversation>, onOpen: (String) -> Unit) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            "Chats",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(conversations, key = { it.id }) { c ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(c.id) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(c.name)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(c.name, fontWeight = FontWeight.Bold)
                        Text(
                            c.messages.lastOrNull()?.text ?: "",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            c.lastTime,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (c.unread > 0) {
                            Spacer(Modifier.height(4.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                Text(c.unread.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationScreen(
    modifier: Modifier,
    conversation: Conversation,
    onBack: () -> Unit,
    onSend: (String) -> Unit
) {
    var draft by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(conversation.messages.size) {
        if (conversation.messages.isNotEmpty()) {
            listState.animateScrollToItem(conversation.messages.lastIndex)
        }
    }

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Avatar(conversation.name, size = 40)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(conversation.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "Online",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        if (conversation.productName != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Build, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(conversation.productName, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(conversation.messages) { m ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (m.mine) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (m.mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = if (m.mine) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            m.text,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            color = if (m.mine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                placeholder = { Text("Type a message") },
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = {
                    val t = draft.trim()
                    if (t.isNotEmpty()) {
                        onSend(t)
                        draft = ""
                    }
                },
                enabled = draft.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}