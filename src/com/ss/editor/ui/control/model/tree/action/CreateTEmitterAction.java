package com.ss.editor.ui.control.model.tree.action;

import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.ss.editor.model.undo.editor.ModelChangeConsumer;
import com.ss.editor.ui.control.model.tree.ModelNodeTree;
import com.ss.editor.ui.control.model.tree.action.operation.AddChildOperation;
import com.ss.editor.ui.control.model.tree.node.ModelNode;
import com.ss.editor.util.GeomUtils;

import org.jetbrains.annotations.NotNull;

import tonegod.emitter.EmitterMesh;
import tonegod.emitter.ParticleEmitterNode;
import tonegod.emitter.ParticleEmitterNode.BillboardMode;
import tonegod.emitter.influencers.AlphaInfluencer;
import tonegod.emitter.influencers.ColorInfluencer;
import tonegod.emitter.influencers.SizeInfluencer;
import tonegod.emitter.particle.ParticleDataTriMesh;

/**
 * The action for creating new {@link ParticleEmitterNode}.
 *
 * @author JavaSaBr
 */
public class CreateTEmitterAction extends AbstractNodeAction {

    public CreateTEmitterAction(@NotNull final ModelNodeTree nodeTree, @NotNull final ModelNode<?> node) {
        super(nodeTree, node);
    }

    @NotNull
    @Override
    protected String getName() {
        return "Create TEmitter";
    }

    @Override
    protected void process() {

        final ModelNodeTree nodeTree = getNodeTree();
        final ModelChangeConsumer modelChangeConsumer = nodeTree.getModelChangeConsumer();

        final ParticleEmitterNode emitter = new ParticleEmitterNode();
        emitter.setEnabled(true);
        emitter.setMaxParticles(100);
        emitter.addInfluencers(new ColorInfluencer(), new AlphaInfluencer(), new SizeInfluencer());

        // Shape & Emissions
        emitter.setShapeSimpleEmitter();
        emitter.setDirectionType(EmitterMesh.DirectionType.Random);
        emitter.setEmissionsPerSecond(100);
        emitter.setParticlesPerEmission(1);

        // Particle props
        emitter.setParticleType(ParticleDataTriMesh.class, (Mesh) null);
        emitter.setBillboardMode(BillboardMode.CAMERA);
        emitter.setForce(1);
        emitter.setLife(0.999f);
        emitter.setSprite("graphics/textures/sprite/default.png");

        final SizeInfluencer sizeInfluencer = emitter.getInfluencer(SizeInfluencer.class);

        if (sizeInfluencer != null) {
            sizeInfluencer.addSize(0.1f);
            sizeInfluencer.addSize(0f);
        }

        emitter.initialize(EDITOR.getAssetManager());

        final ModelNode<?> modelNode = getNode();
        final Node element = (Node) modelNode.getElement();

        final int index = GeomUtils.getIndex(modelChangeConsumer.getCurrentModel(), element);

        modelChangeConsumer.execute(new AddChildOperation(emitter, index));
    }
}
