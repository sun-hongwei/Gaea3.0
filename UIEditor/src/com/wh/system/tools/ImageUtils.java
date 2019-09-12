package com.wh.system.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.builders.BufferedImageBuilder;
import net.coobird.thumbnailator.util.exif.ExifUtils;
import net.coobird.thumbnailator.util.exif.Orientation;

public abstract class ImageUtils {

	private static Boolean DEFAULT_FORCE = false;

	public static class NotSupportFormat extends Exception {

		/**
		* 
		*/
		private static final long serialVersionUID = -247114710665020678L;
	}

	static void copyStream(InputStream inputStream, int size, OutputStream outputStream) throws IOException {
		byte[] buffer = new byte[8196];
		int len = -1;
		while ((len = inputStream.read(buffer, 0, Math.min(buffer.length, size))) != -1 && size > 0) {
			outputStream.write(buffer, 0, len);
			size -= len;
		}
	}

	public static BufferedImage loadAndroidImage(InputStream inputStream) throws Exception {
		DataInputStream dataInputStream = new DataInputStream(inputStream);
		int len = dataInputStream.readInt();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		copyStream(dataInputStream, len, outputStream);

		ByteArrayInputStream buffer = new ByteArrayInputStream(outputStream.toByteArray());

		ImageInputStream imageInputStream = ImageIO.createImageInputStream(buffer);
		return ImageIO.read(imageInputStream);
	}

	public static void saveAndroidImage(RenderedImage img, OutputStream outputStream) throws Exception {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ImageIO.write(img, "jpg", ImageIO.createImageOutputStream(buffer));
		DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
		byte[] data = buffer.toByteArray();
		dataOutputStream.writeInt(data.length);
		dataOutputStream.write(data);
	}

	public static BufferedImage loadImage(InputStream inputStream) throws IOException  {
		ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
		return ImageIO.read(imageInputStream);
	}

	public static BufferedImage loadImage(byte[] data) throws IOException {
		try(ByteArrayInputStream inputStream = new ByteArrayInputStream(data);){
			ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
			return ImageIO.read(imageInputStream);			
		}catch (Exception e) {
			throw e;
		}
	}

	public static String getExts() {
		// ImageIO ֧ : [BMP, bmp, jpg, JPG, wbmp, jpeg, png, PNG, JPEG, WBMP,
		// GIF, gif]
		String types = Arrays.toString(ImageIO.getReaderFormatNames()).replace(" ", "");
		return "图片=" + types.substring(1, types.length() - 2).replace(",", ";");
	}

	public static BufferedImage loadImage(File imgFile) throws Exception {
		if (imgFile.exists()) {
			// ImageIO: [BMP, bmp, jpg, JPG, wbmp, jpeg, png, PNG, JPEG, WBMP,
			// GIF, gif]
			String types = Arrays.toString(ImageIO.getReaderFormatNames());
			String suffix = null;

			if (imgFile.getName().indexOf(".") > -1) {
				suffix = imgFile.getName().substring(imgFile.getName().lastIndexOf(".") + 1);
			}
			if (suffix == null || types.toLowerCase().indexOf(suffix.toLowerCase()) < 0) {
				throw new NotSupportFormat();
			}
			return ImageIO.read(imgFile);
		} else {
			throw new FileNotFoundException();
		}
	}

	public static void saveImage(RenderedImage img, File imgFile) throws Exception {
		saveImage(img, imgFile, "png");
	}

	public static void saveImage(RenderedImage img, OutputStream outputStream) throws Exception {
		ImageIO.write(img, "png", ImageIO.createImageOutputStream(outputStream));
	}

	public static void saveImage(RenderedImage img, File imgFile, String imageType) throws Exception {
		ImageIO.write(img, imageType, imgFile);
	}

	public static BufferedImage thumbnailImage(File imgFile, int w, int h) throws Exception {
		return thumbnailImage(new FileInputStream(imgFile), w, h);
	}

	public static BufferedImage thumbnailImage(BufferedImage img, int w, int h) throws Exception {
		byte[] data = imageToBytes(img);
		return thumbnailImage(data, w, h);
	}

