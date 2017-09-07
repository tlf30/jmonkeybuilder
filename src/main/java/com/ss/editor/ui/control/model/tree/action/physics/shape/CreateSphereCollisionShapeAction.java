package com.ss.editor.ui.control.model.tree.action.physics.shape;

import static com.ss.editor.extension.property.EditablePropertyType.FLOAT;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.plugin.api.property.PropertyDefinition;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.rlib.util.VarTable;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The action to create a sphere collision shape.
 *
 * @author JavaSaBr
 */
public class CreateSphereCollisionShapeAction extends AbstractCreateShapeAction {

    @NotNull
    private static final String PROPERTY_RADIUS = "radius";

    public CreateSphereCollisionShapeAction(@NotNull final NodeTree<?> nodeTree, @NotNull final TreeNode<?> node) {
        super(nodeTree, node);
    }

    @Override
    @FXThread
    protected @Nullable Image getIcon() {
        return Icons.SPHERE_16;
    }

    @Override
    @FXThread
    protected @NotNull String getName() {
        return Messages.MODEL_NODE_TREE_ACTION_SPHERE_COLLISION_SHAPE;
    }

    @Override
    @FXThread
    protected @NotNull String getDialogTitle() {
        return Messages.CREATE_SPHERE_COLLISION_SHAPE_DIALOG_TITLE;
    }

    @Override
    @FXThread
    protected @NotNull Array<PropertyDefinition> getPropertyDefinitions() {
        final Array<PropertyDefinition> definitions = ArrayFactory.newArray(PropertyDefinition.class);
        definitions.add(new PropertyDefinition(FLOAT, Messages.MODEL_PROPERTY_RADIUS, PROPERTY_RADIUS, 1F));
        return definitions;
    }

    @Override
    @FXThread
    protected @NotNull CollisionShape createShape(@NotNull final VarTable vars) {
        final float radius = vars.get(PROPERTY_RADIUS);
        return new SphereCollisionShape(radius);
    }
}
