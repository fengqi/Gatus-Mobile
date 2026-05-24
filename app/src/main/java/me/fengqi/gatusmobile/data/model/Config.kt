package me.fengqi.gatusmobile.data.model

import com.google.gson.annotations.SerializedName

data class AppConfig(
    val announcements: List<Announcement>? = emptyList(),
    val ui: UIConfig? = null
)

data class UIConfig(
    val title: String? = null,
    val description: String? = null,
    val header: String? = null,
    val dashboardHeading: String? = null,
    val dashboardSubheading: String? = null,
    val logo: String? = null,
    val link: String? = null,
    val buttons: List<UIButton>? = emptyList(),
    @SerializedName("dark-mode")
    val darkMode: Boolean? = true
)

data class UIButton(
    val name: String = "",
    val link: String = ""
)

data class Announcement(
    val timestamp: String = "",
    val type: String = "none",
    val message: String = "",
    val archived: Boolean = false
)
