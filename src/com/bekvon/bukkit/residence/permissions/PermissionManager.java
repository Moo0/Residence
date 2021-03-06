/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.permissions;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author Administrator
 */
public class PermissionManager {
    protected static PermissionsInterface perms;
    protected Map<String,PermissionGroup> groups;
    protected Map<String,String> playersGroup;
    protected FlagPermissions globalFlagPerms;

    public PermissionManager(Configuration config)
    {
        try
        {
            groups = Collections.synchronizedMap(new HashMap<String,PermissionGroup>());
            playersGroup = Collections.synchronizedMap(new HashMap<String,String>());
            globalFlagPerms = new FlagPermissions();
            this.readConfig(config);
            boolean enable = config.getBoolean("Global.EnablePermissions", true);
            if(enable)
                this.checkPermissions();
        }
        catch(Exception ex)
        {
            Logger.getLogger(PermissionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public PermissionGroup getGroup(Player player)
    {
        return groups.get(this.getGroupNameByPlayer(player));
    }

    public PermissionGroup getGroup(String player, String world)
    {
        return groups.get(this.getGroupNameByPlayer(player, world));
    }

    public PermissionGroup getGroupByName(String group)
    {
        group = group.toLowerCase();
        if(!groups.containsKey(group))
            return groups.get(Residence.getConfig().getDefaultGroup());
        return groups.get(group);
    }

    public String getGroupNameByPlayer(Player player)
    {
        return this.getGroupNameByPlayer(player.getName(), player.getWorld().getName());
    }

    public String getGroupNameByPlayer(String player, String world) {
        String defaultGroup = Residence.getConfig().getDefaultGroup().toLowerCase();
        if (playersGroup.containsKey(player)) {
            String group = playersGroup.get(player);
            if (group != null) {
                group = group.toLowerCase();
                if (group != null && groups.containsKey(group)) {
                    return group;
                }
            }
        }
        String group = this.getPermissionsGroup(player,world);
        if (group == null || !groups.containsKey(group)) {
            return defaultGroup;
        } else {
            return group;
        }
    }

    public String getPermissionsGroup(Player player)
    {
        return this.getPermissionsGroup(player.getName(), player.getWorld().getName());
    }

    public String getPermissionsGroup(String player, String world)
    {
        if(perms == null)
            return Residence.getConfig().getDefaultGroup();
        return perms.getPlayerGroup(player, world);
    }

    public boolean hasAuthority(Player player, String permission, boolean def) {
        if(perms==null)
            return def;
        return perms.hasPermission(player, permission);
    }

    public boolean isResidenceAdmin(Player player)
    {
        return (this.hasAuthority(player, "residence.admin", false) || (player.isOp() && Residence.getConfig().getOpsAreAdmins()));
    }

    private void checkPermissions() {
        Server server = Residence.getServ();
        Plugin p = server.getPluginManager().getPlugin("bPermissions");
        if (p != null) {
            perms = new BPermissionsAdapter((de.bananaco.permissions.Permissions)p);
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Found bPermissions Plugin!");
            return;
        }
       p = server.getPluginManager().getPlugin("PermissionsBukkit");
       if(p!=null)
       {
           perms = new PermissionsBukkitAdapter((PermissionsPlugin) p);
           Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Found PermissionsBukkit Plugin!");
           return;
       }
       p = server.getPluginManager().getPlugin("Permissions");
       if(p!=null)
       {
           if(Residence.getConfig().useLegacyPermissions())
           {
                perms = new LegacyPermissions(((Permissions) p).getHandler());
                Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Found Permissions Plugin!");
                Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Permissions running in Legacy mode!");
           }
           else
           {
               perms = new OrigionalPermissions(((Permissions) p).getHandler());
               Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Found Permissions Plugin!");
           }
           return;
       }
       Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Permissions plugin NOT FOUND!");
    }

    private void readConfig(Configuration config)
    {
        String defaultGroup = Residence.getConfig().getDefaultGroup();
        globalFlagPerms = FlagPermissions.parseFromConfigNode("FlagPermission", config.getNode("Global"));
        Map<String, ConfigurationNode> nodes = config.getNodes("Groups");
        if(nodes!=null)
        {
            Set<Entry<String, ConfigurationNode>> entrys = nodes.entrySet();
            for(Entry<String, ConfigurationNode> entry : entrys)
            {
                String key = entry.getKey().toLowerCase();
                try
                {
                    groups.put(key, new PermissionGroup(key,entry.getValue(),globalFlagPerms));
                }
                catch(Exception ex)
                {
                    System.out.println("[Residence] Error parsing group from config:" + key + " Exception:" + ex);
                }
            }
        }
        if(!groups.containsKey(defaultGroup))
        {
            groups.put(defaultGroup, new PermissionGroup(defaultGroup));
        }
        List<String> keys = config.getKeys("GroupAssignments");
        if(keys!=null)
        {
            for(String key : keys)
            {
                playersGroup.put(key, config.getString("GroupAssignments."+key, defaultGroup).toLowerCase());
            }
        }
    }

    public boolean hasGroup(String group)
    {
        group = group.toLowerCase();
        return groups.containsKey(group); 
    }
}
