package com.filipeteixeira.listadecompras.data.local

import android.content.Context
import com.filipeteixeira.listadecompras.data.model.Item
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class LocalPersistence(private val context: Context) {
    private val gson = Gson()
    private val fileName = "lista_compras.json"

    fun salvarItens(itens: List<Item>) {
        val json = gson.toJson(itens)
        try {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                output.write(json.toByteArray())
            }
        } catch (e: Exception) {
            // Falha de escrita: manter o app funcional (sem crash).
        }
    }

    fun carregarItens(): List<Item> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return emptyList()

        return try {
            val json = context.openFileInput(fileName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Item>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
