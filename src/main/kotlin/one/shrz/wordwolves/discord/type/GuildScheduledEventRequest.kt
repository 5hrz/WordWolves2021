package one.shrz.wordwolves.discord.type

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import javax.annotation.Nonnull
import javax.annotation.Nullable

class GuildScheduledEventRequest {

    /**
     * the id of the scheduled event
     */
    @Nullable
    @SerializedName("channel_id")
    var channelId: String? = null

    fun setChannelId(channelId: String): GuildScheduledEventRequest {
        this.channelId = channelId
        return this
    }

    @Nullable
    @SerializedName("entity_metadata")
    var entityMetadata: GuildScheduledEvent.EntityMetadata? = null

    fun setEntityMetadata(entityMetadata: GuildScheduledEvent.EntityMetadata): GuildScheduledEventRequest {
        this.entityMetadata = entityMetadata
        return this
    }

    @Nonnull
    lateinit var name: String

    fun setName(name: String): GuildScheduledEventRequest {
        this.name = name
        return this
    }

    @Nonnull
    @SerializedName("privacy_level")
    var privacyLevel: GuildScheduledEvent.PrivacyLevel = GuildScheduledEvent.PrivacyLevel.GUILD_ONLY

    fun setPrivacyLevel(privacyLevel: GuildScheduledEvent.PrivacyLevel): GuildScheduledEventRequest {
        this.privacyLevel = privacyLevel
        return this
    }

    @Nonnull
    @SerializedName("scheduled_start_time")
    lateinit var scheduledStartTime: String

    fun setScheduledStartTime(localDateTime: LocalDateTime): GuildScheduledEventRequest {
        scheduledStartTime = localDateTime.toString()
        return this
    }

    @Nullable
    @SerializedName("scheduled_end_time")
    lateinit var scheduledEndTime: String

    fun setScheduledEndTime(localDateTime: LocalDateTime): GuildScheduledEventRequest {
        scheduledEndTime = localDateTime.toString()
        return this
    }

    @Nullable
    var description: String? = null

    fun setDescription(description: String): GuildScheduledEventRequest {
        if (description.length > 100) {
            throw IllegalArgumentException()
        } else if (description.isEmpty()) {
            this.description = null
        } else {
            this.description = description
        }
        return this
    }

    @Nonnull
    @SerializedName("entity_type")
    lateinit var entityType: GuildScheduledEvent.EntityType

    fun setEntityType(entityType: GuildScheduledEvent.EntityType): GuildScheduledEventRequest {
        this.entityType = entityType
        return this
    }


}