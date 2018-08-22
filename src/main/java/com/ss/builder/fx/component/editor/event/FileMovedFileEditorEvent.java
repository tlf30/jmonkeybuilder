package com.ss.builder.fx.component.editor.event;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * The event about a moved file.
 *
 * @author JavaSaBr
 */
public class FileMovedFileEditorEvent extends FilePathChangedFileEditorEvent {

    public FileMovedFileEditorEvent(@NotNull Path prevFile, @NotNull Path newFile) {
        super(prevFile, newFile);
    }
}