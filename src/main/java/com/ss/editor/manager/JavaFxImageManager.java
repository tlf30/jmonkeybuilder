package com.ss.editor.manager;

import static com.ss.rlib.common.util.FileUtils.getExtension;
import static com.ss.rlib.common.util.array.ArrayFactory.asArray;
import static java.awt.Image.SCALE_DEFAULT;
import static java.nio.file.StandardOpenOption.*;
import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import com.ss.editor.FileExtensions;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.config.Config;
import com.ss.editor.file.reader.DdsReader;
import com.ss.editor.file.reader.TgaReader;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.event.FxEventManager;
import com.ss.editor.ui.event.impl.ChangedCurrentAssetFolderEvent;
import com.ss.editor.ui.event.impl.DeletedFileEvent;
import com.ss.editor.util.EditorUtil;
import com.ss.rlib.common.logging.Logger;
import com.ss.rlib.common.logging.LoggerManager;
import com.ss.rlib.common.manager.InitializeManager;
import com.ss.rlib.common.util.FileUtils;
import com.ss.rlib.common.util.StringUtils;
import com.ss.rlib.common.util.Utils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.IntegerDictionary;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import jme3tools.converters.ImageToAwt;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

/**
 * The class to manage previews of images to JavaFX
 *
 * @author JavaSaBr
 */
public class JavaFxImageManager {

    @NotNull
    private static final Logger LOGGER = LoggerManager.getLogger(JavaFxImageManager.class);

    @NotNull
    private static final FxEventManager FX_EVENT_MANAGER = FxEventManager.getInstance();

    @NotNull
    private static final String PREVIEW_CACHE_FOLDER = "preview-cache";

    @NotNull
    private static final Array<String> FX_FORMATS = asArray(
            FileExtensions.IMAGE_PNG,
            FileExtensions.IMAGE_JPG,
            FileExtensions.IMAGE_JPEG,
            FileExtensions.IMAGE_GIF
    );

    @NotNull
    private static final Array<String> JME_FORMATS = asArray(FileExtensions.IMAGE_BMP);

    @NotNull
    private static final Array<String> IMAGE_IO_FORMATS = asArray(
            FileExtensions.IMAGE_HDR,
            FileExtensions.IMAGE_TIFF);

    @NotNull
    private static final Array<String> IMAGE_FORMATS = ArrayFactory.newArray(String.class);

    /**
     * The size of cached images.
     */
    private static final int CACHED_IMAGES_SIZE = 30;

    static {
        IMAGE_FORMATS.addAll(FX_FORMATS);
        IMAGE_FORMATS.addAll(JME_FORMATS);
        IMAGE_FORMATS.addAll(IMAGE_IO_FORMATS);
        IMAGE_FORMATS.add(FileExtensions.IMAGE_TGA);
        IMAGE_FORMATS.add(FileExtensions.IMAGE_DDS);
    }

    /**
     * Check the file.
     *
     * @param file the file
     * @return true if the file is image.
     */
    @FromAnyThread
    public static boolean isImage(@Nullable final Path file) {
        if (file == null) return false;
        final String extension = getExtension(file);
        return IMAGE_FORMATS.contains(extension);
    }

    /**
     * Check the file by the asset path.
     *
     * @param assetPath the asset path
     * @return true if the file is image.
     */
    @FromAnyThread
    public static boolean isImage(@Nullable final String assetPath) {
        if (assetPath == null) return false;
        final String extension = getExtension(assetPath);
        return IMAGE_FORMATS.contains(extension);
    }

    @Nullable
    private static JavaFxImageManager instance;

    @FromAnyThread
    public static @NotNull JavaFxImageManager getInstance() {
        if (instance == null) instance = new JavaFxImageManager();
        return instance;
    }

    /**
     * The cache of small images.
     */
    @NotNull
    private IntegerDictionary<IntegerDictionary<ObjectDictionary<String, Image>>> smallImageCache;

