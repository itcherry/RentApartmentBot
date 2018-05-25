package com.transcendensoft.handler

import com.transcendensoft.model.Action
import com.transcendensoft.model.Apartment
import com.transcendensoft.model.FlatRooms
import com.transcendensoft.model.PublishState
import com.transcendensoft.model.rent.RentBotCommons
import com.transcendensoft.model.rent.RentBotTextConstants
import com.transcendensoft.model.rent_out.RentOutBotCommons
import com.transcendensoft.model.rent_out.RentOutBotCommons.Companion.BOT_NAME
import com.transcendensoft.model.rent_out.RentOutBotCommons.Companion.TOKEN
import com.transcendensoft.model.rent_out.RentOutBotCommons.Companion.USER_TELEGRAM_ID
import com.transcendensoft.model.rent_out.RentOutPost
import com.transcendensoft.model.rent_out.RentOutTextConstants
import com.transcendensoft.model.rent_out.RentOutTextConstants.Companion.ENTER_REQUIRED_LOCATION
import com.transcendensoft.util.logger
import com.transcendensoft.util.withEmoji
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.*
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.api.methods.send.SendMessage
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

class RentOutBot : AbilityBot(TOKEN, BOT_NAME) {
    companion object {
        val LOG by logger()
    }

    init {
        System.setProperty("log.name", "RentOutBotLogs");
    }

    private val userMap = db.getMap<EndUser, MutableList<RentOutPost>>(RentOutBotCommons.USER_MAP)

    override fun creatorId(): Int = USER_TELEGRAM_ID

    fun startAbility() = Ability.builder()
            .name(RentOutBotCommons.COMMAND_START)
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .input(0)
            .action { ctx ->
                silent.send(RentOutTextConstants.START.withEmoji(), ctx.chatId())
                LOG.info("User ${ctx.user()?.username()} started bot.")
                //println(ResourceBundle.getBundle("i18n/strings", Locale.forLanguageTag("ua_UK")).getString("welcome"))
            }
            .build()

    fun helpAbility() = Ability.builder()
            .name(RentBotCommons.COMMAND_HELP)
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .input(0)
            .action { ctx ->
                val sendMessage = SendMessage()
                sendMessage.setChatId(ctx.chatId())
                sendMessage.text = RentOutBotCommons.HELP_TEXT.trimMargin()
                sendMessage.enableHtml(true)
                sendMessageToTelegram(sendMessage)

                LOG.info("User ${ctx.user()?.username()} ask help.")
            }
            .build()

    fun sendMessageToUserAbility() = Ability.builder() // /sendmessage itcherry 'someMessage'
            .name(RentOutBotCommons.COMMAND_SEND_MESSAGE)
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
            .name(RentOutBotCommons.COMMAND_CANCEL)
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .input(0)
            .action {
                // Process database
                val orderList = userMap[it.user()]
                val currentUserOrder = orderList?.lastOrNull()

                if (currentUserOrder?.questionState !in setOf(RentOutPost.QuestionState.FINISHED, null) && currentUserOrder != null) {
                    orderList.remove(currentUserOrder)
                    silent.send(RentOutTextConstants.CANCELLED, it.chatId())
                    LOG.info("User ${it.user()?.username()} cancelled order $currentUserOrder")
                } else {
                    silent.send(RentOutTextConstants.ERROR_CANCEL.withEmoji(), it.chatId())
                    LOG.info("User ${it.user()?.username()} invokes error of cancel order $currentUserOrder")
                }

                userMap[it.user()] = orderList
            }.build()

    fun createPostAbility() = Ability.builder()
            .name(RentOutBotCommons.COMMAND_CREATE_POST)
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .input(0)
            .action {
                val endUser = it.user()

                var orderList = userMap[endUser]
                if (orderList == null) {
                    orderList = mutableListOf()
                }

                val order = RentOutPost(
                        id = orderList.size + 1,
                        telegram = endUser.username(),
                        name = "${endUser.lastName()} ${endUser.firstName()}",
                        questionState = RentOutPost.QuestionState.ENTER_NAME,
                        chatId = it.chatId())

                orderList.add(order)
                userMap += (endUser to orderList)

                silent.send(RentOutTextConstants.CREATE_POST_TEXT.withEmoji(), it.chatId())
                silent.send(RentOutTextConstants.ENTER_NAME, it.chatId())
                LOG.info("User ${it.user()?.username()} starts creating post")
            }.build()

