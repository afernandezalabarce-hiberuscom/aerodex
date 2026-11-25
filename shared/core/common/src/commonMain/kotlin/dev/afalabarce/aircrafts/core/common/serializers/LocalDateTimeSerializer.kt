package dev.afalabarce.aircrafts.core.common.serializers

import dev.afalabarce.aircrafts.core.common.extensions.format
import dev.afalabarce.aircrafts.core.common.extensions.toLocalDateTime
import dev.afalabarce.aircrafts.core.common.utilities.supportedDateFormat
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val raw = decoder.decodeString()
        for (format in supportedDateFormat) {
            val parsed = runCatching { raw.toLocalDateTime(format) }.getOrNull()
            if (parsed != null) return parsed
        }

        throw SerializationException("Error parsing date: $raw")
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value = value.format())
    }
}
