package caralarm;

import java.util.ArrayList;

// Current State: Requirement 3, 
// TODO: finish doors and Pincode
// TODO: add wait call if state not changed?
// TODO: rules for luggage
public class CarAlarm {
    // for requirement 6 + 7
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
                System.out.print("test");
                return OpenAndLocked;
            }

            @Override
            public State getStateAfterUnlock() {
                return OpenAndUnlocked;
            }

            @Override
            public State getStateAfterClose() {
                return ClosedAndUnlocked;
            }

            @Override
            public State getStateAfterOpen() {
                return OpenAndUnlocked;
            }

            @Override
            public State getStateAfterWait() {
                return OpenAndUnlocked;
            }

            @Override
            public int getWaitTime() {
                return 0;
            }
        },     
        
        ClosedAndUnlocked {
            @Override
            public State getStateAfterLock() {
                return ClosedAndLocked;
            }

            @Override
            public State getStateAfterUnlock() {
                return ClosedAndUnlocked;
            }

            @Override
            public State getStateAfterClose() {
                return ClosedAndUnlocked;
            }

            @Override
            public State getStateAfterOpen() {
                return OpenAndUnlocked;
            }

            @Override
            public State getStateAfterWait() {
                return ClosedAndUnlocked;
            }

            @Override
            public int getWaitTime() {
                return 0;
            }
        }, 
        OpenAndLocked {
            @Override
            public State getStateAfterLock() {
                return OpenAndLocked;
            }

            @Override
            public State getStateAfterUnlock() {
                return OpenAndUnlocked;
            }

            @Override
            public State getStateAfterClose() {
                return ClosedAndLocked;
            }

            @Override
            public State getStateAfterOpen() {
                return OpenAndLocked;
            }

            @Override
            public State getStateAfterWait() {
                return OpenAndLocked;
            }

            @Override
            public int getWaitTime() {
                return 0;
            }
        }, 
        ClosedAndLocked {
            @Override
            public State getStateAfterLock() {
                return ClosedAndLocked;
            }

            @Override
            public State getStateAfterUnlock() {
                return ClosedAndUnlocked;
            }

            @Override
            public State getStateAfterClose() {
                return ClosedAndLocked;
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
            public State getStateAfterLock() {
                return Armed;
            }

            @Override
            public State getStateAfterUnlock() {
                return ClosedAndUnlocked;
            }

            @Override
            public State getStateAfterClose() {
                return Armed;
            }

            @Override
            public State getStateAfterOpen() {
                return Alarm;
            }

            @Override
            public State getStateAfterWait() {
                return Armed;
            }

            @Override
            public int getWaitTime() {
                return 0;
            }
        }, 
        Alarm {
            @Override
            public State getStateAfterLock() {
                return Armed;
            }

            @Override
            public State getStateAfterUnlock() {
                return OpenAndUnlocked;
            }

            @Override
            public State getStateAfterClose() {
                return Alarm;
            }

            @Override
            public State getStateAfterOpen() {
                return Alarm;
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
            public State getStateAfterLock() {
                return SilentAndFlashing;
            }

            @Override
            public State getStateAfterUnlock() {
                return OpenAndUnlocked;
            }

            @Override
            public State getStateAfterClose() {
                return SilentAndFlashing;
            }

            @Override
            public State getStateAfterOpen() {
                return SilentAndFlashing;
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
            public State getStateAfterLock() {
                return SilentAndOpen;
            }

            @Override
            public State getStateAfterUnlock() {
                return OpenAndUnlocked;
            }

            @Override
            public State getStateAfterClose() {
                return Armed;
            }

            @Override
            public State getStateAfterOpen() {
                return SilentAndOpen;
            }

            @Override
            public State getStateAfterWait() {
                return SilentAndOpen;
            }

            @Override
            public int getWaitTime() {
                return 0;
            }
        };


        public abstract State getStateAfterLock();
        public abstract State getStateAfterUnlock();
        public abstract State getStateAfterClose();
        public abstract State getStateAfterOpen();
        public abstract State getStateAfterWait();
        public abstract int getWaitTime();
    }

//------------------------------------------------------------------------------------
    private ArrayList<Door> doors;
    private State currentState;
    private int waitCounter;

    private boolean isNewState(State newState) {
        return newState == this.currentState;
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

        if (isNewState(this.currentState.getStateAfterOpen())) { // if a single door is open, we consider it open for now
            this.currentState = this.currentState.getStateAfterOpen();
            waitCounter = 0;
        }
    }

    public void Close(int door) {
        Door carDoor = getDoor(door);
        if (carDoor.isOpen == true) 
            carDoor.isOpen = false;

        if (allDoorsClosed()) {
            if (isNewState(this.currentState.getStateAfterClose())) {
                this.currentState = this.currentState.getStateAfterClose();
                waitCounter = 0;
            }
        }
    }

    public void Lock() {
        if (isNewState(this.currentState.getStateAfterLock())) {
            this.currentState = this.currentState.getStateAfterLock();
            waitCounter = 0;
        }
    }

    public void Unlock(int door) {
        if (isNewState(this.currentState.getStateAfterUnlock())) {
            this.currentState = this.currentState.getStateAfterUnlock();
            waitCounter = 0;
        }
    }

    // waits one second
    public void Wait() { 
        waitCounter++;
        if (this.currentState.getWaitTime() != 0 && waitCounter >= this.currentState.getWaitTime()) {
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