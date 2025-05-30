package de.dopemc.hardcore.ChatAPI.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Listener_PlayerChat implements Listener{

	@EventHandler
	public void onPlayerChatEvent(@SuppressWarnings("deprecation") AsyncPlayerChatEvent e) {
		
		Player p = (Player) e.getPlayer();
        PermissionUser user = PermissionsEx.getUser(p);
        String group = getPrimaryGroup(user);
        
        // Definiere Präfix je Gruppe
        String prefix = getGroupPrefix(group);
        String coloredName = ChatColor.translateAlternateColorCodes('&', prefix + p.getName());

        // Nachricht in Weiß
        String coloredMessage = ChatColor.translateAlternateColorCodes('&', "&f" + e.getMessage());
        

        // Setze Format: <Name>: <Nachricht>
        e.setFormat(coloredName + ChatColor.RESET + " §7» " + coloredMessage);
	}
    private String getPrimaryGroup(PermissionUser user) {
    	
        if (user == null) {
        	
            return "default";
            
        }
        PermissionGroup[] groups = user.getGroups();
        
        if (groups.length == 0) {
        	
            return "default";
            
        }
        
        return groups[0].getName().toLowerCase();
        
    }
	
	
    private String getGroupPrefix(String group) {
        switch (group) {
            case "admin":
            	
                return "&cAdmin &7» &f";
                
            case "Developer":
            	
                return "&bDeveloper &7» &f";
                
            case "Freund":
            	
                return "&3Freund &7» &f";
                
            case "support":
            	
                return "&eSupporter &7» &f";
                
            case "Builder":
            	
                return "&2Builder &7» &f";
                
            case "Creator":
            	
                return "&dCreator &7» &f";
                
            case "VIP":
            	
                return "&5VIP &7» &f";
                
            case "Doper":
            	
                return "&7Doper &7» &f";
                
            default:
            	
                return "&7Doper &7» &f";
                
        }
    }
	
}	


