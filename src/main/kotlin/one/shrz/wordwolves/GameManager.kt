package one.shrz.wordwolves

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import one.shrz.wordwolves.discord.type.ModifyChannelRequest
import one.shrz.wordwolves.discord.type.ModifyUserVoiceStateRequest
import java.awt.Color
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayDeque
import kotlin.concurrent.schedule
import kotlin.math.ceil

/**
 * ゲームシステムのコア部分
 */
object GameManager {

    /**
     * Discordサーバーの初期設定を行なう
     */
    fun setup(replyTo: Message?) {
        // すでに設定が行われている場合は処理を行わない
        // また、その場合はメッセージを送信する
        if (Main.config.status != Config.GameStatus.BEFORE_SETUP) {
            replyTo!!.reply(MessageBuilder().apply {
                setContent("すでにセットアップ済みです。")
            }.build()).queue()
            return
        }

        /**
         * 現在行っている動作を示すためのメッセージ
         */
        var message = replyTo?.replyEmbeds(EmbedBuilder().apply {
            setDescription("運営用ロール・チャンネルを作成中")
        }.build())?.complete()

        // 運営用のロールを作成
        Discord.guild.createRole().apply {
            setColor(Color.RED) // 色を赤色に設定
            setName("●") // ロールの名前を"●"に設定
            setPermissions(Permission.ADMINISTRATOR) // 管理者権限を付与
        }.complete().also { role ->
            // ロールを作成した後にコンフィグに作成したロールのIDを設定する
            Main.config.gameAdmin = role.idLong
        }

        // 運営カテゴリ作成
        Discord.guild.createCategory("運営")
            .addPermissionOverride(Discord.guild.publicRole, listOf(), listOf(Permission.VIEW_CHANNEL))
            .complete()// everyoneからチャンネル閲覧権限を剥奪
            .also { cat ->
                // カテゴリ作成後、テキストチャンネルを作成
                cat.createTextChannel("運営チャット").complete()
            }

        // スタッフ用のロールを作成
        Discord.guild.createRole().apply {
            setColor(Color.GREEN) // ロールを緑色に設定
            setName("Staff") // ロールの名前を"Staff"に設定
            setMentionable(true) // メンション可能にする
            setHoisted(true) // 別枠で表示にする
        }.complete().also { role ->
            // ロール作成後、コンフィグにロールのIDを設定
            Main.config.staffRoleId = role.idLong
        }

        // スタッフカテゴリ作成
        Discord.guild.createCategory("スタッフ").addRolePermissionOverride(
            Main.config.staffRoleId, listOf( // スタッフロールに対して権限設定を行なう
                Permission.VIEW_CHANNEL, // チャンネル閲覧を許可
                Permission.MESSAGE_SEND, // メッセージ送信を許可
                Permission.MESSAGE_HISTORY, // メッセージ履歴の閲覧を許可
                Permission.MESSAGE_MANAGE, // メッセージの管理を許可
                Permission.CREATE_PUBLIC_THREADS // パブリックスレッドの作成を許可
            ), listOf()
        ).addPermissionOverride(Discord.guild.publicRole, listOf(), listOf(Permission.VIEW_CHANNEL))
            .complete() // everyoneからチャンネル閲覧権限を剥奪
            .also { cat ->
                // カテゴリ作成後の処理

                // コンフィグにスタッフカテゴリのIDを設定
                Main.config.staffCatId = cat.idLong

                // カテゴリ内にスタッフチャットを作成
                cat.createTextChannel("スタッフチャット").complete()

                // カテゴリ内にゲームパネルを作成
                cat.createTextChannel("ゲームパネル").complete().also { ch ->
                    // ゲームパネル作成後の処理

                    // コンフィグにゲームパネルのチャンネルIDを設定
                    Main.config.panelCh = ch.idLong

                    // ゲームパネルチャンネルに空メッセージを送信
                    ch.sendMessage(".").complete().also { panelMessage ->
                        // メッセージを送信後、メッセージのIDをコンフィグに設定
                        Main.config.panelId = panelMessage.idLong
                    }
                }
            }

        // 運営関連、スタッフ関連の設定が完了したことを表示
        message?.editMessageEmbeds(EmbedBuilder().apply {
            addField("運営", "<@&${Main.config.gameAdmin}>", true)
            addField("スタッフ", "<@&${Main.config.staffRoleId}>", true)
        }.build())?.complete().also {
            // １分後にメッセージを削除
            Timer().schedule(1000 * 60) {
                it?.delete()?.queue()
            }
        }

        // チームを作成することを表示
        message = replyTo?.reply(MessageBuilder().apply {
            setEmbeds(EmbedBuilder().apply {
                setDescription("チームを作成中")
            }.build())
        }.build())?.complete()

        val createChannelsEmbedBuilder = EmbedBuilder()
        var embedString = ""
        // チャンネルとロール作成
        var count = 1
        for (categories in 0 until 1) {
            Discord.guild.createCategory("↓に参加してください").apply {
                addPermissionOverride(Discord.guild.publicRole, listOf(), listOf(Permission.VIEW_CHANNEL))
            }.complete().also { cat ->
                val channels = mutableListOf<Config.Channel>()
                // 25チャンネル作成
                for (i in 0 until 25) {
                    // ロール作成
                    val role = Discord.guild.createRole().apply {
                        setName("T${count}")
                    }.complete()
                    // チャンネル作成
                    val vc = cat.createVoiceChannel("チームVC").apply {
                        addRolePermissionOverride(
                            role.idLong,
                            listOf(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK),
                            listOf()
                        )
                    }.complete()
                    // チャンネルをリストに追加
                    channels.add(Config.Channel(vc.idLong, role.idLong))

                    // embedに表示する文字列に追加
                    embedString += "T${count}: <#${vc.id}>\n<@&${role.id}>\n"

                    // カウントをインクリメント
                    count++
                    if (count % 10 == 0) {
                        createChannelsEmbedBuilder.addField("Channels", embedString, true)
                        embedString = ""
                    }
                    // 待機
                    Thread.sleep(75)
                }
                Main.config.channels[cat.idLong] = channels
            }
        }

        // 完了を表示
        message?.editMessageEmbeds(createChannelsEmbedBuilder.addField("Channels", embedString, true).build())
            ?.complete().also {
                Timer().schedule(1000 * 60) {
                    it?.delete()?.queue()
                }
            }

        // 作成することを送信
        message = message?.replyEmbeds(EmbedBuilder().apply {
            setDescription("答えカテゴリ・チャンネル・ロール作成中")
        }.build())?.complete()

        // 答えカテゴリの作成完了を通知するembedのビルダー
        val answerCreationEmbedBuilder = EmbedBuilder()

        // answerロール・チャンネル作成
        Discord.guild.createCategory("Your word is").apply {
            addPermissionOverride(
                Discord.guild.publicRole, // everyone
                listOf(), // 何もなし
                listOf(
                    Permission.VIEW_CHANNEL,
                    Permission.MESSAGE_HISTORY,
                    Permission.MESSAGE_SEND
                ) // チャンネル閲覧、メッセージ履歴、メッセージ送信を禁止
            )
        }.complete().also { cat ->
            // 正解用ロール・チャンネルの作成
            Discord.guild.createRole().apply {
                setName("\u200B") // 幅ゼロすぺーす
            }.complete().also { role ->
                cat.createVoiceChannel("LOADING").apply {
                    addRolePermissionOverride(role.idLong, listOf(Permission.VIEW_CHANNEL), listOf()) // チャンネルの閲覧のみ許可
                }.complete().also { ch ->
                    Main.config.trueRole = role.idLong
                    Main.config.trueChannelId = ch.idLong
                    answerCreationEmbedBuilder.addField("True Answer", "<#${ch.id}>\n<@&${role.id}>", true)
                }
            }

            // 不正解用ロール・チャンネルの作成
            Discord.guild.createRole().apply {
                setName("\u200B") // 幅ゼロすぺーす
            }.complete().also { role ->
                // チャンネルの作成
                cat.createVoiceChannel("LOADING").apply {
                    addRolePermissionOverride(role.idLong, listOf(Permission.VIEW_CHANNEL), listOf())
                }.complete().also { ch ->
                    Main.config.falseRole = role.idLong
                    Main.config.falseChannelId = ch.idLong
                    answerCreationEmbedBuilder.addField("False Answer", "<#${ch.id}>\n<@&${role.id}>", true)
                }
            }

            // ダミーチャンネルの作成
            cat.createVoiceChannel("LOADING").complete().also {
                Main.config.dummyChannelId = it.idLong
                answerCreationEmbedBuilder.addField("Dummy Answer", "<#${it.id}>", true)
            }
        }

        message?.editMessageEmbeds(answerCreationEmbedBuilder.build())?.complete()?.delete()
            ?.queueAfter(60, TimeUnit.SECONDS)

        message = message?.replyEmbeds(EmbedBuilder().apply {
            setDescription("参加者ロールを生成中")
        }.build())?.complete()

        Discord.guild.getRoleById(Main.config.gameParticipant) ?: Discord.guild.createRole().apply {
            setName("参加者")
            setColor(Color.GREEN)
        }.complete().also {
            Main.config.gameParticipant = it.idLong
        }

        message?.editMessageEmbeds(EmbedBuilder().apply {
            setDescription("<@&${Main.config.gameParticipant}>")
        }.build())?.complete().also {
            Timer().schedule(1000 * 60) {
                it?.delete()?.queue()
            }
        }

        message?.replyEmbeds(EmbedBuilder().apply {
            setDescription("設定完了")
        }.build())?.complete().also {
            Timer().schedule(1000 * 60) {
                it?.delete()?.queue()
            }
        }

        Main.config.status = Config.GameStatus.WAITING_TEAM_CREATE

        Util.updatePanel()

    }