    /**
     * The cache folder.
     */
    @NotNull
    private final Path cacheFolder;

    private JavaFxImageManager() {
        InitializeManager.valid(getClass());

        final Path appFolder = Config.getAppFolderInUserHome();

        this.cacheFolder = appFolder.resolve(PREVIEW_CACHE_FOLDER);
        this.smallImageCache = DictionaryFactory.newIntegerDictionary();

        final ExecutorManager executorManager = ExecutorManager.getInstance();
        executorManager.addFxTask(() -> FX_EVENT_MANAGER.addEventHandler(DeletedFileEvent.EVENT_TYPE,
                event -> processEvent((DeletedFileEvent) event)));
        executorManager.addFxTask(() -> FX_EVENT_MANAGER.addEventHandler(ChangedCurrentAssetFolderEvent.EVENT_TYPE,
                event -> processEvent((ChangedCurrentAssetFolderEvent) event)));
    }

    /**
     * Get the cache folder.
     *
     * @return the cache folder.
     */
    @FromAnyThread
    private @NotNull Path getCacheFolder() {
        return cacheFolder;
    }

    /**
     * Get an image preview.
     *
     * @param file   the image file.
     * @param width  the required width.
     * @param height the required height.
     * @return the image.
     */
    @FxThread
    public @NotNull Image getImagePreview(@Nullable final Path file, final int width, final int height) {
        if (file == null || !Files.exists(file)) {
            return Icons.IMAGE_512;
        }

        if (width <= CACHED_IMAGES_SIZE && height <= CACHED_IMAGES_SIZE) {
            final Image image = getFromCache(file.toString(), width, height);
            if (image != null) {
                return image;
            }
        }

        final URL url = Utils.get(file, f -> f.toUri().toURL());
        final FileTime lastModFile = Utils.get(file, f -> Files.getLastModifiedTime(f));

        final Image image = getImagePreview(url, lastModFile, width, height);

        if (width <= CACHED_IMAGES_SIZE && height <= CACHED_IMAGES_SIZE) {
            putImageToCache(file.toString(), image, width, height);
        }

        return image;
    }

    /**
     * Try to get an image from the cache by path and size.
     *
     * @param path   the path to the image.
     * @param width  the width.
     * @param height the height.
     * @return the image or null.
     */
    @FxThread
    private @Nullable Image getFromCache(@NotNull final String path, final int width, final int height) {

        final IntegerDictionary<ObjectDictionary<String, Image>> heightImages = smallImageCache.get(width);
        if (heightImages == null) {
            return null;
        }

        final ObjectDictionary<String, Image> images = heightImages.get(height);
        if (images == null) {
            return null;
        }

        return images.get(path);
    }

    /**
     * Put the image to the cache.
     *
     * @param path   the ath to the image.
     * @param image  the image.
     * @param width  the width.
     * @param height the height.
     */
    @FxThread
    private void putImageToCache(@NotNull final String path, @NotNull final Image image, final int width,
                                 final int height) {
        smallImageCache.get(width, DictionaryFactory::newIntegerDictionary)
                .get(height, DictionaryFactory::newObjectDictionary)
                .put(path, image);
    }

    /**
     * Get an image preview.
     *
     * @param resourcePath the resource path to an image.
     * @param width        the required width.
     * @param height       the required height.
     * @return the image.
     */
    @FxThread
    public @NotNull Image getImagePreview(@Nullable final String resourcePath, final int width, final int height) {

        if (width <= CACHED_IMAGES_SIZE && height <= CACHED_IMAGES_SIZE) {
            final Image image = getFromCache(resourcePath, width, height);
            if (image != null) {
                return image;
            }
        }

        final ResourceManager resourceManager = ResourceManager.getInstance();
        URL url = resourceManager.tryToFindResource(resourcePath);

        if (url == null) {

            final Path realFile = EditorUtil.getRealFile(resourcePath);
            if (realFile == null || !Files.exists(realFile)) {
                return Icons.IMAGE_512;
            }

            url = FileUtils.toUrl(realFile);
        }

        final Image image = getImagePreview(url, null, width, height);

        if (width <= CACHED_IMAGES_SIZE && height <= CACHED_IMAGES_SIZE) {
            putImageToCache(resourcePath, image, width, height);
        }

        return image;
    }

