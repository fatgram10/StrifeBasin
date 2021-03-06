package com.rossallenbell.strifebasin.domain;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Map;

import com.rossallenbell.strifebasin.connection.domain.NetworkBuilding;
import com.rossallenbell.strifebasin.connection.domain.NetworkPlayer;
import com.rossallenbell.strifebasin.connection.domain.NetworkUnit;
import com.rossallenbell.strifebasin.connection.gameevents.AttackEvent;
import com.rossallenbell.strifebasin.domain.buildings.Building;
import com.rossallenbell.strifebasin.domain.buildings.buildable.BuildableBuilding;
import com.rossallenbell.strifebasin.domain.buildings.nonbuildable.Sanctuary;
import com.rossallenbell.strifebasin.domain.units.PlayerUnit;
import com.rossallenbell.strifebasin.domain.util.Pathing;
import com.rossallenbell.strifebasin.ui.effects.Effect;
import com.rossallenbell.strifebasin.ui.effects.EffectsFactory;
import com.rossallenbell.strifebasin.ui.effects.EffectsManager;
import com.rossallenbell.strifebasin.ui.resources.AnimationManager;

public class Game {
    
    public static final int BOARD_WIDTH = 250;
    public static final int BOARD_HEIGHT = 125;
    public static final double STARTING_INCOME = 10;
    public static final long INCOME_COOLDOWN = 10000;
    public static final int BUILD_ZONE_WIDTH = (int) (BOARD_WIDTH * 0.15);
    public static final int MIDDLE_PATH_WIDTH = BOARD_HEIGHT / 5;
    
    private final Me me;
    private NetworkPlayer them;
    
    public static final class FogOfWar {
        public static final byte UNEXPLORED = (byte) 0;
        public static final byte EXPLORED = (byte) 1;
        public static final byte VISIBLE = (byte) 2;
    }
    
    private final byte[][] fogOfWar;
    
    private static Game theInstance;
    
    private BuildableBuilding buildingPreview;
    
    public static Game getInstance() {
        if (theInstance == null) {
            synchronized (Game.class) {
                if (theInstance == null) {
                    theInstance = new Game();
                }
            }
        }
        return theInstance;
    }
    
    private Game() {
        me = new Me();
        them = new NetworkPlayer();
        
        Sanctuary mySantuary = new Sanctuary(me);
        mySantuary.setLocation(0, BOARD_HEIGHT / 2 - (mySantuary.getShape().height / 2));
        mySantuary.setAssetId(me.getNextAssetId());
        me.addBuilding(mySantuary);
        
        fogOfWar = new byte[BOARD_WIDTH][BOARD_HEIGHT];
        for(int i=0; i<BUILD_ZONE_WIDTH + PlayerUnit.MINIMUM_AGGRO_RANGE; i++) {
            for(int j=0; j<fogOfWar[0].length; j++) {
                fogOfWar[i][j] = FogOfWar.VISIBLE;
            }
        }
        for(int i=(int) (BUILD_ZONE_WIDTH + PlayerUnit.MINIMUM_AGGRO_RANGE); i<fogOfWar.length; i++) {
            for(int j=0; j<fogOfWar[0].length; j++) {
                fogOfWar[i][j] = FogOfWar.UNEXPLORED;
            }
        }
    }
    
    public Me getMe() {
        return me;
    }
    
    public NetworkPlayer getThem() {
        return them;
    }
    
    public byte[][] getFogOfWar() {
        return fogOfWar;
    }
    
    public void wheelIn() {
        
    }
    
    public void wheelOut() {
        
    }
    
    public void keyPressed(int keyCode) {
        
    }
    
    public void keyReleased(int keyCode) {
        
    }
    
    public void update(long updateTime) {
        Pathing.getInstance().clearQuadTrees();
        me.update(updateTime);
        them.update(updateTime);
    }

    public void clearVisibility() {
        for(int i=BUILD_ZONE_WIDTH; i<fogOfWar.length; i++) {
            for(int j=0; j<fogOfWar[0].length; j++) {
                if(fogOfWar[i][j] == FogOfWar.VISIBLE) {
                    fogOfWar[i][j] = FogOfWar.EXPLORED;
                }
            }
        }
    }

    public void setVisibility(PlayerUnit unit) {
        int originX = (int) Math.round(unit.getLocation().x);
        int originY = (int) Math.round(unit.getLocation().y);
        for(int i=Math.max(0, (int) (originX - unit.getAggroRange())); i < Math.min(fogOfWar.length, originX + unit.getAggroRange()); i++) {
            for(int j=Math.max(0, (int) (originY - unit.getAggroRange())); j < Math.min(fogOfWar[0].length, originY + unit.getAggroRange()); j++) {
                fogOfWar[i][j] = FogOfWar.VISIBLE;
            }
        }
    }
    
    public void buildingPlaced(Point buildLocation) {
        if (buildingPreview != null && isValidBuildLocation(buildLocation)) {
            BuildableBuilding building;
            try {
                building = buildingPreview.getClass().getConstructor(Me.class).newInstance(Game.getInstance().getMe());
                building.setLocation(buildLocation.x, buildLocation.y);
                if (me.getMoney() >= building.cost()) {
                    me.alterMoney(-1 * building.cost());
                    me.addBuilding(building);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public Map<Long, Building> getMyBuildings() {
        return me.getBuildings();
    }
    
    public Map<Long, NetworkBuilding> getTheirBuildings() {
        return them.getBuildings();
    }
    
    public Map<Long, PlayerUnit> getMyUnits() {
        return me.getUnits();
    }
    
    public Map<Long, NetworkUnit> getTheirUnits() {
        return them.getUnits();
    }
    
    public void applyTheirRemoteData(NetworkPlayer networkPlayer) {
        them.applyRemotePlayerData(networkPlayer);
    }
    
    public void attackEvent(AttackEvent attackEvent) {
        PlayerAsset myAsset = me.getAssetById(attackEvent.targetId);
        if (myAsset != null) {
            NetworkUnit attackingUnit = (NetworkUnit) them.getAssetById(attackEvent.unitId);
            if (attackingUnit != null) {
                myAsset.takeDamage(attackingUnit);
                attackingUnit.getFrameHelper().setAction(AnimationManager.Action.ATTACKING);
                
                attackingUnit.getLocation().setLocation(Game.getMirroredLocation(attackingUnit.getLocation()));
                Effect effect = EffectsFactory.getInstance().buildEffect(attackingUnit, myAsset);
                if (effect != null) {
                    EffectsManager.getInstance().addEffect(effect);
                }
            }
        }
    }
    
    public void setBuildingPreview(BuildableBuilding building) {
        buildingPreview = building;
    }
    
    public void clearBuildingPreview() {
        buildingPreview = null;
    }
    
    public BuildableBuilding getBuildingPreview() {
        return buildingPreview;
    }
    
    public boolean isValidBuildLocation(Point gamePoint) {
        if (buildingPreview != null) {
            if (gamePoint.x + buildingPreview.getSize() > Game.BUILD_ZONE_WIDTH) {
                return false;
            }
            
            buildingPreview.setLocation(gamePoint.x, gamePoint.y);
            for (Building otherBuilding : me.getBuildings().values()) {
                if (Pathing.getInstance().buildingsOverlap(buildingPreview, otherBuilding)) {
                    return false;
                }
            }
            
            return true;
        }
        return false;
    }
    
    public static Point2D.Double getMirroredLocation(Point2D.Double location) {
        double mirroredLocationX = BOARD_WIDTH - location.x;
        double mirroredLocationY = location.y;
        return new Point2D.Double(mirroredLocationX, mirroredLocationY);
    }
    
}
