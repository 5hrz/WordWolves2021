package one.shrz.wordwolves.discord.type

import com.google.gson.annotations.SerializedName

/**
 * チャンネルを作成する際のリクエスト、レスポンス
 */
class CreateGuildChannel {
    class Request {
        @SerializedName("name")
        lateinit var name: String

        @SerializedName("type")
        lateinit var type: ChannelType

        @SerializedName("topic")
        var topic: String? = null

        @SerializedName("bitrate")
        var bitrate: Int? = null

        @SerializedName("user_limit")
        var userLimit: Int? = null

        @SerializedName("rate_limit_per_user")
        var rateLimitPerUser: Int? = null

        @SerializedName("position")
        var position: Int? = null

        @SerializedName("parent_id")
        var parentId: Long? = null

        @SerializedName("nsfw")
        var nsfw: Boolean = false
    }

    class Response {
        @SerializedName("id")
        lateinit var id: String

        @SerializedName("name")
        lateinit var name: String

        @SerializedName("type")
        lateinit var type: ChannelType

        @SerializedName("topic")
        var topic: String? = null

        @SerializedName("bitrate")
        var bitrate: Int? = null

        @SerializedName("user_limit")
        var userLimit: Int? = null

        @SerializedName("rate_limit_per_user")
        var rateLimitPerUser: Int? = null

        @SerializedName("position")
        var position: Int? = null

        @SerializedName("parent_id")
        var parentId: Long? = null

        @SerializedName("nsfw")
        var nsfw: Boolean = false
    }

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
