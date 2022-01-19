package caralarm;

import java.util.ArrayList;



// Current State: Requirement 6 + Logging, 
// TODO: finish doors + requirement 7
// TODO: add wait call if state not changed?
// TODO: rules for luggage
// TODO: PIN code
public class CarAlarm {
    private static boolean PRINT_DEBUG = true; // set to false to get rid of prints

    class Door {
        public int number;
        public boolean isOpen;
        public boolean isLuggage;
        //public boolean isBonnet; //TODO
        public Door(int number, boolean isLuggage) {
            this.number = number;
            this.isLuggage = isLuggage;
            this.isOpen = true; // NOTE: currently each door is open at start
        }
    }

    public enum State {
        OpenAndUnlocked {
            @Override
            public State getStateAfterLock() {
                return OpenAndLocked;
            }

            @Override
            public State getStateAfterClose() {
                return ClosedAndUnlocked;
            }
        },     
        
        ClosedAndUnlocked {
            @Override
            public State getStateAfterLock() {
                return ClosedAndLocked;
            }

            @Override
            public State getStateAfterOpen() {
                return OpenAndUnlocked;
            }
        }, 
        OpenAndLocked {
            @Override
            public State getStateAfterUnlock() {
                return OpenAndUnlocked;
            }

            @Override
            public State getStateAfterClose() {
                return ClosedAndLocked;
            }
        }, 
        ClosedAndLocked {
            @Override
            public State getStateAfterUnlock() {
                return ClosedAndUnlocked;
            }

            @Override
            public State getStateAfterOpen() {
                return OpenAndLocked;
            }

            @Override
            public State getStateAfterWait() {
                return Armed;
            }

            @Override
            public int getWaitTime() {
                return 20;
            }
        }, 
        Armed {
            @Override
            public State getStateAfterUnlock() {
                return ClosedAndUnlocked;
            }

            @Override
            public State getStateAfterOpen() {
                return Alarm;
            }
        }, 
        Alarm {
            @Override
            public State getStateAfterUnlock() {
                return OpenAndUnlocked;
            }

            @Override
            public State getStateAfterWait() {
                return SilentAndFlashing;
            }

            @Override
            public int getWaitTime() {
                return 30;
            }
        }, 
        SilentAndFlashing {
            @Override
            public State getStateAfterUnlock() {
                return OpenAndUnlocked;
            }

            @Override
            public State getStateAfterWait() {
                return SilentAndOpen;
            }

            @Override
            public int getWaitTime() {
                return 300;
            }
        }, 
        SilentAndOpen {
            @Override
            public State getStateAfterUnlock() {
                return OpenAndUnlocked;
            }

            @Override
            public State getStateAfterClose() {
                return Armed;
            }
        };


        public State getStateAfterLock() { return this; }
        public State getStateAfterUnlock() { return this; }
        public State getStateAfterClose() { return this; }
        public State getStateAfterOpen() { return this; }
        public State getStateAfterWait() { return this; }
        public int getWaitTime() { return 0; }
    }

//------------------------------------------------------------------------------------
    private ArrayList<Door> doors;
    private State currentState;
    private int waitCounter;

    private boolean isNewState(State newState) {
        return newState != this.currentState;
    }

    private Door getDoor(int door) {
        for (Door d : this.doors) {
            if (d.number == door)
                return d;
        }
        
        System.out.println("Error: invalid door."); //TODO: throw error
        return null;
    }

    private boolean allDoorsClosed() {
        boolean closed = true;
        for (Door door : doors) {
            if (door.isOpen)
                closed = false;
        }
        return closed;
    }

    private void log(String action, State nextState) {
        if (PRINT_DEBUG)
            System.out.println("Moving from state: |" + this.currentState + "|  -->  " + action + "  |" + nextState + "|\n");
    }

    private void debug() {
        if (PRINT_DEBUG) {
            System.out.println("Current State: " + this.currentState);
            for (Door d : this.doors) {
                System.out.println("Door: " + d.number + " Open: " + d.isOpen);
            }
            System.out.println("All doors closed: " + allDoorsClosed() + "\n");
        }
    }

    public CarAlarm() {
        this.currentState = State.OpenAndUnlocked;
        this.doors = new ArrayList<>();

        Door door1 = new Door(1, false);
        Door door2 = new Door(2, false);
        Door door3 = new Door(3, false);
        Door door4 = new Door(4, false);
        Door doorLuggage = new Door(5, true);
        // Door doorBonnet = new Door(6, false); // TODO
        doors.add(door1);
        doors.add(door2);
        doors.add(door3);
        doors.add(door4);
        doors.add(doorLuggage);
        //doors.add(door6);
    }

    // TODO: remove
    public void test() {
        System.out.println("Test");
    }

    public void Open(int door) {
        Door carDoor = getDoor(door);
        carDoor.isOpen = true;

        debug();
        if (isNewState(this.currentState.getStateAfterOpen())) { // if a single door is open, we consider it open for now
            log("OPEN", this.currentState.getStateAfterOpen());
            this.currentState = this.currentState.getStateAfterOpen();
            waitCounter = 0;
        }
    }

    public void Close(int door) {
        Door carDoor = getDoor(door);
        if (carDoor.isOpen == true) 
            carDoor.isOpen = false;

        debug();
        if (allDoorsClosed()) {
            if (isNewState(this.currentState.getStateAfterClose())) {
                log("CLOSE", this.currentState.getStateAfterClose());
                this.currentState = this.currentState.getStateAfterClose();
                waitCounter = 0;
            }
        }
    }

    public void Lock() {
        debug();
        if (isNewState(this.currentState.getStateAfterLock())) {
            log("CLOSE", this.currentState.getStateAfterLock());
            this.currentState = this.currentState.getStateAfterLock();
            waitCounter = 0;
        }
    }

    public void Unlock(int door) {
        debug();
        if (isNewState(this.currentState.getStateAfterUnlock())) {
            log("CLOSE", this.currentState.getStateAfterUnlock());
            this.currentState = this.currentState.getStateAfterUnlock();
            waitCounter = 0;
        }
    }

    // waits one second
    public void Wait() { 
        debug();
        waitCounter++;
        if (this.currentState.getWaitTime() != 0 && waitCounter >= this.currentState.getWaitTime()) {
            log("CLOSE", this.currentState.getStateAfterWait());
            this.currentState = this.currentState.getStateAfterWait();
            waitCounter = 0;
        }
    }

    public State getCurrentState() {
        return this.currentState;
    }

    public int getWaitCounter() {
        return this.waitCounter;
    }
}