import javax.sound.sampled.AudioInputStream; // for playing sound clips
import javax.sound.sampled.*;
import java.io.*;

import java.util.HashMap; // for storing sound clips

public class SoundManager { // a Singleton class
	HashMap<String, Clip> clips;
	
	private static SoundManager instance = null; // keeps track of Singleton instance

	private SoundManager() {
		clips = new HashMap<String, Clip>();

		Clip clip = loadClip("sounds/bgm.wav");
		clips.put("background", clip);

		clip = loadClip("sounds/attack.wav");
		clips.put("attack", clip);

		clip = loadClip("sounds/coin.wav");
		clips.put("coin", clip);

		clip = loadClip("sounds/enemy_hurt.wav");
		clips.put("enemy_hurt", clip);

		clip = loadClip("sounds/player_hurt.wav");
		clips.put("player_hurt", clip);

		clip = loadClip("sounds/powerup.wav");
		clips.put("powerup", clip);
	}

	public static SoundManager getInstance() { // class method to get Singleton instance
		if (instance == null)
			instance = new SoundManager();

		return instance;
	}

	public Clip getClip(String title) {

		return clips.get(title); // gets a sound by supplying key
	}

	public Clip loadClip(String fileName) { // gets clip from the specified file
		AudioInputStream audioIn;
		Clip clip = null;

		try {
			File file = new File(fileName);
			audioIn = AudioSystem.getAudioInputStream(file.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
		} catch (Exception e) {
			System.out.println("Error opening sound files: " + e);
		}
		return clip;
	}

	public void playSound(String title, Boolean looping) {
		Clip clip = getClip(title);
		if (clip != null) {
			clip.setFramePosition(0);
			if (looping)
				clip.loop(Clip.LOOP_CONTINUOUSLY);
			else
				clip.start();
		}
	}

	public void stopSound(String title) {
		Clip clip = getClip(title);
		if (clip != null) {
			clip.stop();
		}
	}

}