package com.rossallenbell.strifebasin.ui.menus;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.reflections.Reflections;

import com.rossallenbell.strifebasin.domain.Game;
import com.rossallenbell.strifebasin.domain.Me;
import com.rossallenbell.strifebasin.domain.buildings.buildable.AdvancedBuilding;
import com.rossallenbell.strifebasin.domain.buildings.buildable.BasicBuilding;
import com.rossallenbell.strifebasin.domain.buildings.buildable.BuildableBuilding;

public class BuildMenu extends Menu {
    
    private static enum State {
        CLOSED, TYPE, BASIC, ADVANCED
    };
    
    private List<BuildableBuilding> basicBuildings;
    private List<BuildableBuilding> advancedBuildings;
    
    private State state;
    
    private static BuildMenu theInstance;
    
    public static BuildMenu getInstance() {
        if (theInstance == null) {
            synchronized (BuildMenu.class) {
                if (theInstance == null) {
                    theInstance = new BuildMenu();
                }
            }
        }
        return theInstance;
    }
    
    private BuildMenu() {
        state = State.CLOSED;
        
        basicBuildings = new ArrayList<>();
        advancedBuildings = new ArrayList<>();
        
        Reflections reflections = new Reflections("com.rossallenbell.strifebasin.domain.buildings.buildable");
        try {
            for (Class<?> clazz : reflections.getTypesAnnotatedWith(BasicBuilding.class)) {
                basicBuildings.add((BuildableBuilding) clazz.getConstructor(Me.class).newInstance(Game.getInstance().getMe()));
            }
            for (Class<?> clazz : reflections.getTypesAnnotatedWith(AdvancedBuilding.class)) {
                advancedBuildings.add((BuildableBuilding) clazz.getConstructor(Me.class).newInstance(Game.getInstance().getMe()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        Collections.sort(basicBuildings, new CostComparator());
        Collections.sort(advancedBuildings, new CostComparator());
    }
    
    @Override
    public List<List<String>> getDisplayStrings() {
        List<List<String>> displayStrings = new ArrayList<List<String>>();
        
        List<String> base = new ArrayList<String>();
        base.add("(B) Build");
        displayStrings.add(base);
        
        if (state != State.CLOSED) {
            List<String> type = new ArrayList<String>();
            type.add("(1) Basic");
            type.add("(2) Advanced");
            displayStrings.add(type);
        }
        
        if (state == State.BASIC) {
            List<String> basicBuildingNames = new ArrayList<String>();
            int num = 1;
            for (BuildableBuilding building : basicBuildings) {
                basicBuildingNames.add(String.format("[%d] - %d - %s", num, building.cost(), building.getClass().getSimpleName()));
                num++;
            }
            displayStrings.add(basicBuildingNames);
        } else if (state == State.ADVANCED) {
            List<String> advancedBuildingNames = new ArrayList<String>();
            int num = 1;
            for (BuildableBuilding building : advancedBuildings) {
                advancedBuildingNames.add(String.format("[%d] - %d - %s", num, building.cost(), building.getClass().getSimpleName()));
                num++;
            }
            displayStrings.add(advancedBuildingNames);
        }
        
        return displayStrings;
    }
    
    @Override
    public void keyPressed(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_ESCAPE:
                state = State.CLOSED;
                Game.getInstance().clearBuildingPreview();
                break;
            case KeyEvent.VK_B:
                state = State.TYPE;
                Game.getInstance().clearBuildingPreview();
                break;
            case KeyEvent.VK_1:
                if (state == State.TYPE) {
                    state = State.BASIC;
                    break;
                }
            case KeyEvent.VK_2:
                if (state == State.TYPE) {
                    state = State.ADVANCED;
                    break;
                }
            case KeyEvent.VK_3:
            case KeyEvent.VK_4:
            case KeyEvent.VK_5:
            case KeyEvent.VK_6:
            case KeyEvent.VK_7:
            case KeyEvent.VK_8:
            case KeyEvent.VK_9:
            case KeyEvent.VK_0:
                if (state == State.BASIC) {
                    Game.getInstance().setBuildingPreview(basicBuildings.get(keyCode - 49));
                }
                if (state == State.ADVANCED) {
                    Game.getInstance().setBuildingPreview(advancedBuildings.get(keyCode - 49));
                }
                break;
        }
    }
    
    public class CostComparator implements Comparator<BuildableBuilding> {
        @Override
        public int compare(BuildableBuilding o1, BuildableBuilding o2) {
            return Integer.compare(o1.cost(), o2.cost());
        }
    }
    
}
