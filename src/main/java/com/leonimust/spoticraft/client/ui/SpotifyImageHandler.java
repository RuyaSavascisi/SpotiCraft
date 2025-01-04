package com.leonimust.spoticraft.client.ui;

import com.leonimust.spoticraft.SpotiCraft;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

public class SpotifyImageHandler {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final HashMap<String, ResourceLocation> CACHE = new HashMap<>();
    private static final File CACHE_DIR = new File(MC.gameDirectory, "spoticraft/cache");

    static {
        if (!CACHE_DIR.exists()) {
            boolean result = CACHE_DIR.mkdirs();
            if (!result) {
                throw new RuntimeException("Unable to create directory " + CACHE_DIR);
            }
        }
    }

    public static ResourceLocation downloadImage(String url) {
        try {
            // Check cache first
            if (CACHE.containsKey(url)) {
                return CACHE.get(url);
            }

            // Generate a hash for the URL to use as a filename
            String fileName = UUID.nameUUIDFromBytes(url.getBytes()) + ".png";
            File cachedFile = new File(CACHE_DIR, fileName);

            // Check if the image is already downloaded
            if (cachedFile.exists()) {
                System.out.println(cachedFile.getAbsolutePath());
                return loadFromDisk(cachedFile, url);
            }

            // Download the image
            URL imageUrl = new URI(url).toURL();
            InputStream inputStream = imageUrl.openStream();
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            IOUtils.closeQuietly(inputStream);

            // Save the image to disk
            ImageIO.write(bufferedImage, "png", cachedFile);

            // Load the image and cache it
            return loadFromDisk(cachedFile, url);

        } catch (Exception e) {
            System.out.println("Failed to load image from " + url + ": " + e.getMessage());
            return null; // Return null if something goes wrong
        }
    }

    private static ResourceLocation loadFromDisk(File file, String url) throws Exception {
        BufferedImage bufferedImage = ImageIO.read(file);

        // Convert BufferedImage to NativeImage
        NativeImage nativeImage = convertToNativeImage(bufferedImage);

        // Create a DynamicTexture from the NativeImage
        DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);

        // Register the texture in Minecraft's TextureManager
        ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/spotify_cover_" + UUID.randomUUID());
        MC.getTextureManager().register(textureLocation, dynamicTexture);

        // Cache the texture
        CACHE.put(url, textureLocation);
        return textureLocation;
    }

    private static NativeImage convertToNativeImage(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, width, height, true);

        // Transfer pixels from BufferedImage to NativeImage
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int argb = bufferedImage.getRGB(x, y); // Get pixel in ARGB format
                nativeImage.setPixel(x, y, argb);
            }
        }

        return nativeImage;
    }
}
