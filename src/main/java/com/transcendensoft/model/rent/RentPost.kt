package com.transcendensoft.model.rent

import com.transcendensoft.model.Apartment
import com.transcendensoft.model.FlatRooms
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.APARTMENTS_FREE
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.APARTMENTS_RENTED
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.FACILITIES
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.HASHTAG_RENT
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.I_AM
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.PHONE
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.PHOTO_OF_APARTMENTS
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.PRICE
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.RENT
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.SQUARE
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.TELEGRAM
import com.transcendensoft.model.rent.RentBotTextConstants.Companion.WHO_AM_I
import com.transcendensoft.util.isNull
import java.io.Serializable

data class RentPost(
        var id: Int,
        var name: String = "",
        var price: String = "",
        var square: Int = 0,
        var phone: String? = null,
        var telegram: String? = "",
        var facilities: String = "",
        var flatRooms: FlatRooms? = null,
        var apartment: Apartment? = null,
        var master: Master? = null,
        var comment: String? = null,
        var address: String? = null,
        var questionState: QuestionState? = null,
        var isFree: Boolean = true,
        var isWithPhoto: Boolean = false,
        var photoIds: MutableList<String?>? = mutableListOf(),
        var chatId: Long = 0L,
        var sharedMessageId: Int? = null) : Serializable {

    fun createPost(): String {
        val apartmentString = if (apartment == Apartment.FLAT) {
            flatRooms?.wantText ?: ""
        } else {
            apartment?.wantText?.toLowerCase() ?: ""
        }

        val addressString = if (!address.isNullOrBlank()) "по адресу: <b>${address}</b>" else ""
        val masterString = if (master != null) master!!.text.toLowerCase() else ""
        val phoneString = if (!phone.isNullOrBlank()) "\n<b>$PHONE</b> $phone" else ""
        val telegramString = if(!telegram.isNull()) "\n<b>$TELEGRAM</b> @$telegram" else ""
        val freeString = if (isFree) APARTMENTS_FREE else APARTMENTS_RENTED
        val photoString = if (isWithPhoto) PHOTO_OF_APARTMENTS else ""

        return """$HASHTAG_RENT
            |$RENT $apartmentString $addressString
            |
            |<b>$PRICE</b> $price
            |<b>$SQUARE</b> $square кв.м.
            |
            |<b>$FACILITIES</b> $facilities
            |
            |$comment
            |
            |<b>$I_AM</b> $name
            |<b>$WHO_AM_I</b> $masterString$telegramString$phoneString
            |
            |$freeString
            |
            |$photoString
        """.trimMargin()
    }

    enum class Master(val text: String, val callbackData: String) {
        MASTER("Хозяин(-ка)", "callbackMaster"),
        RIELTOR("Риелтор", "callbackRieltor"),
        AGENCY("Агенство", "callbackAgency")
    }

    enum class QuestionState {
        ENTER_NAME,
        ENTER_APARTMENT,
        ENTER_FLAT_ROOMS,
        ENTER_PRICE,
        ENTER_ADDRESS,
        ENTER_SQUARE,
        ENTER_FACILITIES,
        ENTER_COMMENT,
        ENTER_MASTER,
        ENTER_PHONE,
        ENTER_LOAD_PHOTO_QUESTION,
        ENTER_LOAD_PHOTO,
        FINISHED
    }

    enum class ApartmentState(val text: String, val callbackData: String) {
        FREE("Отметить, что уже свободны", "apartmentFreeCallback"),
        RENTED("Отметить, что уже сданы", "apartmentRentedCallback")
    }
}