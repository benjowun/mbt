package model

import caralarm.CarAlarm
import caralarm.CarAlarm.State._
import modbat.dsl._

class CarAlarmModel extends Model {
  // precondition with require (condition)
  // assertions with assert(assertion)
  // choose(actions: AnyFunc*): Takes a list of actions, randomly chooses one action, and executes it.
  // maybe (doSomething)
  // TODO: LockLuggage() test?

  val sut = new CarAlarm() // SUT

  val num_doors = 4
  val luggage_door_number = 5
  val closed = 0
  val open = 1
  // internal tracking of door state
  val doors: Array[Int] = Array.fill(num_doors)(open)
  var time_in_closedandlocked = 0
  var time_in_alarm = 0
  var pin: Int = sut.DEFAULT_PINCODE
  var wrong_pin_count = 0
  var luggage_locked = 0

  def unlockCorrectPin(): Unit = {
    sut.PinUnlock(pin)
    wrong_pin_count = 0
  }

  def unlockWrongPin(): Unit = {
    val wrong_pin = -1
    require(wrong_pin != pin)
    sut.PinUnlock(wrong_pin)
    wrong_pin_count += 1
  }

  def changePinCorrect(): Unit = {
    val new_pin = choose(0, 10000)
    sut.SetPinCode(pin, new_pin)
    pin = new_pin
    wrong_pin_count = 0
    assert(sut.getCurrentPin == new_pin)
  }

  def changePinWrong(): Unit = {
    val new_pin = choose(-10000, 0)
    sut.SetPinCode(pin, new_pin)
    wrong_pin_count += 1
  }

  // transitions

  // Do something with a random door (ignoring special ones), with closing the door being more likely.
  // Only closes the door if the car would still be open after the action.
  "OpenAndUnlocked" -> "OpenAndUnlocked" := {
    assert(sut.getCurrentState == OpenAndUnlocked)
    val door_index = choose(0, num_doors)
    val door_number = door_index + 1
    // 1 in 4 chance to open the door, 3 in 4 in chance to close it
    val new_door_state = if (choose(0, 5) == 0) open else closed
    if (new_door_state == open) {
      sut.Open(door_number)
      doors(door_index) = new_door_state
    }
    else {
      // make sure that at least one other door would still be open, otherwise we would be in ClosedAndUnlocked
      var car_still_open = false
      for (i <- 0 until num_doors) {
        if (doors(i) == open && i != door_index) {
          car_still_open = true
        }
      }
      // backtracks if car would be closed now
      require(car_still_open)
      sut.Close(door_number)
      doors(door_index) = new_door_state
    }
    assert(sut.getCurrentState == OpenAndUnlocked)
  } label "Open or close random door in OpenAndUnlocked"

  // If there is exactly one open door, close it and enter ClosedAndUnlocked state.
  "OpenAndUnlocked" -> "ClosedAndUnlocked" := {
    assert(sut.getCurrentState == OpenAndUnlocked)
    var num_open_doors = num_doors
    var last_open_door_index = -1
    for (i <- 0 until num_doors) {
      if (doors(i) == closed) num_open_doors -= 1
      else last_open_door_index = i
    }
    // sanity check
    if (num_open_doors == 0) {
      throw new Exception("ERROR: All doors were closed in the model, but we were in the OpenAndUnlocked model state.")
    }
    // if exactly one door is open, close it - this should make it more likely to enter the closed state
    require(num_open_doors == 1)
    sut.Close(last_open_door_index + 1)
    doors(last_open_door_index) = closed
    assert(sut.getCurrentState == ClosedAndUnlocked)
  } label "Close random door in OpenAndUnlocked"

  // Simple transition to the OpenAndLocked state, no special behavior
  "OpenAndUnlocked" -> "OpenAndLocked" := {
    assert(sut.getCurrentState == OpenAndUnlocked)
    sut.Lock()
    luggage_locked = 1
    assert(sut.getCurrentState == OpenAndLocked)
  } label "Lock in OpenAndUnlocked"

  // Opening any door (appart frorm luggage) advances from ClosedAndUnlocked to OpenAndUnlocked
  "ClosedAndUnlocked" -> "OpenAndUnlocked" := {
    assert(sut.getCurrentState == ClosedAndUnlocked)
    val door_index = choose(0, num_doors)
    val door_number = door_index + 1
    sut.Open(door_number)
    doors(door_index) = open
    assert(sut.getCurrentState == OpenAndUnlocked)
  } label "Open random door in ClosedAndUnlocked"

