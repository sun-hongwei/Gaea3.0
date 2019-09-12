package com.wh.system.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import sun.audio.AudioData;
import sun.audio.ContinuousAudioDataStream;

@SuppressWarnings("restriction")
public abstract class AudioUtils {
	public static String getExts() {
		return "音频=midi;cda;rmf;aif;aiff;gsm;mid;mpa;swf;mp2;mp3;au;wav;ape;flac";
	}

	public abstract static class AudioPlayer {

		static InputStream inputStream;

		public static void reStart() {
			stop();
			if (inputStream != null)
				sun.audio.AudioPlayer.player.start(inputStream);
		}

		public static void start(byte[] data, boolean isLooping)
				throws IOException {
			if (isLooping) {
				inputStream = new ContinuousAudioDataStream(new AudioData(data));
			} else {
				inputStream = new ByteArrayInputStream(data);
			}

			reStart();
		}

		public static void start(String filename, boolean isLooping)
				throws IOException {
			if (isLooping) {
				FileInputStream fileInputStream = new FileInputStream(new File(
						filename));
				byte[] data = new byte[fileInputStream.available()];
				int index = 0;
				while (fileInputStream.available() > 0) {
					int len = fileInputStream.read(data, index,
							fileInputStream.available());
					index += len;
				}
				fileInputStream.close();
				inputStream = new ContinuousAudioDataStream(new AudioData(data));
			} else {
				inputStream = new FileInputStream(new File(filename));
			}

			reStart();
		}

		public static void stop() {
			if (inputStream != null)
				sun.audio.AudioPlayer.player.stop(inputStream);
		}

	}
}