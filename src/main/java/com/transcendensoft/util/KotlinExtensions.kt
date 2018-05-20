package com.transcendensoft.util

import com.vdurmont.emoji.EmojiParser

fun String.withEmoji() = EmojiParser.parseToUnicode(this)



