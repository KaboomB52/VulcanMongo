package dev.ianrich.vulcanmongo.listener;

import dev.ianrich.vulcanmongo.VulcanMongo;
import dev.ianrich.vulcanmongo.log.LogManager;
import dev.ianrich.vulcanmongo.log.construct.Log;
import me.frep.vulcan.api.VulcanAPI;
import me.frep.vulcan.api.event.VulcanFlagEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VulcanListener implements Listener {

    @EventHandler
    public void onFlag(VulcanFlagEvent event){
        LogManager.addLog(event.getPlayer().getUniqueId(),
                new Log(
                        event.getPlayer().getUniqueId(), event.getPlayer().getName(),
                        System.currentTimeMillis(),
                        VulcanMongo.instance.getConfig().getString("server-name"),
                        event.getInfo(),
                        event.getCheck().getDisplayName(),
                        String.valueOf(event.getCheck().getType()).toUpperCase(),
                        event.getCheck().getVl() + 1, VulcanAPI.Factory.getApi().getClientVersion(event.getPlayer()),
                        VulcanAPI.Factory.getApi().getPing(event.getPlayer()),
                        VulcanAPI.Factory.getApi().getTps()));
    }
}
