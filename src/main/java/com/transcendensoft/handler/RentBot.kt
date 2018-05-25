package com.transcendensoft.handler

import com.transcendensoft.model.Action
import com.transcendensoft.model.Apartment
import com.transcendensoft.model.FlatRooms
import com.transcendensoft.model.PublishState
import com.transcendensoft.model.rent.RentBotCommons
import com.transcendensoft.model.rent.RentBotCommons.Companion.BOT_NAME
import com.transcendensoft.model.rent.RentBotCommons.Companion.COMMAND_CANCEL
import com.transcendensoft.model.rent.RentBotCommons.Companion.COMMAND_CREATE_POST
import com.transcendensoft.model.rent.RentBotCommons.Companion.COMMAND_HELP
import com.transcendensoft.model.rent.RentBotCommons.Companion.COMMAND_SEND_MESSAGE
import com.transcendensoft.model.rent.RentBotCommons.Companion.COMMAND_START
import com.transcendensoft.model.rent.RentBotCommons.Companion.HELP_TEXT
import com.transcendensoft.model.rent.RentBotCommons.Companion.ID_OF_GROUP_WITH_POSTS
import com.transcendensoft.model.rent.RentBotCommons.Companion.KVARTIR_HUB_CHAT_ID
import com.transcendensoft.model.rent.RentBotCommons.Companion.PARAMETER_ORDER_ID
import com.transcendensoft.model.rent.RentBotCommons.Companion.PARAMETER_USER_ID
import com.transcendensoft.model.rent.RentBotCommons.Companion.SHARE_ACTION
import com.transcendensoft.model.rent.RentBotCommons.Companion.SHARE_TO_CHANNEL_CALLBACK
import com.transcendensoft.model.rent.RentBotCommons.Companion.TOKEN
import com.transcendensoft.model.rent.RentBotCommons.Companion.USER_MAP
import com.transcendensoft.model.rent.RentBotCommons.Companion.USER_TELEGRAM_ID
import com.transcendensoft.model.rent.RentPost
import com.transcendensoft.model.rent.RentPost.QuestionState.*
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.ALMOST_DONE
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.APARTMENT_STATE_CHANGED
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.AWESOME
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.CANCELLED
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.COOL
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.CREATE_POST_TEXT
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.ENTER_ADDRESS
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.ENTER_APARTMENT_TYPE
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.ENTER_COMMENT
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.ENTER_FACILITIES
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.ENTER_MASTER
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.ENTER_NAME
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.ENTER_PHONE
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.ENTER_PRICE
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.ENTER_ROOMS_COUNT
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.ENTER_SQUARE
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.ERROR_CANCEL
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.ERROR_ENTER_SQUARE
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.ERROR_RENTED_COMMAND
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.ERROR_SEND_PHOTO
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.FINISH
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.GOOD
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.LAST_STEP
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.LOAD_PHOTO
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.LOAD_PHOTO_QUESTION
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.POST_PREVIEW
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.PUBLISH_CANCELLED
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.START
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.SURE_TO_PUBLISH
import com.transcendensoft.util.logger
import com.transcendensoft.util.withEmoji
import org.mapdb.CC.LOG
import org.slf4j.LoggerFactory
import org.slf4j.MDC
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
import org.telegram.telegrambots.bots.DefaultBotOptions

import org.telegram.telegrambots.exceptions.TelegramApiException
import org.telegram.telegrambots.logging.BotLogger
import java.util.*
import java.util.logging.Logger
import kotlin.concurrent.schedule

class RentBot() : AbilityBot(TOKEN, BOT_NAME) {
    companion object {
        val LOG by logger()
    }

    init {
        System.setProperty("log.name", "RentBotLogs");
    }

    private val userMap = db.getMap<EndUser, MutableList<RentPost>>(USER_MAP)

    override fun creatorId(): Int = USER_TELEGRAM_ID

    fun helloAbility() = Ability.builder()
            .name(COMMAND_START)
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .input(0)
            .action { ctx ->
                silent.send(START.withEmoji(), ctx.chatId())
                LOG.info("User ${ctx.user()?.username()} started bot.")
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

                LOG.info("User ${ctx.user()?.username()} ask help.")
            }
            .build()

    fun sendMessageToUserAbility() = Ability.builder() // /sendmessage itcherry 'someMessage'
            .name(COMMAND_SEND_MESSAGE)
            .locality(Locality.ALL)
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

                    LOG.info("Creator send message to ${endUser?.username()}. Message: $text")
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
                val currentUserOrder = orderList?.lastOrNull()

