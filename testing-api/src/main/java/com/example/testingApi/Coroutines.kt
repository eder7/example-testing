package com.example.testingApi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

class DispatcherProvider(val main: CoroutineDispatcher, val io: CoroutineDispatcher) {
    constructor() : this(Dispatchers.Main, Dispatchers.IO)
    constructor(all: CoroutineDispatcher) : this(all, all)
    constructor(all: CoroutineContext) : this(all[ContinuationInterceptor] as CoroutineDispatcher)
    constructor(all: CoroutineScope) : this(all.coroutineContext)
}