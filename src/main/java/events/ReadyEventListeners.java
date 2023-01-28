package events;


import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class ReadyEventListeners implements EventListener {

    //Class to know if bot is running
    @Override
    public void onEvent(GenericEvent event) {
        if(event instanceof ReadyEvent) {
            System.out.println("The Bot is ready and online");
        }
    }
}