                if (currentUserOrder?.questionState !in setOf(RentPost.QuestionState.FINISHED, null) && currentUserOrder != null) {
                    orderList.remove(currentUserOrder)
                    silent.send(CANCELLED, it.chatId())
                    LOG.info("User ${it.user()?.username()} cancelled order $currentUserOrder")
                } else {
                    silent.send(ERROR_CANCEL.withEmoji(), it.chatId())
                    LOG.info("User ${it.user()?.username()} invokes error of cancel order $currentUserOrder")
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

                val order = RentPost(
                        id = orderList.size + 1,
                        telegram = endUser.username(),
                        name = "${endUser.lastName()} ${endUser.firstName()}",
                        questionState = RentPost.QuestionState.ENTER_NAME,
                        chatId = it.chatId())

                orderList.add(order)
                userMap += (endUser to orderList)

                silent.send(CREATE_POST_TEXT.withEmoji(), it.chatId())
                silent.send(ENTER_NAME, it.chatId())
                LOG.info("User ${it.user()?.username()} starts creating post")
            }.build()

    fun setApartmentsStateAbility() = Ability.builder()
            .name(RentBotCommons.COMMAND_APARTMENT_STATE)
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

                LOG.info("User ${msgContext.user()?.username()} requests all orders to change isFree")
            }
        } else {
            silent.send(ERROR_RENTED_COMMAND, msgContext.chatId())
            LOG.info("User ${msgContext.user()?.username()} user requests all orders ERROR.")
        }
    }

    private fun inlineKeyboardForApartmentState(rentPost: RentPost): InlineKeyboardMarkup? {
        val apartmentState = if (rentPost.isFree) {
            RentPost.ApartmentState.RENTED
        } else RentPost.ApartmentState.FREE
        val inlineButton = InlineKeyboardButton(apartmentState.text)
                .setCallbackData("${apartmentState.callbackData}_${rentPost.sharedMessageId}")
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
                        RentPost.QuestionState.ENTER_NAME -> processName(it)
                        RentPost.QuestionState.ENTER_APARTMENT -> processApartmentType(it)
                        RentPost.QuestionState.ENTER_FLAT_ROOMS -> processFlatRooms(it)
                        RentPost.QuestionState.ENTER_PRICE -> processPrice(it)
                        RentPost.QuestionState.ENTER_ADDRESS -> processAddress(it)
                        RentPost.QuestionState.ENTER_SQUARE -> processSquare(it)
                        RentPost.QuestionState.ENTER_FACILITIES -> processFacilities(it)
                        RentPost.QuestionState.ENTER_COMMENT -> processComment(it)
                        RentPost.QuestionState.ENTER_MASTER -> processMaster(it)
                        RentPost.QuestionState.ENTER_PHONE -> processPhone(it)
                        RentPost.QuestionState.ENTER_LOAD_PHOTO -> processLoadPhoto(it)
                        RentPost.QuestionState.ENTER_LOAD_PHOTO_QUESTION -> processLoadPhotoQuestion(it)
                        RentPost.QuestionState.FINISHED -> processFinished(it)
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
                                sendPhotosAlbum(KVARTIR_HUB_CHAT_ID, it)

                                LOG.info("Share post to KvartirHub $it")
                                userMap[user] = orderList
                                return true
                            }
                        }
                        ?: LOG.info("Couldn't share post to KvartirHub because. couldn't parse string by regex: $it")

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
            if (it.startsWith(RentPost.ApartmentState.FREE.callbackData, ignoreCase = true)) {
                order?.isFree = true
            } else if (it.startsWith(RentPost.ApartmentState.RENTED.callbackData, ignoreCase = true)) {
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

            LOG.info("User ${msgContext.user()?.username()} changed order : $order")

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

        LOG.info("Ask name of user ${it.user()}")

        askAboutApartment(it, currentUserOrder)
    }

    private fun askAboutApartment(it: MessageContext, currentUserRentPost: RentPost?) {
        // Ask question about apartments
        val sendMessage = SendMessage(it.chatId(),
                "$GOOD, ${currentUserRentPost?.name}!\n${ENTER_APARTMENT_TYPE}".withEmoji())
        val inlineKeyboardMarkup = getInlineKeyboard {
            val inlineKeyboardList = mutableListOf<InlineKeyboardButton>()
            Apartment.values().forEach {
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

        val apartment = Apartment.values()
                .firstOrNull { apartment -> it.update()?.callbackQuery?.data == apartment.callbackData }
        currentUserOrder?.apartment = apartment

        when (it.update()?.callbackQuery?.data) {
            Apartment.FLAT.callbackData -> {
                // Process database
                currentUserOrder?.questionState = ENTER_FLAT_ROOMS

                askAboutRoomQuantity(it)
            }
            null -> askAboutApartment(it, currentUserOrder)
            else -> {
                currentUserOrder?.questionState = RentPost.QuestionState.ENTER_PRICE
                val msg = SendMessage(it.chatId(), "$COOL\n\n$ENTER_PRICE".withEmoji())
                msg.enableHtml(true)
                silent.execute(msg)
            }
        }

        LOG.info("Ask apartment type of user ${it.user()}. Order: $currentUserOrder")

        // Update in db
        userMap[it.user()] = orderList
    }

    private fun askAboutRoomQuantity(it: MessageContext) {
        // Ask question about room quantity
        val sendMessage = SendMessage(it.chatId(), "$AWESOME\n$ENTER_ROOMS_COUNT".withEmoji())
        val replyKeyboardMarkup = ReplyKeyboardMarkup()

        val keyboardRows = mutableListOf<KeyboardRow>()
        FlatRooms.values().forEach {
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

        val flatRooms = FlatRooms.values()
                .firstOrNull { flatRoom -> it.update()?.message?.text == flatRoom.infinitiveText }

        if (flatRooms != null) {
            currentUserOrder?.flatRooms = flatRooms
            currentUserOrder?.questionState = RentPost.QuestionState.ENTER_PRICE

            // Update in db
            userMap[it.user()] = orderList

            val msg = SendMessage(it.chatId(), "$COOL\n\n$ENTER_PRICE".withEmoji())
            msg.enableHtml(true)
            silent.execute(msg)

            LOG.info("Ask roomQuantity of user ${it.user()}. Order: $currentUserOrder")
        } else {
            askAboutRoomQuantity(it)
        }
    }

    private fun processPrice(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        currentUserOrder?.price = it.update().message.text
        currentUserOrder?.questionState = RentPost.QuestionState.ENTER_ADDRESS

        // Update in db
        userMap[it.user()] = orderList
        silent.send(ENTER_ADDRESS.withEmoji(), it.chatId())

        LOG.info("Ask price of user ${it.user()}. Order: $currentUserOrder")
    }

    private fun processAddress(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        currentUserOrder?.address = it.update().message.text
        currentUserOrder?.questionState = RentPost.QuestionState.ENTER_SQUARE

        // Update in db
        userMap[it.user()] = orderList
        silent.send(ENTER_SQUARE, it.chatId())

        LOG.info("Ask address of user ${it.user()}. Order: $currentUserOrder")
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

        currentUserOrder?.questionState = RentPost.QuestionState.ENTER_FACILITIES

        // Update in db
        userMap[it.user()] = orderList
        silent.send(ENTER_FACILITIES, it.chatId())

        LOG.info("Ask square of user ${it.user()}. Order: $currentUserOrder")
    }

    private fun processFacilities(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        currentUserOrder?.facilities = it.update().message.text
        currentUserOrder?.questionState = RentPost.QuestionState.ENTER_COMMENT

        // Update in db
        userMap[it.user()] = orderList
        silent.send("${AWESOME}!\n${ENTER_COMMENT}", it.chatId())

        LOG.info("Ask facilities of user ${it.user()}. Order: $currentUserOrder")
    }

    private fun processComment(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        currentUserOrder?.comment = it.update().message.text
        currentUserOrder?.questionState = RentPost.QuestionState.ENTER_MASTER

        // Update in db
        userMap[it.user()] = orderList

        askAboutMaster(it)

        LOG.info("Ask comment of user ${it.user()}. Order: $currentUserOrder")
    }

    private fun askAboutMaster(it: MessageContext) {
        // Ask question about master
        val sendMessage = SendMessage(it.chatId(),
                "${ALMOST_DONE}!\n${ENTER_MASTER}")
        val inlineKeyboardMarkup = getInlineKeyboard {
            val inlineKeyboardList = mutableListOf<InlineKeyboardButton>()
            RentPost.Master.values().forEach {
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

        val master = RentPost.Master.values()
                .firstOrNull() { master -> it.update()?.callbackQuery?.data == master.callbackData }
        if (master != null) {
            currentUserOrder?.master = master
            currentUserOrder?.questionState = RentPost.QuestionState.ENTER_PHONE

            // Update in db
            userMap[it.user()] = orderList
            silent.send("$LAST_STEP\n$ENTER_PHONE", it.chatId())

            LOG.info("Ask master of user ${it.user()}. Order: $currentUserOrder")
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
        LOG.info("Ask phone of user ${it.user()}. Order: $currentUserOrder")
    }

    private fun askQuestionAboutPhoto(it: MessageContext) {
        // Ask question about photo
        val sendMessage = SendMessage(it.chatId(), LOAD_PHOTO_QUESTION.withEmoji())
        val inlineKeyboardMarkup = getInlineKeyboard {
            val inlineKeyboardList = mutableListOf<InlineKeyboardButton>()
            Action.values().forEach {
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

        val action = Action.values()
                .firstOrNull { action -> it.update()?.callbackQuery?.data == action.callbackData }
        currentUserOrder?.isWithPhoto = action == Action.YES

        when (action) {
            Action.YES -> {
                currentUserOrder?.questionState = ENTER_LOAD_PHOTO
                silent.send(LOAD_PHOTO.withEmoji(), it.chatId())
            }
            Action.NO -> {
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
                LOG.info("User ${it.user().username()} loaded photos. Order: $innerCurrentUserOrder")
            }
        } else {
            silent.send(ERROR_SEND_PHOTO, it.chatId())
            LOG.warn("User ${it.user().username()} didn't load the photo. But entered another shit.")
        }

        // Update in db
        userMap[it.user()] = orderList
    }

    private fun processPhoto(update: Update, currentUserRentPost: RentPost?) {
        val photoList = update.message.photo
        val largestPhotoFileId = photoList.maxBy(PhotoSize::getFileSize)?.fileId

        currentUserRentPost?.photoIds?.add(largestPhotoFileId)
    }

    private fun onPhotoLoadingFinished(chatId: Long, currentUserRentPost: RentPost?) {
        currentUserRentPost?.questionState = FINISHED

        val sendMessageFinish = SendMessage(chatId, POST_PREVIEW.withEmoji())
        sendMessageFinish.enableHtml(true)
        sendMessageToTelegram(sendMessageFinish)

        val sendMessageWithOrder = SendMessage(chatId, currentUserRentPost?.createPost()?.withEmoji())
        sendMessageWithOrder.enableHtml(true)
        sendMessageToTelegram(sendMessageWithOrder)

        sendPhotosAlbum(chatId, currentUserRentPost)

        askAboutCorrectnessOfPost(chatId)
    }

    private fun askAboutCorrectnessOfPost(chatId: Long) {
        // Ask question about correctness of publication
        val sendMessage = SendMessage(chatId, SURE_TO_PUBLISH.withEmoji())
        sendMessage.enableHtml(true)
        val inlineKeyboardMarkup = getInlineKeyboard {
            val inlineKeyboardList = mutableListOf<InlineKeyboardButton>()
            PublishState.values().forEach {
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
            val publishState = PublishState.values()
                    .firstOrNull { publish -> msgContext.update()?.callbackQuery?.data == publish.callbackData }
            when (publishState) {
                PublishState.PUBLISH -> {
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

                    LOG.info("User ${msgContext.user().username()} published post. $currentUserOrder")
                }
                PublishState.CANCEL -> {
                    silent.send(PUBLISH_CANCELLED, msgContext.chatId())
                    helpAbility().action().accept(msgContext)
                    orderList?.remove(currentUserOrder)

                    LOG.info("User ${msgContext.user().username()} cancelled post. $currentUserOrder")
                }
                null -> {
                    val sendMessagePreview = SendMessage(msgContext.chatId(), POST_PREVIEW.withEmoji())
                    sendMessagePreview.enableHtml(true)
                    sendMessageToTelegram(sendMessagePreview)

                    sendOrderText(msgContext.chatId(), currentUserOrder)

                    askAboutCorrectnessOfPost(msgContext.chatId())

                    LOG.info("User ${msgContext.user().username()} entered shit when asking publish or cancel. $currentUserOrder")
                }
            }
        }

        userMap[msgContext.user()] = orderList
    }

    private fun sendOrderText(chatId: Long, rentPost: RentPost?) {
        val sendMessageWithOrder = SendMessage(chatId,
                rentPost?.createPost()?.withEmoji())
        sendMessageWithOrder.enableHtml(true)
        sendMessageToTelegram(sendMessageWithOrder)
    }

    private fun sendPhotosAlbum(chatId: Long, rentPost: RentPost?) {
        rentPost?.isWithPhoto?.let {
            if (it) {
                rentPost.photoIds?.let {
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

    private fun sendPhotosAlbum(chatId: String, rentPost: RentPost?) {
        rentPost?.isWithPhoto?.let {
            if (it) {
                rentPost.photoIds?.let {
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
            LOG.error(e.toString())
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