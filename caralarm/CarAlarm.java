package caralarm;

// really basic first implementation, still missing flash and sound plus timings
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

    private boolean isNewState(State newState) {
        return newState == this.currentState;
    }

    public CarAlarm() {
        this.currentState = State.OpenAndUnlocked;
    }

    public void Open() {
        if (isNewState(this.currentState.getStateAfterOpen())) {
            this.currentState = this.currentState.getStateAfterOpen();
            waitCounter = 0;
        }
    }

    public void Close() {
        if (isNewState(this.currentState.getStateAfterClose())) {
            this.currentState = this.currentState.getStateAfterClose();
            waitCounter = 0;
        }
    }

    public void Lock() {
        if (isNewState(this.currentState.getStateAfterLock())) {
            this.currentState = this.currentState.getStateAfterLock();
            waitCounter = 0;
        }
    }

    public void Unlock() {
        if (isNewState(this.currentState.getStateAfterUnlock())) {
            this.currentState = this.currentState.getStateAfterUnlock();
            waitCounter = 0;
        }
    }

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

    private State currentState;
    private int waitCounter;
}