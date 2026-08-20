package com.parikiganesh.tomato365.notifications

import com.parikiganesh.tomato365.utils.Constants

object NotificationTopics {

    fun marketTopic(marketId: String): String {
        val topicPart = marketId.trim()
            .lowercase()
            .replace(Regex("[^a-z0-9_-]"), "_")
        return "${Constants.FCM_MARKET_TOPIC_PREFIX}$topicPart"
    }
}
