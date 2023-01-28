package events;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class InteractionEventListener extends ListenerAdapter {

    //This is a test Class made before
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);
        User name = event.getUser();

        if (name.getName().equals("YourName")){
            event.reply("Bonjour a toi mon maitre & créateur :smile:").queue();
        }
        event.reply("Me parle pas sale gueux").queue();



    }
}
