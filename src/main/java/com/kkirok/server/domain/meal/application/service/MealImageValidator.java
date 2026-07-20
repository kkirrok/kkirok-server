package com.kkirok.server.domain.meal.application.service;

import com.kkirok.server.domain.meal.exception.MealErrorCode;
import com.kkirok.server.domain.meal.exception.MealException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

@Component
public class MealImageValidator {

    private static final Set<String> IMAGE_IO_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/bmp"
    );

    private static final int MAX_SAMPLES_PER_AXIS = 160;
    private static final int NEAR_BLACK_LUMINANCE = 12;
    private static final int NEAR_WHITE_LUMINANCE = 245;
    private static final double DOMINANT_EMPTY_PIXEL_RATIO = 0.98;
    private static final double MIN_LUMINANCE_STANDARD_DEVIATION = 2.0;

    public void validate(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);

            if (image == null) {
                if (isImageIoSupportedContentType(file.getContentType())) {
                    throw new MealException(
                            MealErrorCode.UNRECOGNIZABLE_MEAL_IMAGE
                    );
                }

                return;
            }

            if (isVisuallyEmpty(image)) {
                throw new MealException(
                        MealErrorCode.UNRECOGNIZABLE_MEAL_IMAGE
                );
            }
        } catch (IOException exception) {
            throw new MealException(
                    MealErrorCode.UNRECOGNIZABLE_MEAL_IMAGE
            );
        }
    }

    private boolean isImageIoSupportedContentType(String contentType) {
        return contentType != null
                && IMAGE_IO_CONTENT_TYPES.contains(
                contentType.toLowerCase(Locale.ROOT)
        );
    }

    private boolean isVisuallyEmpty(BufferedImage image) {
        int stepX = Math.max(
                1,
                image.getWidth() / MAX_SAMPLES_PER_AXIS
        );

        int stepY = Math.max(
                1,
                image.getHeight() / MAX_SAMPLES_PER_AXIS
        );

        long sampleCount = 0;
        long nearBlackCount = 0;
        long nearWhiteCount = 0;

        double luminanceSum = 0.0;
        double luminanceSquaredSum = 0.0;

        for (int y = 0; y < image.getHeight(); y += stepY) {
            for (int x = 0; x < image.getWidth(); x += stepX) {
                int rgb = image.getRGB(x, y);

                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                double luminance =
                        (0.299 * red)
                                + (0.587 * green)
                                + (0.114 * blue);

                sampleCount++;
                luminanceSum += luminance;
                luminanceSquaredSum += luminance * luminance;

                if (luminance <= NEAR_BLACK_LUMINANCE) {
                    nearBlackCount++;
                }

                if (luminance >= NEAR_WHITE_LUMINANCE) {
                    nearWhiteCount++;
                }
            }
        }

        if (sampleCount == 0) {
            return true;
        }

        double nearBlackRatio =
                (double) nearBlackCount / sampleCount;

        double nearWhiteRatio =
                (double) nearWhiteCount / sampleCount;

        double averageLuminance =
                luminanceSum / sampleCount;

        double variance = Math.max(
                0.0,
                (luminanceSquaredSum / sampleCount)
                        - (averageLuminance * averageLuminance)
        );

        double standardDeviation = Math.sqrt(variance);

        return nearBlackRatio >= DOMINANT_EMPTY_PIXEL_RATIO
                || nearWhiteRatio >= DOMINANT_EMPTY_PIXEL_RATIO
                || standardDeviation
                < MIN_LUMINANCE_STANDARD_DEVIATION;
    }
}