  // Same as OpenAndUnlocked -> ClosedAndUnlocked, also reset the arming timer
  "OpenAndLocked" -> "ClosedAndLocked" := {
    assert(sut.getCurrentState == OpenAndLocked)
    var num_open_doors = num_doors
    var last_open_door_index = -1
    for (i <- 0 until num_doors) {
      if (doors(i) == closed) num_open_doors -= 1
      else last_open_door_index = i
    }
    // sanity check
    if (num_open_doors == 0) {
      throw new Exception("ERROR: All doors were closed in the model, but we were in the OpenAndLocked model state.")
    }
    // if exactly one door is open, close it - this should make it more likely to enter the closed state
    require(num_open_doors == 1)
    sut.Close(last_open_door_index + 1)
    doors(last_open_door_index) = closed
    time_in_closedandlocked = 0
    assert(sut.getCurrentState == ClosedAndLocked)
  } label "Close random door in OpenAndLocked"

  // When entering ClosedAndLocked, reset the arming timer
  "ClosedAndUnlocked" -> "ClosedAndLocked" := {
    assert(sut.getCurrentState == ClosedAndUnlocked)
    sut.Lock()
    luggage_locked = 1
    time_in_closedandlocked = 0
    assert(sut.getCurrentState == ClosedAndLocked)
  } label "Lock while closed"

  "ClosedAndLocked" -> "ClosedAndLocked" := {
    assert(sut.getCurrentState == ClosedAndLocked)
    val wait_time = choose(0, 20)
    // if waiting would increase time to 20 seconds or more, model would advance to armed state
    require(wait_time + time_in_closedandlocked < 20)
    time_in_closedandlocked += wait_time
    sut.Wait(wait_time)

    assert(sut.getCurrentState == ClosedAndLocked)
    assert(sut.getWaitCounter == time_in_closedandlocked)
  } label "Wait in ClosedAndLocked, staying there"

  "ClosedAndLocked" -> "Armed" := {
    assert(sut.getCurrentState == ClosedAndLocked)
    val wait_time = choose(0, 20)
    // only advance to armed state if total waiting time would surpass 20 seconds
    require(wait_time + time_in_closedandlocked >= 20)
    sut.Wait(wait_time)
    assert(sut.getCurrentState == Armed)
  } label "Wait in ClosedAndLocked until Armed"

  // Opening any door advances to OpenAndLocked, same as ClosedAndUnlocked -> OpenAndUnlocked
  "ClosedAndLocked" -> "OpenAndLocked" := {
    assert(sut.getCurrentState == ClosedAndLocked)
    val door_index = choose(0, num_doors)
    val door_number = door_index + 1
    sut.Open(door_number)
    doors(door_index) = open
    assert(sut.getCurrentState == OpenAndLocked)
  } label "Open random door while Closed and locked"

  // Unlocking requires a Pin code
  "ClosedAndLocked" -> "ClosedAndUnlocked" := {
    assert(sut.getCurrentState == ClosedAndLocked)
    unlockCorrectPin()
    assert(sut.getCurrentState == ClosedAndUnlocked)
    assert(sut.getPinErrorCount == wrong_pin_count)
  } label "Unlock (correct pin) while closed and locked"

  // Same as unlocking in ClosedAndLocked
  "Armed" -> "ClosedAndUnlocked" := {
    assert(sut.getCurrentState == Armed)
    unlockCorrectPin()
    assert(sut.getCurrentState == ClosedAndUnlocked)
    assert(sut.getPinErrorCount == wrong_pin_count)
  } label "Unlock (correct pin) while Armed"

  // Same as unlocking in ClosedAndLocked
  "OpenAndLocked" -> "OpenAndUnlocked" := {
    assert(sut.getCurrentState == OpenAndLocked)
    unlockCorrectPin()
    assert(sut.getCurrentState == OpenAndUnlocked)
    assert(sut.getPinErrorCount == wrong_pin_count)
  } label "Unlock (correct pin) while open and locked"

  // can always unlock luggage seperatly, should not affect state
  @States(Array("ClosedAndLocked", "Armed", "OpenAndLocked", "Alarm", "Flash", "SilentAndOpen"))
  def UnlockLuggage(): Unit = {
    val old_state = sut.getCurrentState
    luggage_locked = 0
    sut.unlockLuggage(pin)
    assert(sut.getCurrentState == old_state)
  } weight 2

  // can always open an unlocked luggage, or close a luggage. Should not affect state
  @States(Array("OpenAndUnlocked", "ClosedAndUnlocked", "ClosedAndLocked", "Armed", "OpenAndLocked", "Alarm", "Flash", "SilentAndOpen"))
  def OpenUnlockedLuggage(): Unit = {
    val old_state = sut.getCurrentState
    val new_door_state = if (choose(0, 5) == 0) open else closed

    if (new_door_state == open) {
      require(luggage_locked == 0)
      sut.Open(luggage_door_number)
      doors(luggage_door_number-1) = new_door_state
    } else {
      sut.Close(luggage_door_number)
      doors(luggage_door_number-1) = new_door_state
    }
    assert(sut.getCurrentState == old_state)
  } weight 2

