package com.transcendensoft.model.rent_out

import com.transcendensoft.model.Apartment
import com.transcendensoft.model.FlatRooms
import com.transcendensoft.model.rent.RentBotTextConstants
import com.transcendensoft.model.rent.RentPost
import com.transcendensoft.model.rent_out.RentOutTextConstants.Companion.PLACE
import com.transcendensoft.util.isNull
import java.io.Serializable

data class RentOutPost(
        var id: Int,
        var name: String = "",
        var price: String = "",
        var phone: String? = null,
        var telegram: String? = "",
        var facilities: String = "",
        var flatRooms: FlatRooms? = null,
        var requiredDate: String = "",
        var apartment: Apartment? = null,
        var aboutRenter: String? = null,
        var place: String? = null,
        var questionState: QuestionState? = null,
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

        val addressString = if (!place.isNullOrBlank()) "<b>$PLACE</b> $place" else ""
        val phoneString = if (!phone.isNullOrBlank()) "\n<b>${RentOutTextConstants.PHONE}</b> $phone" else ""
        val telegramString = if(!telegram.isNull()) "\n<b>${RentOutTextConstants.TELEGRAM}</b> @$telegram" else ""
        return """${RentOutTextConstants.HASHTAG_RENT}
            |${RentOutTextConstants.RENT} $apartmentString ${requiredDate.toLowerCase()}
            |
            |<b>${RentOutTextConstants.PRICE}</b> $price
            |$addressString
            |
            |<b>${RentOutTextConstants.ABOUT_RENTER}</b> $aboutRenter
            |
            |<b>${RentOutTextConstants.FACILITIES}</b> $facilities
            |
            |<b>${RentOutTextConstants.I_AM}</b> $name$telegramString$phoneString
        """.trimMargin()
    }

    enum class QuestionState {
        ENTER_IS_INFOGRAPHIC,
        ENTER_NAME,
        ENTER_APARTMENT,
        ENTER_FLAT_ROOMS,
        ENTER_PRICE,
        ENTER_LOCATION,
        ENTER_DATE,
        ENTER_FACILITIES,
        ENTER_ABOUT_RENTER,
        ENTER_PHONE,
        ENTER_LOAD_PHOTO_QUESTION,
        ENTER_LOAD_PHOTO,
        FINISHED
    }
}