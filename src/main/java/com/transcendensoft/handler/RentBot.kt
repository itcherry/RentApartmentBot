package com.transcendensoft.handler

import com.transcendensoft.model.BotCommons
import com.transcendensoft.model.BotCommons.Companion.BOT_NAME
import com.transcendensoft.model.BotCommons.Companion.COMMAND_CANCEL
import com.transcendensoft.model.BotCommons.Companion.COMMAND_CREATE_POST
import com.transcendensoft.model.BotCommons.Companion.COMMAND_HELP
import com.transcendensoft.model.BotCommons.Companion.COMMAND_SEND_MESSAGE
import com.transcendensoft.model.BotCommons.Companion.COMMAND_START
import com.transcendensoft.model.BotCommons.Companion.HELP_TEXT
import com.transcendensoft.model.BotCommons.Companion.ID_OF_GROUP_WITH_POSTS
import com.transcendensoft.model.BotCommons.Companion.KVARTIR_HUB_CHAT_ID
import com.transcendensoft.model.BotCommons.Companion.PARAMETER_ORDER_ID
import com.transcendensoft.model.BotCommons.Companion.PARAMETER_USER_ID
import com.transcendensoft.model.BotCommons.Companion.SHARE_ACTION
import com.transcendensoft.model.BotCommons.Companion.SHARE_TO_CHANNEL_CALLBACK
import com.transcendensoft.model.BotCommons.Companion.TOKEN
import com.transcendensoft.model.BotCommons.Companion.USER_MAP
import com.transcendensoft.model.BotCommons.Companion.USER_TELEGRAM_ID
import com.transcendensoft.model.Order
import com.transcendensoft.model.Order.QuestionState.*
import com.transcendensoft.model.TextConstants.Companion.ALMOST_DONE
import com.transcendensoft.model.TextConstants.Companion.APARTMENT_STATE_CHANGED
import com.transcendensoft.model.TextConstants.Companion.AWESOME
import com.transcendensoft.model.TextConstants.Companion.CANCELLED
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
import com.transcendensoft.model.TextConstants.Companion.ERROR_CANCEL
import com.transcendensoft.model.TextConstants.Companion.ERROR_ENTER_SQUARE
import com.transcendensoft.model.TextConstants.Companion.ERROR_RENTED_COMMAND
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
import org.telegram.telegrambots.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.api.objects.PhotoSize
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow

