package com.cheesejuice.fancymansion.model

import kotlinx.serialization.Serializable

@Serializable
data class Comment(val uid: String, val email: String, val userName: String, var comment: String, var updateTime: Long, var bookPublishCode: String)