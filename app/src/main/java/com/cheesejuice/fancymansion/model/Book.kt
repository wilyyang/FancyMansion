package com.cheesejuice.fancymansion.model

import com.cheesejuice.fancymansion.util.Const.Companion.FIRST_SLIDE
import kotlinx.serialization.Serializable

@Serializable
data class Book(var config: Config, var slides: ArrayList<Slide>)

// 1
@Serializable
data class Config(var id: Long, var version: Long = 0L, var updateDate: Long = System.currentTimeMillis(), var publish: Long = 0, var title: String, var writer: String = "", var illustrator: String = "", var description: String = "", var defaultImage: String = "", var startId: Long = FIRST_SLIDE, var defaultEndId: Long = FIRST_SLIDE, var readMode: String = "edit", var briefs: ArrayList<SlideBrief> = arrayListOf())

// 1.2 <SlideBrief>
@Serializable
data class SlideBrief(var slideId: Long, var slideTitle: String)

// 2 <Slide>
@Serializable
data class Slide(var id: Long, var slideImage: String = "", var title: String, var description: String = "", var count: Int = 0, var question: String, var choiceItems: ArrayList<ChoiceItem> = arrayListOf())

// 2.2 <ChoiceItem>
@Serializable
data class ChoiceItem(var id: Long, var title: String, var showConditions: ArrayList<Condition>, var enterItems: ArrayList<EnterItem>)

// 2.2.3 <EnterItem>
@Serializable
data class EnterItem(var id: Long, var enterSlideId: Long, var enterConditions: ArrayList<Condition>)

// 2.2.2 showConditions <Condition> /  2.2.3.2 enterConditions <Condition>
@Serializable
data class Condition(var id: Long, var conditionId1: Long, var conditionId2: Long, var conditionCount: Int, var conditionOp: String, var nextLogic: String)