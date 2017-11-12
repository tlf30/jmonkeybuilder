package com.ss.editor.ui.control.model.node;

import com.jme3.math.Vector3f;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.tree.node.TreeNode;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The implementation of node to show vectors in the tree.
 *
 * @author JavaSaBr
 */
public class PositionTreeNode extends TreeNode<Vector3f> {

    /**
     * The name.
     */
    @Nullable
    private String name;

    public PositionTreeNode(@NotNull final Vector3f element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FXThread
    public @Nullable Image getIcon() {
        return Icons.WAY_POINT_16;
    }

    @Override
    @FromAnyThread
    public @NotNull String getName() {
        return name == null ? "Point 3D" : name;
    }

    @Override
    @FXThread
    public boolean isNeedToSaveName() {
        return true;
    }

    @Override
    @FXThread
    public void setName(@Nullable final String name) {
        this.name = name;
    }
}
