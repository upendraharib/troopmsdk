package com.tvisha.trooponprime.lib.listeneres

import org.json.JSONObject

interface CallBackListener {
    fun onSuccess(jsonObjet:JSONObject)
    fun onFailure(jsonObjet: JSONObject)
}