package com.transcendensoft.handler

import com.transcendensoft.model.BotCommons.Companion.BOT_NAME
import com.transcendensoft.model.BotCommons.Companion.COMMAND_CREATE_POST
import com.transcendensoft.model.BotCommons.Companion.COMMAND_HELP
import com.transcendensoft.model.BotCommons.Companion.COMMAND_SEND_MESSAGE
import com.transcendensoft.model.BotCommons.Companion.COMMAND_START
import com.transcendensoft.model.BotCommons.Companion.HELP_TEXT
import com.transcendensoft.model.BotCommons.Companion.ID_OF_GROUP_WITH_POSTS
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
import com.transcendensoft.model.TextConstants.Companion.POST_PREVIEW
import com.transcendensoft.model.TextConstants.Companion.PUBLISH_CANCELLED
import com.transcendensoft.model.TextConstants.Companion.START
import com.transcendensoft.model.TextConstants.Companion.SURE_TO_PUBLISH
import com.transcendensoft.util.withEmoji
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.*
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.api.methods.BotApiMethod
import org.telegram.telegrambots.api.methods.ForwardMessage
import org.telegram.telegrambots.api.methods.groupadministration.DeleteChatPhoto
import org.telegram.telegrambots.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.methods.send.SendPhoto
import org.telegram.telegrambots.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageCaption
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.PhotoSize
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow

import org.telegram.telegrambots.exceptions.TelegramApiException
import org.telegram.telegrambots.exceptions.TelegramApiRequestException
import org.telegram.telegrambots.logging.BotLogger
import org.telegram.telegrambots.updateshandlers.SentCallback
import java.lang.Exception
import java.util.*
import kotlin.concurrent.schedule

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

    fun sendMessageToUserAbility() = Ability.builder() // /sendmessage itcherry 'someMessage'
            .name(COMMAND_SEND_MESSAGE)
            .locality(Locality.USER)
            .privacy(Privacy.CREATOR)
            .action { ctx ->
                val endUser = userMap.keys.first {
                    it.username() == ctx.firstArg()
                }
                val userOrder = userMap[endUser]?.last()
                if (userOrder != null) {
                    val textList = ctx.arguments().slice(1 until ctx.arguments().size)
                    val text = textList.joinToString(separator = " ")
                    silent.send("Модератор: $text", userOrder.chatId)
                } else {
                    silent.send("Такого пользователя не существует в БД.", ctx.chatId())
                }
            }.build()

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
                        questionState = Order.QuestionState.ENTER_NAME,
                        chatId = it.chatId())

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
                        null -> silent.send(START.withEmoji(), it.chatId())
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
        val sendMessage = SendMessage(it.chatId(), LOAD_PHOTO_QUESTION.withEmoji())
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
                processFinished(it)
            }
        }
        sendAnswerToInlineButton(it)


        // Update in db
        userMap[it.user()] = orderList
    }

    private fun processLoadPhoto(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        val update = it.update()
        if (update.hasMessage() && update.message.hasPhoto()) {
            processPhoto(update, currentUserOrder)

            val photoCount = currentUserOrder?.photoIds?.size
            Timer("schedule", true).schedule(1500) {
                val innerOrderList = userMap[it.user()]
                val innerCurrentUserOrder = innerOrderList?.last()
                if (photoCount == innerCurrentUserOrder?.photoIds?.size) {
                    onPhotoLoadingFinished(it.chatId(), innerCurrentUserOrder)

                    // Update in db
                    userMap[it.user()] = innerOrderList
                }
            }
        } else {
            silent.send(ERROR_SEND_PHOTO, it.chatId())
        }

        // Update in db
        userMap[it.user()] = orderList
    }

    private fun processPhoto(update: Update, currentUserOrder: Order?) {
        val photoList = update.message.photo
        val largestPhotoFileId = photoList.maxBy(PhotoSize::getFileSize)?.fileId

        currentUserOrder?.photoIds?.add(largestPhotoFileId)
    }

    private fun onPhotoLoadingFinished(chatId: Long, currentUserOrder: Order?) {
        currentUserOrder?.questionState = FINISHED

        val sendMessageFinish = SendMessage(chatId, POST_PREVIEW.withEmoji())
        sendMessageFinish.enableHtml(true)
        sendMessageToTelegram(sendMessageFinish)

        val sendMessageWithOrder = SendMessage(chatId, currentUserOrder?.createPost()?.withEmoji())
        sendMessageWithOrder.enableHtml(true)
        sendMessageToTelegram(sendMessageWithOrder)

        sendPhotosAlbum(chatId, currentUserOrder)

        askAboutCorrectnessOfPost(chatId)
    }

    private fun askAboutCorrectnessOfPost(chatId: Long) {
        // Ask question about correctness of publication
        val sendMessage = SendMessage(chatId, SURE_TO_PUBLISH.withEmoji())
        sendMessage.enableHtml(true)
        val inlineKeyboardMarkup = getInlineKeyboard {
            val inlineKeyboardList = mutableListOf<InlineKeyboardButton>()
            Order.PublishState.values().forEach {
                inlineKeyboardList += InlineKeyboardButton(it.text)
                        .setCallbackData(it.callbackData)
            }
            inlineKeyboardList
        }
        sendMessage.replyMarkup = inlineKeyboardMarkup
        sendMessageToTelegram(sendMessage)
    }

    private fun processFinished(msgContext: MessageContext) {
        val orderList = userMap[msgContext.user()]
        val currentUserOrder = orderList?.last()

        if (msgContext.update().hasCallbackQuery()) {
            sendAnswerToInlineButton(msgContext)
            val publishState = Order.PublishState.values()
                    .firstOrNull { publish -> msgContext.update()?.callbackQuery?.data == publish.callbackData }
            when (publishState) {
                Order.PublishState.PUBLISH -> {
                    val sendMessageWithOrder = SendMessage(ID_OF_GROUP_WITH_POSTS,
                            currentUserOrder?.createPost()?.withEmoji())
                    sendMessageWithOrder.enableHtml(true)
                    sendMessageToTelegram(sendMessageWithOrder)

                    sendPhotosAlbum(ID_OF_GROUP_WITH_POSTS, currentUserOrder)

                    val sendMessagePublished = SendMessage(msgContext.chatId(), FINISH.withEmoji())
                    sendMessagePublished.enableHtml(true)
                    sendMessageToTelegram(sendMessagePublished)
                }
                Order.PublishState.CANCEL -> {
                    silent.send(PUBLISH_CANCELLED, msgContext.chatId())
                    helpAbility().action().accept(msgContext)
                    orderList?.remove(currentUserOrder)
                }
                null -> {
                    val sendMessagePreview = SendMessage(msgContext.chatId(), POST_PREVIEW.withEmoji())
                    sendMessagePreview.enableHtml(true)
                    sendMessageToTelegram(sendMessagePreview)

                    val sendMessageWithOrder = SendMessage(msgContext.chatId(),
                            currentUserOrder?.createPost()?.withEmoji())
                    sendMessageWithOrder.enableHtml(true)
                    sendMessageToTelegram(sendMessageWithOrder)

                    askAboutCorrectnessOfPost(msgContext.chatId())
                }
            }
        }

        userMap[msgContext.user()] = orderList
    }

    private fun sendPhotosAlbum(chatId: Long, currentUserOrder: Order?) {
        currentUserOrder?.isWithPhoto?.let {
            currentUserOrder.photoIds?.let {
                val inputMediaPhotos = it.map { fileId -> InputMediaPhoto(fileId, null) }
                val mediaGroup = SendMediaGroup(chatId, inputMediaPhotos)
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