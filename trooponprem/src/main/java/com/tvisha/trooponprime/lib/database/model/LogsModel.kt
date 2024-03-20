package com.tvisha.troopmessenger.FileDeck.Model

data class LogsModel (
    var message:String? = null,
    var created_at:String?= null,
    var isHeader:Int =0,
    var closeView:Int =0
        )