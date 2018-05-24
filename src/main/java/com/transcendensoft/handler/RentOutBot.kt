package com.transcendensoft.handler

import com.transcendensoft.model.rent_out.RentOutBotCommons
import com.transcendensoft.model.rent_out.RentOutBotCommons.Companion.BOT_NAME
import com.transcendensoft.model.rent_out.RentOutBotCommons.Companion.TOKEN
import com.transcendensoft.model.rent_out.RentOutBotCommons.Companion.USER_TELEGRAM_ID
import org.telegram.abilitybots.api.bot.AbilityBot

class RentOutBot : AbilityBot(TOKEN, BOT_NAME) {
    override fun creatorId(): Int = USER_TELEGRAM_ID


}