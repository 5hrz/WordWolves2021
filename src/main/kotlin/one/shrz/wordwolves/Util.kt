package one.shrz.wordwolves

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.ButtonStyle
import net.dv8tion.jda.api.interactions.components.Component
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu

/**
 * 便利な機能をまとめるところ
 */
object Util {

    /**
     * ゲームのスタッフかどうかを判定する
     *
     * @param member 判定したい[Member]
     */
    fun isStaff(member: Member): Boolean {
        // メンバーに付与されているロールのうち、いずれかがスタッフロールもしくは管理者ロールだった場合はtrueを返す
        return member.roles.any { it.idLong == Main.config.staffRoleId || it.idLong == Main.config.gameAdmin }
    }

    /**
     * ゲームの管理パネルを更新する
     */
    fun updatePanel() {
        // ゲームパネルのチャンネルを取得
        Discord.guild.getTextChannelById(Main.config.panelCh)!!
            .editMessageById(Main.config.panelId, MessageBuilder().apply { // パネルのメッセージを編集する
                setEmbeds(
                    listOf(
                        EmbedBuilder().apply {
                            setTitle("Manage Panel")
                        }.build()
                    )
                )
                setActionRows(
                    ActionRow.of(
                        getNextComponent() // 次に進むためのボタンを取得する
                    )
                )
            }.build()).queue() // 送信
    }

    /**
     * 現在のゲーム進捗状況に合わせたゲームパネルに表示する[Component]を取得する
     */
    fun getNextComponent(): Component {
        // 現在のゲーム進捗状況に合わせて返す内容を変える
        return when (Main.config.status) {
            // セットアップ前の場合
            Config.GameStatus.BEFORE_SETUP -> {
                Button.of(ButtonStyle.PRIMARY, "ww_guild_setup", "セットアップ")
            }

            // チーム作成前の場合
            Config.GameStatus.WAITING_TEAM_CREATE -> {
                // ドロップダウンメニューを作成

                SelectionMenu.create("ww_team_create").apply {
                    // 1～10までの選択肢を追加
                    addOption("1", "1")
                    addOption("2", "2")
                    addOption("3", "3")
                    addOption("4", "4")
                    addOption("5", "5")
                    addOption("6", "6")
                    addOption("7", "7")
                    addOption("8", "8")
                    addOption("9", "9")
                    addOption("10", "10")

                    placeholder = "チームの最大人数"
                }.build()
            }

            // ゲーム開始前の場合
            Config.GameStatus.WAITING_START -> {
                Button.of(ButtonStyle.PRIMARY, "ww_game_start", "ゲームを開始")
            }

            // ゲーム進行中の場合
            Config.GameStatus.RUNNING -> {
                Button.of(ButtonStyle.DANGER, "ww_force_finish", "ゲームを強制終了")
            }

            // 結果発表中の場合
            Config.GameStatus.WAITING_NEXT -> {
                Button.of(ButtonStyle.PRIMARY, "ww_game_next", "次のゲームに進む")
            }
        }
    }
}