package com.dhbw.brainstorm.websocket

import com.dhbw.brainstorm.websocket.model.ReceiveMessage
import com.dhbw.brainstorm.websocket.model.SubscribeMessage
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Flowable

interface RoomWebSocketService {

    @Send
    fun sendSubscribe(subscribe: SubscribeMessage)

    @Receive
    fun observeEvent(): Flowable<WebSocket.Event>

    @Receive
    fun observeMessage(): Flowable<ReceiveMessage>
}
