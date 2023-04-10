package de.lamali.musicquizbot.games;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

public class ControllerMessage {

    String hash;
    Message mess;
    EmbedBuilder builder;

    public ControllerMessage(TextChannel channel, String hash, SlashCommandInteraction interaction) {
        this.hash = hash;

        builder = new EmbedBuilder();
        builder.setTitle("Song Quiz.");
        builder.setColor(Color.red);
        builder.addField("","",true);
        builder.addField("","",true);
        builder.addField("","",true);
        channel.sendMessageEmbeds(builder.build()).addActionRow(Button.primary(hash+"_join","join"),
                Button.secondary(hash+"_start","start"),
                Button.danger(hash+"_remove","end")).queue(this::setMessage);

    }

    public void setDescription(String desc){
        setMessage(builder.setDescription(desc).build());
    }

    public void updatePlayerStats(PlayerStats leaderboard) {
        StringBuilder userField = new StringBuilder();
        StringBuilder pointField = new StringBuilder();
        StringBuilder songField = new StringBuilder();
        for (User user: leaderboard.points.keySet()) {
            int point = leaderboard.points.get(user);
            int songs = leaderboard.songCount.get(user);
            userField.append(user.getAsMention()).append("\n");
            pointField.append(point).append("\n");
            songField.append(songs).append("\n");
        }
        builder.getFields().set(0,new MessageEmbed.Field("Player",userField.toString(),true));
        builder.getFields().set(1,new MessageEmbed.Field("Points",pointField.toString(),true));
        builder.getFields().set(2,new MessageEmbed.Field("Songs",songField.toString(),true));
        setMessage(builder.build());
    }

    public void setMessage(Message message) {
        this.mess = message;
    }


    public void setActionRow(ItemComponent... components) {
       mess.editMessageEmbeds(builder.build()).setActionRow(components).queue();
    }

    public void setMessage(MessageEmbed message) {
        mess.editMessageEmbeds(message).queue();
    }

    public void setMessageAction(MessageEmbed message, ItemComponent... components) {
        mess.editMessageEmbeds(message).setActionRow(components).queue();
    }

    public EmbedBuilder getBuilder(){
        return builder;
    }

}
