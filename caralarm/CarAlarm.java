package caralarm;

import java.util.ArrayList;



// Current State: Requirement 6 + (7) + Logging, 
// TODO: 7 could be done nicer
// TODO: add wait call if state not changed?
// TODO: PIN code
public class CarAlarm {
    private static boolean PRINT_DEBUG = true; // set to false to get rid of prints

    class Door {
        public int number;
        public boolean isOpen;
        public boolean isLuggage;
        public boolean isLuggageLocked;
        //public boolean isBonnet; //TODO
        public Door(int number, boolean isLuggage) {
            this.number = number;
            this.isLuggage = isLuggage;
            this.isOpen = true; // NOTE: currently each door is open at start
            this.isLuggageLocked = false;
        }
    }

    public enum State {
        OpenAndUnlocked {
            @Override
            public State lock() {
                return OpenAndLocked;
            }

            @Override
            public State close() {
                return ClosedAndUnlocked;
            }
        },     
        
        ClosedAndUnlocked {
            @Override
            public State lock() {
                return ClosedAndLocked;
            }

            @Override
            public State open() {
                return OpenAndUnlocked;
            }
        }, 
        OpenAndLocked {
            @Override
            public State unlock() {
                return OpenAndUnlocked;
            }

            @Override
            public State close() {
                return ClosedAndLocked;
            }
        }, 
        ClosedAndLocked {
            @Override
            public State unlock() {
                return ClosedAndUnlocked;
            }

            @Override
            public State open() {
                return OpenAndLocked;
            }

            @Override
            public State waitSecond() {
                return Armed;
            }

            @Override
            public int getWaitTime() {
                return 20;
            }
        }, 
        Armed {
            @Override
            public State unlock() {
                return ClosedAndUnlocked;
            }

            @Override
            public State open() {
                return Alarm;
            }
        }, 
        Alarm {
            @Override
            public State unlock() {
                return OpenAndUnlocked;
            }

            @Override
            public State waitSecond() {
                return SilentAndFlashing;
            }

            @Override
            public int getWaitTime() {
                return 30;
            }
        }, 
        SilentAndFlashing {
            @Override
            public State unlock() {
                return OpenAndUnlocked;
            }

            @Override
            public State waitSecond() {
                return SilentAndOpen;
            }

            @Override
            public int getWaitTime() {
                return 300;
            }
        }, 
        SilentAndOpen {
            @Override
            public State unlock() {
                return OpenAndUnlocked;
            }

            @Override
            public State close() {
                return Armed;
            }
        };


        public State lock() { return this; }
        public State unlock() { return this; }
        public State close() { return this; }
        public State open() { return this; }
        public State waitSecond() { return this; }
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
        if (isNewState(this.currentState.open())) { // if a single door is open, we consider it open for now
            if (carDoor.isLuggage && !carDoor.isLuggageLocked) {
                waitCounter = 0;
                return;
            }
            log("OPEN", this.currentState.open());
            this.currentState = this.currentState.open();
            waitCounter = 0;
        }
    }

    public void Close(int door) {
        Door carDoor = getDoor(door);
        if (carDoor.isOpen == true) 
            carDoor.isOpen = false;

        debug();
        if (allDoorsClosed()) {
            if (isNewState(this.currentState.close())) {
                log("CLOSE", this.currentState.close());
                this.currentState = this.currentState.close();
                waitCounter = 0;
            }
        }
    }

    public void Lock() {
        debug();
        getDoor(5).isLuggageLocked = true;

        if (isNewState(this.currentState.lock())) {
            log("CLOSE", this.currentState.lock());
            this.currentState = this.currentState.lock();
            waitCounter = 0;
        }
    }

    public void Unlock(int door) {
        debug();
        Door carDoor = getDoor(door);

        if (carDoor.isLuggage) { // do not activate alarm state, just allow access to luggage.
            carDoor.isLuggageLocked = false;
            waitCounter = 0;
            return;
        } else if (isNewState(this.currentState.unlock())) {
            log("CLOSE", this.currentState.unlock());
            this.currentState = this.currentState.unlock();
            waitCounter = 0;
        }
    }

    // waits one second
    public void Wait(int seconds) { 
        debug();
        waitCounter += seconds;
        if (this.currentState.getWaitTime() != 0 && waitCounter >= this.currentState.getWaitTime()) {
            log("CLOSE", this.currentState.waitSecond());
            this.currentState = this.currentState.waitSecond();
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