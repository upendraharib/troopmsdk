package com.tvisha.trooponprime.lib.apiResponse

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


class FileDeckApiResponse {
    @SerializedName("success")
    var success: Boolean? = null


    @SerializedName("workspace_id")
    var workspaceId: String? = null

}
class Data {
    @SerializedName("message_id")
    var messageId: Int? = null

    @SerializedName("user_id")
    @Expose
    var userId: Int? = null

    @SerializedName("comments")
    @Expose
    var comments: String? = null

    @SerializedName("tags")
    @Expose
    var tags: String? = null

    @SerializedName("filename")
    @Expose
    var filename: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null
}