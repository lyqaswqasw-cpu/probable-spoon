package com.example.data.model

import java.util.UUID

enum class StreamType(val title: String, val extensionNote: String) {
    M3U8("m3u8", "HLS Live Stream"),
    TS("ts", "MPEG-TS Stream"),
    NORMAL("عادي", "Direct / MP4 Link"),
    EMBED("embed", "كود تضمين / ويب");

    companion object {
        fun fromString(value: String?): StreamType {
            return when (value?.lowercase()?.trim()) {
                "m3u8" -> M3U8
                "ts" -> TS
                "embed", "iframe", "code" -> EMBED
                else -> NORMAL
            }
        }
    }
}

data class ChannelServer(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "SD",
    val streamUrl: String = "",
    val streamType: String = "m3u8" // "m3u8", "ts", "normal"
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "streamUrl" to streamUrl,
            "streamType" to streamType
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any?>): ChannelServer {
            return ChannelServer(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                name = map["name"] as? String ?: "SD",
                streamUrl = map["streamUrl"] as? String ?: "",
                streamType = map["streamType"] as? String ?: "m3u8"
            )
        }
    }
}

data class Channel(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val imageUrl: String = "",
    val category: String = "كرة قدم",
    val branchId: String = "",
    val branchName: String = "",
    val servers: List<ChannelServer> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val views: Long = 0L,
    // Embed code and player editor custom styling
    val isEmbedCode: Boolean = false,
    val embedCode: String = "",
    val strictAdBlock: Boolean = true,
    val topText: String = "",
    val topTextSize: Int = 16,
    val topTextColor: String = "#FFFFFF",
    val topImageUrl: String = "",
    val bottomText: String = "",
    val bottomTextSize: Int = 14,
    val bottomTextColor: String = "#CCCCCC",
    val bottomImageUrl: String = ""
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "name" to name,
            "imageUrl" to imageUrl,
            "category" to category,
            "servers" to servers.map { it.toMap() },
            "createdAt" to createdAt,
            "views" to views,
            "appVersion" to "1.0.0",
            "isEmbedCode" to isEmbedCode,
            "strictAdBlock" to strictAdBlock,
            "topTextSize" to topTextSize,
            "bottomTextSize" to bottomTextSize
        )
        if (branchId.isNotBlank()) {
            map["branchId"] = branchId
        }
        if (branchName.isNotBlank()) {
            map["branchName"] = branchName
        }
        if (embedCode.isNotBlank()) {
            map["embedCode"] = embedCode
        }
        if (topText.isNotBlank()) {
            map["topText"] = topText
        }
        if (topTextColor.isNotBlank()) {
            map["topTextColor"] = topTextColor
        }
        if (topImageUrl.isNotBlank()) {
            map["topImageUrl"] = topImageUrl
        }
        if (bottomText.isNotBlank()) {
            map["bottomText"] = bottomText
        }
        if (bottomTextColor.isNotBlank()) {
            map["bottomTextColor"] = bottomTextColor
        }
        if (bottomImageUrl.isNotBlank()) {
            map["bottomImageUrl"] = bottomImageUrl
        }
        return map
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): Channel {
            val serversRaw = map["servers"]
            val serverList = mutableListOf<ChannelServer>()

            when (serversRaw) {
                is List<*> -> {
                    for (item in serversRaw) {
                        if (item is Map<*, *>) {
                            serverList.add(ChannelServer.fromMap(item as Map<String, Any?>))
                        }
                    }
                }
                is Map<*, *> -> {
                    for ((_, item) in serversRaw) {
                        if (item is Map<*, *>) {
                            serverList.add(ChannelServer.fromMap(item as Map<String, Any?>))
                        }
                    }
                }
            }

            return Channel(
                id = (map["id"] as? String)?.takeIf { it.isNotBlank() } ?: id,
                name = map["name"] as? String ?: "",
                imageUrl = map["imageUrl"] as? String ?: "",
                category = map["category"] as? String ?: "كرة قدم",
                branchId = map["branchId"] as? String ?: "",
                branchName = map["branchName"] as? String ?: "",
                servers = serverList,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                views = (map["views"] as? Number)?.toLong() ?: 0L,
                isEmbedCode = (map["isEmbedCode"] as? Boolean) ?: false,
                embedCode = (map["embedCode"] as? String) ?: "",
                strictAdBlock = (map["strictAdBlock"] as? Boolean) ?: true,
                topText = (map["topText"] as? String) ?: "",
                topTextSize = (map["topTextSize"] as? Number)?.toInt() ?: 16,
                topTextColor = (map["topTextColor"] as? String) ?: "#FFFFFF",
                topImageUrl = (map["topImageUrl"] as? String) ?: "",
                bottomText = (map["bottomText"] as? String) ?: "",
                bottomTextSize = (map["bottomTextSize"] as? Number)?.toInt() ?: 14,
                bottomTextColor = (map["bottomTextColor"] as? String) ?: "#CCCCCC",
                bottomImageUrl = (map["bottomImageUrl"] as? String) ?: ""
            )
        }
    }
}