    fun start() {
        Discord.guild.getTextChannelById(Main.config.panelCh)?.sendMessage("ゲームを開始します。")?.queue()

        // お題を決定する
        println("start")
        if (Main.config.status != Config.GameStatus.WAITING_START) return

        // ゲームの状態を実行中にする
        Main.config.status = Config.GameStatus.RUNNING

        // データセットをシャッフルする
        Main.dataSet.words.shuffle()
        // データセットの最初の値を取得し、削除する。存在しない場合はエラーを吐き異常終了する
        val word = Main.dataSet.words.removeFirstOrNull() ?: throw IllegalStateException()

        // デバッグ
        println(Main.dataSet)
        println(word)

        // 正解・不正解用のロールを取得する
        val trueRole = Discord.guild.getRoleById(Main.config.trueRole)!!
        val falseRole = Discord.guild.getRoleById(Main.config.falseRole)!!

        // 正解・不正解・ダミーのチャンネルを取得
        val trueCh = Discord.guild.getVoiceChannelById(Main.config.trueChannelId)!!
        val falseCh = Discord.guild.getVoiceChannelById(Main.config.falseChannelId)!!
        val dummyCh = Discord.guild.getVoiceChannelById(Main.config.dummyChannelId)!!

        // チャンネルの名前をLOADINGに変更する
        trueCh.manager.setName("LOADING").queue()
        falseCh.manager.setName("LOADING").queue()
        dummyCh.manager.setName("LOADING").queue()

        // ロール割り振り
        Main.config.teams.forEach { team ->
            // チームに所属するメンバーがいる場合のみ処理を行う
            if (team.members.size != 0) {

                // チーム内のユーザーを取得しシャッフルする
                val users = mutableListOf<Long>().also {
                    it.addAll(team.members)
                    it.shuffle()
                }

                // チームの一覧を取得する
                println(users)

                // チームのユーザーの中から一人選び、不正解ロールを付与する
                Discord.guild.addRoleToMember(users.removeFirstOrNull()!!.also { team.wolf = it }, falseRole).queue()
                // それ以外のユーザーには正解ロールを付与する
                users.forEach {
                    Discord.guild.addRoleToMember(it, trueRole).queue()
                }
            }
        }

        // VC割り振り
        moveMembersToTeamVC()

        // チャンネル名変更
        Discord.origin.modifyChannel(ModifyChannelRequest(trueCh.idLong).apply {
            name = word.trueWord
            Main.config.currentTrue = word.trueWord
        })
        Discord.origin.modifyChannel(ModifyChannelRequest(falseCh.idLong).apply {
            name = word.falseWord
            Main.config.currentFalse = word.falseWord
        })
        Discord.origin.modifyChannel(ModifyChannelRequest(dummyCh.idLong).apply {
            name = word.dummy ?: "?"
        })


        val announceCh = Discord.guild.getTextChannelById(Main.config.announceChannel)!!
        announceCh.sendMessage("${Discord.guild.publicRole.asMention} 自己紹介を開始してください。").queue()
        // タイマー開始
        Timer().schedule(1000 * 60 * 5) {
            val announceCh = Discord.guild.getTextChannelById(Main.config.announceChannel)!!
            announceCh.sendMessage("${Discord.guild.publicRole.asMention} 話し合いを開始してください。").queue()
        }.also { tasks.add(it) }
        Timer().schedule(1000 * 60 * 10) {
            forceFinish()
        }.also { tasks.add(it) }

        Discord.guild.getTextChannelById(Main.config.panelCh)?.sendMessage("ゲームを開始しました。")?.queue()
    }

