package com.example.jsonlab4

data class Director(
    val name: String = "Не указан",
    val born: Int = 0
)

data class Movie(
    val id: Int = 0,
    val title: String = "Без названия",
    val rating: Double = 0.0,
    val watched: Boolean = false,
    val director: Director = Director(),
    val genres: List<String> = emptyList()
)

data class MovieResponse(
    val movies: List<Movie> = emptyList(),
    val total: Int = 0
)
