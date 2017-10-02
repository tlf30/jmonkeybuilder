package com.ss.editor.ui.control.model.tree.action.particle.emitter.toneg0d.shape;

import static com.ss.editor.extension.property.EditablePropertyType.FLOAT;
import static com.ss.editor.extension.property.EditablePropertyType.INTEGER;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Cylinder;
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
import tonegod.emitter.ParticleEmitterNode;

/**
 * The action to switch an emitter shape of the {@link ParticleEmitterNode} to a {@link Cylinder}.
 *
 * @author JavaSaBr
 */
public class CreateCylinderShapeEmitterAction extends AbstractCreateShapeEmitterAction {

    @NotNull
    private static final String PROPERTY_AXIS_SAMPLES = "axisSamples";

    @NotNull
    private static final String PROPERTY_RADIAL_SAMPLES = "radialSamples";

    @NotNull
    private static final String PROPERTY_RADIUS = "radius";

    @NotNull
    private static final String PROPERTY_HEIGHT = "height";

    public CreateCylinderShapeEmitterAction(@NotNull final NodeTree<?> nodeTree, @NotNull final TreeNode<?> node) {
        super(nodeTree, node);
    }

    @Override
    @FXThread
    protected @Nullable Image getIcon() {
        return Icons.CYLINDER_16;
    }

    @Override
    @FXThread
    protected @NotNull String getName() {
        return Messages.MODEL_NODE_TREE_ACTION_PARTICLE_EMITTER_CYLINDER_SHAPE;
    }

    @Override
    @FXThread
    protected @NotNull Array<PropertyDefinition> getPropertyDefinitions() {

        final Array<PropertyDefinition> definitions = ArrayFactory.newArray(PropertyDefinition.class);
        definitions.add(new PropertyDefinition(INTEGER, Messages.MODEL_PROPERTY_AXIS_SAMPLES, PROPERTY_AXIS_SAMPLES, 8));
        definitions.add(new PropertyDefinition(INTEGER, Messages.MODEL_PROPERTY_RADIAL_SAMPLES, PROPERTY_RADIAL_SAMPLES, 16));
        definitions.add(new PropertyDefinition(FLOAT, Messages.MODEL_PROPERTY_RADIUS, PROPERTY_RADIUS, 0.25F));
        definitions.add(new PropertyDefinition(FLOAT, Messages.MODEL_PROPERTY_HEIGHT, PROPERTY_HEIGHT, 0.5F));

        return definitions;
    }

    @Override
    @FXThread
    protected @NotNull String getDialogTitle() {
        return Messages.CREATE_PARTICLE_EMITTER_CYLINDER_SHAPE_DIALOG_TITLE;
    }

    @Override
    @FXThread
    protected @NotNull Mesh createMesh(@NotNull final VarTable vars) {
        final int axisSamples = vars.getInteger(PROPERTY_AXIS_SAMPLES);
        final int radialSamples = vars.getInteger(PROPERTY_RADIAL_SAMPLES);
        final float radius = vars.getFloat(PROPERTY_RADIUS);
        final float height = vars.getFloat(PROPERTY_HEIGHT);
        return new Cylinder(axisSamples, radialSamples, radius, height);
    }
}
