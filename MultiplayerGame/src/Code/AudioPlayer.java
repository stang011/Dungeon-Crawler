package Code;

import javax.sound.sampled.*;

public class AudioPlayer {
	
	private Clip clip;
	
	public AudioPlayer(String s) {
		
		try {
			
			AudioInputStream asi = AudioSystem.getAudioInputStream(getClass().getResourceAsStream(s));
			
			AudioFormat baseFormat = asi.getFormat();
			AudioFormat decodeFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
			
			AudioInputStream dais = AudioSystem.getAudioInputStream(decodeFormat, asi);
			
			clip = AudioSystem.getClip();
			clip.open(dais);
			
			clip.setFramePosition(clip.getFrameLength());
			
		} catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
	public void play(float volume) {
		if(clip == null) 
			return;
		stop();
		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		gainControl.setValue((20f * (float) Math.log10(volume)));
		clip.setFramePosition(0);
		clip.start();
		
	}
	
	public boolean stillPlaying() {
		if(clip.getFramePosition() < clip.getFrameLength())
			return true;
		else
			return false;
	}
	
	public void stop() {
		if(clip.isRunning()) 
			clip.stop();
	}
	
	public void close() {
		stop();
		clip.close();
	}
}