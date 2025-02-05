package com.geek.commonobserver

import kotlin.reflect.full.superclasses

object CommonObserver {

    private val observerMap = mutableMapOf<Class<*>, MutableSet<Any>>()

    fun <T : Any> registerObserver(observer: T) {
        val eventKeyList = observer::class.superclasses.filter { BaseObserverEvent::class.java.isAssignableFrom(it.java) }.map { it.java }
        eventKeyList.forEach { eventKey ->
            eventKey.let { event ->
                observerMap.getOrPut(event) { mutableSetOf() }.add(observer)
            }
        }
    }

    fun <T : Any> unregisterObserver(observer: T) {
        val eventKeyList = observer::class.superclasses.filter { BaseObserverEvent::class.java.isAssignableFrom(it.java) }.map { it.java }
        eventKeyList.forEach { eventKey ->
            eventKey.let { event ->
                observerMap[event]?.let { observerSet ->
                    if (observerSet.contains(observer)) {
                        observerSet.remove(observer)
                    }

                    if (observerSet.isEmpty()) {
                        observerMap.remove(event)
                    }
                }
            }
        }
    }

    fun <T : Any> sendMessage(event: Class<T>, action: (T) -> Unit) {
        if (observerMap.contains(event)) {
            try {
                observerMap[event]?.let { observerSet ->
                    observerSet.forEach { observer ->
                        val currentObserver = observer as? T
                        currentObserver?.let(action)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unregisterAllObserver() {
        observerMap.clear()
    }
}