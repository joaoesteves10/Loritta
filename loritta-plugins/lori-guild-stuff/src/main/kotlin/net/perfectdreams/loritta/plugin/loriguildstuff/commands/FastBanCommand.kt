package net.perfectdreams.loritta.plugin.loriguildstuff.commands

import com.mrpowergamerbr.loritta.commands.vanilla.administration.AdminUtils.ModerationConfigSettings
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.discordCommand

object FastBanCommand {

    private val punishmentReasons = hashMapOf(
            "spam" to "Floodar/spammar (Enviar várias mensagens repetidas, enviar uma mensagem com caracteres aletórios, adicionar reações aleatórias, etc) nos canais de texto.",
            "div" to "Não é permitido divulgar conteúdos em canais de texto sem que a equipe permita.",
            "divdm" to "Enviar conteúdo (não solicitado!) via mensagem direta, fazer spam (ou seja, mandar conteúdo indesejado para outras pessoas) é contra as regras do servidor da Loritta e dos termos de uso do Discord e, caso continuar, você poderá ser suspenso do Discord e irá perder a sua conta!",
            "nsfw" to "É proibido compartilhar conteúdo NSFW (coisas obscenas como pornografia, gore e coisas relacionadas), conteúdo sugestivo, jumpscares, conteúdo de ódio, racismo, assédio, links com conteúdo ilegal e links falsos. Será punido até se passar via mensagem direta, até mesmo se a outra pessoa pedir.",
            "toxic" to "Ser tóxico (irritar e desrespeitar) com outros membros do servidor. Aprenda a respeitar e conviver com outras pessoas!",
            "owo" to "Apenas um teste ^-^"
    )

    fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("b", "fastban"), CommandCategory.ADMIN) {
        this.hideInHelp = true
        this.commandCheckFilter {lorittaMessageEvent, _, _, _, _ ->
            lorittaMessageEvent.guild?.idLong == 297732013006389252L
        }

        executesDiscord {
            val bodyguardRole = guild.getRoleById(351473717194522647L)!!
            val author = this.member!!
            val userToBePunished = this.user(0)?.handle
            val reason = this.args.getOrNull(1)?.toLowerCase()
            val proof = this.args.getOrNull(2)
            val settings = ModerationConfigSettings(
                    sendPunishmentViaDm = true,
                    sendPunishmentToPunishLog = true
            )

            if (!author.roles.contains(bodyguardRole)) {
                fail("Você não pode usar o meu super comandinho de banir as pessoas com motivos bonitinhos ;w;")
            }

            if (userToBePunished == null) {
                fail("Usuário inválido!")
            }

            if (reason == null) {
                fail("Motivos disponíveis: `spam, div, divdm, nsfw, toxic`!")
            }

            var fancyReason = punishmentReasons[reason]!!

            reply(
                    LorittaReply(
                            "Punindo `${userToBePunished.asTag}` por `$fancyReason`!",
                            "<a:lori_happy:521721811298156558>"
                    )
            )

            if (proof != null) {
                fancyReason = "[$fancyReason]($proof)"
            }

            BanCommand.ban(
                    settings,
                    this.guild,
                    author.user,
                    loritta.getLegacyLocaleById("default"),
                    userToBePunished,
                    fancyReason,
                    false,
                    0
            )
        }
    }
}