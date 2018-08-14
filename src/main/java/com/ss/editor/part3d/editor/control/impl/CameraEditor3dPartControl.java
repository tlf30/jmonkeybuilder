package com.ss.editor.part3d.editor.control.impl;

import static com.ss.editor.part3d.editor.control.impl.InputStateEditor3dPartControl.PROP_IS_BUTTON_MIDDLE_DOWN;
import static com.ss.editor.part3d.editor.control.impl.InputStateEditor3dPartControl.PROP_IS_SHIFT_DOWN;
import static com.ss.rlib.common.util.array.ArrayFactory.toArray;
import com.jme3.app.Application;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.annotation.JmeThread;
import com.ss.editor.config.Config;
import com.ss.editor.model.EditorCamera;
import com.ss.editor.model.EditorCamera.Direction;
import com.ss.editor.model.EditorCamera.Perspective;
import com.ss.editor.part3d.editor.ExtendableEditor3dPart;
import com.ss.editor.part3d.editor.control.InputEditor3dPartControl;
import com.ss.editor.util.JmeUtils;
import com.ss.rlib.common.function.FloatConsumer;
import com.ss.rlib.common.logging.LoggerLevel;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The editor's camera control.
 *
 * @author JavaSaBr
 */
public class CameraEditor3dPartControl extends BaseInputEditor3dPartControl<ExtendableEditor3dPart> implements
        InputEditor3dPartControl, ActionListener, AnalogListener {

    public static class CameraState implements Serializable {

        @NotNull Vector3f cameraLocation;

        float hRotation;
        float vRotation;
        float targetDistance;
        float cameraFlySpeed;

        public CameraState() {
            this.cameraLocation = new Vector3f();
        }

        public CameraState(
                @NotNull Vector3f cameraLocation,
                float hRotation,
                float vRotation,
                float targetDistance,
                float cameraFlySpeed
        ) {
            this.cameraLocation = cameraLocation;
            this.hRotation = hRotation;
            this.vRotation = vRotation;
            this.targetDistance = targetDistance;
            this.cameraFlySpeed = cameraFlySpeed;
        }

        @FromAnyThread
        public float getCameraFlySpeed() {
            return cameraFlySpeed;
        }

        @FromAnyThread
        public @NotNull Vector3f getCameraLocation() {
            return cameraLocation;
        }

        @FromAnyThread
        public float getHRotation() {
            return hRotation;
        }

        @FromAnyThread
        public float getVRotation() {
            return vRotation;
        }

        @FromAnyThread
        public float getTargetDistance() {
            return targetDistance;
        }
    }

    private enum KeyState {
        KEY_A,
        KEY_D,
        KEY_W,
        KEY_S,
        KEY_ANY
    }

    private static final ObjectDictionary<String, Trigger> ACTION_TRIGGERS =
            ObjectDictionary.ofType(String.class, Trigger.class);

    private static final ObjectDictionary<String, Trigger> ANALOG_TRIGGERS =
            ObjectDictionary.ofType(String.class, Trigger.class);

    private static final ObjectDictionary<String, Trigger[]> ACTION_MULTI_TRIGGERS =
            ObjectDictionary.ofType(String.class, Trigger[].class);

    private static final String MOUSE_RIGHT_CLICK = "jMB.editorCamera.rightClick";
    private static final String MOUSE_LEFT_CLICK = "jMB.editorCamera.leftClick";
    private static final String MOUSE_MIDDLE_CLICK = "jMB.editorCamera.middleClick";

    private static final String MOUSE_X_AXIS = "jMB.editorCamera.mouseXAxis";
    private static final String MOUSE_X_AXIS_NEGATIVE = "jMB.editorCamera.mouseXAxisNegative";
    private static final String MOUSE_Y_AXIS = "jMB.baseEditor.editorCamera";
    private static final String MOUSE_Y_AXIS_NEGATIVE = "jMB.editorCamera.mouseYAxisNegative";

    private static final String KEY_W = "jMB.editorCamera.W";
    private static final String KEY_S = "jMB.editorCamera.S";
    private static final String KEY_A = "jMB.editorCamera.A";
    private static final String KEY_D = "jMB.editorCamera.D";
    private static final String KEY_ALT = "jMB.editorCamera.Alt";
    private static final String KEY_CTRL = "jMB.editorCamera.Ctrl";

    private static final String KEY_NUM_1 = "jMB.editorCamera.num1";
    private static final String KEY_NUM_2 = "jMB.editorCamera.num2";
    private static final String KEY_NUM_3 = "jMB.editorCamera.num3";
    private static final String KEY_NUM_4 = "jMB.editorCamera.num4";
    private static final String KEY_NUM_6 = "jMB.editorCamera.num6";
    private static final String KEY_NUM_7 = "jMB.editorCamera.num7";
    private static final String KEY_NUM_8 = "jMB.editorCamera.num8";
    private static final String KEY_NUM_9 = "jMB.editorCamera.num9";

    private static final String[] ACTION_MAPPINGS;
    private static final String[] ANALOG_MAPPINGS;

    static {

        ACTION_TRIGGERS.put(MOUSE_RIGHT_CLICK, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        ACTION_TRIGGERS.put(MOUSE_LEFT_CLICK, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        ACTION_TRIGGERS.put(MOUSE_MIDDLE_CLICK, new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));

        ACTION_TRIGGERS.put(KEY_W, new KeyTrigger(KeyInput.KEY_W));
        ACTION_TRIGGERS.put(KEY_S, new KeyTrigger(KeyInput.KEY_S));
        ACTION_TRIGGERS.put(KEY_A, new KeyTrigger(KeyInput.KEY_A));
        ACTION_TRIGGERS.put(KEY_D, new KeyTrigger(KeyInput.KEY_D));

        ACTION_TRIGGERS.put(KEY_NUM_1, new KeyTrigger(KeyInput.KEY_NUMPAD1));
        ACTION_TRIGGERS.put(KEY_NUM_2, new KeyTrigger(KeyInput.KEY_NUMPAD2));
        ACTION_TRIGGERS.put(KEY_NUM_3, new KeyTrigger(KeyInput.KEY_NUMPAD3));
        ACTION_TRIGGERS.put(KEY_NUM_4, new KeyTrigger(KeyInput.KEY_NUMPAD4));
        ACTION_TRIGGERS.put(KEY_NUM_6, new KeyTrigger(KeyInput.KEY_NUMPAD6));
        ACTION_TRIGGERS.put(KEY_NUM_7, new KeyTrigger(KeyInput.KEY_NUMPAD7));
        ACTION_TRIGGERS.put(KEY_NUM_8, new KeyTrigger(KeyInput.KEY_NUMPAD8));
        ACTION_TRIGGERS.put(KEY_NUM_9, new KeyTrigger(KeyInput.KEY_NUMPAD9));

        ACTION_MULTI_TRIGGERS.put(KEY_CTRL, toArray(new KeyTrigger(KeyInput.KEY_RCONTROL), new KeyTrigger(KeyInput.KEY_LCONTROL)));
        ACTION_MULTI_TRIGGERS.put(KEY_ALT, toArray(new KeyTrigger(KeyInput.KEY_RMENU), new KeyTrigger(KeyInput.KEY_LMENU)));

        ANALOG_TRIGGERS.put(MOUSE_X_AXIS, new MouseAxisTrigger(MouseInput.AXIS_X, false));
        ANALOG_TRIGGERS.put(MOUSE_X_AXIS_NEGATIVE, new MouseAxisTrigger(MouseInput.AXIS_X, true));
        ANALOG_TRIGGERS.put(MOUSE_Y_AXIS, new MouseAxisTrigger(MouseInput.AXIS_X, false));
        ANALOG_TRIGGERS.put(MOUSE_Y_AXIS_NEGATIVE, new MouseAxisTrigger(MouseInput.AXIS_X, true));

        Array<String> mappings = ACTION_TRIGGERS.keyArray(String.class);
        mappings.addAll(ACTION_MULTI_TRIGGERS.keyArray(String.class));

        ACTION_MAPPINGS = mappings.toArray(String.class);
        ANALOG_MAPPINGS = ANALOG_TRIGGERS.keyArray(String.class)
                .toArray(String.class);
    }

    /**
     * The editor camera.
     */
    @NotNull
    private final EditorCamera editorCamera;

    /**
     * The light of the camera.
     */
    @NotNull
    private final DirectionalLight light;

    /**
     * The node on which the camera is looking.
     */
    @NotNull
    private final Node cameraNode;

    /**
     * The flag of flying camera.
     */
    @NotNull
    private final AtomicInteger cameraFlying;

    /**
     * The flag of moving camera.
     */
    @NotNull
    private final AtomicInteger cameraMoving;

    /**
     * The state of camera keys.
     */
    @NotNull
    private final boolean[] keyStates;

    /**
     * The key state handlers.
     */
    @NotNull
    private final FloatConsumer[] keyStateHandlers;

    /**
     * The previous state of the camera.
     */
    @NotNull
    private final CameraState prevState;

    /**
     * The camera fly speed.
     */
    private float cameraFlySpeed;

    /**
     * True of need to add light for the camera.
     */
    private boolean needLight;

    /**
     * True if need to update light to follow the camera.
     */
    private boolean needUpdateLight;

    /**
     * TRue if need to allow the camera to be moved.
     */
    private boolean needMovableCamera;

    public CameraEditor3dPartControl(@NotNull ExtendableEditor3dPart editor3dPart) {
        this(editor3dPart, true, true, true);
    }

    public CameraEditor3dPartControl(
            @NotNull ExtendableEditor3dPart editor3dPart,
            boolean needLight,
            boolean needUpdateLight,
            boolean needMovableCamera
    ) {
        super(editor3dPart);
        this.cameraFlying = new AtomicInteger();
        this.cameraMoving = new AtomicInteger();
        this.keyStates = new boolean[4];
        this.keyStateHandlers = new FloatConsumer[4];
        this.cameraFlySpeed = 1F;
        this.cameraNode = new Node("CameraNode");
        this.editorCamera = createEditorCamera(editor3dPart.getCamera());
        this.light = createLight();
        this.needLight = needLight;
        this.needUpdateLight = needUpdateLight;
        this.needMovableCamera = needMovableCamera;
        this.prevState = new CameraState();

        actionHandlers.put(KEY_NUM_1, (isPressed, tpf) -> rotateTo(Perspective.BACK, isPressed));
        actionHandlers.put(KEY_NUM_3, (isPressed, tpf) -> rotateTo(Perspective.RIGHT, isPressed));
        actionHandlers.put(KEY_NUM_7, (isPressed, tpf) -> rotateTo(Perspective.TOP, isPressed));
        actionHandlers.put(KEY_NUM_9, (isPressed, tpf) -> rotateTo(Perspective.BOTTOM, isPressed));
        actionHandlers.put(KEY_NUM_2, (isPressed, tpf) -> rotateTo(Direction.BOTTOM, isPressed));
        actionHandlers.put(KEY_NUM_8, (isPressed, tpf) -> rotateTo(Direction.TOP, isPressed));
        actionHandlers.put(KEY_NUM_4, (isPressed, tpf) -> rotateTo(Direction.LEFT, isPressed));
        actionHandlers.put(KEY_NUM_6, (isPressed, tpf) -> rotateTo(Direction.RIGHT, isPressed));

        actionHandlers.put(MOUSE_MIDDLE_CLICK, (isPressed, tpf) -> {
            if (isCameraFlying() && !isPressed) {
                finishCameraMoving(KeyState.KEY_ANY, true);
            }
        });

        actionHandlers.put(KEY_CTRL, (isPressed, tpf) -> {
            if (isCameraFlying() && isPressed && cameraFlySpeed > 0) {
                cameraFlySpeed = Math.max(cameraFlySpeed - 0.4F, 0.1F);
            }
        });

        actionHandlers.put(KEY_ALT, (isPressed, tpf) -> {
            if (isCameraFlying() && isPressed && cameraFlySpeed > 0) {
                cameraFlySpeed += 0.4F;
            }
        });

        actionHandlers.put(KEY_A, (isPressed, tpf) ->
                moveCameraSide(tpf, true, isPressed, KeyState.KEY_A));
        actionHandlers.put(KEY_D, (isPressed, tpf) ->
                moveCameraSide(-tpf, true, isPressed, KeyState.KEY_D));
        actionHandlers.put(KEY_W, (isPressed, tpf) ->
                moveCameraForward(tpf, true, isPressed, KeyState.KEY_W));
        actionHandlers.put(KEY_S, (isPressed, tpf) ->
                moveCameraForward(-tpf, true, isPressed, KeyState.KEY_S));

        analogHandlers.put(MOUSE_X_AXIS, (value, tpf) -> moveXMouse(value));
        analogHandlers.put(MOUSE_X_AXIS_NEGATIVE, (value, tpf) -> moveXMouse(-value));
        analogHandlers.put(MOUSE_Y_AXIS, (value, tpf) -> moveYMouse(-value));
        analogHandlers.put(MOUSE_Y_AXIS_NEGATIVE, (value, tpf) -> moveYMouse(value));

        keyStateHandlers[KeyState.KEY_A.ordinal()] = tpf ->
                moveCameraSide(tpf * 30, false, false, KeyState.KEY_A);
        keyStateHandlers[KeyState.KEY_D.ordinal()] = tpf ->
                moveCameraSide(-tpf * 30, false, false, KeyState.KEY_D);
        keyStateHandlers[KeyState.KEY_W.ordinal()] = tpf ->
                moveCameraForward(tpf * 30, false, false, KeyState.KEY_W);
        keyStateHandlers[KeyState.KEY_S.ordinal()] = tpf ->
                moveCameraForward(-tpf * 30, false, false, KeyState.KEY_S);
    }

    /**
     * Create light for camera directional light.
     *
     * @return the light for the camera.
     */
    @FromAnyThread
    protected @NotNull DirectionalLight createLight() {

        var light = new DirectionalLight();
        light.setColor(ColorRGBA.White);

        return light;
    }

    /**
     * sets the default horizontal rotation in radian of the camera at start of the application
     *
     * @param angleInRad the angle in rad
     */
    @FromAnyThread
    public void setDefaultHorizontalRotation(float angleInRad) {
        editorCamera.setDefaultHorizontalRotation(angleInRad);
    }

    /**
     * sets the default vertical rotation in radian of the camera at start of the application
     *
     * @param angleInRad the angle in rad
     */
    @FromAnyThread
    public void setDefaultVerticalRotation(float angleInRad) {
        editorCamera.setDefaultVerticalRotation(angleInRad);
    }

    /**
     * Create an editor camera.
     *
     * @param camera the camera.
     * @return the new editor camera.
     */
    @FromAnyThread
    protected @NotNull EditorCamera createEditorCamera(@NotNull Camera camera) {

        var editorCamera = new EditorCamera(camera, cameraNode);
        editorCamera.setMaxDistance(10000);
        editorCamera.setMinDistance(0.01F);
        editorCamera.setSmoothMotion(false);
        editorCamera.setRotationSensitivity(1);
        editorCamera.setZoomSensitivity(0.2F);

        return editorCamera;
    }

    /**
     * Set the light's direction.
     *
     * @param direction the light's direction.
     */
    @FromAnyThread
    public void setLightDirection(@NotNull Vector3f direction) {
        this.light.setDirection(direction);
    }

    @Override
    @JmeThread
    public void initialize(@NotNull Application application) {
        super.initialize(application);

        var rootNode = editor3dPart.getRootNode();
        rootNode.attachChild(cameraNode);

        if (needLight) {
            rootNode.addLight(light);
        }
    }

    @Override
    @JmeThread
    public void cleanup(@NotNull Application application) {
        super.cleanup(application);

        var rootNode = editor3dPart.getRootNode();
        rootNode.detachChild(cameraNode);

        if (needLight) {
            rootNode.removeLight(light);
        }
    }

    /**
     * Enable camera's light.
     */
    @JmeThread
    public void enableLight() {

        if (needLight) {
            return;
        }

        editor3dPart.getRootNode()
                .addLight(light);

        needLight = true;
    }

    /**
     * Disable camera's light.
     */
    @JmeThread
    public void disableLight() {

        if (!needLight) {
            return;
        }

        editor3dPart.getRootNode()
                .removeLight(light);

        needLight = false;
    }

    @Override
    @JmeThread
    public void register(@NotNull InputManager inputManager) {

        ACTION_TRIGGERS.forEach(inputManager, JmeUtils::addMapping);
        ACTION_MULTI_TRIGGERS.forEach(inputManager, JmeUtils::addMapping);
        ANALOG_TRIGGERS.forEach(inputManager, JmeUtils::addMapping);

        inputManager.addListener(getActionListener(), ACTION_MAPPINGS);
        inputManager.addListener(getAnalogListener(), ANALOG_MAPPINGS);

        editorCamera.registerInput(inputManager);
    }

    @Override
    @JmeThread
    public void unregister(@NotNull InputManager inputManager) {
        super.unregister(inputManager);
        editorCamera.unregisterInput(inputManager);
    }

    @Override
    @JmeThread
    public void onAction(@NotNull String name, boolean isPressed, float tpf) {
        super.onAction(name, isPressed, tpf);

        if (needMovableCamera) {
            boolean isShiftDown = editor3dPart.getBooleanProperty(PROP_IS_SHIFT_DOWN);
            boolean isButtonMiddleDown = editor3dPart.getBooleanProperty(PROP_IS_BUTTON_MIDDLE_DOWN);
            editorCamera.setLockRotation(isShiftDown && isButtonMiddleDown);
        }
    }

    @Override
    @JmeThread
    public void cameraUpdate(float tpf) {
        editorCamera.updateCamera(tpf);

        if (isCameraFlying()) {
            for (int i = 0; i < keyStateHandlers.length; i++) {
                if (keyStates[i]) {
                   // keyStateHandlers[i].consume(tpf);
                }
            }
        }

        checkCameraChanges(editorCamera);
    }

    @Override
    @JmeThread
    public void postCameraUpdate(float tpf) {
        if (needLight && needUpdateLight) {
            light.setDirection(editorCamera.getDirection());
        }
    }

    /**
     * Rotate to the perspective.
     *
     * @param perspective the perspective.
     * @param isPressed   true if a key is pressed.
     */
    @JmeThread
    private void rotateTo(@NotNull Perspective perspective, boolean isPressed) {
        if (isPressed) {
            editorCamera.rotateTo(perspective);
        }
    }

    /**
     * Rotate to the direction.
     *
     * @param direction the direction.
     * @param isPressed true if a key is pressed.
     */
    @JmeThread
    private void rotateTo(@NotNull Direction direction, boolean isPressed) {
        if (isPressed) {
            editorCamera.rotateTo(direction, 10F);
        }
    }

    /**
     * Return true if the camera is flying now.
     *
     * @return true if the camera is flying now.
     */
    @JmeThread
    public boolean isCameraFlying() {
        return cameraFlying.get() != 0;
    }

    /**
     * Return true if the camera is moving now.
     *
     * @return true if the camera is moving now.
     */
    @JmeThread
    public boolean isCameraMoving() {
        return cameraMoving.get() != 0;
    }

    /**
     * Start to move the camera.
     */
    @JmeThread
    private void startCameraFlying(@NotNull KeyState key) {

        if (Config.DEV_CAMERA_DEBUG && LOGGER.isEnabled(LoggerLevel.DEBUG)) {
            LOGGER.debug(this, "start camera moving[" + cameraFlying + "] for key " + key);
        }

        if (cameraFlying.get() == 0) {

            var location = editor3dPart.getCamera()
                    .getLocation();

            if (Config.DEV_CAMERA_DEBUG && LOGGER.isEnabled(LoggerLevel.DEBUG)) {
                LOGGER.debug(this, "init position: " + location + " of the camera.");
            }

            //FIXME change the logic
            cameraNode.setLocalTranslation(Vector3f.ZERO);
            editorCamera.setTargetDistance(0);
            editorCamera.updateCamera(1);
        }

        if (!keyStates[key.ordinal()]) {
            keyStates[key.ordinal()] = true;
            cameraFlying.incrementAndGet();
        }
    }


    /**
     * Finish to move the camera.
     */
    @JmeThread
    private void finishCameraMoving(@NotNull KeyState key, boolean force) {

        if (Config.DEV_CAMERA_DEBUG && LOGGER.isEnabled(LoggerLevel.DEBUG)) {
            LOGGER.debug(this, "finish camera moving[" + cameraFlying + "] for key " + key + ", force = " + force);
        }

        if (key != KeyState.KEY_ANY) {
            keyStates[key.ordinal()] = false;
        }

        if (cameraFlying.get() == 0) {
            return;
        }

        if (force) {
            cameraFlying.set(0);
            Arrays.fill(keyStates, false);
        } else {
            cameraFlying.decrementAndGet();
        }
    }

    /**
     * Move a camera to direction.
     *
     * @param value the value to move.
     */
    @JmeThread
    private void moveCameraForward(float value, boolean isAction, boolean isPressed, @NotNull KeyState key) {

        if (!canCameraFly()) {
            return;
        }  else if (isAction && isPressed) {
            startCameraFlying(key);
        } else if (isAction) {
            finishCameraMoving(key, false);
        }

        var direction = editorCamera.getDirection();
        direction.multLocal(value * cameraFlySpeed);
        direction.addLocal(cameraNode.getLocalTranslation());

        cameraNode.setLocalTranslation(direction);
    }

    /**
     * Move a camera to side.
     *
     * @param value the value to move.
     */
    @JmeThread
    private void moveCameraSide(float value, boolean isAction, boolean isPressed, @NotNull KeyState key) {

        if (!canCameraFly()) {
            return;
        }  else if (isAction && isPressed) {
            startCameraFlying(key);
        } else if (isAction) {
            finishCameraMoving(key, false);
        }

        var left = editorCamera.getLeft();
        left.multLocal(value * cameraFlySpeed);
        left.addLocal(cameraNode.getLocalTranslation());

        cameraNode.setLocalTranslation(left);
    }


    /**
     * Move a mouse on X axis.
     *
     * @param value the value to move.
     */
    @JmeThread
    protected void moveXMouse(float value) {

        /*final EditorCamera editorCamera = getEditorCamera();
        final Camera camera = EDITOR.getCamera();
        final Node nodeForCamera = getNodeForCamera();

        final Vector3f left = camera.getLeft();
        left.multLocal(value * (float) Math.sqrt(editorCamera.getTargetDistance()));
        left.addLocal(nodeForCamera.getLocalTranslation());

        nodeForCamera.setLocalTranslation(left);*/
    }

    /**
     * Move a mouse on Y axis.
     *
     * @param value the value to move.
     */
    @JmeThread
    protected void moveYMouse(float value) {

        /*final EditorCamera editorCamera = getEditorCamera();
        final Camera camera = EDITOR.getCamera();
        final Node nodeForCamera = getNodeForCamera();

        final Vector3f up = camera.getUp();
        up.multLocal(value * (float) Math.sqrt(editorCamera.getTargetDistance()));
        up.addLocal(nodeForCamera.getLocalTranslation());

        nodeForCamera.setLocalTranslation(up);*/
    }

    @JmeThread
    private boolean canCameraFly() {
        var isButtonMiddleDown = editor3dPart.getBooleanProperty(PROP_IS_BUTTON_MIDDLE_DOWN);
        var isShiftMiddleDown = editor3dPart.getBooleanProperty(PROP_IS_SHIFT_DOWN);
        return isButtonMiddleDown && !isShiftMiddleDown && !isCameraMoving();
    }

    @JmeThread
    private boolean canCameraMoveOrFly() {
        return editor3dPart.getBooleanProperty(PROP_IS_BUTTON_MIDDLE_DOWN);
    }


    /**
     * Check camera changes.
     *
     * @param editorCamera the editor's camera.
     */
    @JmeThread
    protected void checkCameraChanges(@NotNull EditorCamera editorCamera) {

        int changes = 0;

        var cameraLocation = cameraNode.getLocalTranslation();

        var hRotation = editorCamera.getHRotation();
        var vRotation = editorCamera.getVRotation();
        var targetDistance = editorCamera.getTargetDistance();

        if (!prevState.cameraLocation.equals(cameraLocation)) {
            changes++;
        } else if (prevState.hRotation != hRotation || prevState.vRotation != vRotation) {
            changes++;
        } else if (prevState.targetDistance != targetDistance) {
            changes++;
        } else if (prevState.cameraFlySpeed != cameraFlySpeed) {
            changes++;
        }

        if (changes > 0) {
            notifyChangedCameraState(new CameraState(cameraLocation.clone(), hRotation, vRotation, targetDistance, cameraFlySpeed));
        }

        prevState.cameraLocation.set(cameraLocation);
        prevState.hRotation = hRotation;
        prevState.vRotation = vRotation;
        prevState.targetDistance = targetDistance;
        prevState.cameraFlySpeed = cameraFlySpeed;
    }

    /**
     * Notify about changed camera's state.
     *
     * @param cameraState the camera's state.
     */
    @JmeThread
    protected void notifyChangedCameraState(@NotNull CameraState cameraState) {

      //  ExecutorManager.getInstance()
       //         .addFxTask(() -> fileEditor.notifyChangedCameraSettings(cameraLocation, hRotation, vRotation, targetDistance, cameraFlySpeed));
    }

    /**
     * Update the camera's state to this camera.
     *
     * @param cameraState the camera's state.
     */
    @JmeThread
    public void applyState(@NotNull CameraState cameraState) {

        prevState.cameraLocation.set(cameraState.getCameraLocation());
        prevState.hRotation = cameraState.getHRotation();
        prevState.vRotation = cameraState.getVRotation();
        prevState.targetDistance = cameraState.getTargetDistance();
        prevState.cameraFlySpeed = cameraState.getCameraFlySpeed();

        editorCamera.setTargetHRotation(cameraState.getHRotation());
        editorCamera.setTargetVRotation(cameraState.getVRotation());
        editorCamera.setTargetDistance(cameraState.getTargetDistance());

        cameraNode.setLocalTranslation(cameraState.getCameraLocation());

        editorCamera.updateCamera(1);
    }
}
