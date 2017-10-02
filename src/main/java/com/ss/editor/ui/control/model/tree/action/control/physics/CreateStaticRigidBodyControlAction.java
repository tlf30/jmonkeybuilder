package com.ss.editor.ui.control.model.tree.action.control.physics;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Spatial;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.node.TreeNode;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The action to create a {@link RigidBodyControl}.
 *
 * @author JavaSaBr
 */
public class CreateStaticRigidBodyControlAction extends CreateRigidBodyControlAction {

    public CreateStaticRigidBodyControlAction(@NotNull final NodeTree<?> nodeTree,
                                              @NotNull final TreeNode<?> node) {
        super(nodeTree, node);
    }

    @Override
    @FXThread
    protected @Nullable Image getIcon() {
        return Icons.STATIC_RIGID_BODY_16;
    }

    @Override
    @FXThread
    protected @NotNull String getName() {
        return Messages.MODEL_NODE_TREE_ACTION_ADD_CONTROL_STATIC_RIGID_BODY;
    }


    @Override
    @FXThread
    protected @NotNull RigidBodyControl createControl(@NotNull final Spatial parent) {
        final RigidBodyControl rigidBodyControl = super.createControl(parent);
        rigidBodyControl.setMass(0F);
        return rigidBodyControl;
    }
}
