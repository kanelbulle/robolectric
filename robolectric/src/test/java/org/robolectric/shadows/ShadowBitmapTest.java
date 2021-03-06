package org.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.internal.Shadow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowBitmapTest {
  @Test
  public void shouldCreateScaledBitmap() throws Exception {
    Bitmap originalBitmap = create("Original bitmap");
    Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 200, false);
    assertEquals("Original bitmap scaled to 100 x 200", shadowOf(scaledBitmap).getDescription());
    assertEquals(100, scaledBitmap.getWidth());
    assertEquals(200, scaledBitmap.getHeight());
  }

  @Test
  public void shouldCreateActiveBitmap() throws Exception {
    Bitmap bitmap = Bitmap.createBitmap(100, 200, Config.ARGB_8888);
    assertFalse(bitmap.isRecycled());
    assertThat(bitmap.getPixel(0, 0)).isZero();
    assertThat(bitmap.getWidth()).isEqualTo(100);
    assertThat(bitmap.getHeight()).isEqualTo(200);
    assertThat(bitmap.getConfig()).isEqualTo(Config.ARGB_8888);
  }

  @Test
  public void shouldCreateBitmapWithColors() throws Exception {
    int[] colors = new int[] {
        Color.parseColor("#ff0000"), Color.parseColor("#00ff00"), Color.parseColor("#0000ff"),
        Color.parseColor("#990000"), Color.parseColor("#009900"), Color.parseColor("#000099")
    };
    Bitmap bitmap = Bitmap.createBitmap(colors, 3, 2, Config.ARGB_8888);
    assertThat(bitmap.getWidth()).isEqualTo(3);
    assertThat(bitmap.getHeight()).isEqualTo(2);
    assertThat(bitmap.getConfig()).isEqualTo(Config.ARGB_8888);
    assertThat(bitmap.getPixel(0, 0)).isEqualTo(Color.parseColor("#ff0000"));
    assertThat(bitmap.getPixel(0, 1)).isEqualTo(Color.parseColor("#990000"));
    assertThat(bitmap.getPixel(1, 0)).isEqualTo(Color.parseColor("#00ff00"));
    assertThat(bitmap.getPixel(1, 1)).isEqualTo(Color.parseColor("#009900"));
    assertThat(bitmap.getPixel(2, 0)).isEqualTo(Color.parseColor("#0000ff"));
    assertThat(bitmap.getPixel(2, 1)).isEqualTo(Color.parseColor("#000099"));
  }

  @Test
  public void shouldCreateBitmapFromAnotherBitmap() {
    Bitmap originalBitmap = create("Original bitmap");
    Bitmap newBitmap = Bitmap.createBitmap(originalBitmap);
    assertEquals("Original bitmap created from Bitmap object", shadowOf(newBitmap).getDescription());
  }

  @Test
  public void shouldCreateMutableBitmap() throws Exception {
    Bitmap mutableBitmap = Bitmap.createBitmap(100, 200, Config.ARGB_8888);
    assertTrue(mutableBitmap.isMutable());
  }

  @Test
  public void shouldRecycleBitmap() throws Exception {
    Bitmap bitmap = Bitmap.createBitmap(100, 200, Config.ARGB_8888);
    bitmap.recycle();
    assertTrue(bitmap.isRecycled());
  }

  @Test
  public void equals_shouldCompareDescriptions() throws Exception {
    assertFalse(create("bitmap A").equals(create("bitmap B")));
    assertTrue(create("bitmap A").equals(create("bitmap A")));
  }

  @Test
  public void equals_shouldCompareWidthAndHeight() throws Exception {
    Bitmap bitmapA1 = create("bitmap A");
    shadowOf(bitmapA1).setWidth(100);
    shadowOf(bitmapA1).setHeight(100);

    Bitmap bitmapA2 = create("bitmap A");
    shadowOf(bitmapA2).setWidth(101);
    shadowOf(bitmapA2).setHeight(101);

    assertFalse(bitmapA1.equals(bitmapA2));
  }

  @Test
  public void shouldReceiveDescriptionWhenDrawingToCanvas() throws Exception {
    Bitmap bitmap1 = create("Bitmap One");
    Bitmap bitmap2 = create("Bitmap Two");

    Canvas canvas = new Canvas(bitmap1);
    canvas.drawBitmap(bitmap2, 0, 0, null);

    assertEquals("Bitmap One\nBitmap Two", shadowOf(bitmap1).getDescription());
  }

  @Test
  public void shouldReceiveDescriptionWhenDrawingToCanvasWithBitmapAndMatrixAndPaint() throws Exception {
    Bitmap bitmap1 = create("Bitmap One");
    Bitmap bitmap2 = create("Bitmap Two");

    Canvas canvas = new Canvas(bitmap1);
    canvas.drawBitmap(bitmap2, new Matrix(), null);

    assertEquals("Bitmap One\nBitmap Two transformed by matrix", shadowOf(bitmap1).getDescription());
  }

  @Test
  public void shouldReceiveDescriptionWhenDrawABitmapToCanvasWithAPaintEffect() throws Exception {
    Bitmap bitmap1 = create("Bitmap One");
    Bitmap bitmap2 = create("Bitmap Two");

    Canvas canvas = new Canvas(bitmap1);
    Paint paint = new Paint();
    paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix()));
    canvas.drawBitmap(bitmap2, new Matrix(), paint);

    assertEquals("Bitmap One\nBitmap Two with ColorMatrixColorFilter<1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0> transformed by matrix", shadowOf(bitmap1).getDescription());
  }

  @Test
  public void visualize_shouldReturnDescription() throws Exception {
    Bitmap bitmap = create("Bitmap One");
    assertEquals("Bitmap One", ShadowBitmap.visualize(bitmap));
  }

  @Test
  public void shouldCopyBitmap() {
    Bitmap bitmap = Shadow.newInstanceOf(Bitmap.class);
    Bitmap bitmapCopy = bitmap.copy(Config.ARGB_8888, true);
    assertEquals(shadowOf(bitmapCopy).getConfig(), Config.ARGB_8888);
    assertTrue(shadowOf(bitmapCopy).isMutable());
  }

  @Test
  public void rowBytesIsAccurate() {
    Bitmap b1 = Bitmap.createBitmap(10, 10, Config.ARGB_8888);
    assertThat(b1.getRowBytes()).isEqualTo(40);
    Bitmap b2 = Bitmap.createBitmap(10, 10, Config.RGB_565);
    assertThat(b2.getRowBytes()).isEqualTo(20);

    // Null config is not allowed.
    try {
      Bitmap b3 = Bitmap.createBitmap(10, 10, null);
      b3.getRowBytes();
      fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void byteCountIsAccurate() {
    Bitmap b1 = Bitmap.createBitmap(10, 10, Config.ARGB_8888);
    assertThat(b1.getByteCount()).isEqualTo(400);
    Bitmap b2 = Bitmap.createBitmap(10, 10, Config.RGB_565);
    assertThat(b2.getByteCount()).isEqualTo(200);

    // Null config is not allowed.
    try {
      Bitmap b3 = Bitmap.createBitmap(10, 10, null);
      b3.getByteCount();
      fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void shouldSetPixel() {
    Bitmap bitmap = Bitmap.createBitmap(new int[] { 1 }, 1, 1, Config.ARGB_8888);
    shadowOf(bitmap).setMutable(true);
    bitmap.setPixel(0, 0, 2);
    assertThat(bitmap.getPixel(0, 0)).isEqualTo(2);
    assertThat(shadowOf(bitmap).getCreatedFromColors()).isEqualTo(new int[] { 1 });
  }

  @Test
  public void shouldSetPixel_allocateOnTheFly() {
    Bitmap bitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
    shadowOf(bitmap).setMutable(true);
    bitmap.setPixel(0, 0, 2);
    assertThat(bitmap.getPixel(0, 0)).isEqualTo(2);
    assertThat(shadowOf(bitmap).getCreatedFromColors()).isNull();
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionForSetPixelOnImmutableBitmap() {
    Bitmap bitmap = Bitmap.createBitmap(new int[] { 1 }, 1, 1, Config.ARGB_8888);
    bitmap.setPixel(0, 0, 2);
  }

  @Test
  public void bitmapsAreReused() {
    Bitmap b = Bitmap.createBitmap(10, 10, Config.ARGB_8888);
    Bitmap b1 = Bitmap.createBitmap(b, 0, 0, 10, 10);
    assertThat(b1).isSameAs(b);
    Bitmap b2 = Bitmap.createBitmap(b, 0, 0, 10, 10, null, false);
    assertThat(b2).isSameAs(b);
    Bitmap b3 = Bitmap.createScaledBitmap(b, 10, 10, false);
    assertThat(b3).isSameAs(b);
  }

  @Test
  public void equalsSizeTransformReturnsOriginal() {
    Bitmap b1 = Bitmap.createBitmap(10, 10, Config.ARGB_8888);
    Bitmap b2 = Bitmap.createBitmap(b1, 0, 0, 10, 10, null, false);
    assertThat(b1).isSameAs(b2);
    Bitmap b3 = Bitmap.createBitmap(b1, 0, 0, 10, 10, null, true);
    assertThat(b1).isSameAs(b3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void throwsExceptionForInvalidDimensions() {
    Bitmap b = Bitmap.createBitmap(10, 20, Config.ARGB_8888);
    Bitmap.createBitmap(b, 0, 0, 20, 10, null, false);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void throwsExceptionForNegativeWidth() {
    Bitmap.createBitmap(-100, 10, Config.ARGB_8888);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void throwsExceptionForZeroHeight() {
    Bitmap.createBitmap(100, 0, Config.ARGB_8888);
  }

  private static Bitmap create(String name) {
    Bitmap bitmap = Shadow.newInstanceOf(Bitmap.class);
    shadowOf(bitmap).appendDescription(name);
    return bitmap;
  }
}
