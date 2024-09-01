
package com.example.myapplication

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : AppCompatActivity() {
    private lateinit var etBookName: EditText
    private lateinit var btnSearch: Button
    private lateinit var llResults: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etBookName = findViewById(R.id.etBookName)
        btnSearch = findViewById(R.id.btnSearch)
        llResults = findViewById(R.id.llResults)

        btnSearch.setOnClickListener {
            val query = etBookName.text.toString()
            if (query.isNotBlank()) {
                searchBooks(query)
            } else {
                Toast.makeText(this, "Digite o nome de um livro", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchBooks(query: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/books/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.searchBooks(query)
                withContext(Dispatchers.Main) {
                    displayResults(response.items)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun displayResults(books: List<BookItem>) {
        llResults.removeAllViews() // Limpa resultados anteriores
        for (book in books) {
            val title = book.volumeInfo.title
            val authors = book.volumeInfo.authors?.joinToString(", ") ?: "Desconhecido"
            val publisher = book.volumeInfo.publisher ?: "Desconhecido"

            val textView = TextView(this).apply {
                text = "Título: $title\nAutor(es): $authors\nEditora: $publisher"
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 0, 0, 16)
            }

            llResults.addView(textView)
        }
    }
}

// Definição da interface ApiService
interface ApiService {
    @GET("volumes")
    suspend fun searchBooks(@Query("q") query: String): BookResponse
}

// Modelos de Dados para a resposta da API
data class BookResponse(
    val items: List<BookItem>
)

data class BookItem(
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String,
    val authors: List<String>?,
    val publisher: String?
)
