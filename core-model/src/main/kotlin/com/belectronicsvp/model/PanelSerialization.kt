package com.belectronicsvp.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Handles encoding, decoding, and validation of panel JSON documents.
 */
object PanelSerialization {

    val jsonFormat = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Serializes a [PanelModel] into a formatted JSON string.
     */
    fun toJson(panel: PanelModel): String {
        return jsonFormat.encodeToString(panel)
    }

    /**
     * Deserializes a JSON string into a [PanelModel], returning [Result.success] or [Result.failure].
     */
    fun fromJson(jsonString: String): Result<PanelModel> {
        return runCatching {
            jsonFormat.decodeFromString<PanelModel>(jsonString)
        }
    }
}
