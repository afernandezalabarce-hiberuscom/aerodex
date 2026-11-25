package dev.afalabarce.aircrafts.core.common.serializers

import dev.afalabarce.aircrafts.core.common.extensions.format
import dev.afalabarce.aircrafts.core.common.extensions.toLocalDate
import dev.afalabarce.aircrafts.core.common.utilities.supportedDateFormat
import kotlinx.datetime.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate {
        val raw = decoder.decodeString()
        for (format in supportedDateFormat) {
            val parsed = runCatching { raw.toLocalDate(format) }.getOrNull()
            if (parsed != null) return parsed
        }

        throw SerializationException("Error parsing date: $raw")
    }

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value = value.format())
    }
}
