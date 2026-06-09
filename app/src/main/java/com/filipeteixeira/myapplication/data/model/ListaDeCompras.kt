package com.filipeteixeira.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class ListaDeCompras(
    @SerializedName("_id") val id: String? = null,
    val nome: String,
    val itens: List<Item> = emptyList(),
    val criadoEm: String? = null
)

data class CriarListaRequest(
    val nome: String
)
