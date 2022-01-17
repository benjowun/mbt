package car;

// really basic first implementation, still missing flash and sound plus timings
package caralarm;
public class CarAlarm {
    public void test() {
        System.out.println("Test");
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
                return SilentAndOpen;
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
        };


        public abstract State getStateAfterLock();
        public abstract State getStateAfterUnlock();
        public abstract State getStateAfterClose();
        public abstract State getStateAfterOpen();
        public abstract State getStateAfterWait();
    }

    public CarAlarm() {
        this.currentState = State.OpenAndUnlocked;
    }

    public void Open() {
        this.currentState = this.currentState.getStateAfterOpen();
    }

    public void Close() {
        this.currentState = this.currentState.getStateAfterClose();
    }

    public void Lock() {
        this.currentState = this.currentState.getStateAfterLock();
    }

    public void Unlock() {
        this.currentState = this.currentState.getStateAfterUnlock();
    }

    public void Wait() {
        this.currentState = this.currentState.getStateAfterWait();
    }

    public State getCurrentState() {
        return this.currentState;
    }

    private State currentState;

}