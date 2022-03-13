package one.shrz.wordwolves.discord.type

import com.google.gson.annotations.SerializedName

class ModifyUserVoiceStateRequest {

    @SerializedName("channel_id")
    var channelId: Long = -1

}