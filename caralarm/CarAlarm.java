package caralarm;

// basic first implementation, still missing flash and sound plus timings
public class CarAlarm {
    public void test() {
        System.out.println("Test");
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

    private boolean isNewState(State newState) {
        return newState != this.currentState;
    }

    public CarAlarm() {
        this.currentState = State.OpenAndUnlocked;
    }

    public void Open() {
        if (isNewState(this.currentState.open())) {
            this.currentState = this.currentState.open();
            waitCounter = 0;
        }
    }

    public void Close() {
        if (isNewState(this.currentState.close())) {
            this.currentState = this.currentState.close();
            waitCounter = 0;
        }
    }

    public void Lock() {
        if (isNewState(this.currentState.lock())) {
            this.currentState = this.currentState.lock();
            waitCounter = 0;
        }
    }

    public void Unlock() {
        if (isNewState(this.currentState.unlock())) {
            this.currentState = this.currentState.unlock();
            waitCounter = 0;
        }
    }

    public void Wait(int seconds) {
        waitCounter += seconds;
        if (this.currentState.getWaitTime() != 0 && waitCounter >= this.currentState.getWaitTime()) {
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

    private State currentState;
    private int waitCounter;
}