package one.shrz.wordwolves.discord.type

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ModifyChannelRequest(@Expose val channelId: Long) {
    @SerializedName("name")
    var name: String? = null

    @SerializedName("type")
    var channelType: ChannelType? = null

    @SerializedName("position")
    var channelPosition: Int? = null

    @SerializedName("topic")
    var channelTopic: String? = null

    @SerializedName("nsfw")
    var nsfw: Boolean? = null

    @SerializedName("rate_limit_per_user")
    var userRateLimit: Int? = null

    @SerializedName("bitrate")
    var bitrate: Int? = null

    @SerializedName("user_limit")
    var userLimit: Int? = null

    @SerializedName("parent_id")
    var parentChannelId: String? = null


    enum class ChannelType {
        @SerializedName("0")
        GUILD_TEXT,

        @SerializedName("1")
        DM,

        @SerializedName("2")
        GUILD_VOICE,

        @SerializedName("3")
        GROUP_DM,

        @SerializedName("4")
        GUILD_CATEGORY,

        @SerializedName("5")
        GUILD_NEWS,

        @SerializedName("6")
        GUILD_STORE,

        @SerializedName("10")
        GUILD_NEWS_THREAD,

        @SerializedName("11")
        GUILD_PUBLIC_THREAD,

        @SerializedName("12")
        GUILD_PRIVATE_THREAD,

        @SerializedName("13")
        GUILD_STAGE_VOICE
    }
}
