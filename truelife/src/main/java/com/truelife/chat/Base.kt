package com.truelife.chat

import io.reactivex.disposables.CompositeDisposable

interface Base {
    val disposables:CompositeDisposable
}