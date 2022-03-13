package one.shrz.wordwolves.listeners

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import one.shrz.wordwolves.GameManager
import one.shrz.wordwolves.Main
import one.shrz.wordwolves.Util
import java.awt.Color

/**
 * インタラクションを検知するためのリスナ
 */
class InteractionListener : ListenerAdapter() {

    /**
     * ボタンをクリックしたとき
     */
    override fun onButtonClick(event: ButtonClickEvent) {

        // ボタンをクリックしたのがスタッフではない場合は処理を停止
        if (Util.isStaff(event.member!!).not()) return

        // クリックしたボタンのIDが"ww"から始まっていない場合は処理を停止
        if (!event.componentId.startsWith("ww")) return

        // クリックしたボタンのIDによって処理を振り分ける
        when (event.componentId) {
            "ww_guild_setup" -> {
                // Discordサーバーのセットアップを行なう
                GameManager.setup(event.message)

                // 設定が完了したことを表示
                event.reply(MessageBuilder().apply {
                    setEmbeds(
                        listOf(
                            EmbedBuilder().apply {
                                setTitle("設定が完了しました。")
                                setColor(Color.GREEN)
                            }.build()
                        )
                    )
                }.build()).setEphemeral(true).queue()
            }

            "ww_game_start" -> {
                // ゲームを開始する
                GameManager.start()

                // ゲームを開始したことを表示
                event.reply(MessageBuilder().apply {
                    setEmbeds(
                        listOf(
                            EmbedBuilder().apply {
                                setTitle("ゲームを開始しました。")
                                setColor(Color.GREEN)
                            }.build()
                        )
                    )
                }.build()).setEphemeral(true).queue()
            }

            "ww_force_finish" -> {
                // ゲームを強制終了する
                GameManager.forceFinish()

                // ゲームを強制終了したことを表示
                event.reply(MessageBuilder().apply {
                    setEmbeds(
                        listOf(
                            EmbedBuilder().apply {
                                setTitle("ゲームを強制終了しました。")
                                setColor(Color.RED)
                            }.build()
                        )
                    )
                }.build()).setEphemeral(true).queue()
            }

            "ww_game_next" -> {
                // 次のゲームに進める
                GameManager.next()

                // 次のゲームに進めたことを表示
                event.reply(MessageBuilder().apply {
                    setEmbeds(
                        listOf(
                            EmbedBuilder().apply {
                                setTitle("ゲームを進行します。")
                                setColor(Color.GREEN)
                            }.build()
                        )
                    )
                }.build()).setEphemeral(true).queue()
            }
        }

        // ゲームパネルを更新する
        Util.updatePanel()
    }

    /**
     * ドロップダウンメニューを選択した時
     */
    override fun onSelectionMenu(event: SelectionMenuEvent) {

        // 選択したのがスタッフではない場合は処理を停止
        if (Util.isStaff(event.member!!).not()) return

        // 選択したメニューのIDが"ww"から始まっていない場合は処理を停止
        if (!event.componentId.startsWith("ww")) return

        // 選択したメニューによって処理を割り振る
        when (event.componentId) {
            "ww_team_create" -> {
                // チームを作成する
                // 人数は選択した値をそのまま用いる
                GameManager.createTeams(event.selectedOptions?.firstOrNull()?.value!!.toInt())

                // チームを作成したことを表示
                event.reply(MessageBuilder().apply {
                    setEmbeds(
                        listOf(
                            EmbedBuilder().apply {
                                setTitle("チームを作成しました。")
                                setColor(Color.GREEN)
                            }.build()
                        )
                    )
                }.build()).setEphemeral(true).queue()
            }
        }

        // ゲームパネルを更新する　
        Util.updatePanel()
    }
}