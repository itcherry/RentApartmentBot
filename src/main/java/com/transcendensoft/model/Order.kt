package com.transcendensoft.model

import com.transcendensoft.model.TextConstants.Companion.HASHTAG_RENT
import com.transcendensoft.model.TextConstants.Companion.PRICE
import com.transcendensoft.model.TextConstants.Companion.RENT
import com.transcendensoft.model.TextConstants.Companion.SQUARE
import java.io.Serializable

data class Order(
        var name: String = "",
        var price: String = "",
        var square: Int = 0,
        var phone: String? = null,
        var telegram: String = "",
        var facilities: String = "",
        var flatRooms: FlatRooms? = null,
        var apartment: Apartment? = null,
        var master: Master? = null,
        var comment: String? = null,
        var address: String? = null,
        var questionState: QuestionState? = null,
        var isFree: Boolean = true) : Serializable {

    fun createPost(): String {
        val apartmentString = if (apartment == Apartment.FLAT) {
            flatRooms?.wantText ?: ""
        } else {
            apartment?.wantText?.toLowerCase() ?: ""
        }

        val addressString = if(!address.isNullOrBlank()) "по адресу: <i>${address}</i>" else ""
        val masterString = if(master != null) ", я ${master!!.text}" else ""
        val phoneString = if(!phone.isNullOrBlank()) ":telephone: Телефон: $phone" else ""
        val freeString = if(isFree) ":white_check_mark: Апартаменты еще свободны" else ":red_circle: Апартаменты сданы"

        return """$HASHTAG_RENT
            |:house_with_garden: $RENT $apartmentString $addressString
            |
            |$PRICE $price
            |$SQUARE $square кв.м.
            |
            |Предлагаю такие удобства: $facilities
            |
            |$comment
            |
            |:bust_in_silhouette: Меня зовут $name$masterString
            |:call_me: Телеграм: @$telegram
            |$phoneString
            |
            |$freeString
        """.trimMargin()
    }

    enum class Apartment(val infinitiveText: String, val wantText: String, val callbackData: String) {
        FLAT("Квартирa", "Квартиру", "callbackFlat"),
        ROOM("Комната", "Комнату", "callbackRoom"),
        HOUSE("Дом", "Дом", "callbackHouse"),
        OFFICE("Офис", "Офис", "callbackOffice")
    }

    enum class FlatRooms(val infinitiveText: String, val wantText: String) {
        ONE_ROOM("1-к квартира", "1-к квартиру"),
        TWO_ROOM("2-к квартира", "2-к квартиру"),
        THREE_ROOM("3-к квартира", "3-к квартиру"),
        FOUR_ROOM("4-к квартира", "4-к квартиру"),
        FIVE_ROOM("5-к квартира", "5-к квартиру"),
        SMART("Смарт квартира", "Смарт квартиру"),
        STUDIO("Квартира студио", "Квартиру студио"),
        TWO_FLOORS("2-х этажная квартира", "2-х этажную квартиру"),
        PENTHOUSE("Пентхаус", "Пентхаус")
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
        ENTER_LOAD_PHOTO,
    }
}