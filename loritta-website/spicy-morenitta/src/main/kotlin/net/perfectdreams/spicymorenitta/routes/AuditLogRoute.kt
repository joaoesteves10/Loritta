package net.perfectdreams.spicymorenitta.routes

import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.parse
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.utils.ActionType
import net.perfectdreams.spicymorenitta.utils.select
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import org.w3c.dom.HTMLDivElement
import utils.Moment
import kotlin.browser.document
import kotlin.browser.window

class AuditLogRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/audit-log") {
    override val keepLoadingScreen: Boolean
        get() = true

    @UseExperimental(ImplicitReflectionSerializer::class)
    override fun onRender(call: ApplicationCall) {
        Moment.locale("pt-br")

        m.showLoadingScreen()

        SpicyMorenitta.INSTANCE.launch {
            val result = http.get<String> {
                url("${window.location.origin}/api/v1/guild/${call.parameters["guildid"]}/audit-log")
            }

            val list = kotlinx.serialization.json.JSON.nonstrict.parse<ServerConfig.WebAuditLogWrapper>(result)

            fixDummyNavbarHeight(call)
            m.fixLeftSidebarScroll {
                switchContent(call)
            }

            val entriesDiv = document.select<HTMLDivElement>("#audit-log-entries")

            for (entry in list.entries) {
                val user = list.users.first { it.id == entry.id.toString() }

                entriesDiv.append {
                    div {
                        createAuditLogEntry(user, entry)
                    }
                }
            }

            m.hideLoadingScreen()
        }
    }

    fun DIV.createAuditLogEntry(selfMember: ServerConfig.SelfMember, entry: ServerConfig.WebAuditLogEntry) {
        val type = ActionType.valueOf(entry.type)

        this.div(classes = "discord-generic-entry timer-entry") {
            img(classes = "amino-small-image") {
                style = "width: 6%; height: auto; border-radius: 999999px; float: left; position: relative; bottom: 8px;"

                src = selfMember.effectiveAvatarUrl
            }
            div(classes = "pure-g") {
                div(classes = "pure-u-1 pure-u-md-18-24") {
                    div {
                        style = "margin-left: 10px; margin-right: 10;"

                        val updateString = if (type.updateType == "updated") {
                            locale["modules.auditLog.${type.updateType}"]
                        } else {
                            locale["modules.auditLog.generic"]
                        }

                        div(classes = "amino-title entry-title") {
                            style = "font-family: Lato,Helvetica Neue,Helvetica,Arial,sans-serif;"

                            var isControl = false
                            var ignoreNext = false

                            val genericStringBuilder = StringBuilder()

                            for (ch in updateString) {
                                if (ignoreNext) {
                                    ignoreNext = false
                                    continue
                                }
                                if (isControl) {
                                    if (genericStringBuilder.isNotEmpty()) {
                                        span {
                                            style = "opacity: 0.8;"

                                            + genericStringBuilder.toString()
                                        }
                                        genericStringBuilder.clear()
                                    }

                                    ignoreNext = true
                                    isControl = false

                                    val num = ch.toString().toInt()
                                    if (num == 0) {
                                        + selfMember.name

                                        span {
                                            style = "font-size: 0.8em; opacity: 0.6;"
                                            + "#${selfMember.discriminator}"
                                        }
                                    }

                                    if (num == 1) {
                                        span {
                                            var sectionName = when (type) {
                                                ActionType.UPDATE_YOUTUBE -> "YouTube"
                                                ActionType.UPDATE_TWITCH -> "Twitch"
                                                else -> locale["modules.sectionNames.${type.sectionName}"]
                                            }

                                            + sectionName
                                        }
                                    }
                                    continue
                                }
                                if (ch == '{') {
                                    isControl = true
                                    continue
                                }

                                genericStringBuilder.append(ch)
                            }
                        }
                        div(classes = "amino-title toggleSubText") {
                            + (Moment.unix(entry.executedAt / 1000).calendar())
                        }
                    }
                }
                /* div(classes = "pure-u-1 pure-u-md-6-24 vertically-centered-right-aligned") {
                    button(classes="button-discord button-discord-edit pure-button edit-timer-button") {
                        onClickFunction = {
                            println("Saving!")
                            SaveUtils.prepareSave("premium", {
                                it["keyId"] = donationKey.id
                            }, onFinish = {
                                val guild = JSON.nonstrict.parse<ServerConfig.Guild>(it.body)

                                PremiumKeyView.generateStuff(guild)
                            })
                        }
                        + "Ativar"
                    }
                } */
            }
        }
    }
}