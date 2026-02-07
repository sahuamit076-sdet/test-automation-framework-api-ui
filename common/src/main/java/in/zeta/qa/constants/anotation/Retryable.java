package in.zeta.qa.constants.anotation;

@FunctionalInterface
public interface Retryable<T> {
    T execute() throws Throwable;
}