    @FxThread
    private @NotNull Image getImagePreview(@NotNull URL url, @Nullable final FileTime lastModFile, final int width,
                                           final int height) {

        final String externalForm = url.toExternalForm();
        final String fileHash = StringUtils.toMD5(externalForm) + ".png";

        final Path cacheFolder = getCacheFolder();
        final Path imageFolder = cacheFolder.resolve(String.valueOf(width)).resolve(String.valueOf(height));
        final Path cacheFile = imageFolder.resolve(fileHash);

        if (Files.exists(cacheFile)) {

            final FileTime lastModCacheFile = Utils.get(cacheFile, file -> Files.getLastModifiedTime(file));

            if (lastModFile == null || lastModCacheFile.compareTo(lastModFile) >= 0) {
                final String pathToCache = Utils.get(cacheFile, first -> first.toUri().toURL().toExternalForm());
                return new Image(pathToCache, width, height, false, false);
            }
        }

        Utils.run(cacheFile, first -> Files.createDirectories(first.getParent()));

        final String extension = getExtension(externalForm);

        if (FX_FORMATS.contains(extension)) {
            return readFxImage(width, height, externalForm, cacheFile);
        } else if (JME_FORMATS.contains(extension)) {
            return readJMETexture(width, height, externalForm, cacheFile);
        } else if (IMAGE_IO_FORMATS.contains(extension)) {
            return readIOImage(url, width, height, cacheFile);
        } else if (FileExtensions.IMAGE_DDS.equals(extension)) {

            final byte[] content = Utils.get(url, first -> IOUtils.toByteArray(first.openStream()));
            final int[] pixels = DdsReader.read(content, DdsReader.ARGB, 0);
            final int currentWidth = DdsReader.getWidth(content);
            final int currentHeight = DdsReader.getHeight(content);

            final BufferedImage read = new BufferedImage(currentWidth, currentHeight, BufferedImage.TYPE_INT_ARGB);
            read.setRGB(0, 0, currentWidth, currentHeight, pixels, 0, currentWidth);

            return scaleAndWrite(width, height, cacheFile, read, currentWidth, currentHeight);

        } else if (FileExtensions.IMAGE_TGA.equals(extension)) {

            final byte[] content = Utils.get(url, first -> IOUtils.toByteArray(first.openStream()));
            final BufferedImage awtImage;
            try {
                awtImage = (BufferedImage) TgaReader.getImage(content);
            } catch (final Exception e) {
                LOGGER.warning(e);
                writeDefaultToCache(cacheFile);
                return Icons.IMAGE_512;
            }

            final int imageWidth = awtImage.getWidth();
            final int imageHeight = awtImage.getHeight();

            return scaleAndWrite(width, height, cacheFile, awtImage, imageWidth, imageHeight);
        }

        return Icons.IMAGE_512;
    }

    @FxThread
    private void writeDefaultToCache(@NotNull final Path cacheFile) {
        final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(Icons.IMAGE_512, null);
        try (final OutputStream out = Files.newOutputStream(cacheFile, WRITE, TRUNCATE_EXISTING, CREATE)) {
            ImageIO.write(bufferedImage, "png", out);
        } catch (final IOException ex) {
            LOGGER.warning(ex);
        }
    }

    @FxThread
    private @NotNull Image readIOImage(@NotNull final URL url, final int width, final int height,
                                       @NotNull final Path cacheFile) {

        final BufferedImage read;
        try {
            read = ImageIO.read(url);
        } catch (final IOException e) {
            EditorUtil.handleException(LOGGER, this, e);
            return Icons.IMAGE_512;
        }

        return scaleAndWrite(width, height, cacheFile, read, read.getWidth(), read.getHeight());
    }