  // Unlocking with wrong pin in ClosedAndLocked and OpenAndLocked should do nothing if
  // it is the first or second time
  // Should also be the same for SilentAndOpen
  @States(Array("ClosedAndLocked", "OpenAndLocked", "SilentAndOpen"))
  def doWrongUnlock(): Unit = {
    val old_state = sut.getCurrentState
    require(wrong_pin_count < 2)
    unlockWrongPin()
    assert(sut.getCurrentState == old_state)
    assert(sut.getPinErrorCount == wrong_pin_count)
  } weight 5

  // Since we can enter the Alarm state from doing an incorrect pin change, and in the alarm state
  // the doors are implicitly open, it is safe to assume that we can only change the pin in the
  // OpenAndUnlocked and OpenAndLocked states
  @States(Array("OpenAndUnlocked", "OpenAndLocked"))
  def doCorrectPinChange(): Unit = {
    val old_state = sut.getCurrentState
    changePinCorrect()
    assert(sut.getCurrentState == old_state)
    assert(sut.getPinErrorCount == wrong_pin_count)
  }

  // Changing pin with the wrong old pin also should do nothing if it is the first or second time
  @States(Array("OpenAndUnlocked", "OpenAndLocked"))
  def doWrongPinChange(): Unit = {
    require(wrong_pin_count < 2)
    val old_state = sut.getCurrentState
    changePinWrong()
    assert(sut.getCurrentState == old_state)
    assert(sut.getPinErrorCount == wrong_pin_count)
  }

  // Changing pin with the wrong old pin for the third time should trigger an alarm
  "OpenAndUnlocked" -> "Alarm" := {
    if (wrong_pin_count >= 3) {
      throw new Exception(s"ERROR: The wrong pin was entered $wrong_pin_count times, but we were " +
        s"still in OpenAndUnlocked")
    }
    require(wrong_pin_count == 2)
    assert(sut.getCurrentState == OpenAndUnlocked)
    changePinWrong()
    time_in_alarm = 0
    assert(sut.getCurrentState == Alarm)
    assert(sut.getPinErrorCount == wrong_pin_count)
  } label "Change pin wrong for 3rd time in OpenAndUnlocked"

  // Same as in OpenAndUnlocked
  "OpenAndLocked" -> "Alarm" := {
    if (wrong_pin_count >= 3) {
      throw new Exception(s"ERROR: The wrong pin was entered $wrong_pin_count times, but we were " +
        s"still in OpenAndLocked")
    }
    require(wrong_pin_count == 2)
    assert(sut.getCurrentState == OpenAndLocked)
    changePinWrong()
    time_in_alarm = 0
    assert(sut.getCurrentState == Alarm)
    assert(sut.getPinErrorCount == wrong_pin_count)
  } label "Change pin wrong for 3rd time in OpenAndLocked"


  // Alarm goes off when opening in Armed
  "Armed" -> "Alarm"  := {
    assert(sut.getCurrentState == Armed)
    val door_index = choose(0, num_doors)
    val door_number = door_index + 1
    if (door_number == luggage_door_number) require(luggage_locked == 1) // alarm only triggers if luggage is locked when opened
    sut.Open(door_number)
    doors(door_index) = open
    time_in_alarm = 0
    assert(sut.getCurrentState == Alarm)
  } label "Open random door in Armed"

  // Alarm can be turned off by unlocking
  "Alarm" -> "OpenAndUnlocked" := {
    assert(sut.getCurrentState == Alarm)
    unlockCorrectPin()
    assert(sut.getCurrentState == OpenAndUnlocked)
    assert(sut.getPinErrorCount == wrong_pin_count)
  } label "Unlock while in Alarm, disabling the alarm"

  // 300 seconds after the Alarm was started, the flashing lights turn off
  "Alarm" -> "SilentAndOpen" := {
    assert(sut.getCurrentState == Alarm)
    val wait_time = choose(300, 1000)
    time_in_alarm += wait_time
    sut.Wait(wait_time)
    assert(sut.getCurrentState == SilentAndOpen)
  } label "Wait 300s in Alarm, advancing to SilentAndOpen"

