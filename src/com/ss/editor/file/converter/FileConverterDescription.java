package com.ss.editor.file.converter;

import java.util.concurrent.Callable;

import rlib.util.array.Array;

/**
 * Класс для описания конвертера файлов.
 *
 * @author Ronn
 */
public class FileConverterDescription {

    /**
     * Описание конвертера.
     */
    private String description;

    /**
     * Конструктор конвертера.
     */
    private Callable<FileConverter> constructor;

    /**
     * Список поддерживаемых расширений.
     */
    private Array<String> extensions;

    /**
     * @return список поддерживаемых расширений.
     */
    public Array<String> getExtensions() {
        return extensions;
    }

    /**
     * @param extensions список поддерживаемых расширений.
     */
    public void setExtensions(final Array<String> extensions) {
        this.extensions = extensions;
    }

    /**
     * @return конструктор конвертера.
     */
    public Callable<FileConverter> getConstructor() {
        return constructor;
    }

    /**
     * @param constructor конструктор конвертера.
     */
    public void setConstructor(final Callable<FileConverter> constructor) {
        this.constructor = constructor;
    }

    /**
     * @return описание конвертера.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description описание конвертера.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "FileConverterDescription{" +
                "extensions=" + extensions +
                ", description='" + description + '\'' +
                '}';
    }
}