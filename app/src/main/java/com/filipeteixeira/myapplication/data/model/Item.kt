package com.filipeteixeira.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Item(
    @SerializedName("_id") val id: String? = null,
    val nome: String,
    val preco: Double = 0.0,
    val quantidade: Int = 1,
    val unidade: String = "un",
    val noCarrinho: Boolean = false
)

data class AtualizarItemRequest(
    val quantidade: Int? = null,
    val noCarrinho: Boolean? = null
)
