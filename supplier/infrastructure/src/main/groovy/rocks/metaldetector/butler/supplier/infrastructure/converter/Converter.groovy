package rocks.metaldetector.butler.supplier.infrastructure.converter

/**
 * This interface defines an implementation contract for a class that can convert a defined data type to another defined data type.
 * @param <S> The source type
 * @param <T> The target type
 */
interface Converter<S, T> {

  T convert(S source)

}