    // 実行したタスクたち
    val tasks = mutableListOf<TimerTask>()

    fun resetGame() {
        Discord.guild.getRoleById(Main.config.trueRole)!!.delete().queue()
        Discord.guild.getRoleById(Main.config.falseRole)!!.delete().queue()

        Discord.guild.createRole().apply {
            setName("*")
        }.complete().also {
            Main.config.trueRole = it.idLong
        }

        Discord.guild.createRole().apply {
            setName("*")
        }.complete().also {
            Main.config.falseRole = it.idLong
        }
    }

    /**
     * VC作成する。
     *
     * @param size チームの大きさ(0のときはチームを削除する)
     */
    @Deprecated("廃止")
    fun createVCs(size: Int) {
        val teamSizes = mutableListOf<Int>()
        val participants =
            ArrayDeque<Member>(Discord.guild.members.filter { member -> member.roles.any { it.idLong == Main.config.gameParticipant } })
        val teams: Int = (participants.size / size)
        val rem: Int = participants.size % size

        for (i in 0 until teams) {
            teamSizes.add(size)
        }
        /*if (rem == 0) {

        } else if (size - 1 == rem) {
            teamSizes[teams - 1] = rem
        } else {
            if (i in )
        }*/
        // TODO: 2022/01/19 丸めるのは後回し

        // VCの作成先のカテゴリ
        val category = Discord.guild.getCategoryById(Main.config.teamCat)!!

        for (i in 0 until teams) {
            val team = Config.Team(size = size, id = i + 1, name = "T${i + 1}")

            Discord.guild.createRole().apply {
                setName(team.name)
            }.complete().also {
                team.roleId = it.idLong
            }

            category.createVoiceChannel(team.name).apply {
                addRolePermissionOverride(
                    team.roleId,
                    listOf(
                        Permission.VIEW_CHANNEL,
                        Permission.VOICE_CONNECT,
                        Permission.VOICE_SPEAK,
                        Permission.MESSAGE_SEND
                    ),
                    listOf()
                )
            }.complete().also {
                team.vcId = it.idLong
            }

            Main.config.teams.add(team)

            Thread.sleep(100)
        }
    }

