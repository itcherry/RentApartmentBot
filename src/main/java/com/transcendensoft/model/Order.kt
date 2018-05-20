package com.transcendensoft.model

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
        var address:String? = null,
        var questionState: QuestionState? = null) : Serializable {

    fun createPost(): String {
        return ""
    }

    enum class Apartment(val infinitiveText: String, val wantText: String, val callbackData: String) {
        FLAT("Квартирa", "Квартиру", "callbackFlat"),
        ROOM("Комната", "Комнату", "callbackRoom"),
        HOUSE("Дом", "Дом", "callbackHouse"),
        OFFICE("Офис", "Офис", "callbackOffice")
    }

    enum class FlatRooms(val infinitiveText: String, val wantText: String, val callbackData: String) {
        ONE_ROOM("1-к квартира", "1-к квартиру", "callbackOneRoom"),
        TWO_ROOM("2-к квартира", "2-к квартиру", "callbackTwoRoom"),
        THREE_ROOM("3-к квартира", "3-к квартиру", "callbackThreeRoom"),
        FOUR_ROOM("4-к квартира", "4-к квартиру", "callbackFourRoom"),
        FIVE_ROOM("5-к квартира", "5-к квартиру", "callbackFiveRoom"),
        SMART("Смарт квартира", "Смарт квартиру", "callbackSmart"),
        STUDIO("Квартира студио", "Квартиру студио", "callbackStudio"),
        TWO_FLOORS("2-х этажная квартира", "2-х этажную квартиру", "callbackTwoFloors"),
        PENTHOUSE("Пентхаус", "Пентхаус", "callbackPenthouse")
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