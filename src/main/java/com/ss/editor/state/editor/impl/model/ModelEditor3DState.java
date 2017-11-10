package com.ss.editor.state.editor.impl.model;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.environment.generation.JobProgressAdapter;
import com.jme3.light.DirectionalLight;
import com.jme3.light.LightProbe;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.state.editor.impl.scene.AbstractSceneEditor3DState;
import com.ss.editor.ui.component.editor.impl.model.ModelFileEditor;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tonegod.emitter.filter.TonegodTranslucentBucketFilter;

/**
 * The implementation of the {@link AbstractSceneEditor3DState} for the {@link ModelFileEditor}.
 *
 * @author JavaSaBr
 */
public class ModelEditor3DState extends AbstractSceneEditor3DState<ModelFileEditor, Spatial> {

    @NotNull
    private final JobProgressAdapter<LightProbe> probeHandler = new JobProgressAdapter<LightProbe>() {

        @Override
        public void done(final LightProbe result) {
            if (!isInitialized()) return;
            notifyProbeComplete();
        }
    };

    /**
     * The array of custom skies.
     */
    @NotNull
    private final Array<Spatial> customSky;

    /**
     * The node for the placement of custom sky.
     */
    @NotNull
    private final Node customSkyNode;

    /**
     * The current fast sky.
     */
    @Nullable
    private Spatial currentFastSky;

    /**
     * The flag of activity light of the camera.
     */
    private boolean lightEnabled;

    /**
     * The frame rate.
     */
    private int frame;

    public ModelEditor3DState(final ModelFileEditor fileEditor) {
        super(fileEditor);
        this.customSkyNode = new Node("Custom Sky");
        this.customSky = ArrayFactory.newArray(Spatial.class);

        final Node stateNode = getStateNode();
        stateNode.attachChild(getCustomSkyNode());

        setLightEnabled(true);
    }

    /**
     * @return the node for the placement of custom sky.
     */
    @JMEThread
    private @NotNull Node getCustomSkyNode() {
        return customSkyNode;
    }

    /**
     * @return the array of custom skies.
     */
    @JMEThread
    private @NotNull Array<Spatial> getCustomSky() {
        return customSky;
    }

    /**
     * Activate the node with models.
     */
    @JMEThread
    private void notifyProbeComplete() {

        final Node stateNode = getStateNode();
        stateNode.attachChild(getModelNode());
        stateNode.attachChild(getToolNode());

        final Node customSkyNode = getCustomSkyNode();
        customSkyNode.detachAllChildren();

        final TonegodTranslucentBucketFilter translucentBucketFilter = EDITOR.getTranslucentBucketFilter();
        translucentBucketFilter.refresh();
    }

    /**
     * @param currentFastSky the current fast sky.
     */
    @JMEThread
    private void setCurrentFastSky(@Nullable final Spatial currentFastSky) {
        this.currentFastSky = currentFastSky;
    }

    /**
     * @return the current fast sky.
     */
    @JMEThread
    private @Nullable Spatial getCurrentFastSky() {
        return currentFastSky;
    }

    /**
     * @return true if the light of the camera is enabled.
     */
    @JMEThread
    private boolean isLightEnabled() {
        return lightEnabled;
    }

    /**
     * @param lightEnabled the flag of activity light of the camera.
     */
    @JMEThread
    private void setLightEnabled(final boolean lightEnabled) {
        this.lightEnabled = lightEnabled;
    }

    @Override
    @JMEThread
    public void initialize(@NotNull final AppStateManager stateManager, @NotNull final Application application) {
        super.initialize(stateManager, application);
        frame = 0;
    }

    @Override
    @JMEThread
    public void cleanup() {
        super.cleanup();

        final Node stateNode = getStateNode();
        stateNode.detachChild(getModelNode());
        stateNode.detachChild(getToolNode());
    }

    @Override
    @JMEThread
    public void update(final float tpf) {
        super.update(tpf);

        if (frame == 2) {

            final Node customSkyNode = getCustomSkyNode();

            final Array<Spatial> customSky = getCustomSky();
            customSky.forEach(spatial -> customSkyNode.attachChild(spatial.clone(false)));

            EDITOR.updateLightProbe(probeHandler);
        }

        frame++;
    }

    @Override
    @JMEThread
    protected boolean needUpdateCameraLight() {
        return true;
    }

    @Override
    @JMEThread
    protected boolean needLightForCamera() {
        return true;
    }

    /**
     * Update light.
     *
     * @param enabled the enabled
     */
    @FromAnyThread
    public void updateLightEnabled(final boolean enabled) {
        EXECUTOR_MANAGER.addJMETask(() -> updateLightEnabledImpl(enabled));
    }

    /**
     * The process of updating the light.
     */
    @JMEThread
    private void updateLightEnabledImpl(boolean enabled) {
        if (enabled == isLightEnabled()) return;

        final DirectionalLight light = getLightForCamera();
        final Node stateNode = getStateNode();

        if (enabled) {
            stateNode.addLight(light);
        } else {
            stateNode.removeLight(light);
        }

        setLightEnabled(enabled);
    }

    /**
     * Change the fast sky.
     *
     * @param fastSky the fast sky
     */
    @FromAnyThread
    public void changeFastSky(@Nullable final Spatial fastSky) {
        EXECUTOR_MANAGER.addJMETask(() -> changeFastSkyImpl(fastSky));
    }

    /**
     * The process of changing the fast sky.
     */
    @JMEThread
    private void changeFastSkyImpl(@Nullable final Spatial fastSky) {

        final Node stateNode = getStateNode();
        final Spatial currentFastSky = getCurrentFastSky();

        if (currentFastSky != null) {
            stateNode.detachChild(currentFastSky);
        }

        if (fastSky != null) {
            stateNode.attachChild(fastSky);
        }

        stateNode.detachChild(getModelNode());
        stateNode.detachChild(getToolNode());

        setCurrentFastSky(fastSky);

        frame = 0;
    }

    /**
     * Add the custom sky.
     *
     * @param sky the sky
     */
    @FromAnyThread
    public void addCustomSky(@NotNull final Spatial sky) {
        EXECUTOR_MANAGER.addJMETask(() -> addCustomSkyImpl(sky));
    }

    /**
     * The process of adding the custom sky.
     */
    @JMEThread
    private void addCustomSkyImpl(@NotNull final Spatial sky) {
        final Array<Spatial> customSky = getCustomSky();
        customSky.add(sky);
    }

    /**
     * Remove the custom sky.
     *
     * @param sky the sky
     */
    @FromAnyThread
    public void removeCustomSky(@NotNull final Spatial sky) {
        EXECUTOR_MANAGER.addJMETask(() -> removeCustomSkyImpl(sky));
    }

    /**
     * The process of removing the custom sky.
     */
    @JMEThread
    private void removeCustomSkyImpl(@NotNull final Spatial sky) {
        final Array<Spatial> customSky = getCustomSky();
        customSky.slowRemove(sky);
    }

    /**
     * Update the light probe.
     */
    @FromAnyThread
    public void updateLightProbe() {
        EXECUTOR_MANAGER.addJMETask(() -> {

            final Node stateNode = getStateNode();
            stateNode.detachChild(getModelNode());
            stateNode.detachChild(getToolNode());

            frame = 0;
        });
    }

    @Override
    public String toString() {
        return "ModelEditor3DState{" +
                ", lightEnabled=" + lightEnabled +
                "} " + super.toString();
    }
}
