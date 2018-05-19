package com.transcendensoft.handler

import com.transcendensoft.model.BotCommons.Companion.BOT_NAME
import com.transcendensoft.model.BotCommons.Companion.COMMAND_CREATE_POST
import com.transcendensoft.model.BotCommons.Companion.COMMAND_HELP
import com.transcendensoft.model.BotCommons.Companion.COMMAND_START
import com.transcendensoft.model.BotCommons.Companion.HELP_TEXT
import com.transcendensoft.model.BotCommons.Companion.TOKEN
import com.transcendensoft.model.BotCommons.Companion.USER_MAP
import com.transcendensoft.model.BotCommons.Companion.USER_TELEGRAM_ID
import com.transcendensoft.model.Order
import com.transcendensoft.model.TextConstants.Companion.COOL
import com.transcendensoft.model.TextConstants.Companion.ENTER_APARTMENT_TYPE
import com.transcendensoft.model.TextConstants.Companion.ENTER_NAME
import com.transcendensoft.model.TextConstants.Companion.ENTER_PRICE
import com.transcendensoft.model.TextConstants.Companion.GOOD
import com.transcendensoft.model.TextConstants.Companion.START
import com.vdurmont.emoji.EmojiParser
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.*
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton

import org.telegram.telegrambots.exceptions.TelegramApiException

class RentBot : AbilityBot(TOKEN, BOT_NAME) {
    private val userMap = db.getMap<EndUser, Order>(USER_MAP)

    override fun creatorId(): Int = USER_TELEGRAM_ID

    fun helloAbility() = Ability.builder()
            .name(COMMAND_START)
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .input(0)
            .action { ctx -> silent.send(EmojiParser.parseToUnicode(START), ctx.chatId()) }
            .build()

    fun helpAbility() = Ability.builder()
            .name(COMMAND_HELP)
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .input(0)
            .action { ctx -> silent.send(HELP_TEXT.trimMargin(), ctx.chatId()) }
            .build()

    fun createPostAbility() = Ability.builder()
            .name(COMMAND_CREATE_POST)
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .input(0)
            .action {
                val endUser = it.user()
                val order = Order(
                        telegram = endUser.username(),
                        name = "${endUser.lastName()} ${endUser.firstName()}",
                        questionState = Order.QuestionState.ENTER_NAME)
                userMap += (endUser to order)

                silent.send(ENTER_NAME, it.chatId())
            }.build()

    fun enterNameAbility(): Ability {
        var name = ""
        return Ability.builder()
                .name(DEFAULT)
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .flag(Flag.MESSAGE)
                .action {
                    val currentUserOrder = userMap[it.user()]
                    if (currentUserOrder?.questionState == Order.QuestionState.ENTER_NAME) {
                        // Process database
                        currentUserOrder.name = it.update().message?.text ?: ""
                        currentUserOrder.questionState = Order.QuestionState.ENTER_APARTMENT
                        name = currentUserOrder.name

                        // Ask question about apartments
                        val sendMessage = SendMessage(it.chatId(), "${GOOD} ${name}!\n${ENTER_APARTMENT_TYPE}")
                        val inlineKeyboardMarkup = getInlineKeyboard {
                            val inlineKeyboardList = mutableListOf<InlineKeyboardButton>()
                            Order.Apartment.values().forEach {
                                inlineKeyboardList += InlineKeyboardButton(it.infinitiveText)
                                        .setCallbackData(it.callbackData)
                            }
                            inlineKeyboardList
                        }
                        sendMessage.replyMarkup = inlineKeyboardMarkup

                        sendMessageToTelegram(sendMessage)
                    }
                }
                .build()
    }

    private fun getInlineKeyboard(getKeyboardList: () -> MutableList<InlineKeyboardButton>): InlineKeyboardMarkup {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        inlineKeyboardMarkup.keyboard = listOf(getKeyboardList())
        return inlineKeyboardMarkup
    }

    fun apartmentCallbackAbility() = Ability.builder()
            .name(DEFAULT)
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .flag(Flag.CALLBACK_QUERY)
            .action {
                val currentUserOrder = userMap[it.user()]
                if (currentUserOrder?.questionState == Order.QuestionState.ENTER_APARTMENT) {
                    val apartment = Order.Apartment.values()
                            .first { apartment -> it.update()?.callbackQuery?.data == apartment.callbackData }
                    currentUserOrder.apartment = apartment
                    println("Current order: $currentUserOrder")

                    when (it.update()?.callbackQuery?.data) {
                        Order.Apartment.FLAT.callbackData -> {
                            // Process database
                            currentUserOrder.questionState = Order.QuestionState.ENTER_FLAT_ROOMS

                            // Ask question about apartments
                            val sendMessage = SendMessage(it.chatId(), "$COOL\n$ENTER_APARTMENT_TYPE")
                            val inlineKeyboardMarkup = getInlineKeyboard {
                                val inlineKeyboardList = mutableListOf<InlineKeyboardButton>()
                                Order.FlatRooms.values().forEach {
                                    inlineKeyboardList += InlineKeyboardButton(it.infinitiveText)
                                            .setCallbackData(it.callbackData)
                                }
                                inlineKeyboardList
                            }
                            sendMessage.replyMarkup = inlineKeyboardMarkup

                            sendMessageToTelegram(sendMessage)
                        }
                        else -> {
                            currentUserOrder.questionState = Order.QuestionState.ENTER_PRICE
                            silent.send("${COOL}\n${ENTER_PRICE}", it.chatId())
                        }
                    }
                }
            }.build()

    private fun sendMessageToTelegram(sendMessage: SendMessage) {
        try {
            silent.execute(sendMessage)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}