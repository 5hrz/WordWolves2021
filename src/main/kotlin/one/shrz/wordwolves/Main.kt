package one.shrz.wordwolves

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import one.shrz.wordwolves.discord.DiscordBot
import one.shrz.wordwolves.listeners.ChatListener
import one.shrz.wordwolves.listeners.InteractionListener
import java.io.File
import java.nio.charset.Charset

/**
 * コンフィグデータを保持するクラス
 */
class Main {
    companion object {
        lateinit var config: Config // botの設定情報
        lateinit var dataSet: DataSet // ワードウルフのデータセットt
        val gson = Gson() // com.google.gson

        /**
         * 設定を読み込む
         */
        fun load() {
            // コンフィグファイルを取得
            val configFile = File("config.json")
            if (!configFile.exists()) { // コンフィグファイルが存在しない場合の処理
                configFile.createNewFile() // ファイルを作成
                // ファイルにデフォルトコンフィグを書き込む
                configFile.writeText(
                    GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(Config()), // フォーマットしておく
                    Charset.forName("UTF-8") // 文字コードをUTF-8に
                )
            }

            // gsonを用いてファイルの中身をデシリアライズ
            // 文字コードはUTF-8
            config = gson.fromJson(configFile.readText(Charset.forName("UTF-8")), Config::class.java)

            // データセットファイルを取得
            val dataSetFile = File("dataset.json")
            if (!dataSetFile.exists()) { // データセットファイルが存在しない場合の処理
                dataSetFile.createNewFile() // ファイルを作成
                // ファイルに初期データを書き込む
                dataSetFile.writeText(
                    GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(DataSet().seed()),
                    Charset.forName("UTF-8") // 文字コードはUTF-8
                )
            }

            // gsonを用いてファイルの中身をデシリアライズ
            dataSet = gson.fromJson(dataSetFile.readText(Charset.forName("UTF-8")), DataSet::class.java)
        }

        /**
         * 設定を保存
         */
        fun save() {
            // コンフィグファイルを取得
            val configFile = File("config.json")

            // ファイルに書き込む
            configFile.writeText(gson.toJson(config))
        }
    }

}

/**
 * Discord関連をまとめたオブジェクト
 */
object Discord {
    /**
     * JDA製のbot
     */
    val bot: JDA = JDABuilder.createDefault(Main.config.tokens[0]) // JDAのbotのインスタンスをビルダーから作成する。トークンの１番目を用いる
        .enableIntents( // インテントを有効化
            GatewayIntent.GUILD_MEMBERS, // ギルドメンバーの情報
            GatewayIntent.GUILD_PRESENCES // ギルドの設定の情報
        )
        .enableCache(CacheFlag.ROLE_TAGS) // ロールのキャッシュを有効化
        .addEventListeners(ChatListener(), InteractionListener()) // チャット、インタラクションのリスナを追加
        .build()

    /**
     * メイン会場となるDiscordサーバー
     */
    lateinit var guild: Guild

    /**
     * 自作のDiscord Botクライアント
     */
    val origin = DiscordBot(Main.config.tokens.getOrNull(1) ?: Main.config.tokens[0]) // トークンの2番目を用いるが、存在しない場合は1番目を用いる
}

/**
 * コンフィグファイルのオブジェクト
 */
class Config {
    /**
     * Discord Botのトークン
     */
    var tokens: List<String> = mutableListOf()

    /**
     * ゲームの進行状況
     * @see GameStatus
     */
    var status: GameStatus = GameStatus.BEFORE_SETUP

    /**
     * ゲームのスタッフにつけるロールのID
     */
    var staffRoleId: Long = -1

    /**
     * ゲームスタッフ専用カテゴリのID
     */
    var staffCatId: Long = -1

    /**
     * ゲームに参加している[Team]の[MutableList]
     */
    var teams: MutableList<Team> = mutableListOf()

    /**
     * ゲームに使用するDiscordサーバーのID
     */
    var guild: Long = -1

    /**
     * ゲームに使用するメイン会場のVCのID
     */
    var mainVC: Long = -1

    /**
     * ゲームの管理者につけるロールのID
     */
    var gameAdmin: Long = -1

    /**
     * ゲームの参加者につけるロールのID
     */
    var gameParticipant: Long = -1

    /**
     * チームのVCのカテゴリのID
     */
    var teamCat: Long = -1

    /**
     * 多数派につけるロールのID
     */
    var trueRole: Long = -1

    /**
     * 少数派につけるロールのID
     */
    var falseRole: Long = -1

    /**
     * 多数派に見せるチャンネルのID
     */
    var trueChannelId: Long = -1

