package com.henrisusanto.vitmate.data.model

data class SearchIndexResponse(
    val id: String,
    val slug: String,
    val title: String,
    val category: String,
    val tags: List<String>? = null,
    val ingredientsText: String?,
    val cover: String?,
    val thumb: String?,
    val estCapital: Int?,
    val portions: Int?,
    val isViral: Boolean?
)