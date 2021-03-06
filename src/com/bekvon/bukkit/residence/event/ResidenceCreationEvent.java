/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class ResidenceCreationEvent extends CancellableResidencePlayerEvent {

    protected String resname;
    CuboidArea area;

    public ResidenceCreationEvent(Player player, String newname, ClaimedResidence resref, CuboidArea resarea)
    {
        super("RESIDENCE_CREATE",resref,player);
        resname = newname;
        area = resarea;
    }

    public String getResidenceName()
    {
        return resname;
    }

    public void setResidenceName(String name)
    {
        resname = name;
    }

    public CuboidArea getPhysicalArea()
    {
        return area;
    }

    public void setPhysicalArea(CuboidArea newarea)
    {
        area = newarea;
    }
}
