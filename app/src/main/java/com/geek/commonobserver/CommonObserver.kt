package com.geek.commonobserver

import com.geek.commonobserver.event.EventKey
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.superclasses

object CommonObserver {

    private val observerMap = mutableMapOf<Class<*>, MutableSet<Any>>()

    fun <T : Any> registerObserver(observer: T, event: Class<T>? = null) {
        val eventKeys = event?.let { listOf(it) } ?: getEventKeys(observer)
        eventKeys.forEach { addObserver(observer, it) }
    }

    fun <T : Any> unregisterObserver(observer: T, event: Class<T>? = null) {
        val eventKeys = event?.let { listOf(it) } ?: getEventKeys(observer)
        eventKeys.forEach { removeObserver(observer, it) }
    }

    private fun getEventKeys(observer: Any): List<Class<*>> {
        return observer::class.superclasses
            .filter { it.hasAnnotation<EventKey>() }
            .map { it.java }
    }

    private fun addObserver(observer: Any, event: Class<*>) {
        observerMap.getOrPut(event) { mutableSetOf() }.add(observer)
    }

    private fun removeObserver(observer: Any, event: Class<*>) {
        observerMap[event]?.apply {
            remove(observer)
            if (isEmpty()) observerMap.remove(event)
        }
    }

    fun <T : Any> sendMessage(event: Class<T>, action: (T) -> Unit) {
        observerMap[event]?.forEach { (it as? T)?.let(action) }
    }

    fun unregisterAllObservers() {
        observerMap.clear()
    }
}