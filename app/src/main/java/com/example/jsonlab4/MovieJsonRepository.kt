package com.example.jsonlab4

import android.content.Context
import android.util.Log
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.IOException

class MovieJsonRepository(private val context: Context) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val responseAdapter = moshi.adapter(MovieResponse::class.java)
    private val movieListType = Types.newParameterizedType(List::class.java, Movie::class.java)
    private val movieListAdapter = moshi.adapter<List<Movie>>(movieListType)

    fun readRawJson(rawResId: Int = R.raw.data): Result<String> {
        return try {
            val text = context.resources.openRawResource(rawResId)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            if (text.isBlank()) {
                Result.failure(IllegalArgumentException("Файл data.json пуст"))
            } else {
                Result.success(text)
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    fun parseMoviesFromJson(jsonText: String): Result<List<Movie>> {
        return try {
            val parsed = responseAdapter.fromJson(jsonText)
                ?: return Result.failure(IllegalArgumentException("Не удалось разобрать корневой объект JSON"))
            Result.success(parsed.movies)
        } catch (e: JsonDataException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun filterByMinRatingStrict(movies: List<Movie>, minRating: Double): List<Movie> {
        return movies.filter { it.rating > minRating }
    }

    fun sortByRatingDesc(movies: List<Movie>): List<Movie> {
        return movies.sortedByDescending { it.rating }
    }

    fun findMinRatingInfo(movies: List<Movie>): String {
        if (movies.isEmpty()) return "Список фильмов пуст — минимум не определён."
        val minMovie = movies.minByOrNull { it.rating } ?: return "Минимум не определён."
        return "Минимальный рейтинг: ${minMovie.rating} — «${minMovie.title}»"
    }

    fun serializeMoviesToPrettyJson(movies: List<Movie>, logTag: String = "JSON_LAB_4"): String {
        val prettyJson = movieListAdapter.indent("  ").toJson(movies)
        Log.d(logTag, prettyJson)
        return prettyJson
    }

    fun saveAndReadInternalFile(jsonText: String, fileName: String = "movies_output.json"): Result<String> {
        return try {
            val file = File(context.filesDir, fileName)
            file.writeText(jsonText, Charsets.UTF_8)
            val readBack = file.readText(Charsets.UTF_8)
            Result.success(readBack)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: SecurityException) {
            Result.failure(e)
        }
    }
}
