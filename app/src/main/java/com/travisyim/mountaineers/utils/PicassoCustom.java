package com.travisyim.mountaineers.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

public class PicassoCustom {
    public static class CropCircleTransformation implements Transformation {
        /* This class takes an image and removes the corner fillets with a radius of half the
         * minimum edge length */

         @Override
        public Bitmap transform(Bitmap source) {
            Bitmap finalBitmap;
            Canvas canvas;
            Paint paint;
            BitmapShader shader;
            float radius;
            int size;

            size = Math.min(source.getWidth(), source.getHeight());

            finalBitmap = Bitmap.createBitmap(size, size, source.getConfig());

            canvas = new Canvas(finalBitmap);
            paint = new Paint();
            shader = new BitmapShader
                    (source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            radius = size / 2f;
            canvas.drawCircle(radius, radius, radius, paint);

            source.recycle();

            return finalBitmap;
        }

        @Override
        public String key() {
            return "cropCircle";
        }
    }

    public static class ScaleByWidthTransformation implements Transformation {
        /* This class takes an image and resizes it based on the provided width while maintain the
         * aspect ratio */

         private int mWidth;

        public ScaleByWidthTransformation(final int width) {
            mWidth = width;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            Bitmap result;
            double aspectRatio;
            int targetHeight;

            aspectRatio = (double) source.getHeight() / (double) source.getWidth();
            targetHeight = (int) (mWidth * aspectRatio);
            result = Bitmap.createScaledBitmap(source, mWidth, targetHeight, false);

            source.recycle();

            return result;
        }

        @Override
        public String key() {
            return "scaledByWidth";
        }
    }
}