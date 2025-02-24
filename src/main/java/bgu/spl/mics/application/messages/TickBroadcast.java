package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    
    private final int currentTick;

    public TickBroadcast(int tick) {
        this.currentTick = tick;
    }

    public int getTick() {
        return currentTick;
    }
}