    /**
     * チームを割り振る
     */
    fun createTeams(size: Int) {
        val teamSize = size

        val panelChannel = Discord.guild.getTextChannelById(Main.config.panelCh)

        panelChannel?.sendMessage("チーム振り分けを行います。")?.queue()

        // ロールを削除
        Main.config.teams.forEach { team ->
            team.members.forEach { user ->
                val member = Discord.guild.getMemberById(user)
                member?.roles?.filter { it.idLong == team.roleId || it.idLong == Main.config.trueRole || it.idLong == Main.config.falseRole }
                    ?.forEach {
                        Discord.guild.removeRoleFromMember(user, it).queue()
                    }
            }
        }

        Main.config.teams.clear()

        // VCに参加していて参加者ロールのついているメンバーを取得する
        val participants =
            ArrayDeque<Member>(Discord.guild.getVoiceChannelById(Main.config.mainVC)!!.members.filter { member -> member.roles.any { it.idLong == Main.config.gameParticipant } })
        participants.shuffle() // シャッフル！

        // 品川メンバーを取得
        var shinagawas = mutableListOf<Member>()
        shinagawas.addAll(participants.filter { it.roles.any { it.idLong == 946403951480274965L } })
        shinagawas.shuffle()

        // チームに割り振れるチャンネルをすべて取得する
        val channelsQue = ArrayDeque<Config.Channel>()
        Main.config.channels.forEach { (_, channels) -> channelsQue.addAll(channels) }

        // 作成するチーム数を計算
        var teams = ceil(participants.size / teamSize.toDouble()).toInt()

        // 最後のチームの人数
        val lastTeamSize = participants.size % (teams * teamSize)

        // 最後のチームの人数が-2人以上少なくなり、その人数が作成されるチーム数よりも小さい場合は最初のlastTeamSize個のチームはメンバーを+1にする
        val addition = lastTeamSize <= teams && lastTeamSize + 2 <= teamSize
        if (addition) {
            teams--
        }

        // 品川のうち、最初のチーム数分は各チームに配置するため、参加者一覧からは削除する
        for (count in 0 until teams) {
            participants.remove(shinagawas.getOrNull(count) ?: continue)
        }

        // 参加者一覧から削除されなかった品川の人は削除する
        if (shinagawas.size > teams) {
            shinagawas = shinagawas.subList(0, teams)
        }

        // メッセージを表示
        panelChannel?.sendMessageEmbeds(
            listOf(
                EmbedBuilder().apply {
                    setTitle("チーム作成")
                    addField("チーム数", teams.toString(), true)
                    addField("チームあたりの人数", teamSize.toString(), true)
                    addField("最終チームをばらけさせるかどうか", addition.toString(), true)
                    addField("最終チームの人数", lastTeamSize.toString(), true)
                    addField("通常参加者数", participants.size.toString(), true)
                    addField("品川参加者数", shinagawas.size.toString(), true)
                    addField("品川メンバー", shinagawas.map { "<@${it.idLong}>" }.joinToString(" "), false)
                }.build()
            )
        )?.queue()

        // 作成したチーム数のカウンター
        var idCount = 0

        // チームに割り振っていく
        for (i in 0 until teams) {
            // このチームの人数を取得
            val currentTeamSize = if (addition && i < lastTeamSize) teamSize + 1 else teamSize

            // 作成するチームに割り振られるメンバーの一覧を作成
            val teamMembers = mutableListOf<Member>()
            var shinagawa = shinagawas.removeFirstOrNull()
            if (shinagawa == null) {
                Discord.guild.getTextChannelById(Main.config.panelCh)?.sendMessage("チーム${i + 1}に品川メンバーを追加できませんでした。")
                    ?.queue()
                shinagawa = participants.removeFirstOrNull()
            }
            // 品川メンバーが取得できている場合はチームに追加する
            if (shinagawa != null) {
                teamMembers.add(shinagawa)
            }

            // participantsからメンバーを取ってくる
            for (j in 0 until currentTeamSize - 1) {
                val member = participants.removeFirstOrNull()
                if (member != null) teamMembers.add(member)
            }

            // チャンネルとロールを取得
            val chPair = channelsQue.removeFirstOrNull() ?: throw IllegalStateException()
            val role = Discord.guild.getRoleById(chPair.role)

            // チームの作成カウンターを増やす
            ++idCount
            val team = Config.Team(
                size = currentTeamSize,
                id = idCount,
                name = "T${idCount}",
                vcId = chPair.voice,
                roleId = chPair.role
            )

            // チームにメンバーを追加し、ロールを付与する
            teamMembers.forEach { member ->
                Discord.guild.addRoleToMember(member, role!!).queue()
                team.members.add(member.idLong)
            }

            Main.config.teams.add(team)
        }

        Main.config.status = Config.GameStatus.WAITING_START

        Discord.guild.getTextChannelById(Main.config.panelCh)?.sendMessage("チーム振り分けが完了しました。")?.queue()
    }

