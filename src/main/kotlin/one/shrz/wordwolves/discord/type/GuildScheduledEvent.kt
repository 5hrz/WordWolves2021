package one.shrz.wordwolves.discord.type

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import javax.annotation.Nonnull
import javax.annotation.Nullable

class GuildScheduledEvent {
    /**
     * the id of the scheduled event
     */
    @Nonnull
    lateinit var id: Snowflake

    /**
     * 	the guild id which the scheduled event belongs to
     */
    @Nonnull
    @SerializedName("guild_id")
    lateinit var guildId: Snowflake

    /**
     * 	the channel id in which the scheduled event will be hosted, or null if scheduled entity type is EXTERNAL
     */
    @Nullable
    @SerializedName("channel_id")
    var channelId: String? = null

    /**
     * the id of the user that created the scheduled event
     */
    @Nullable
    @SerializedName("creator_id")
    var creatorId: String? = null

    /**
     * the name of the scheduled event (1-100 characters)
     */
    @Nonnull
    var name: String = ""
        set(name) = if (name.length > 100 || name.isEmpty()) throw IllegalArgumentException() else field = name

    /**
     * the description of the scheduled event (1-1000 characters)
     */
    @Nullable
    var description: String? = null
        set(value) = if (value?.length!! > 1000 || value.isEmpty()) throw IllegalArgumentException() else field = value

    /**
     * 	the time the scheduled event will start
     */
    @Nonnull
    @SerializedName("scheduled_start_time")
    lateinit var scheduledStartTimeString: String


    fun getScheduledStartTime(): LocalDateTime {
        return LocalDateTime.parse(scheduledStartTimeString.subSequence(0, scheduledStartTimeString.lastIndexOf('+')))
    }

    /**
     * 	the time the scheduled event will end, required if entity_type is EXTERNAL
     */
    @Nullable
    @SerializedName("scheduled_end_time")
    var scheduledEndTimeString: String? = null

    fun getScheduledEndTime(): LocalDateTime {
        return LocalDateTime.parse(scheduledEndTimeString!!.subSequence(0, scheduledEndTimeString!!.lastIndexOf('+')))
    }

    /**
     * 	the privacy level of the scheduled event
     */
    @Nonnull
    @SerializedName("privacy_level")
    var privacyLevel: PrivacyLevel = PrivacyLevel.GUILD_ONLY

    /**
     * the status of the scheduled event
     */
    @Nonnull
    lateinit var status: Status

    /**
     * 	the type of the scheduled event
     */
    @Nonnull
    @SerializedName("entity_type")
    lateinit var entityType: EntityType

    /**
     * 	the id of an entity associated with a guild scheduled event
     */
    @Nullable
    @SerializedName("entity_id")
    var entityId: String? = null

    @Nullable
    @SerializedName("entity_metadata")
    var entityMetadata: EntityMetadata? = null

    @Nullable
    var creator: User? = null

    @Nonnull
    @SerializedName("user_count")
    var userCount: Int = 0

    enum class PrivacyLevel(val value: Long) {
        @SerializedName("2")
        GUILD_ONLY(2)
    }

    enum class Status(val value: Long) {
        @SerializedName("1")
        SCHEDULED(1),

        @SerializedName("2")
        ACTIVE(2),

        @SerializedName("3")
        COMPLETED(3),

        @SerializedName("4")
        CANCELED(4)
    }

    enum class EntityType(val value: Long) {
        @SerializedName("1")
        STAGE_INSTANCE(1),

        @SerializedName("2")
        VOICE(2),

        @SerializedName("3")
        EXTERNAL(3)
    }

    class EntityMetadata(@Nullable var location: String? = null) {

        fun setLocation(location: String): EntityMetadata {
            if (location.length > 100) {
                throw IllegalArgumentException()
            } else if (location.isEmpty()) {
                this.location = null
            } else {
                this.location = location
            }
            return this
        }
    }

}