    @FxThread
    private @NotNull Image readJMETexture(final int width, final int height, @NotNull final String externalForm,
                                          @NotNull final Path cacheFile) {

        final AssetManager assetManager = EditorUtil.getAssetManager();
        final Texture texture = assetManager.loadTexture(externalForm);

        final BufferedImage textureImage;
        try {
            textureImage = ImageToAwt.convert(texture.getImage(), false, true, 0);
        } catch (final UnsupportedOperationException e) {
            EditorUtil.handleException(LOGGER, this, e);
            return Icons.IMAGE_512;
        }

        final int imageWidth = textureImage.getWidth();
        final int imageHeight = textureImage.getHeight();

        return scaleAndWrite(width, height, cacheFile, textureImage, imageWidth, imageHeight);
    }

    @FxThread
    private @NotNull Image readFxImage(final int width, final int height, @NotNull final String externalForm,
                                       @NotNull final Path cacheFile) {

        Image image = new Image(externalForm);

        final int imageWidth = (int) image.getWidth();
        final int imageHeight = (int) image.getHeight();

        if (imageWidth > width || imageHeight > height) {
            if (imageWidth == imageHeight) {
                image = new Image(externalForm, width, height, false, false);
            } else if (imageWidth > imageHeight) {
                float mod = imageHeight * 1F / imageWidth;
                image = new Image(externalForm, width, height * mod, false, false);
            } else if (imageHeight > imageWidth) {
                float mod = imageWidth * 1F / imageHeight;
                image = new Image(externalForm, width * mod, height, false, false);
            }
        }

        final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

        try (final OutputStream out = Files.newOutputStream(cacheFile, WRITE, TRUNCATE_EXISTING, CREATE)) {
            ImageIO.write(bufferedImage, "png", out);
        } catch (final IOException e) {
            LOGGER.warning(e);
        }

        return image;
    }

    @FxThread
    private @NotNull Image scaleAndWrite(final int targetWidth, final int targetHeight, @NotNull final Path cacheFile,
                                         @NotNull final BufferedImage textureImage, final int currentWidth,
                                         final int currentHeight) {

        final BufferedImage newImage = scaleImage(targetWidth, targetHeight, textureImage, currentWidth, currentHeight);

        try (final OutputStream out = Files.newOutputStream(cacheFile, WRITE, TRUNCATE_EXISTING, CREATE)) {
            ImageIO.write(newImage, "png", out);
            return new Image(cacheFile.toUri().toString());
        } catch (final IOException e) {
            LOGGER.warning(e);
            return Icons.IMAGE_512;
        }
    }

    @FxThread
    private @NotNull BufferedImage scaleImage(final int width, final int height, @NotNull final BufferedImage read,
                                              final int imageWidth, final int imageHeight) {

        java.awt.Image newImage = read;

        if (imageWidth > width || imageHeight > height) {
            if (imageWidth == imageHeight) {
                newImage = read.getScaledInstance(width, height, SCALE_DEFAULT);
            } else if (imageWidth > imageHeight) {
                float mod = imageHeight * 1F / imageWidth;
                newImage = read.getScaledInstance(width, (int) (height * mod), SCALE_DEFAULT);
            } else if (imageHeight > imageWidth) {
                float mod = imageWidth * 1F / imageHeight;
                newImage = read.getScaledInstance((int) (width * mod), height, SCALE_DEFAULT);
            }
        }

        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(newImage, 0, 0, null);
        g2d.dispose();

        return bufferedImage;
    }

    @FxThread
    private void processEvent(@NotNull final DeletedFileEvent event) {
        //TODO need to add remove from cache
    }

    @FxThread
    private void processEvent(@NotNull final ChangedCurrentAssetFolderEvent event) {
        smallImageCache.clear();
    }
}
