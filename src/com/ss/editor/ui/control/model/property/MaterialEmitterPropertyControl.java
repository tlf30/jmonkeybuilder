package com.ss.editor.ui.control.model.property;

import static com.ss.editor.util.EditorUtil.getRealFile;

import com.jme3.asset.AssetKey;
import com.jme3.material.Material;
import com.ss.editor.model.undo.editor.ModelChangeConsumer;
import com.ss.editor.ui.dialog.asset.ParticlesAssetEditorDialog;
import com.ss.editor.ui.event.impl.RequestedOpenFileEvent;
import com.ss.editor.ui.scene.EditorFXScene;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.scene.control.Label;
import rlib.util.StringUtils;
import tonegod.emitter.ParticleEmitterNode;
import tonegod.emitter.material.ParticlesMaterial;

/**
 * The implementation of the {@link ModelPropertyControl} for editing the {@link Material}.
 *
 * @author JavaSaBr
 */
public class MaterialEmitterPropertyControl extends MaterialModelPropertyEditor<ParticleEmitterNode, ParticlesMaterial> {

    public MaterialEmitterPropertyControl(@NotNull final ParticlesMaterial element, final String paramName, final ModelChangeConsumer modelChangeConsumer) {
        super(element, paramName, modelChangeConsumer);
    }

    /**
     * Show dialog for choosing another material.
     */
    protected void processChange() {

        final EditorFXScene scene = JFX_APPLICATION.getScene();

        final ParticlesAssetEditorDialog dialog = new ParticlesAssetEditorDialog(this::addMaterial);
        dialog.setExtensionFilter(MATERIAL_EXTENSIONS);
        dialog.show(scene.getWindow());
    }

    /**
     * Add the mew material.
     */
    private void addMaterial(@NotNull final ParticlesMaterial particlesMaterial) {
        changed(particlesMaterial, getPropertyValue());
        setIgnoreListener(true);
        try {
            reload();
        } finally {
            setIgnoreListener(false);
        }
    }

    @Override
    protected void processEdit() {

        final ParticlesMaterial element = getPropertyValue();
        final Material material = element.getMaterial();
        if (material == null) return;

        final AssetKey<?> key = material.getKey();
        if (key == null) return;

        final Path assetFile = Paths.get(key.getName());
        final Path realFile = getRealFile(assetFile);
        if (!Files.exists(realFile)) return;

        final RequestedOpenFileEvent event = new RequestedOpenFileEvent();
        event.setFile(realFile);

        FX_EVENT_MANAGER.notify(event);
    }


    @Override
    protected void reload() {

        final ParticlesMaterial element = getPropertyValue();
        final Material material = element.getMaterial();
        final AssetKey<?> key = material == null ? null : material.getKey();

        final Label materialLabel = getMaterialLabel();
        materialLabel.setText(key == null || StringUtils.isEmpty(key.getName()) ? NO_MATERIAL : key.getName());
    }
}