    fun callbackAbility(): Ability {
        return Ability.builder()
                .name(DEFAULT)
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action {
                    val currentUserOrder = userMap[it.user()]?.lastOrNull()

                    if (processShareToChannelCallback(it)) return@action

                    when (currentUserOrder?.questionState) {
                        RentOutPost.QuestionState.ENTER_IS_INFOGRAPHIC -> TODO()
                        RentOutPost.QuestionState.ENTER_NAME -> processName(it)
                        RentOutPost.QuestionState.ENTER_ABOUT_RENTER-> processAboutRenter(it)
                        RentOutPost.QuestionState.ENTER_APARTMENT -> processApartmentType(it)
                        RentOutPost.QuestionState.ENTER_FLAT_ROOMS -> processFlatRooms(it)
                        RentOutPost.QuestionState.ENTER_PRICE -> processPrice(it)
                        RentOutPost.QuestionState.ENTER_DATE -> processRequiredDate(it)
                        RentOutPost.QuestionState.ENTER_LOCATION -> processLocation(it)
                        RentOutPost.QuestionState.ENTER_FACILITIES -> processRequiredFacilities(it)
                        RentOutPost.QuestionState.ENTER_PHONE -> processPhone(it)
                        RentOutPost.QuestionState.ENTER_LOAD_PHOTO -> processLoadPhoto(it)
                        RentOutPost.QuestionState.ENTER_LOAD_PHOTO_QUESTION -> processLoadPhotoQuestion(it)
                        RentOutPost.QuestionState.FINISHED -> processFinished(it)
                        null -> silent.send(RentBotTextConstants.START.withEmoji(), it.chatId())
                    }
                }
                .build()
    }

