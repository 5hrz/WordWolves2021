package one.shrz.wordwolves.discord.type

import com.google.gson.annotations.SerializedName
import javax.annotation.Nonnull
import javax.annotation.Nullable

class User {

    /**
     * the user's id
     */
    @Nonnull
    lateinit var id: String

    @Nonnull
    lateinit var username: String

    @Nonnull
    lateinit var discriminator: String

    @Nullable
    var avatar: String? = null

    @Nonnull
    @SerializedName("bot")
    var isBot: Boolean = false

    @Nonnull
    @SerializedName("system")
    var isSystem: Boolean = false

    @Nonnull
    @SerializedName("mfa_enabled")
    var isMfaEnabled: Boolean = false

    @Nullable
    var banner: String? = null

    @Nullable
    @SerializedName("accent_color")
    var accentColor: Int? = null

    @Nonnull
    var local: String = ""

    @Nonnull
    var email: String = ""

    @Nonnull
    var flags: Int = 0

    @Nonnull
    @SerializedName("premium_type")
    var premiumType: Int = 0

    @Nonnull
    @SerializedName("public_flags")
    var publicFlags: Int = 0


}