    /**
     * 少数派に見せるチャンネルのID
     */
    var falseChannelId: Long = -1

    /**
     * ダミーチャンネルのID
     */
    var dummyChannelId: Long = -1

    /**
     * ゲームパネルを設置するチャンネルのID
     */
    var panelCh: Long = -1

    /**
     * ゲームパネルのメッセージID
     */
    var panelId: Long = -1

    /**
     * チームに設定可能なチャンネルのマップ
     *
     * 構造
     * Map<カテゴリID, List<Channel<VCのChannelID, RoleのID>>>
     */
    var channels: MutableMap<Long, MutableList<Channel>> = mutableMapOf()

    /**
     * お知らせを流すためのチャンネルのID
     */
    var announceChannel: Long = -1

    /**
     * 進行中のゲームの多数派のお題
     */
    var currentTrue: String = ""

    /**
     * 進行中のゲームの少数派のお題
     */
    var currentFalse: String = ""

    /**
     * ゲームのチームのオブジェクト
     */
    class Team(
        /**
         * チームの人数
         */
        var size: Int = -1,

        /**
         * チームのID
         */
        var id: Int = -1,

        /**
         * チームの名前
         */
        var name: String = "",

        /**
         * チームのVCのチャンネルID
         */
        var vcId: Long = -1,

        /**
         * チームのロールのID
         */
        var roleId: Long = -1,

        /**
         * チームに所属するメンバーのID
         */
        var members: MutableList<Long> = mutableListOf(),

        /**
         * このチームの少数派のユーザーのID
         */
        var wolf: Long = -1
    )

    /**
     * VCとロールをまとめるためのクラス
     */
    class Channel(var voice: Long = -1, var role: Long = -1)

    /**
     * ゲームの進行状況を表す列挙型
     */
    enum class GameStatus {
        BEFORE_SETUP,
        WAITING_TEAM_CREATE,
        WAITING_START,
        RUNNING,
        WAITING_NEXT
    }

    /**
     * ゲームの次の進行状況を取得する
     */
    fun getNextStatus(): GameStatus {
        return when (status) {
            GameStatus.BEFORE_SETUP -> GameStatus.WAITING_TEAM_CREATE
            GameStatus.WAITING_TEAM_CREATE -> GameStatus.WAITING_START
            GameStatus.WAITING_START -> GameStatus.RUNNING
            GameStatus.RUNNING -> GameStatus.WAITING_NEXT
            GameStatus.WAITING_NEXT -> GameStatus.WAITING_TEAM_CREATE
        }
    }

    /**
     * ゲームの進行状況を１つ進める
     */
    fun setStatusNext() {
        status = when (status) {
            GameStatus.BEFORE_SETUP -> GameStatus.WAITING_TEAM_CREATE
            GameStatus.WAITING_TEAM_CREATE -> GameStatus.WAITING_START
            GameStatus.WAITING_START -> GameStatus.RUNNING
            GameStatus.RUNNING -> GameStatus.WAITING_NEXT
            GameStatus.WAITING_NEXT -> GameStatus.WAITING_TEAM_CREATE
        }
    }
}

/**
 * ゲームのお題をまとめるクラス
 *
 * @param words お題の[MutableList]
 */
class DataSet(val words: MutableList<WordSet> = mutableListOf()) {

    /**
     * 多数派、少数派、ダミーをまとめるためのクラス
     *
     * @param trueWord 多数派のワード
     * @param falseWord 少数派のワード
     * @param dummy ダミーのワード
     */
    class WordSet(
        var trueWord: String = "★",
        var falseWord: String = "☆",
        var dummy: String? = null
    ) {
        override fun toString(): String {
            return "{correct: ${trueWord}, incorrect: ${falseWord}, dummy: ${dummy}}"
        }
    }

    override fun toString(): String {
        return words.joinToString(",") { it.toString() }
    }

    /**
     * データセットの初期値を設定する
     */
    fun seed(): DataSet {
        words.add(WordSet("抹茶", "緑茶", "ほうじ茶"))
        return this
    }
}

fun main(args: Array<String>) {
    Main.load()

    // Discord Botの読み込みが終わるまで待機
    Discord.bot.awaitReady()

    // ゲームを行なうDiscordサーバーを取得
    Discord.guild = Discord.bot.getGuildById(Main.config.guild)!!

    // Botが終了したときにデータを保存するようにShutdownHookを追加
    Runtime.getRuntime().addShutdownHook(Thread {
        Main.save()
    })

    // セットアップが完了していればゲームの管理パネルのアップデートを行なう
    if (Main.config.status != Config.GameStatus.BEFORE_SETUP) Util.updatePanel()

}