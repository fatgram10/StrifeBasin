package com.rossallenbell.strifebasin.domain.units;

import com.rossallenbell.strifebasin.domain.Me;
import com.rossallenbell.strifebasin.ui.resources.HasAnimation;

@HasAnimation
public class Knight extends PlayerUnit {
    
    public Knight(Me owner) {
        super(owner);
    }

    @Override
    public int getMaxHealth() {
        return 80;
    }
    
    @Override
    public double getDamage() {
        return 20;
    }
    
    @Override
    public double getSpeed() {
        return 4;
    }
    
}
