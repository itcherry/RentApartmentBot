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
import com.transcendensoft.model.TextConstants.Companion.ALMOST_DONE
import com.transcendensoft.model.TextConstants.Companion.AWESOME
import com.transcendensoft.model.TextConstants.Companion.COOL
import com.transcendensoft.model.TextConstants.Companion.ENTER_ADDRESS
import com.transcendensoft.model.TextConstants.Companion.ENTER_APARTMENT_TYPE
import com.transcendensoft.model.TextConstants.Companion.ENTER_COMMENT
import com.transcendensoft.model.TextConstants.Companion.ENTER_FACILITIES
import com.transcendensoft.model.TextConstants.Companion.ENTER_MASTER
import com.transcendensoft.model.TextConstants.Companion.ENTER_NAME
import com.transcendensoft.model.TextConstants.Companion.ENTER_PHONE
import com.transcendensoft.model.TextConstants.Companion.ENTER_PRICE
import com.transcendensoft.model.TextConstants.Companion.ENTER_ROOMS_COUNT
import com.transcendensoft.model.TextConstants.Companion.ENTER_SQUARE
import com.transcendensoft.model.TextConstants.Companion.ERROR_ENTER_SQUARE
import com.transcendensoft.model.TextConstants.Companion.GOOD
import com.transcendensoft.model.TextConstants.Companion.LAST_STEP
import com.transcendensoft.model.TextConstants.Companion.LOAD_PHOTO
import com.transcendensoft.model.TextConstants.Companion.START
import com.vdurmont.emoji.EmojiParser
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.*
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.bots.TelegramLongPollingBot

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
                db.clear()
                userMap += (endUser to order)

                silent.send(ENTER_NAME, it.chatId())
            }.build()

    fun callbackAbility(): Ability {
        return Ability.builder()
                .name(DEFAULT)
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action {
                    val currentUserOrder = userMap[it.user()]
                    when (currentUserOrder?.questionState) {
                        Order.QuestionState.ENTER_NAME -> processName(currentUserOrder, it)
                        Order.QuestionState.ENTER_APARTMENT -> processApartmentType(it, currentUserOrder)
                        Order.QuestionState.ENTER_FLAT_ROOMS -> processFlatRooms(it, currentUserOrder)
                        Order.QuestionState.ENTER_PRICE -> processPrice(it, currentUserOrder)
                        Order.QuestionState.ENTER_ADDRESS -> processAddress(it, currentUserOrder)
                        Order.QuestionState.ENTER_SQUARE -> processSquare(it, currentUserOrder)
                        Order.QuestionState.ENTER_FACILITIES -> processFacilities(it, currentUserOrder)
                        Order.QuestionState.ENTER_COMMENT -> processComment(it, currentUserOrder)
                        Order.QuestionState.ENTER_MASTER -> processMaster(it, currentUserOrder)
                        Order.QuestionState.ENTER_PHONE -> processPhone(it, currentUserOrder)
                        Order.QuestionState.ENTER_LOAD_PHOTO -> TODO()
                        null -> TODO()
                    }
                }
                .build()
    }

    private fun processName(currentUserOrder: Order?, it: MessageContext) {
        // Process database
        currentUserOrder?.name = it.update().message?.text ?: ""
        currentUserOrder?.questionState = Order.QuestionState.ENTER_APARTMENT
        userMap[it.user()] = currentUserOrder

        // Ask question about apartments
        val sendMessage = SendMessage(it.chatId(),
                "${GOOD} ${currentUserOrder?.name}!\n${ENTER_APARTMENT_TYPE}")
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

    private fun processApartmentType(it: MessageContext, currentUserOrder: Order) {
        sendAnswerToInlineButton(it)

        val apartment = Order.Apartment.values()
                .first { apartment -> it.update()?.callbackQuery?.data == apartment.callbackData }
        currentUserOrder.apartment = apartment
        println("Current order: $currentUserOrder")

        when (it.update()?.callbackQuery?.data) {
            Order.Apartment.FLAT.callbackData -> {
                // Process database
                currentUserOrder.questionState = Order.QuestionState.ENTER_FLAT_ROOMS

                // Ask question about apartments
                val sendMessage = SendMessage(it.chatId(), "$COOL\n$ENTER_ROOMS_COUNT")
                val replyKeyboardMarkup = ReplyKeyboardMarkup()

                val keyboardRows = mutableListOf<KeyboardRow>()
                Order.FlatRooms.values().forEach {
                    val keyboardRow = KeyboardRow()
                    keyboardRow += KeyboardButton(it.infinitiveText)
                    keyboardRows += keyboardRow
                }
                replyKeyboardMarkup.keyboard = keyboardRows
                replyKeyboardMarkup.oneTimeKeyboard = true

                sendMessage.replyMarkup = replyKeyboardMarkup
                sendMessageToTelegram(sendMessage)
            }
            else -> {
                currentUserOrder.questionState = Order.QuestionState.ENTER_PRICE
                silent.send("${COOL}\n${ENTER_PRICE}", it.chatId())
            }
        }
        // Update in db
        userMap[it.user()] = currentUserOrder
    }

    private fun processFlatRooms(it: MessageContext, currentUserOrder: Order) {
        val flatRooms = Order.FlatRooms.values()
                .first { flatRoom -> it.update()?.message?.text == flatRoom.infinitiveText }
        currentUserOrder.flatRooms = flatRooms
        println("Current order: $currentUserOrder")

        currentUserOrder.questionState = Order.QuestionState.ENTER_PRICE

        // Update in db
        userMap[it.user()] = currentUserOrder

        silent.send("${COOL}\n${ENTER_PRICE}", it.chatId())
    }

    private fun processPrice(it: MessageContext, currentUserOrder: Order) {
        currentUserOrder.price = it.update().message.text
        currentUserOrder.questionState = Order.QuestionState.ENTER_ADDRESS

        // Update in db
        userMap[it.user()] = currentUserOrder
        silent.send(ENTER_ADDRESS, it.chatId())
    }

    private fun processAddress(it: MessageContext, currentUserOrder: Order) {
        currentUserOrder.address = it.update().message.text
        currentUserOrder.questionState = Order.QuestionState.ENTER_SQUARE

        // Update in db
        userMap[it.user()] = currentUserOrder
        silent.send(ENTER_SQUARE, it.chatId())
    }

    private fun processSquare(it: MessageContext, currentUserOrder: Order) {
        try {
            currentUserOrder.square = it.update()?.message?.text?.toInt() ?: 0
        } catch (e: NumberFormatException) {
            silent.sendMd(EmojiParser.parseToUnicode(ERROR_ENTER_SQUARE), it.chatId())
            return
        }

        currentUserOrder.questionState = Order.QuestionState.ENTER_FACILITIES

        // Update in db
        userMap[it.user()] = currentUserOrder
        silent.send(ENTER_FACILITIES, it.chatId())
    }

    private fun processFacilities(it: MessageContext, currentUserOrder: Order) {
        currentUserOrder.facilities = it.update().message.text
        currentUserOrder.questionState = Order.QuestionState.ENTER_COMMENT

        // Update in db
        userMap[it.user()] = currentUserOrder
        silent.send("${AWESOME}!\n${ENTER_COMMENT}", it.chatId())
    }

    private fun processComment(it: MessageContext, currentUserOrder: Order) {
        currentUserOrder.comment = it.update().message.text
        currentUserOrder.questionState = Order.QuestionState.ENTER_MASTER

        // Update in db
        userMap[it.user()] = currentUserOrder

        // Ask question about master
        val sendMessage = SendMessage(it.chatId(),
                "${ALMOST_DONE}!\n${ENTER_MASTER}")
        val inlineKeyboardMarkup = getInlineKeyboard {
            val inlineKeyboardList = mutableListOf<InlineKeyboardButton>()
            Order.Master.values().forEach {
                inlineKeyboardList += InlineKeyboardButton(it.text)
                        .setCallbackData(it.callbackData)
            }
            inlineKeyboardList
        }
        sendMessage.replyMarkup = inlineKeyboardMarkup

        sendMessageToTelegram(sendMessage)
    }

    private fun processMaster(it: MessageContext, currentUserOrder: Order) {
        sendAnswerToInlineButton(it)

        val master = Order.Master.values()
                .first { master -> it.update()?.callbackQuery?.data == master.callbackData }
        currentUserOrder.master = master
        currentUserOrder.questionState = Order.QuestionState.ENTER_PHONE

        // Update in db
        userMap[it.user()] = currentUserOrder
        silent.send("$LAST_STEP\n$ENTER_PHONE", it.chatId())
    }

    private fun processPhone(it: MessageContext, currentUserOrder: Order) {
        currentUserOrder.phone = it.update().message.text
        currentUserOrder.questionState = Order.QuestionState.ENTER_LOAD_PHOTO

        // Update in db
        userMap[it.user()] = currentUserOrder
        silent.send(LOAD_PHOTO, it.chatId())
    }

    private fun sendMessageToTelegram(sendMessage: SendMessage) {
        try {
            silent.execute(sendMessage)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    private fun getInlineKeyboard(getKeyboardList: () -> MutableList<InlineKeyboardButton>): InlineKeyboardMarkup {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        inlineKeyboardMarkup.keyboard = listOf(getKeyboardList())
        return inlineKeyboardMarkup
    }

    private fun sendAnswerToInlineButton(it: MessageContext) {
        val answerCallbackQuery = AnswerCallbackQuery()
        answerCallbackQuery.callbackQueryId = it.update().callbackQuery.id
        silent.execute(answerCallbackQuery)
    }
}