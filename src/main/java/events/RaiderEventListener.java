package events;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import netscape.javascript.JSObject;


import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class RaiderEventListener extends ListenerAdapter {

    //Main class for providing information about a character on the realm Ysondre from Raider-IO API
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);
        String command = event.getName();
        System.out.println(command);
        if (command.compareTo("raider-io-character") == 0) {

            String name = event.getMember().getEffectiveName();

            try {
                URL url = new URL("https://raider.io/api/v1/characters/profile?region=eu&realm=Ysondre&name="+URLEncoder.encode(name, "UTF-8"));
                System.out.println(url);
                HttpsURLConnection req = (HttpsURLConnection) url.openConnection();
                req.setRequestMethod("GET");

                if (req.getResponseCode() == 200) {

                    InputStream inputStream = req.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                    String input;
                    StringBuffer reponse = new StringBuffer();

                    while ((input = in.readLine()) != null) {
                        reponse.append(input);
                    }
                    in.close();
                    String info = reponse.toString();
                    JsonObject object = new JsonParser().parse(info).getAsJsonObject();
                    //JSON INFO BE LIKE (example)
                    //{"name":"Kazoie"
                    // "race":"Highmountain Tauren"
                    // "class":"Shaman"
                    // "active_spec_name":"Enhancement"
                    // "active_spec_role":"DPS"
                    // "gender":"male"
                    // "faction":"horde"
                    // "achievement_points":9130
                    // "honorable_kills":0
                    // "thumbnail_url":"https://render.worldofwarcraft.com/eu/character/ysondre/118/165128822-avatar.jpg?alt=wow/static/images/2d/avatar/28-0.jpg"
                    // "region":"eu"
                    // "realm":"Ysondre"
                    // "last_crawled_at":"2023-01-20T01:24:19.000Z"
                    // "profile_url":"https://raider.io/characters/eu/ysondre/Kazoie"
                    // "profile_banner":"hordebanner1"}

                    String profile_url = object.get("profile_url").toString();
                    profile_url = profile_url.replace("\"", "");


                    event.reply("Le joueur " + object.get("name") + " est un " + object.get("race") + " de la classe" +
                            object.get("class") + " spé " + object.get("active_spec_name") + " voici un lien direct vers sa page RIO : " + profile_url).queue();


                } else event.reply("Erreur dans la requète, code : " +req.getResponseCode()).queue();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
