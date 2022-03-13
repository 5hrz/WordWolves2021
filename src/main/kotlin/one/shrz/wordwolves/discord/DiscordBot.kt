package one.shrz.wordwolves.discord

import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import one.shrz.wordwolves.discord.type.*
import java.time.LocalDateTime

/**
 * 独自のDiscord Botクライアント
 *
 * @param token 使用するDiscord Botトークン
 * @param encoding APIを叩くときに用いる文字コード
 */
class DiscordBot(var token: String, encoding: String = "UTF-8") {

    val client = OkHttpClient()
    val gson = Gson()
    val mediaType = "application/json; charset=$encoding".toMediaTypeOrNull()

    fun getHeader(): Headers {
        return Headers.headersOf("Content-Type", "application/json", "Authorization", "Bot $token")
    }

    companion object {
        /**
         * DiscordAPIのエンドポイントのprefix
         *
         * API v9を用いる
         */
        const val API_ENDPOINT = "https://discord.com/api/v9"
    }

    /**
     * 指定したURLにPOSTリクエストを送る
     *
     * @param path DiscordAPIの目的のパス
     * @param json リクエストの内容
     * @return Discordサーバーからのレスポンス
     */
    inline fun <reified T> post(path: String, content: Any): T {
        val request = gson.toJson(content).toRequestBody(mediaType)
        return gson.fromJson(
            Request.Builder().url("$API_ENDPOINT${path}").headers(getHeader()).post(request).build()
                .let { client.newCall(it).execute() }.body!!.string(), T::class.java
        )
    }

    /**
     * 指定したURLにGETリクエストを送る
     *
     * @param path DiscordAPIの送信先のパス
     * @return Discordサーバーからのレスポンス
     */
    inline fun <reified T> get(path: String): T {
        try {
            return gson.fromJson(
                Request.Builder().url("$API_ENDPOINT${path}").headers(getHeader()).get().build()
                    .let { client.newCall(it).execute() }.body!!.string(), T::class.java
            )
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    /**
     * 指定したURLにPUTリクエストを送る
     *
     * @param path DiscordAPIの送信先のパス
     * @param content 送信する内容のインスタンス
     * @return Discordサーバーからのレスポンス
     */
    inline fun <reified T> put(path: String, content: Any): T {
        val request = gson.toJson(content).toRequestBody(mediaType)
        return gson.fromJson(
            Request.Builder().url("$API_ENDPOINT${path}").headers(getHeader()).put(request).build()
                .let { client.newCall(it).execute() }.body!!.string(), T::class.java
        )
    }

    /**
     * 指定したURLにDELETEリクエストを送る
     *
     * @param path DiscordAPIの送信先のパス
     * @param content 送信する内容のインスタンス
     * @return Discordサーバーからのレスポンス
     */
    inline fun <reified T> delete(path: String, content: Any): T {
        val request = gson.toJson(content).toRequestBody(mediaType)
        return gson.fromJson(
            Request.Builder().url("$API_ENDPOINT${path}").headers(getHeader()).delete(request).build()
                .let { client.newCall(it).execute() }.body!!.string(), T::class.java
        )
    }

    /**
     * 指定したURLにPATCHリクエストを送る
     *
     * @param path DiscordAPIの送信先のパス
     * @param content 送信する内容のインスタンス
     * @return Discordサーバーからのレスポンス
     */
    inline fun <reified T> patch(path: String, content: Any): T {
        val request = gson.toJson(content).toRequestBody(mediaType)
        return gson.fromJson(
            Request.Builder().url("$API_ENDPOINT${path}").headers(getHeader()).patch(request).build()
                .let { client.newCall(it).execute() }.body!!.string(), T::class.java
        )
    }

    /**
     * 指定したDiscordサーバーのスケジュールされたイベントを取得する
     *
     * @param guildId 取得先のDiscordサーバーのID
     *
     * @return そのサーバーのスケジュール[GuildScheduledEvent]の[Array]
     */
    fun getGuildScheduledEvents(guildId: String): Array<GuildScheduledEvent> {
        return get("/guilds/${guildId}/scheduled-events")
    }

    /**
     * 指定したDiscordサーバーにスケジュールイベントを追加する
     */
    fun createGuildScheduledEvent(guildId: String, request: GuildScheduledEventRequest): GuildScheduledEvent {
        return post("/guilds/${guildId}/scheduled-events", request)
    }

    /**
     * 指定したDiscordサーバーから指定したIDのスケジュールイベントを取得する
     */
    fun getGuildScheduledEvent(guildId: String, eventId: String): GuildScheduledEvent {
        return get("/guilds/${guildId}/scheduled-events/${eventId}")
    }

    /**
     * 指定したDiscordサーバーの指定したIDのスケジュールイベントを削除する
     */
    fun deleteGuildScheduledEvent(guildId: String, eventId: String) {
        delete<Any>("/guilds/${guildId}/scheduled-events/${eventId}", Any())
    }

    /**
     * 指定したDiscordサーバーのスケジュールイベントの内容を更新する
     */
    fun updateGuildScheduledEvent(guildId: String, request: GuildScheduledEvent): GuildScheduledEvent {
        return patch("/guilds/${guildId}/scheduled-events/${request.id}", request)
    }

    /**
     * 指定したDiscordサーバーの指定したスケジュールイベントの通知をonにしているユーザーを取得する
     *
     * @param guildId 取得先のDiscordサーバーのID
     * @param eventId 取得先のスケジュールイベントのID
     * @param limit 取得する最大数
     * @param withMember ?
     * @param before 指定した時間よりも前のユーザーを取得する
     * @param after 指定した時間よりも後のユーザーを取得する
     */
    fun getGuildScheduledEventSubscribedUsers(
        guildId: String,
        eventId: String,
        limit: Int = 100,
        withMember: Boolean = false,
        before: LocalDateTime? = null,
        after: LocalDateTime? = null
    ): Array<User> {
        return get("/guilds/${guildId}/scheduled-events/${eventId}?limit=${limit}&with_member=${withMember}${if (before != null) "&before=${before}" else ""}${if (after != null) "&after=${after}" else ""}")
    }

    /**
     * 指定したDiscordサーバーにおいて、指定したユーザーのVCを移動させる
     *
     * @param guildId 目的のDiscordサーバーのID
     * @param userId 移動するユーザーID
     * @param request [ModifyChannelRequest]
     */
    fun moveVoiceChannel(guildId: String, userId: String, request: ModifyUserVoiceStateRequest) {
        println(patch<Any>("/guilds/${guildId}/members/${userId}", request))
    }

    /**
     * 指定したDiscordサーバーにチャンネルを作成する
     *
     * @param guildId 目的のDiscordサーバーのID
     * @param request 作成する内容の[CreateGuildChannel.Request]
     */
    fun createGuildChannel(guildId: String, request: CreateGuildChannel.Request): CreateGuildChannel.Response {
        return post("/guilds/${guildId}/channels", request)
    }

    /**
     * チャンネルの設定を変更する
     */
    fun modifyChannel(request: ModifyChannelRequest) {
        println(patch<Any>("/channels/${request.channelId}", request))
    }

}