    fun forceFinish() {
        // メッセージ送信
        // アナウンスチャンネル取得
        val announceCh = Discord.guild.getTextChannelById(Main.config.announceChannel)!!
        announceCh.sendMessage("${Discord.guild.publicRole.asMention} 話し合い時間が終了しました。投票を始めてください。").queue()
        Main.config.status = Config.GameStatus.WAITING_NEXT

        tasks.forEach { it.cancel() }

        Discord.guild.getTextChannelById(Main.config.panelCh)
            ?.sendMessage("<@&${Main.config.staffRoleId}> ゲームが終了しました。")?.queue()
    }

    fun next() {
        // メッセージを送信
        Discord.guild.getTextChannelById(Main.config.panelCh)?.also { ch ->
            ch.sendMessage("次のゲームに進む準備をしています。").queue()
            ch.sendMessageEmbeds(EmbedBuilder().apply {
                setTitle("結果-1")
                addField("正しい答え", Main.config.currentTrue, false)
                addField("間違った答え", Main.config.currentFalse, false)
            }.build()).queue()
            ch.sendMessageEmbeds(EmbedBuilder().apply {
                setTitle("結果-2")
                Main.config.teams.forEach { team ->
                    addField(team.name, "<@${team.wolf}>", true)
                }
            }.build()).queue()
        }

        // メンバー移動
        moveMemberToMainVC()

        val teamRoles = Main.config.teams.map { it.roleId }

        // ロールを削除する
        Discord.guild.members.forEach { member ->
            member.roles.forEach { role ->
                if (role.idLong == Main.config.trueRole || role.idLong == Main.config.falseRole || teamRoles.contains(
                        role.idLong
                    )
                ) {
                    Discord.guild.removeRoleFromMember(member.id, role).complete()
                }
            }
        }

        Main.config.teams.clear()

        // nextが完了したことを送信
        Discord.guild.getTextChannelById(Main.config.panelCh)?.sendMessage("次のゲームに進む準備が完了しました。チームを作成してください。")?.queue()

        Main.config.status = Config.GameStatus.WAITING_TEAM_CREATE
    }

