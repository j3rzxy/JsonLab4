package com.example.jsonlab4

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var repository: MovieJsonRepository
    private lateinit var adapter: MovieAdapter
    private val moviesList = mutableListOf<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        repository = MovieJsonRepository(this)
        adapter = MovieAdapter(emptyList())

        val rvMovies = findViewById<RecyclerView>(R.id.rvMovies)
        val etFilterRating = findViewById<EditText>(R.id.etFilterRating)
        val btnFilter = findViewById<Button>(R.id.btnFilter)
        val btnSortDesc = findViewById<Button>(R.id.btnSortDesc)
        val tvMinInfo = findViewById<TextView>(R.id.tvMinInfo)

        val etNewTitle = findViewById<EditText>(R.id.etNewTitle)
        val etNewRating = findViewById<EditText>(R.id.etNewRating)
        val etNewDirector = findViewById<EditText>(R.id.etNewDirector)
        val etNewGenres = findViewById<EditText>(R.id.etNewGenres)
        val btnAddMovie = findViewById<Button>(R.id.btnAddMovie)
        val tvOutput = findViewById<TextView>(R.id.tvOutput)

        rvMovies.layoutManager = LinearLayoutManager(this)
        rvMovies.adapter = adapter

        // Загрузка и первичный разбор JSON из res/raw/data.json
        loadInitialData(tvMinInfo, tvOutput)

        // Фильтрация по rating строго больше введённого числа
        btnFilter.setOnClickListener {
            val rawInput = etFilterRating.text.toString().trim().replace(',', '.')
            if (rawInput.isEmpty()) {
                etFilterRating.error = "Введите число для фильтрации"
                return@setOnClickListener
            }

            val threshold = rawInput.toDoubleOrNull()
            if (threshold == null || threshold.isNaN()) {
                etFilterRating.error = "Некорректное число"
                return@setOnClickListener
            }

            val filtered = repository.filterByMinRatingStrict(moviesList, threshold)
            adapter.updateItems(filtered)
            tvMinInfo.text = "${repository.findMinRatingInfo(filtered)} | Найдено (> $threshold): ${filtered.size}"
        }

        // Сортировка по rating по убыванию
        btnSortDesc.setOnClickListener {
            val sorted = repository.sortByRatingDesc(moviesList)
            moviesList.clear()
            moviesList.addAll(sorted)
            adapter.updateItems(moviesList)
            tvMinInfo.text = "${repository.findMinRatingInfo(moviesList)} (отсортировано по убыванию рейтинга)"
        }

        // Добавление новой записи, сериализация в JSON и сохранение/чтение из filesDir
        btnAddMovie.setOnClickListener {
            val title = etNewTitle.text.toString().trim()
            val ratingText = etNewRating.text.toString().trim().replace(',', '.')
            val directorName = etNewDirector.text.toString().trim()
            val genresText = etNewGenres.text.toString().trim()

            if (title.isEmpty()) {
                etNewTitle.error = "Введите название фильма"
                return@setOnClickListener
            }

            val rating = ratingText.toDoubleOrNull()
            if (ratingText.isEmpty() || rating == null || rating.isNaN()) {
                etNewRating.error = "Введите корректный рейтинг"
                return@setOnClickListener
            }

            if (directorName.isEmpty()) {
                etNewDirector.error = "Введите имя режиссёра"
                return@setOnClickListener
            }

            val genres = if (genresText.isEmpty()) {
                emptyList()
            } else {
                genresText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }

            val nextId = (moviesList.maxOfOrNull { it.id } ?: 0) + 1
            val newMovie = Movie(
                id = nextId,
                title = title,
                rating = rating,
                watched = false,
                director = Director(name = directorName),
                genres = genres
            )

            moviesList.add(newMovie)
            adapter.updateItems(moviesList)
            tvMinInfo.text = repository.findMinRatingInfo(moviesList)

            val prettyJson = repository.serializeMoviesToPrettyJson(moviesList)
            val fileResult = repository.saveAndReadInternalFile(prettyJson)

            fileResult.fold(
                onSuccess = { readBackJson ->
                    tvOutput.text = "JSON сериализован и прочитан из filesDir:\n\n$readBackJson"
                    etNewTitle.text.clear()
                    etNewRating.text.clear()
                    etNewDirector.text.clear()
                    etNewGenres.text.clear()
                },
                onFailure = { error ->
                    tvOutput.text = "Ошибка работы с файлом во внутреннем хранилище: ${error.localizedMessage}"
                }
            )
        }
    }

    private fun loadInitialData(tvMinInfo: TextView, tvOutput: TextView) {
        val rawResult = repository.readRawJson()
        rawResult.fold(
            onSuccess = { rawJson ->
                val parseResult = repository.parseMoviesFromJson(rawJson)
                parseResult.fold(
                    onSuccess = { parsedMovies ->
                        moviesList.clear()
                        moviesList.addAll(parsedMovies)
                        adapter.updateItems(moviesList)
                        tvMinInfo.text = repository.findMinRatingInfo(moviesList)
                        tvOutput.text = "Загружено фильмов из data.json: ${moviesList.size}"
                    },
                    onFailure = { error ->
                        tvMinInfo.text = "Ошибка разбора JSON"
                        tvOutput.text = "Некорректная структура JSON: ${error.localizedMessage}"
                    }
                )
            },
            onFailure = { error ->
                tvMinInfo.text = "Ошибка чтения ресурса"
                tvOutput.text = "Не удалось прочитать data.json: ${error.localizedMessage}"
            }
        )
    }
}
