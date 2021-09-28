package rocks.metaldetector.butler.supplier.infrastructure.cover

import groovyx.net.http.HttpBuilder

import java.util.function.Function

interface HttpBuilderFunction extends Function<String, HttpBuilder> {
}
