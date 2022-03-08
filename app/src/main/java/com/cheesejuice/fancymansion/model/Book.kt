package com.cheesejuice.fancymansion.model

import kotlinx.serialization.Serializable

@Serializable
data class Book(val config: Config, val slides: ArrayList<Slide>)

// 1
@Serializable
data class Config(val id: Long, val version: Long, val updateDate: Long, val publish: Long, val title: String, val writer: String, val illustrator: String, val description: String, val defaultImage: String, val startId: Long, val defaultEndId: Long, val readMode: String, val briefs: ArrayList<SlideBrief>)

// 1.2 <SlideBrief>
@Serializable
data class SlideBrief(val slideId: Long, val slideTitle: String)

// 2 <Slide>
@Serializable
data class Slide(val id: Long, val slideImage: String, val title: String, val description: String, val count: Int, val question: String, val choiceItems: ArrayList<ChoiceItem>)

// 2.2 <ChoiceItem>
@Serializable
data class ChoiceItem(val id: Long, val title: String, val showConditions: ArrayList<Condition>, val enterItems: ArrayList<EnterItem>)

// 2.2.3 <EnterItem>
@Serializable
data class EnterItem(val id: Long, val enterSlideId: Long, val enterConditions: ArrayList<Condition>)

// 2.2.2 showConditions <Condition> /  2.2.3.2 enterConditions <Condition>
@Serializable
data class Condition(val id: Long, val conditionId1: Long, val conditionId2: Long, val conditionCount: Int, val conditionOp: String, val nextLogic: String)