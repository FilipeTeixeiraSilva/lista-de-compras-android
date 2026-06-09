package com.filipeteixeira.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.filipeteixeira.myapplication.data.local.LocalPersistence
import com.filipeteixeira.myapplication.data.model.Item
import com.filipeteixeira.myapplication.ui.theme.MyApplicationTheme
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ListaDeComprasScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaDeComprasScreen() {
    val context = LocalContext.current
    val persistence = remember { LocalPersistence(context) }

    var itens by remember { mutableStateOf(listOf<Item>()) }
    var showDialog by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }

    // Carregar itens ao iniciar
    LaunchedEffect(Unit) {
        itens = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            persistence.carregarItens()
        }
        loaded = true
    }

    // Salvar itens sempre que a lista mudar
    LaunchedEffect(itens, loaded) {
        if (!loaded) return@LaunchedEffect
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            persistence.salvarItens(itens)
        }
    }

    val total = itens.sumOf { it.preco * it.quantidade }
    val ultimoPreco = itens.lastOrNull()?.preco?.let { formatarPreco(it) } ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Compras") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 50.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar item")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (itens.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Text(
                        text = "Nenhum item na lista.\nToque em + para adicionar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(itens, key = { it.id ?: it.nome }) { item ->
                        ItemCard(
                            item = item,
                            onDelete = {
                                itens = itens.filter { it.id != item.id }
                            },
                            onQuantidadeChange = { novaQtd ->
                                itens = itens.map {
                                    if (it.id == item.id) it.copy(quantidade = novaQtd) else it
                                }
                            }
                        )
                    }
                }
            }

            TotalRodape(total = total)
        }
    }

    if (showDialog) {
        AdicionarItemDialog(
            ultimoPreco = ultimoPreco,
            onDismiss = { showDialog = false },
            onConfirmar = { novoItem ->
                itens = itens + novoItem
                showDialog = false
            }
        )
    }
}

@Composable
fun ItemCard(
    item: Item,
    onDelete: () -> Unit,
    onQuantidadeChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remover item",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "R$ ${formatarPreco(item.preco)} / ${item.unidade}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { if (item.quantidade > 1) onQuantidadeChange(item.quantidade - 1) },
                    modifier = Modifier.width(36.dp).height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("-", style = MaterialTheme.typography.titleMedium)
                }

                Text(
                    text = "${item.quantidade}",
                    modifier = Modifier.padding(horizontal = 10.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = { onQuantidadeChange(item.quantidade + 1) },
                    modifier = Modifier.width(36.dp).height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Subtotal: R$ ${formatarPreco(item.preco * item.quantidade)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TotalRodape(total: Double) {
    HorizontalDivider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Total",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "R$ ${formatarPreco(total)}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

fun formatarPreco(valor: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"))
    formatter.minimumFractionDigits = 2
    formatter.maximumFractionDigits = 2
    return formatter.format(valor)
}

@Composable
fun AdicionarItemDialog(
    ultimoPreco: String,
    onDismiss: () -> Unit,
    onConfirmar: (Item) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf(ultimoPreco) }
    var erroNome by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = {
                        nome = it
                        erroNome = false
                    },
                    label = { Text("Nome do item") },
                    isError = erroNome,
                    supportingText = { if (erroNome) Text("Informe o nome do item") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = preco,
                    onValueChange = { input ->
                        // Remove tudo que não for dígito
                        val digits = input.filter { it.isDigit() }
                        if (digits.isEmpty()) {
                            preco = ""
                        } else {
                            // Limita a 8 dígitos para evitar estouro (999.999,99)
                            val limitedDigits = if (digits.length > 8) digits.substring(0, 8) else digits
                            val cents = limitedDigits.toDouble()
                            preco = formatarPreco(cents / 100.0)
                        }
                    },
                    label = { Text("Preço (R$)") },
                    placeholder = { Text("0,00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nome.isBlank()) {
                        erroNome = true
                        return@Button
                    }
                    
                    // Converte a string formatada (ex: "1,99") de volta para Double
                    val precoLimpo = preco.replace(".", "").replace(",", ".")
                    val precoDouble = precoLimpo.toDoubleOrNull() ?: 0.0
                    
                    onConfirmar(
                        Item(
                            id = UUID.randomUUID().toString(),
                            nome = nome.trim(),
                            preco = precoDouble,
                            quantidade = 1,
                            unidade = "un"
                        )
                    )
                }
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
