package rocks.metaldetector.butler.service.cover

import groovyx.net.http.HTTPBuilder

import java.util.function.Function

interface HTTPBuilderFunction extends Function<URL, HTTPBuilder> {
}