package com.transcendensoft.model

enum class Apartment(val infinitiveText: String, val wantText: String, val callbackData: String) {
    FLAT("Квартирa", "Квартиру", "callbackFlat"),
    ROOM("Комната", "Комнату", "callbackRoom"),
    HOUSE("Дом", "Дом", "callbackHouse"),
    OFFICE("Офис", "Офис", "callbackOffice")
}