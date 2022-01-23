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

        public boolean isCarDoor() {
            return number <= 4;
        }

        public Door(int number) {
            this.number = number;
            this.isOpen = true; // NOTE: currently each door is open at start
        }

        public boolean open() {
            this.isOpen = true;
            return true;
        }

        public void close() {
            this.isOpen = false;
        }
    }

    class LuggageDoor extends Door {
        public boolean isLocked;

        public LuggageDoor(int number) {
            super(number);
            isLocked = false;
        }

        public boolean open() {
            if (isOpen)
                return true;;
            if (!isLocked) {
                isOpen = true;
                return true;
            } else {
                System.out.println("Tried to open luggage door, but it is locked");
                return false;
            }
        }

        public void lock() {
            isLocked = true;
        }

        // logic for pin is in unlockLuggage
        public void unlock() {
            isLocked = false;
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
        public boolean setPIN(int old_pin, int new_pin) {
            if (!isValidPIN(new_pin)) {
                lastMessage = Message.InvalidPin;
                this.errorCount++;
                return false;
            } else if (matchesPIN(old_pin)) {
                this.pincode = new_pin;
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
                return 270;
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

        public State lock() {
            return this;
        }

        public State unlock() {
            return this;
        }

        public State close() {
            return this;
        }

        public State open() {
            return this;
        }

        public State waitSecond() {
            return this;
        }

        public int getWaitTime() {
            return 0;
        }
    }

    //------------------------------------------------------------------------------------
    private ArrayList<Door> doors;
    private State currentState;
    private int waitCounter;
    private PIN pincode;
    public int DEFAULT_PINCODE = 1234;


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

    public void unlockLuggage(int pin) {
        debug();
        LuggageDoor luggageDoor = (LuggageDoor) getDoor(5);
        if (pincode.matchesPIN(pin)) {
            System.out.println("Unlocking luggage door, pincode matches");
            luggageDoor.unlock();
        } else {
            System.out.println("Tried to unlock luggage door with wrong pin code " + pin);
            if (currentState == State.Armed) {
                // trigger alarm
                log("UNLOCKLUGGAGE", State.Alarm);
                this.currentState = State.Alarm;
                waitCounter = 0;
            }
        }
    }

    private boolean allDoorsClosed() {
        boolean closed = true;
        for (Door door : doors) {
            if (door.isOpen && door.isCarDoor())
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
        this.pincode = new PIN(DEFAULT_PINCODE); // set to default value, can be set

        doors.add(new Door(1));
        doors.add(new Door(2));
        doors.add(new Door(3));
        doors.add(new Door(4));
        doors.add(new LuggageDoor(5));
        doors.add(new Door(6));
    }

    public void Open(int door_num) {
        System.out.println("Opening door " + door_num);
        Door door = getDoor(door_num);
        assert door != null;
        boolean noAlarm = door.open();

        debug();
        if ((door.isCarDoor() && isNewState(this.currentState.open())) || (!noAlarm && this.currentState == State.Armed)) { // if a single door is open, we consider it open for now
            log("OPEN", this.currentState.open());
            this.currentState = this.currentState.open();
            waitCounter = 0;
        }
    }

    public void Close(int door_num) {
        Door door = getDoor(door_num);
        assert door != null;
        if (this.currentState != State.Alarm) // closing door that triggered alarm impossible
        {
            door.close();
        }
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
        LuggageDoor luggageDoor = (LuggageDoor) getDoor(5);
        luggageDoor.lock();

        if (isNewState(this.currentState.lock())) {
            log("LOCK", this.currentState.lock());
            this.currentState = this.currentState.lock();
            waitCounter = 0;
        }
    }

    public void Unlock() {
        debug();
        LuggageDoor luggageDoor = (LuggageDoor) getDoor(5);
        luggageDoor.unlock();
        if (isNewState(this.currentState.unlock())) {
            log("UNLOCK", this.currentState.unlock());
            this.currentState = this.currentState.unlock();
            waitCounter = 0;
        }
    }

    // unlocks all doors
    // @param: key - 4 digit pincode, TODO: throw error on invalid code
    public void PinUnlock(int code) {
        System.out.println("Called pinunlock with pin " + code + ", correct is " + getCurrentPin());
        debug();
        if (pincode.matchesPIN(code)) {
            if (isNewState(this.currentState.unlock())) {
                log("PINUNLOCK", this.currentState.unlock());
                this.currentState = this.currentState.unlock();
                waitCounter = 0;
            } else {
                System.out.println("Right pin, but did not change state");
            }
        } else if (pincode.errorCount >= 3) { // only reset errors on correct PIN!
            if (currentState == State.Armed) {
                // trigger alarm
                log("PINUNLOCK", State.Alarm);
                this.currentState = State.Alarm;
                waitCounter = 0;
            } else {
                System.out.println("Wrong pin for 3rd time, but not in armed");
            }
        } else {
            System.out.println("Wrong pin, but not for 3rd time");
        }
    }

    // sets the pincode to a new value
    // requires being in unlocked state, on error, jump to alarm
    // TODO: this introduces a whole bunch of weird new states we still have to think through
    public void SetPinCode(int codeOld, int codeNew) {
        debug();
        if (this.currentState == State.OpenAndUnlocked || this.currentState == State.OpenAndLocked) { // assumed Car must be open to set PIN as it is probably through internal display
            pincode.setPIN(codeOld, codeNew);

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
            int leftover_wait_time = waitCounter - this.currentState.getWaitTime();
            System.out.println(String.format("Waiting for %d seconds, which is %d more than the wait time " +
                            "of the current state. After this, Wait will be called again with the leftover %d seconds.",
                    seconds, this.currentState.getWaitTime(), leftover_wait_time));
            this.currentState = this.currentState.waitSecond();
            waitCounter = 0;
            Wait(leftover_wait_time);
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