    /**
     * VCを削除する
     */
    fun reset() {
        // チームVCのすべてのカテゴリに対してfor-each-loopを回す
        Main.config.channels.forEach { cat ->
            // カテゴリ内のすべてのChannelに対してfor-each-loopを回す
            cat.value.forEach { ch ->
                // VCを削除
                Discord.guild.getVoiceChannelById(ch.voice)?.delete()?.queue()
                // ロールを削除
                Discord.guild.getRoleById(ch.role)?.delete()?.queue()
                // Rate-limit対策のため、0.1秒待つ
                Thread.sleep(100)
            }
        }

        // ゲームパネルのチャンネルを取得し、そのカテゴリ(スタッフカテゴリ)を取得する
        Discord.guild.getTextChannelById(Main.config.panelCh)!!.parentCategory!!.also { cat ->
            // カテゴリ内のすべてのチャンネルを削除
            cat.channels.forEach { ch ->
                ch.delete().queue()
            }

            // カテゴリを削除
            cat.delete().queue()
        }

        // 多数派のお題を表示するためのチャンネルを削除
        Discord.guild.getVoiceChannelById(Main.config.trueChannelId)?.delete()?.queue()

        // 少数派のお題を表示するためのチャンネルを削除
        Discord.guild.getVoiceChannelById(Main.config.falseChannelId)?.delete()?.queue()

        // ダミーのお題を表示するためのチャンネルを削除
        Discord.guild.getVoiceChannelById(Main.config.dummyChannelId)?.delete()?.queue()

        // 多数派ロールを削除
        Discord.guild.getRoleById(Main.config.trueRole)?.delete()?.queue()

        // 少数派ロールを削除
        Discord.guild.getRoleById(Main.config.falseRole)?.delete()?.queue()

        // ゲーム参加者ロールを削除
        Discord.guild.getRoleById(Main.config.gameParticipant)?.delete()?.queue()

        // ゲーム管理者ロールを削除
        Discord.guild.getRoleById(Main.config.gameAdmin)?.delete()?.queue()

        // ゲームスタッフロールを削除
        Discord.guild.getRoleById(Main.config.staffRoleId)?.delete()?.queue()

        // コンフィグの設定を初期化する
        Main.config.teams.clear()
        Main.config.channels.clear()

        // ゲームの進捗状況を初期設定前に変更
        Main.config.status = Config.GameStatus.BEFORE_SETUP
    }

    /**
     * メンバーを各チームのVCに移動させる
     */
    fun moveMembersToTeamVC() {
        // メンバーID, チャンネルID
        val idMap = ConcurrentHashMap<Long, Long>()

        // すべてのチームに対してfor-each-loopする
        Main.config.teams.forEach { team ->
            // チームに所属するメンバーが0人ではない場合
            if (team.members.size != 0) {
                // チームに所属するすべてのメンバーに対してfor-eachを回す
                team.members.forEach { member ->
                    // メンバーのIDと移動先のVCのIDをマップに追加
                    idMap[member] = team.vcId
                }
            }
        }

        // マップに追加したすべてのメンバーに対してfor-eachを回す
        idMap.keys.parallelStream().forEach { member ->

            // 独自のDiscord botにより、VCを移動させる
            Discord.origin.moveVoiceChannel(
                Main.config.guild.toString(),
                member.toString(),
                ModifyUserVoiceStateRequest().apply { channelId = idMap[member]!! })
        }

    }

    /**
     * すべての参加者をメインVCに移動させる
     */
    fun moveMemberToMainVC() {
        // すべてのチームに対してぱられるすとりーーむでfor-eachを回す
        Main.config.teams.parallelStream().forEach { team ->
            // そのチームのVCを取得し、VCに参加しているメンバー全員に対してfor-eachを回す
            Discord.guild.getVoiceChannelById(team.vcId)?.members?.forEach { member ->
                // メインVCに移動させる
                Discord.origin.moveVoiceChannel(
                    Main.config.guild.toString(),
                    member.id,
                    ModifyUserVoiceStateRequest().apply { channelId = Main.config.mainVC })
            }
        }
    }


}