	public static BufferedImage thumbnailImage(byte[] data, int w, int h) throws Exception {
		return thumbnailImage(new ByteArrayInputStream(data), w, h);
	}

	public static BufferedImage thumbnailImage(InputStream inputStream, int w, int h) throws Exception {
		ImageInputStream iis = ImageIO.createImageInputStream(inputStream);

		// get all currently registered readers that recognize the image format
		Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);

		if (!iter.hasNext()) {
			throw new RuntimeException("No readers found!");
		}

		// get the first reader
		ImageReader reader = iter.next();

		return reader.readThumbnail(0, 0);
	}

	public static BufferedImage cloneImage(BufferedImage img) throws Exception {
		int w = img.getWidth();
		int h = img.getHeight();

		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();
		g.drawImage(img, 0, 0, w, h, Color.LIGHT_GRAY, null);
		g.dispose();
		return bi;

	}

	public static BufferedImage thumbnailImage(String imagePath, int w, int h, boolean force) throws Exception {
		return thumbnailImage(imagePath, w, h, force);
	}

	public static BufferedImage thumbnailImage(String imagePath, int w, int h) throws Exception {
		return thumbnailImage(imagePath, w, h, DEFAULT_FORCE);
	}

	private static double[] calculatePosition(double x, double y, double angle) {
		angle = Math.toRadians(angle);
		double nx = (Math.cos(angle) * x) - (Math.sin(angle) * y);
		double ny = (Math.sin(angle) * x) + (Math.cos(angle) * y);
		return new double[] { nx, ny };
	}

	public static byte[] imageToBytes(BufferedImage image) throws IOException {
		return imageToBytes(image, "jpg");
	}

	public static byte[] imageToBytes(BufferedImage image, String format) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (!ImageIO.write(image, format, baos))
			return null;
		return baos.toByteArray();
	}

	public static BufferedImage bytesToImage(byte[] bytes) throws IOException {
		InputStream input = new ByteArrayInputStream(bytes);
		return ImageIO.read(input);
	}

	public static BufferedImage rotateImg(byte[] bytes, double angle) throws IOException {
		return rotateImg(bytesToImage(bytes), angle);
	}

	public static BufferedImage rotateImg(BufferedImage image, double angle) throws IOException {
		int width = image.getWidth();
		int height = image.getHeight();
		double[][] newPositions = new double[4][];
		newPositions[0] = calculatePosition(0, 0, angle);
		newPositions[1] = calculatePosition(width, 0, angle);
		newPositions[2] = calculatePosition(0, height, angle);
		newPositions[3] = calculatePosition(width, height, angle);
		double minX = Math.min(Math.min(newPositions[0][0], newPositions[1][0]),
				Math.min(newPositions[2][0], newPositions[3][0]));
		double maxX = Math.max(Math.max(newPositions[0][0], newPositions[1][0]),
				Math.max(newPositions[2][0], newPositions[3][0]));
		double minY = Math.min(Math.min(newPositions[0][1], newPositions[1][1]),
				Math.min(newPositions[2][1], newPositions[3][1]));
		double maxY = Math.max(Math.max(newPositions[0][1], newPositions[1][1]),
				Math.max(newPositions[2][1], newPositions[3][1]));
		int newWidth = (int) Math.round(maxX - minX);
		int newHeight = (int) Math.round(maxY - minY);
		BufferedImage new_img = new BufferedImageBuilder(newWidth, newHeight, BufferedImage.TYPE_INT_BGR).build();

		Graphics2D g = new_img.createGraphics();

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		double w = newWidth / 2.0;
		double h = newHeight / 2.0;

		g.rotate(Math.toRadians(angle), w, h);
		int centerX = (int) Math.round((newWidth - width) / 2.0);
		int centerY = (int) Math.round((newHeight - height) / 2.0);

		g.drawImage(image, centerX, centerY, null);
		g.dispose();

		return new_img;
		// �½�����
	}

	public static BufferedImage getSystemDegreeImage(File imageFile) throws Exception {
		BufferedImage image = loadImage(imageFile);
		return getSystemDegreeImage(image);
	}

	public static BufferedImage getSystemDegreeImage(BufferedImage image) throws IOException {
		byte[] data = imageToBytes(image);
		return getSystemDegreeImage(data);
	}

	public static BufferedImage zoomImage(BufferedImage im, float scale) {
		/* 原始图像的宽度和高度 */
		int width = im.getWidth();
		int height = im.getHeight();
		/* 调整后的图片的宽度和高度 */
		int toWidth = (int) (width * scale);
		int toHeight = (int) (height * scale);
		return zoomImage(im, toWidth, toHeight);
	}

	public static BufferedImage zoomImage(Image im, int toWidth, int toHeight) {

		BufferedImage result = null;

		try {
			/* 新生成结果图片 */
			result = new BufferedImage(toWidth, toHeight, BufferedImage.TYPE_INT_RGB);
			/** 2、得到画笔对象 */
			Graphics2D g2d = result.createGraphics();
			// ---------- 增加下面的代码使得背景透明 -----------------
			result = g2d.getDeviceConfiguration().createCompatibleImage(toWidth, toHeight, Transparency.TRANSLUCENT);
			g2d.dispose();
			g2d = result.createGraphics();
			// ---------- 背景透明代码结束 -----------------

			// 设置对线段的锯齿状边缘处理
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			g2d.drawImage(im.getScaledInstance(im.getWidth(null), im.getHeight(null), Image.SCALE_SMOOTH),
					0, 0, toWidth, toHeight, null);
			
//			 // 设置水印旋转
//	        if (null != degree) {
//	            //注意rotate函数参数theta，为弧度制，故需用Math.toRadians转换一下
//	            //以矩形区域中央为圆心旋转
//	            g2d.rotate(Math.toRadians(degree), (double) buffImg.getWidth() / 2,
//	                    (double) buffImg.getHeight() / 2);
//	        }
//
//	        // 设置颜色
//	        g2d.setColor(color);
//
//	        // 设置 Font
//	        g2d.setFont(font);
//
//	        //设置透明度:1.0f为透明度 ，值从0-1.0，依次变得不透明
//	        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
//	        //获取真实宽度
//	        float realWidth = getRealFontWidth(text);
//	        float fontSize = font.getSize();
//	        //计算绘图偏移x、y，使得使得水印文字在图片中居中
//	        //这里需要理解x、y坐标是基于Graphics2D.rotate过后的坐标系
//	        float x = 0.5f * width - 0.5f * fontSize * realWidth;
//	        float y = 0.5f * heigth + 0.5f * fontSize;
//	        //取绘制的字串宽度、高度中间点进行偏移，使得文字在图片坐标中居中
//	        g2d.drawString(text, x, y);

			//释放资源
	        g2d.dispose();

		} catch (Exception e) {
			System.out.println("创建缩略图发生异常" + e.getMessage());
		}

		return result;

	}

	public static Icon getIcon(File imageFile, Dimension size){
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();) {
			Thumbnails.of(imageFile).size(size.width, size.height).toOutputStream(outputStream);
			ImageIcon icon = new ImageIcon(outputStream.toByteArray());
			return icon;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static BufferedImage getSystemDegreeImage(byte[] data) throws IOException {
		// create an image input stream from the specified file
		ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(data));

		// get all currently registered readers that recognize the image format
		Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);

		if (!iter.hasNext()) {
			throw new RuntimeException("No readers found!");
		}

		// get the first reader
		ImageReader reader = iter.next();
		reader.setInput(iis, true);
		Orientation ori = ExifUtils.getExifOrientation(reader, 0);

		if (ori != null) {
			switch (ori) {
			case BOTTOM_RIGHT:
				return rotateImg(data, 180.0);
			case RIGHT_TOP:
				return rotateImg(data, 90.0);
			case LEFT_BOTTOM:
				return rotateImg(data, -90.0);
			default:
				return bytesToImage(data);
			}
		} else {
			return bytesToImage(data);
		}
	}
}