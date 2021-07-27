package com.somecmpn.reviews.utils;

public final class ErrorMessageUtils {
    private ErrorMessageUtils() { }

    public static <T> String getEntityNotFoundMessage(Class<T> clazz, long id) {
        return String.format("%s with id = %s not found.", clazz.getSimpleName(), id);
    }
}
