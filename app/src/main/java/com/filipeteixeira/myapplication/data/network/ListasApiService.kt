package com.filipeteixeira.myapplication.data.network

import com.filipeteixeira.myapplication.data.model.AtualizarItemRequest
import com.filipeteixeira.myapplication.data.model.CriarListaRequest
import com.filipeteixeira.myapplication.data.model.Item
import com.filipeteixeira.myapplication.data.model.ListaDeCompras
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ListasApiService {

    @GET("listas")
    suspend fun getListas(): List<ListaDeCompras>

    @POST("listas")
    suspend fun criarLista(@Body request: CriarListaRequest): ListaDeCompras

    @POST("listas/{id}/itens")
    suspend fun adicionarItem(
        @Path("id") listaId: String,
        @Body item: Item
    ): ListaDeCompras

    @PATCH("listas/{id}/itens/{itemId}")
    suspend fun atualizarItem(
        @Path("id") listaId: String,
        @Path("itemId") itemId: String,
        @Body request: AtualizarItemRequest
    ): ListaDeCompras

    @DELETE("listas/{id}/itens/{itemId}")
    suspend fun deletarItem(
        @Path("id") listaId: String,
        @Path("itemId") itemId: String
    ): ListaDeCompras
}
