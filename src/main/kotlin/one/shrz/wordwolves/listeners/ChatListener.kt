package one.shrz.wordwolves.listeners

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import one.shrz.wordwolves.GameManager
import one.shrz.wordwolves.Main
import one.shrz.wordwolves.Util

/**
 * Discordサーバーに送信されてきたメッセージを取得するためのリスナ
 */
class ChatListener : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        // "&"から始まるメッセージのみを処理する
        if (event.message.contentRaw.startsWith("&")) {
            // 送信者がスタッフの場合のみ処理する
            if (Util.isStaff(event.member!!))

                // メッセージのスペースより前の部分で動作を分類する
                when (event.message.contentRaw.substringBefore(" ")) {
                    "&delete" -> {
                        GameManager.reset()
                    }
                    "&start" -> {
                        GameManager.start()
                    }
                    "&cteam" -> {
                        GameManager.createTeams(event.message.contentRaw.substringAfter(" ").toInt())
                    }
                    "&reset" -> {
                        GameManager.resetGame()
                    }
                    "&setup" -> {
                        GameManager.setup(event.message)
                    }
                    "&forcefinish" -> {
                        GameManager.forceFinish()
                    }
                    "&next" -> {
                        GameManager.next()
                    }
                    "&status" -> {
                        event.channel.sendMessage("現在のステータス: ${Main.config.status.name}").queue()
                    }
                }
        }
    }
}