package com.ss.builder.fx.control.tree.node.impl.physics.shape;

import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.ss.builder.Messages;
import com.ss.builder.annotation.FromAnyThread;
import com.ss.builder.annotation.FxThread;
import com.ss.builder.fx.Icons;
import com.ss.builder.Messages;
import com.ss.builder.annotation.FxThread;
import com.ss.builder.annotation.FromAnyThread;
import com.ss.builder.fx.Icons;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The implementation of node to show {@link CapsuleCollisionShape}.
 *
 * @author JavaSaBr
 */
public class CapsuleCollisionShapeTreeNode extends CollisionShapeTreeNode<CapsuleCollisionShape> {

    public CapsuleCollisionShapeTreeNode(@NotNull final CapsuleCollisionShape element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FxThread
    public @Nullable Image getIcon() {
        return Icons.CAPSULE_16;
    }

    @Override
    @FromAnyThread
    public @NotNull String getName() {
        return Messages.MODEL_FILE_EDITOR_NODE_CAPSULE_COLLISION_SHAPE;
    }
}