import org.telegram.telegrambots.exceptions.TelegramApiException
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

    fun cancelAbility() = Ability.builder()
            .name(COMMAND_CANCEL)
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .input(0)
            .action {
                // Process database
                val orderList = userMap[it.user()]
                val currentUserOrder = orderList?.last()

                if (currentUserOrder?.questionState !in setOf(Order.QuestionState.FINISHED, null) && currentUserOrder != null) {
                    orderList.remove(currentUserOrder)
                    silent.send(CANCELLED, it.chatId())
                } else {
                    silent.send(ERROR_CANCEL.withEmoji(), it.chatId())
                }

                userMap[it.user()] = orderList
            }.build()

    fun createPostAbility() = Ability.builder()
            .name(COMMAND_CREATE_POST)
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .input(0)
            .action {
                val endUser = it.user()

                var orderList = userMap[endUser]
                if (orderList == null) {
                    orderList = mutableListOf()
                }

                val order = Order(
                        id = orderList.size + 1,
                        telegram = endUser.username(),
                        name = "${endUser.lastName()} ${endUser.firstName()}",
                        questionState = Order.QuestionState.ENTER_NAME,
                        chatId = it.chatId())

                orderList.add(order)
                userMap += (endUser to orderList)

                silent.send(CREATE_POST_TEXT.withEmoji(), it.chatId())
                silent.send(ENTER_NAME, it.chatId())
            }.build()

    fun setApartmentsStateAbility() = Ability.builder()
            .name(BotCommons.COMMAND_APARTMENT_STATE)
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .input(0)
            .action {
                printAllOrdersAndProcessRented(it)
            }
            .build()

    private fun printAllOrdersAndProcessRented(msgContext: MessageContext) {
        val publishedPosts = userMap[msgContext.user()]?.filter { it.sharedMessageId != null }
        if (publishedPosts != null && !publishedPosts.isEmpty()) {
            publishedPosts.forEach { order ->
                val sendMessageWithOrder = SendMessage(msgContext.chatId(),
                        order.createPost().withEmoji())
                sendMessageWithOrder.enableHtml(true)
                sendMessageWithOrder.replyMarkup = inlineKeyboardForApartmentState(order)
                sendMessageToTelegram(sendMessageWithOrder)

                sendPhotosAlbum(msgContext.chatId(), order)
            }
        } else {
            silent.send(ERROR_RENTED_COMMAND, msgContext.chatId())
        }
    }

    private fun inlineKeyboardForApartmentState(order: Order): InlineKeyboardMarkup? {
        val apartmentState = if (order.isFree) {
            Order.ApartmentState.RENTED
        } else Order.ApartmentState.FREE
        val inlineButton = InlineKeyboardButton(apartmentState.text)
                .setCallbackData("${apartmentState.callbackData}_${order.sharedMessageId}")
        val inlineKeyboard = InlineKeyboardMarkup()
                .setKeyboard(listOf(listOf(inlineButton)))
        return inlineKeyboard
    }

    fun callbackAbility(): Ability {
        return Ability.builder()
                .name(DEFAULT)
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action {
                    val currentUserOrder = userMap[it.user()]?.last()

                    if (processShareToChannelCallback(it)) return@action
                    if (processApartmentStateCallback(it)) return@action

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

    private fun RentBot.processShareToChannelCallback(msgContext: MessageContext): Boolean {
        val callbackData = msgContext.update()?.callbackQuery?.data
        callbackData?.let {
            if (it.startsWith(SHARE_TO_CHANNEL_CALLBACK, ignoreCase = true)) {
                val destructuredRegex = ("$SHARE_TO_CHANNEL_CALLBACK\\?" +
                        "$PARAMETER_USER_ID=(\\d+)&$PARAMETER_ORDER_ID=(\\d+)").toRegex()

                destructuredRegex.matchEntire(it)
                        ?.destructured
                        ?.let { (userId, orderId) ->
                            val user = userMap.keys.firstOrNull { endUser -> endUser.id() == userId.toInt() }
                            val orderList = userMap[user]
                            val order = orderList?.firstOrNull { localOrder -> localOrder.id == orderId.toInt() }
                            order?.let {
                                val msg = SendMessage(KVARTIR_HUB_CHAT_ID, it.createPost().withEmoji())
                                msg.enableHtml(true)
                                it.sharedMessageId = sendMessage(msg)?.messageId

                                userMap[user] = orderList
                                return true
                            }
                        }
                        ?: TODO()
            }
        }
        return false
    }

    private fun RentBot.processApartmentStateCallback(msgContext: MessageContext): Boolean {
        val callbackData = msgContext.update()?.callbackQuery?.data
        callbackData?.let {
            val orderMessageIdStr = it.substringAfterLast("_")
            val orderMsgId = try {
                orderMessageIdStr.toInt()
            } catch (e: NumberFormatException) {
                return@let
            }
            val orderList = userMap[msgContext.user()]
            val order = orderList?.firstOrNull { post -> post.sharedMessageId == orderMsgId }
            if (it.startsWith(Order.ApartmentState.FREE.callbackData, ignoreCase = true)) {
                order?.isFree = true
            } else if (it.startsWith(Order.ApartmentState.RENTED.callbackData, ignoreCase = true)) {
                order?.isFree = false
            }

            userMap[msgContext.user()] = orderList

            val editMessageTextKvartirHub = EditMessageText()
            editMessageTextKvartirHub.chatId = KVARTIR_HUB_CHAT_ID
            editMessageTextKvartirHub.messageId = orderMsgId
            editMessageTextKvartirHub.text = order?.createPost()?.withEmoji()
            editMessageTextKvartirHub.enableHtml(true)

            silent.execute(editMessageTextKvartirHub)

            val editMessageTextInnerChat = EditMessageText()
            editMessageTextInnerChat.chatId = msgContext.chatId().toString()
            editMessageTextInnerChat.messageId = msgContext.update()?.callbackQuery?.message?.messageId
            editMessageTextInnerChat.text = order?.createPost()?.withEmoji()
            editMessageTextInnerChat.replyMarkup = inlineKeyboardForApartmentState(order!!)
            editMessageTextInnerChat.enableHtml(true)

            silent.execute(editMessageTextInnerChat)

            val answerCallbackQuery = AnswerCallbackQuery()
            answerCallbackQuery.callbackQueryId = msgContext.update().callbackQuery?.id
            answerCallbackQuery.showAlert = true
            silent.execute(answerCallbackQuery)

            silent.send(APARTMENT_STATE_CHANGED.withEmoji(), msgContext.chatId())

            return true
        }
        return false
    }

    private fun processName(it: MessageContext) {
        // Process database
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()
        currentUserOrder?.name = it.update().message?.text ?: ""
        currentUserOrder?.questionState = ENTER_APARTMENT
        userMap[it.user()] = orderList

        askAboutApartment(it, currentUserOrder)
    }

    private fun askAboutApartment(it: MessageContext, currentUserOrder: Order?) {
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
                .firstOrNull { apartment -> it.update()?.callbackQuery?.data == apartment.callbackData }
        currentUserOrder?.apartment = apartment

        when (it.update()?.callbackQuery?.data) {
            Order.Apartment.FLAT.callbackData -> {
                // Process database
                currentUserOrder?.questionState = ENTER_FLAT_ROOMS

                askAboutRoomQuantity(it)
            }
            null -> askAboutApartment(it, currentUserOrder)
            else -> {
                currentUserOrder?.questionState = Order.QuestionState.ENTER_PRICE
                val msg = SendMessage(it.chatId(), "$COOL\n\n$ENTER_PRICE".withEmoji())
                msg.enableHtml(true)
                silent.execute(msg)
            }
        }
        // Update in db
        userMap[it.user()] = orderList
    }

    private fun askAboutRoomQuantity(it: MessageContext) {
        // Ask question about room quantity
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

    private fun processFlatRooms(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        val flatRooms = Order.FlatRooms.values()
                .firstOrNull { flatRoom -> it.update()?.message?.text == flatRoom.infinitiveText }

        if (flatRooms != null) {
            currentUserOrder?.flatRooms = flatRooms
            println("Current order: $currentUserOrder")

            currentUserOrder?.questionState = Order.QuestionState.ENTER_PRICE

            // Update in db
            userMap[it.user()] = orderList

            val msg = SendMessage(it.chatId(), "$COOL\n\n$ENTER_PRICE".withEmoji())
            msg.enableHtml(true)
            silent.execute(msg)
        } else {
            askAboutRoomQuantity(it)
        }
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

        askAboutMaster(it)
    }

    private fun askAboutMaster(it: MessageContext) {
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
                .firstOrNull() { master -> it.update()?.callbackQuery?.data == master.callbackData }
        if (master != null) {
            currentUserOrder?.master = master
            currentUserOrder?.questionState = Order.QuestionState.ENTER_PHONE

            // Update in db
            userMap[it.user()] = orderList
            silent.send("$LAST_STEP\n$ENTER_PHONE", it.chatId())
        } else {
            askAboutMaster(it)
        }
    }

    private fun processPhone(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        currentUserOrder?.phone = it.update().message.text
        currentUserOrder?.questionState = ENTER_LOAD_PHOTO_QUESTION

        // Update in db
        userMap[it.user()] = orderList

        askQuestionAboutPhoto(it)
    }

    private fun askQuestionAboutPhoto(it: MessageContext) {
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
                .firstOrNull { action -> it.update()?.callbackQuery?.data == action.callbackData }
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
            null -> askQuestionAboutPhoto(it)
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

                    val inlineKeyboard = InlineKeyboardMarkup()
                    val callbackData = "$SHARE_TO_CHANNEL_CALLBACK?" +
                            "$PARAMETER_USER_ID=${msgContext.user()?.id()}&" +
                            "$PARAMETER_ORDER_ID=${currentUserOrder?.id}"

                    inlineKeyboard.keyboard = listOf(listOf(
                            InlineKeyboardButton(SHARE_ACTION)
                                    .setCallbackData(callbackData)))
                    sendMessageWithOrder.replyMarkup = inlineKeyboard
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

                    sendOrderText(msgContext.chatId(), currentUserOrder)

                    askAboutCorrectnessOfPost(msgContext.chatId())
                }
            }
        }

        userMap[msgContext.user()] = orderList
    }

    private fun sendOrderText(chatId: Long, order: Order?) {
        val sendMessageWithOrder = SendMessage(chatId,
                order?.createPost()?.withEmoji())
        sendMessageWithOrder.enableHtml(true)
        sendMessageToTelegram(sendMessageWithOrder)
    }

    private fun sendPhotosAlbum(chatId: Long, order: Order?) {
        order?.isWithPhoto?.let {
            if(it) {
                order.photoIds?.let {
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
        answerCallbackQuery.callbackQueryId = it.update().callbackQuery?.id
        silent.execute(answerCallbackQuery)
    }
}