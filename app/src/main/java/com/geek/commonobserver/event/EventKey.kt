package com.geek.commonobserver.event

/**
 * When creating an event, you must attach this annotation
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventKey