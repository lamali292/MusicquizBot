package de.lamali.musicquizbot.sound;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import de.lamali.musicquizbot.MusicquizBot;
import de.lamali.musicquizbot.games.Song;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class AudioQuizManager {
    public static void playSong(Song song, AudioChannel audio) {
        SoundController controller = MusicquizBot.INSTANCE.playerManager.getController(audio.getGuild().getIdLong());
        AudioPlayerManager apm = MusicquizBot.INSTANCE.audioPlayerManager;
        net.dv8tion.jda.api.managers.AudioManager audioManager = audio.getGuild().getAudioManager();
        controller.getPlayer().setVolume(100);
        audioManager.openAudioConnection(audio);
        String url = song.getLink();
        apm.loadItem(url, new AudioLoadResult(controller, url));
    }

    public static void endSong(AudioChannel audio) {
        AudioManager audioManager = audio.getGuild().getAudioManager();
        audioManager.closeAudioConnection();
    }
}
