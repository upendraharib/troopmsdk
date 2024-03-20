package com.tvisha.trooponprime.lib

import org.json.JSONObject

interface MessengerOnSocketListeners {
    fun messengerOnSocketConnected()
    fun messengerOnSocketDisconnected()
    fun messengerOnSocketConnectError()
    fun messengerOnSocketError()
    fun messengerOnSocketMessageSent()
    fun messengerOnSocketMessageReceive()
    fun messengerOnSocketErrorWhileSendingMessage()
    fun messengerOnSocketAccountVerified()
    fun messengerOnSocketUnAuthorized()
    fun messengerOnSocketUserStatusOnline()
    fun messengerOnSocketUserStatusOffline()
    fun messengerOnSocketUserStatusDND()
    fun messengerOnSocketUserStatusOptionOne()
    fun messengerOnSocketUserStatusOptionTwo()
    fun messengerOnSocketUserStatusOptionThree()
    fun messengerOnSocketTypingStatus(jsonObject: JSONObject)
    fun messengerOnSocketUserLastSeen(jsonObject: JSONObject)
    fun messengerOnSocketUserPresence(jsonObject: JSONObject)
}