    private fun RentOutBot.processShareToChannelCallback(msgContext: MessageContext): Boolean {
        val callbackData = msgContext.update()?.callbackQuery?.data
        callbackData?.let {
            if (it.startsWith(RentOutBotCommons.SHARE_TO_CHANNEL_CALLBACK, ignoreCase = true)) {
                val destructuredRegex = ("${RentOutBotCommons.SHARE_TO_CHANNEL_CALLBACK}\\?" +
                        "${RentOutBotCommons.PARAMETER_USER_ID}=(\\d+)&" +
                        "${RentOutBotCommons.PARAMETER_ORDER_ID}=(\\d+)").toRegex()

                destructuredRegex.matchEntire(it)
                        ?.destructured
                        ?.let { (userId, orderId) ->
                            val user = userMap.keys.firstOrNull { endUser -> endUser.id() == userId.toInt() }
                            val orderList = userMap[user]
                            val order = orderList?.firstOrNull { localOrder -> localOrder.id == orderId.toInt() }
                            order?.let {
                                val msg = SendMessage(RentOutBotCommons.KVARTIR_HUB_CHAT_ID, it.createPost().withEmoji())
                                msg.enableHtml(true)
                                it.sharedMessageId = sendMessage(msg)?.messageId

                                sendPhotosAlbum(RentOutBotCommons.KVARTIR_HUB_CHAT_ID, it)
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

    private fun processName(it: MessageContext) {
        // Process database
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()
        currentUserOrder?.name = it.update().message?.text ?: ""
        currentUserOrder?.questionState = RentOutPost.QuestionState.ENTER_APARTMENT

        userMap[it.user()] = orderList
        LOG.info("Ask name of user ${it.user()}")

        askAboutApartment(it, currentUserOrder)
    }

    private fun askAboutApartment(it: MessageContext, currentUserRentPost: RentOutPost?) {
        // Ask question about apartments
        val sendMessage = SendMessage(it.chatId(),
                ("${RentOutTextConstants.GOOD}, ${currentUserRentPost?.name}!\n" +
                        RentOutTextConstants.ENTER_APARTMENT_TYPE).withEmoji())

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
                currentUserOrder?.questionState = RentOutPost.QuestionState.ENTER_FLAT_ROOMS

                askAboutRoomQuantity(it)
            }
            null -> askAboutApartment(it, currentUserOrder)
            else -> {
                currentUserOrder?.questionState = RentOutPost.QuestionState.ENTER_PRICE
                val msg = SendMessage(it.chatId(), ("${RentOutTextConstants.COOL}\n\n" +
                        RentOutTextConstants.ENTER_REQUIRED_PRICE).withEmoji())
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
        val sendMessage = SendMessage(it.chatId(), ("${RentOutTextConstants.AWESOME}\n" +
                RentOutTextConstants.ENTER_ROOMS_COUNT).withEmoji())
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
            currentUserOrder?.questionState = RentOutPost.QuestionState.ENTER_PRICE

            // Update in db
            userMap[it.user()] = orderList

            val msg = SendMessage(it.chatId(), ("${RentOutTextConstants.COOL}\n\n" +
                    RentOutTextConstants.ENTER_REQUIRED_PRICE).withEmoji())
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
        currentUserOrder?.questionState = RentOutPost.QuestionState.ENTER_LOCATION

        // Update in db
        userMap[it.user()] = orderList
        val sendMessage = SendMessage(it.chatId(), ENTER_REQUIRED_LOCATION.withEmoji())
        sendMessage.enableHtml(true)
        sendMessageToTelegram(sendMessage)

        LOG.info("Ask price of user ${it.user()}. Order: $currentUserOrder")
    }

    private fun processLocation(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        currentUserOrder?.place = it.update().message.text
        currentUserOrder?.questionState = RentOutPost.QuestionState.ENTER_DATE

        // Update in db
        userMap[it.user()] = orderList
        silent.send(RentOutTextConstants.ENTER_REQUIRED_DATE.withEmoji(), it.chatId())

        LOG.info("Ask address of user ${it.user()}. Order: $currentUserOrder")
    }

    private fun processRequiredDate(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        currentUserOrder?.requiredDate = it.update().message.text
        currentUserOrder?.questionState = RentOutPost.QuestionState.ENTER_FACILITIES

        // Update in db
        userMap[it.user()] = orderList
        silent.send(RentOutTextConstants.ENTER_REQUIRED_FACILITIES.withEmoji(), it.chatId())

        LOG.info("Ask required date for user ${it.user()}. Order: $currentUserOrder")
    }

    private fun processRequiredFacilities(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        currentUserOrder?.facilities = it.update().message.text
        currentUserOrder?.questionState = RentOutPost.QuestionState.ENTER_ABOUT_RENTER

        // Update in db
        userMap[it.user()] = orderList
        val msg = SendMessage(it.chatId(), RentOutTextConstants.ENTER_ABOUT_RENTER.withEmoji())
        msg.enableHtml(true)
        silent.execute(msg)

        LOG.info("Ask required facilities for user ${it.user()}. Order: $currentUserOrder")
    }

    private fun processAboutRenter(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        currentUserOrder?.aboutRenter = it.update().message.text
        currentUserOrder?.questionState = RentOutPost.QuestionState.ENTER_PHONE

        // Update in db
        userMap[it.user()] = orderList
        silent.send("${RentOutTextConstants.LAST_STEP}\n" +
                RentOutTextConstants.ENTER_PHONE, it.chatId())

        LOG.info("Ask required facilities for user ${it.user()}. Order: $currentUserOrder")
    }

    private fun processPhone(it: MessageContext) {
        val orderList = userMap[it.user()]
        val currentUserOrder = orderList?.last()

        currentUserOrder?.phone = it.update().message.text
        currentUserOrder?.questionState = RentOutPost.QuestionState.ENTER_LOAD_PHOTO_QUESTION

        // Update in db
        userMap[it.user()] = orderList

        askQuestionAboutPhoto(it)
        LOG.info("Ask phone of user ${it.user()}. Order: $currentUserOrder")
    }

    private fun askQuestionAboutPhoto(it: MessageContext) {
        // Ask question about photo
        val sendMessage = SendMessage(it.chatId(), RentOutTextConstants.LOAD_PHOTO_QUESTION.withEmoji())
        val inlineKeyboardMarkup = getInlineKeyboard {
            val inlineKeyboardList = mutableListOf<InlineKeyboardButton>()
            Action.values().forEach {
                inlineKeyboardList += InlineKeyboardButton(it.text)
                        .setCallbackData(it.callbackData)
            }
            inlineKeyboardList
        }
        sendMessage.replyMarkup = inlineKeyboardMarkup
        sendMessage.enableHtml(true)
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
                currentUserOrder?.questionState = RentOutPost.QuestionState.ENTER_LOAD_PHOTO
                silent.send(RentOutTextConstants.LOAD_PHOTO.withEmoji(), it.chatId())
            }
            Action.NO -> {
                currentUserOrder?.questionState = RentOutPost.QuestionState.FINISHED
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
            Timer("schedule1", true).schedule(1500) {
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
            silent.send(RentOutTextConstants.ERROR_SEND_PHOTO, it.chatId())
            LOG.warn("User ${it.user().username()} didn't load the photo. But entered another shit.")
        }

        // Update in db
        userMap[it.user()] = orderList
    }

    private fun processPhoto(update: Update, currentUserRentPost: RentOutPost?) {
        val photoList = update.message.photo
        val largestPhotoFileId = photoList.maxBy(PhotoSize::getFileSize)?.fileId

        currentUserRentPost?.photoIds?.add(largestPhotoFileId)
    }

    private fun onPhotoLoadingFinished(chatId: Long, currentUserRentPost: RentOutPost?) {
        currentUserRentPost?.questionState = RentOutPost.QuestionState.FINISHED

        val sendMessageFinish = SendMessage(chatId, RentOutTextConstants.POST_PREVIEW.withEmoji())
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
        val sendMessage = SendMessage(chatId, RentOutTextConstants.SURE_TO_PUBLISH.withEmoji())
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
                    val sendMessageWithOrder = SendMessage(RentOutBotCommons.ID_OF_GROUP_WITH_POSTS,
                            currentUserOrder?.createPost()?.withEmoji())
                    sendMessageWithOrder.enableHtml(true)

                    val inlineKeyboard = InlineKeyboardMarkup()
                    val callbackData = "${RentOutBotCommons.SHARE_TO_CHANNEL_CALLBACK}?" +
                            "${RentOutBotCommons.PARAMETER_USER_ID}=${msgContext.user()?.id()}&" +
                            "${RentOutBotCommons.PARAMETER_ORDER_ID}=${currentUserOrder?.id}"

                    inlineKeyboard.keyboard = listOf(listOf(
                            InlineKeyboardButton(RentOutBotCommons.SHARE_ACTION)
                                    .setCallbackData(callbackData)))
                    sendMessageWithOrder.replyMarkup = inlineKeyboard
                    sendMessageToTelegram(sendMessageWithOrder)

                    sendPhotosAlbum(RentOutBotCommons.ID_OF_GROUP_WITH_POSTS, currentUserOrder)

                    val sendMessagePublished = SendMessage(msgContext.chatId(), RentOutTextConstants.FINISH.withEmoji())
                    sendMessagePublished.enableHtml(true)
                    sendMessageToTelegram(sendMessagePublished)

                    LOG.info("User ${msgContext.user().username()} published post. $currentUserOrder")
                }
                PublishState.CANCEL -> {
                    silent.send(RentOutTextConstants.PUBLISH_CANCELLED, msgContext.chatId())
                    helpAbility().action().accept(msgContext)
                    orderList?.remove(currentUserOrder)

                    LOG.info("User ${msgContext.user().username()} cancelled post. $currentUserOrder")
                }
                null -> {
                    val sendMessagePreview = SendMessage(msgContext.chatId(),
                            RentOutTextConstants.POST_PREVIEW.withEmoji())
                    sendMessagePreview.enableHtml(true)
                    sendMessageToTelegram(sendMessagePreview)

                    sendOrderText(msgContext.chatId(), currentUserOrder)

                    askAboutCorrectnessOfPost(msgContext.chatId())

                    LOG.info("User ${msgContext.user().username()} " +
                            "entered shit when asking publish or cancel. $currentUserOrder")
                }
            }
        }

        userMap[msgContext.user()] = orderList
    }

    private fun sendOrderText(chatId: Long, rentOutPost: RentOutPost?) {
        val sendMessageWithOrder = SendMessage(chatId,
                rentOutPost?.createPost()?.withEmoji())
        sendMessageWithOrder.enableHtml(true)
        sendMessageToTelegram(sendMessageWithOrder)
    }

    private fun sendPhotosAlbum(chatId: Long, rentOutPost: RentOutPost?) {
        rentOutPost?.isWithPhoto?.let {
            if (it) {
                rentOutPost.photoIds?.let {
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

    private fun sendPhotosAlbum(chatId:String, rentOutPost: RentOutPost?){
        rentOutPost?.isWithPhoto?.let {
            if (it) {
                rentOutPost.photoIds?.let {
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
            RentBot.LOG.error(e.toString())
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