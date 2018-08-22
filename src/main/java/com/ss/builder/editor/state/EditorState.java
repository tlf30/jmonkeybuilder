package com.ss.builder.editor.state;

import com.ss.builder.annotation.FxThread;
import com.ss.builder.editor.event.FileEditorEvent;
import com.ss.builder.editor.state.impl.AdditionalEditorState;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * The interface for implementing a state container of Editor.
 *
 * @author JavaSaBr
 */
public interface EditorState extends Serializable {

    /**
     * Sets change handler.
     *
     * @param handle the change handler.
     */
    @FxThread
    void setChangeHandler(@NotNull Runnable handle);

    /**
     * Get or create an additional editor state which will store state in this state.
     *
     * @param type    the type of additional state.
     * @param factory the factory of the state if it will not be exists.
     * @param <T>     the type of the state.
     * @return the additional editor state.
     */
    @FxThread
    <T extends AdditionalEditorState> @NotNull T getOrCreateAdditionalState(
            @NotNull Class<T> type,
            @NotNull Supplier<T> factory
    );

    /**
     * Notify this editor's state about some editor's events.
     *
     * @param event the file editor's state.
     */
    @FxThread
    default void notify(@NotNull FileEditorEvent event) {
    }
}