/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.common.lib

sealed class Either<out L, out R> {
    abstract fun isRight(): Boolean
    abstract fun isLeft(): Boolean
}

data class Left<out L>(val left: L) : Either<L, Nothing>() {
    override fun isRight() = false
    override fun isLeft() = true
    override fun toString(): String = "Left $left"
}

data class Right<out T>(val right: T) : Either<Nothing, T>() {
    override fun isRight() = true
    override fun isLeft() = false
    override fun toString(): String = "Right $right"
}

infix fun <L, R, O> Either<L, R>.map(f: (R) -> O): Either<L, O> = when (this) {
    is Left -> this
    is Right -> Right(f(this.right))
}

infix fun <L, R, O> Either<L, (R) -> O>.apply(f: Either<L, R>): Either<L, O> = when (this) {
    is Left -> this
    is Right -> f.map(this.right)
}

infix fun <L, R, O> Either<L, R>.flatMap(f: (R) -> Either<L, O>): Either<L, O> = when (this) {
    is Left -> this
    is Right -> f(right)
}

inline infix fun <L, R, O> Either<L, O>.mapLeft(f: (L) -> R): Either<R, O> = when (this) {
    is Left -> Left(f(left))
    is Right -> this
}

inline fun <L, R, O> Either<L, R>.fold(lf: (L) -> O, rf: (R) -> O): O = when (this) {
    is Left -> lf(this.left)
    is Right -> rf(this.right)
}
