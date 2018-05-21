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
import com.transcendensoft.model.Order.QuestionState.*
import com.transcendensoft.model.TextConstants.Companion.ALMOST_DONE
import com.transcendensoft.model.TextConstants.Companion.AWESOME
import com.transcendensoft.model.TextConstants.Companion.COOL
import com.transcendensoft.model.TextConstants.Companion.CREATE_POST_TEXT
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
import com.transcendensoft.model.TextConstants.Companion.ERROR_SEND_PHOTO
import com.transcendensoft.model.TextConstants.Companion.FINISH
import com.transcendensoft.model.TextConstants.Companion.GOOD
import com.transcendensoft.model.TextConstants.Companion.LAST_STEP
import com.transcendensoft.model.TextConstants.Companion.LOAD_PHOTO
import com.transcendensoft.model.TextConstants.Companion.LOAD_PHOTO_QUESTION
import com.transcendensoft.model.TextConstants.Companion.START
import com.transcendensoft.util.withEmoji
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.*
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow

import org.telegram.telegrambots.exceptions.TelegramApiException
import org.telegram.telegrambots.logging.BotLogger

class RentBot : AbilityBot(TOKEN, BOT_NAME) {
    private val userMap = db.getMap<EndUser, MutableList<Order>>(USER_MAP)

    override fun creatorId(): Int = USER_TELEGRAM_ID

    fun helloAbility() = Ability.builder()
            .name(COMMAND_START)
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .input(0)
            .action { ctx ->
                silent.send(START.withEmoji(), ctx.chatId())
                //println(ResourceBundle.getBundle("i18n/strings", Locale.forLanguageTag("ua_UK")).getString("welcome"))
            }
            .build()

    fun helpAbility() = Ability.builder()
            .name(COMMAND_HELP)
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .input(0)
            .action { ctx ->
                val sendMessage = SendMessage()
                sendMessage.setChatId(ctx.chatId())
                sendMessage.text = HELP_TEXT.trimMargin()
                sendMessage.enableHtml(true)
                sendMessageToTelegram(sendMessage)
            }
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

                var orderList = userMap[endUser]
                if (orderList == null) {
                    orderList = mutableListOf(order)
                } else {
                    orderList.add(order)
                }
                userMap += (endUser to orderList)

