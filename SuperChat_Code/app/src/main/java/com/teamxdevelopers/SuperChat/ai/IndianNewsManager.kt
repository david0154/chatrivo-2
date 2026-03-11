package com.teamxdevelopers.SuperChat.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

/**
 * IndianNewsManager - Fetches latest Indian news from free RSS feeds
 *
 * Sources:
 *  - NDTV India (Hindi)
 *  - The Hindu
 *  - Times of India
 *  - Aaj Tak (Hindi)
 *  - ANI News
 *  - Kolkata sources: Anandabazar Patrika, Ei Samay
 *
 * Used by the AI chat bot to answer "latest news" questions.
 */
object IndianNewsManager {

    private val client = OkHttpClient()

    data class NewsItem(
        val title: String,
        val description: String,
        val link: String,
        val pubDate: String,
        val source: String
    )

    enum class NewsCategory(val displayName: String, val rssUrl: String) {
        TOP_INDIA(
            "Top India News",
            "https://ndtv.com/rss/india"
        ),
        TECHNOLOGY(
            "Technology",
            "https://ndtv.com/rss/technology"
        ),
        BUSINESS(
            "Business & Economy",
            "https://timesofindia.indiatimes.com/rssfeeds/1898055.cms"
        ),
        SPORTS(
            "Sports",
            "https://timesofindia.indiatimes.com/rssfeeds/4719148.cms"
        ),
        BOLLYWOOD(
            "Bollywood & Entertainment",
            "https://timesofindia.indiatimes.com/rssfeeds/1081479906.cms"
        ),
        KOLKATA(
            "Kolkata News",
            "https://timesofindia.indiatimes.com/rssfeeds/-2128936835.cms"
        ),
        SCIENCE(
            "Science",
            "https://thehindu.com/sci-tech/science/?service=rss"
        ),
        WORLD(
            "World News",
            "https://thehindu.com/news/international/?service=rss"
        ),
        HINDI_TOP(
            "हिंदी समाचार (NDTV)",
            "https://khabar.ndtv.com/rss/hindi-national-news"
        )
    }

    /**
     * Fetch latest news items from a given RSS URL.
     * @param limit Maximum number of articles to return (default 5)
     */
    suspend fun fetchNews(
        category: NewsCategory,
        limit: Int = 5
    ): List<NewsItem> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(category.rssUrl)
                .addHeader("User-Agent", "ChatrivoApp/4.0")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val xmlString = response.body?.string() ?: return@withContext emptyList()
                parseRss(xmlString, category.displayName, limit)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseRss(xml: String, source: String, limit: Int): List<NewsItem> {
        val items = mutableListOf<NewsItem>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var inItem = false
            var title = ""
            var description = ""
            var link = ""
            var pubDate = ""
            var currentTag = ""

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT && items.size < limit) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        if (currentTag == "item") {
                            inItem = true
                            title = ""; description = ""; link = ""; pubDate = ""
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inItem) {
                            when (currentTag) {
                                "title" -> title += parser.text
                                "description" -> description += parser.text
                                "link" -> link += parser.text
                                "pubDate" -> pubDate += parser.text
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item" && inItem) {
                            items.add(NewsItem(
                                title = title.trim().replace("<![CDATA[", "").replace("]]>", ""),
                                description = description.trim()
                                    .replace(Regex("<[^>]*>"), "")
                                    .replace("<![CDATA[", "").replace("]]>", "")
                                    .take(200),
                                link = link.trim(),
                                pubDate = pubDate.trim(),
                                source = source
                            ))
                            inItem = false
                        }
                        currentTag = ""
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Exception) {}
        return items
    }

    /**
     * Format news items as a chat-friendly string for display in Chatrivo AI chat
     */
    fun formatForChat(items: List<NewsItem>, category: NewsCategory): String {
        if (items.isEmpty()) return "📰 No news available right now. Please try again later."
        val sb = StringBuilder("📰 *${category.displayName}*\n\n")
        items.forEachIndexed { index, item ->
            sb.append("${index + 1}. *${item.title}*\n")
            if (item.description.isNotBlank()) {
                sb.append("   ${item.description}...\n")
            }
            sb.append("\n")
        }
        sb.append("_Source: ${items.first().source}_")
        return sb.toString()
    }

    /**
     * Get a summary string to feed as context to Sarvam AI
     */
    fun formatForAIContext(items: List<NewsItem>): String {
        if (items.isEmpty()) return "No recent news available."
        return items.joinToString("\n") { "- ${it.title}: ${it.description}" }
    }
}