  // 30 seconds after the Alarm was started, the alarm sound turns off
  "Alarm" -> "Flash" := {
    assert(sut.getCurrentState == Alarm)
    val wait_time = choose(30, 100)
    time_in_alarm += wait_time
    sut.Wait(wait_time)
    assert(sut.getCurrentState == SilentAndFlashing)
  } label "Wait 30 seconds in Alarm"

  // We can also wait for smaller increments of time in Alarm
  "Alarm" -> "Alarm" := {
    assert(sut.getCurrentState == Alarm)
    val wait_time = choose(0, 30)
    require(time_in_alarm + wait_time < 30)
    time_in_alarm += wait_time
    sut.Wait(wait_time)
    assert(sut.getCurrentState == Alarm)
  } label "Short wait in Alarm, staying there"

  // Same as Alarm -> SilentAndOpen
  "Flash" -> "SilentAndOpen" := {
    assert(sut.getCurrentState == SilentAndFlashing)
    val wait_time = choose(300, 1000)
    time_in_alarm += wait_time
    sut.Wait(wait_time)
    assert(sut.getCurrentState == SilentAndOpen)
  } label "Wait in SilentAndFlashing until lights turn off"

  // We can also wait for smaller increments of time in Flash
  "Flash" -> "Flash" := {
    assert(sut.getCurrentState == SilentAndFlashing)
    val wait_time = choose(150, 250)
    require(time_in_alarm + wait_time < 300)
    time_in_alarm += wait_time
    sut.Wait(wait_time)
    assert(sut.getCurrentState == SilentAndFlashing)
  } label "Short wait in SilentAndFlashing, staying there"

  // If there is only one door open in SilentAndOpen, we can close it to return to Armed
  "SilentAndOpen" -> "Armed" := {
    assert(sut.getCurrentState == SilentAndOpen)
    var num_open_doors = num_doors
    var last_open_door_index = -1
    for (i <- 0 until num_doors) {
      if (doors(i) == closed) num_open_doors -= 1
      else last_open_door_index = i
    }
    // sanity check
    if (num_open_doors == 0) {
      throw new Exception("ERROR: All doors were closed in the model, but we were in the SilentAndOpen state.")
    }
    // if exactly one door is open, close it - this should make it more likely to enter the closed state
    require(num_open_doors == 1)
    sut.Close(last_open_door_index + 1)
    doors(last_open_door_index) = closed
    assert(sut.getCurrentState == Armed)
  } label "Close last in SilentAndOpen, returning to Armed"

  // Same as OpenAndUnlocked -> OpenAndUnlocked
  "SilentAndOpen" -> "SilentAndOpen" := {
    assert(sut.getCurrentState == SilentAndOpen)
    val door_index = choose(0, num_doors)
    val door_number = door_index + 1
    // 1 in 4 chance to open the door, 3 in 4 in chance to close it
    val new_door_state = if (choose(0, 5) == 0) open else closed
    if (new_door_state == open) {
      sut.Open(door_number)
      doors(door_index) = new_door_state
    }
    else {
      // make sure that at least one other door would still be open, otherwise we would be in ClosedAndUnlocked
      var car_still_open = false
      for (i <- 0 until num_doors) {
        if (doors(i) == open && i != door_index) {
          car_still_open = true
        }
      }
      // backtracks if car would be closed now
      require(car_still_open)
      sut.Close(door_number)
      doors(door_index) = new_door_state
    }
    assert(sut.getCurrentState == SilentAndOpen)
  } label "Open or close random door in SilentAndOpen"

  // Or we can unlock the car to advance to OpenAndUnlock
  "SilentAndOpen" -> "OpenAndUnlocked" := {
    assert(sut.getCurrentState == SilentAndOpen)
    unlockCorrectPin()
    assert(sut.getCurrentState == OpenAndUnlocked)
  } label "Unlock in SilentAndOpen"

  //  "OpenAndUnlocked" -> "ClosedAndUnlocked" := {}
  //  "OpenAndUnlocked" -> "OpenAndLocked" := {}
  //  "ClosedAndUnlocked" -> "OpenAndUnlocked" := {}
  //  "ClosedAndUnlocked" -> "ClosedAndLocked" := {}
  //  "OpenAndLocked" -> "ClosedAndLocked" := {}
  //  "ClosedAndLocked" -> "ClosedAndUnlocked" := {}
  //  "ClosedAndLocked" -> "OpenAndLocked" := {}
  //  "ClosedAndLocked" -> "Armed" := {}
  //  "Armed" -> "ClosedAndUnlocked" := {}
  //  "Alarm" -> "OpenAndUnlocked" := {}
  //  "Alarm" -> "SilentAndOpen" := {}
  //  "SilentAndOpen" -> "OpenAndUnlocked" := {}
  //  "SilentAndOpen" -> "Armed" := {}
}

