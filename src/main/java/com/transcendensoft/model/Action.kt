package com.transcendensoft.model

enum class Action(val text: String, val callbackData: String) {
    YES("Да", "callbackYes"),
    NO("Нет", "callbackNo")
}