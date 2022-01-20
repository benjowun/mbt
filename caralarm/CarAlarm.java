package caralarm;

import java.util.ArrayList;



// Current State: Requirement 1-3, 6+7, 4, TOPDO 5
// TODO: add wait call if state not changed?
// TODO: messanging system --> show last message?
// TODO: logged states are incorrect in other branches
// TODO: Open should not open door if car is locked and alarm is activated?
public class CarAlarm {
    private static boolean PRINT_DEBUG = true; // set to false to get rid of prints

    public Message lastMessage = Message.None;

    public enum Message {
        None, InvalidPin, NewPinSet, InvalidDoor;
    }

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

    class PIN {
        private int pincode;
        public int errorCount;

        private boolean isValidPIN(int pin) {
            if (pin >= 10000 || pin < 0) {
                return false;
            } else {
                return true;
            }
        }

        public PIN(int pin) { // sets too default value if invalid PIN is entered
            if (!isValidPIN(pin)) {
                this.pincode = 1234;
                lastMessage = Message.InvalidPin;
            } else {
                this.pincode = pin;
            }
            this.errorCount = 0;
        }

        public boolean matchesPIN(int pin) {
            if (!isValidPIN(pin)) {
                lastMessage = Message.InvalidPin;
                this.errorCount++;
                return false;
            } else if (pin == this.pincode) {
                this.errorCount = 0;
                return true;
            } else {
                this.errorCount++;
                return false;
            }
        }

        // car must be unlocked
        public boolean setPIN(int pin) {
            if (!isValidPIN(pin)) {
                lastMessage = Message.InvalidPin;
                this.errorCount++;
                return false;
            } else if (matchesPIN(pin)) {
                this.pincode = pin;
                this.errorCount = 0;
                lastMessage = Message.NewPinSet;
                return true;
            } else {
                return false; // already incremented error in matchesPIN
            }
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
    private PIN pincode;


    private boolean isNewState(State newState) {
        return newState != this.currentState;
    }

    private Door getDoor(int door) {
        for (Door d : this.doors) {
            if (d.number == door)
                return d;
        }
        
        lastMessage = Message.InvalidDoor;
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
            System.out.println("Moving from state: |" + this.currentState + "|  -->  " + action + "  -->  |" + nextState + "|\n");
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
        this.pincode = new PIN(1234); // set to default value, can be set

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
        if (carDoor.isOpen == true && this.currentState != State.Alarm) // closing door that triggered alarm impossible
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
            log("LOCK", this.currentState.lock());
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
            log("UNLOCK", this.currentState.unlock());
            this.currentState = this.currentState.unlock();
            waitCounter = 0;
        }
    }

    // unlocks all doors
    // @param: key - 4 digit pincode, TODO: throw error on invalid code
    public void PinUnlock(int code) {
        debug();
        if (pincode.matchesPIN(code)) {
            if (isNewState(this.currentState.unlock())) {
                log("PINUNLOCK", this.currentState.unlock());
                this.currentState = this.currentState.unlock();
                waitCounter = 0;
            }
        } else if (pincode.errorCount >= 3) { // only reset errors on correct PIN!
            if (currentState == State.Armed) {
                // trigger alarm
                log("PINUNLOCK", State.Alarm);
                this.currentState = State.Alarm;
                waitCounter = 0;
            } // else do nothing
        }
    }

    // sets the pincode to a new value
    // requires being in unlocked state, on error, jump to alarm
    // TODO: this introduces a whole bunch of weird new states we still have to think through
    public void SetPinCode(int codeOld, int codeNew) {
        debug();
        if (this.currentState == State.OpenAndUnlocked) { // assumed Car must be open to set PIN as it is probably through internal display
            pincode.setPIN(codeNew); 

            if (pincode.errorCount >= 3) { // only reset errors on correct PIN!
                // trigger alarm
                log("SETPINCODE", State.Alarm);
                this.currentState = State.Alarm;
                waitCounter = 0;
            }
        } else {
            // Don't throw error here, just doesnt do anything
        }
    }

    // waits one second
    public void Wait(int seconds) { 
        debug();
        waitCounter += seconds;
        if (this.currentState.getWaitTime() != 0 && waitCounter >= this.currentState.getWaitTime()) {
            log("WAIT", this.currentState.waitSecond());
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

    public int getPinErrorCount() {
        return this.pincode.errorCount;
    }

    public int getCurrentPin() {
        return this.pincode.pincode;
    }

    public Message getLastMessage() {
        return this.lastMessage;
    }
}