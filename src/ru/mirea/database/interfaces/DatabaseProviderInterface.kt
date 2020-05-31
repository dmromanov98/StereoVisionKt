package ru.mirea.database.interfaces

interface DatabaseProviderInterface {
    fun <T : Any> add(classRef: T): Boolean
    fun <T : Any> delete(classRef: T): Boolean
    fun <T : Any> get(classRef: T): List<T>?
}