                silent.send(CREATE_POST_TEXT.withEmoji(), it.chatId())
                silent.send(ENTER_NAME, it.chatId())
            }.build()

    fun callbackAbility(): Ability {
        return Ability.builder()
                .name(DEFAULT)
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action {
                    val currentUserOrder = userMap[it.user()]?.last()
                    when (currentUserOrder?.questionState) {
                        Order.QuestionState.ENTER_NAME -> processName(it)
                        Order.QuestionState.ENTER_APARTMENT -> processApartmentType(it)
                        Order.QuestionState.ENTER_FLAT_ROOMS -> processFlatRooms(it)
                        Order.QuestionState.ENTER_PRICE -> processPrice(it)
                        Order.QuestionState.ENTER_ADDRESS -> processAddress(it)
                        Order.QuestionState.ENTER_SQUARE -> processSquare(it)
                        Order.QuestionState.ENTER_FACILITIES -> processFacilities(it)
                        Order.QuestionState.ENTER_COMMENT -> processComment(it)
                        Order.QuestionState.ENTER_MASTER -> processMaster(it)
                        Order.QuestionState.ENTER_PHONE -> processPhone(it)
                        Order.QuestionState.ENTER_LOAD_PHOTO -> processLoadPhoto(it)
                        Order.QuestionState.ENTER_LOAD_PHOTO_QUESTION -> processLoadPhotoQuestion(it)
                        Order.QuestionState.FINISHED -> processFinished(it)
                        null -> TODO()
                    }
                }
                .build()
    }

    private fun processName(it: MessageContext) {
        // Process database
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()
        currentUserOrder?.name = it.update().message?.text ?: ""
        currentUserOrder?.questionState = ENTER_APARTMENT
        userMap[it.user()] = orderList

        // Ask question about apartments
        val sendMessage = SendMessage(it.chatId(),
                "${GOOD} ${currentUserOrder?.name}!\n${ENTER_APARTMENT_TYPE}".withEmoji())
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

    private fun processApartmentType(it: MessageContext) {
        sendAnswerToInlineButton(it)
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        val apartment = Order.Apartment.values()
                .first { apartment -> it.update()?.callbackQuery?.data == apartment.callbackData }
        currentUserOrder?.apartment = apartment
        println("Current order: $currentUserOrder")

        when (it.update()?.callbackQuery?.data) {
            Order.Apartment.FLAT.callbackData -> {
                // Process database
                currentUserOrder?.questionState = ENTER_FLAT_ROOMS

                // Ask question about apartments
                val sendMessage = SendMessage(it.chatId(), "$AWESOME\n$ENTER_ROOMS_COUNT".withEmoji())
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
                currentUserOrder?.questionState = Order.QuestionState.ENTER_PRICE
                silent.sendMd("$COOL\n\n$ENTER_PRICE".withEmoji(), it.chatId())
            }
        }
        // Update in db
        userMap[it.user()] = orderList
    }

    private fun processFlatRooms(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        val flatRooms = Order.FlatRooms.values()
                .first { flatRoom -> it.update()?.message?.text == flatRoom.infinitiveText }
        currentUserOrder?.flatRooms = flatRooms
        println("Current order: $currentUserOrder")

        currentUserOrder?.questionState = Order.QuestionState.ENTER_PRICE

        // Update in db
        userMap[it.user()] = orderList

        silent.sendMd("$COOL\n\n$ENTER_PRICE".withEmoji(), it.chatId())
    }

    private fun processPrice(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        currentUserOrder?.price = it.update().message.text
        currentUserOrder?.questionState = Order.QuestionState.ENTER_ADDRESS

        // Update in db
        userMap[it.user()] = orderList
        silent.send(ENTER_ADDRESS.withEmoji(), it.chatId())
    }

    private fun processAddress(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        currentUserOrder?.address = it.update().message.text
        currentUserOrder?.questionState = Order.QuestionState.ENTER_SQUARE

        // Update in db
        userMap[it.user()] = orderList
        silent.send(ENTER_SQUARE, it.chatId())
    }

    private fun processSquare(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        try {
            currentUserOrder?.square = it.update()?.message?.text?.toInt() ?: 0
        } catch (e: NumberFormatException) {
            silent.sendMd(ERROR_ENTER_SQUARE.withEmoji(), it.chatId())
            return
        }

        currentUserOrder?.questionState = Order.QuestionState.ENTER_FACILITIES

        // Update in db
        userMap[it.user()] = orderList
        silent.send(ENTER_FACILITIES, it.chatId())
    }

    private fun processFacilities(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        currentUserOrder?.facilities = it.update().message.text
        currentUserOrder?.questionState = Order.QuestionState.ENTER_COMMENT

        // Update in db
        userMap[it.user()] = orderList
        silent.send("${AWESOME}!\n${ENTER_COMMENT}", it.chatId())
    }

    private fun processComment(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        currentUserOrder?.comment = it.update().message.text
        currentUserOrder?.questionState = Order.QuestionState.ENTER_MASTER

        // Update in db
        userMap[it.user()] = orderList

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

    private fun processMaster(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        sendAnswerToInlineButton(it)

        val master = Order.Master.values()
                .first { master -> it.update()?.callbackQuery?.data == master.callbackData }
        currentUserOrder?.master = master
        currentUserOrder?.questionState = Order.QuestionState.ENTER_PHONE

        // Update in db
        userMap[it.user()] = orderList
        silent.send("$LAST_STEP\n$ENTER_PHONE", it.chatId())
    }

    private fun processPhone(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        currentUserOrder?.phone = it.update().message.text
        currentUserOrder?.questionState = ENTER_LOAD_PHOTO_QUESTION

        // Update in db
        userMap[it.user()] = orderList

        // Ask question about photo
        val sendMessage = SendMessage(it.chatId(), LOAD_PHOTO_QUESTION)
        val inlineKeyboardMarkup = getInlineKeyboard {
            val inlineKeyboardList = mutableListOf<InlineKeyboardButton>()
            Order.Action.values().forEach {
                inlineKeyboardList += InlineKeyboardButton(it.text)
                        .setCallbackData(it.callbackData)
            }
            inlineKeyboardList
        }
        sendMessage.replyMarkup = inlineKeyboardMarkup
        sendMessageToTelegram(sendMessage)
    }

    private fun processLoadPhotoQuestion(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        val action = Order.Action.values()
                .first { action -> it.update()?.callbackQuery?.data == action.callbackData }
        currentUserOrder?.isWithPhoto = action == Order.Action.YES

        when (action) {
            Order.Action.YES -> {
                currentUserOrder?.questionState = ENTER_LOAD_PHOTO
                silent.send(LOAD_PHOTO.withEmoji(), it.chatId())
            }
            Order.Action.NO -> {
                currentUserOrder?.questionState = FINISHED
                //silent.send(FINISH.withEmoji(), it.chatId())
            }
        }

        // Update in db
        userMap[it.user()] = orderList
    }

    private fun processLoadPhoto(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        val update = it.update()
        if (update.hasMessage() && update.message.hasPhoto()) {
            val photoList = update.message.photo
            val photoIdList = photoList.map { photoSize -> photoSize.fileId }

            currentUserOrder?.photoIds = photoIdList
            currentUserOrder?.questionState = FINISHED
        } else {
            silent.send(ERROR_SEND_PHOTO, it.chatId())
        }

        // Update in db
        userMap[it.user()] = orderList
    }

    private fun processFinished(msgContext: MessageContext) {
        val orderList = userMap[msgContext.user()]
        val currentUserOrder = orderList?.last()

        silent.send(FINISH.withEmoji(), msgContext.chatId())

        sendFinalPostToChat(msgContext, currentUserOrder)
    }

    private fun sendFinalPostToChat(msgContext: MessageContext, currentUserOrder: Order?) {
        val sendMessageWithOrder = SendMessage(msgContext.chatId(), currentUserOrder?.createPost()?.withEmoji())
        sendMessageWithOrder.enableHtml(true)
        sendMessageToTelegram(sendMessageWithOrder)

        currentUserOrder?.isWithPhoto?.let {
            currentUserOrder.photoIds?.let {
                val inputMediaPhotos = it.map { fileId -> InputMediaPhoto(fileId, null) }
                val mediaGroup = SendMediaGroup(msgContext.chatId(), inputMediaPhotos)
                try {
                    sendMediaGroup(mediaGroup)
                } catch (e: TelegramApiException) {
                    e.printStackTrace()
                }
            }
        }
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