package model

import caralarm.CarAlarm
import caralarm.CarAlarm.State._
import modbat.dsl._

class CarAlarmModel extends Model {
  // precondition with require (condition)
  // assertions with assert(assertion)
  // choose(actions: AnyFunc*): Takes a list of actions, randomly chooses one action, and executes it.
  // maybe (doSomething)

  val sut = new CarAlarm() // SUT
  val num_doors = 5
  val closed = 0
  val open = 1
  // internal tracking of door state
  val doors: Array[Int] = Array.fill(num_doors)(open)
  var arm_timer = 0
  var pin: Int = sut.DEFAULT_PINCODE
  //
  // transitions

  // Do something with a random door, with closing the door being more likely.
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
      for (i <- 0 to num_doors) {
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
  }

  // If there is exactly one open door, close it and enter ClosedAndUnlocked state.
  "OpenAndUnlocked" -> "ClosedAndUnlocked" := {
    assert(sut.getCurrentState == OpenAndUnlocked)
    var num_open_doors = num_doors
    var last_open_door_index = -1
    for (i <- 0 to num_doors) {
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
  }

  // Simple transition to the OpenAndLocked state, no special behavior
  "OpenAndUnlocked" -> "OpenAndLocked" := {
    assert(sut.getCurrentState == OpenAndUnlocked)
    sut.Lock()
    assert(sut.getCurrentState == OpenAndLocked)
  }

  // Opening any door advances from ClosedAndUnlocked to OpenAndUnlocked
  "ClosedAndUnlocked" -> "OpenAndUnlocked" := {
    assert(sut.getCurrentState == ClosedAndUnlocked)
    val door_index = choose(0, num_doors)
    val door_number = door_index + 1
    sut.Open(door_number)
    doors(door_index) = open
    assert(sut.getCurrentState == OpenAndUnlocked)
  }

  // Same as OpenAndUnlocked -> ClosedAndUnlocked, also reset the arming timer
  "OpenAndLocked" -> "ClosedAndLocked" := {
    assert(sut.getCurrentState == OpenAndLocked)
    var num_open_doors = num_doors
    var last_open_door_index = -1
    for (i <- 0 to num_doors) {
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
    arm_timer = 0
    assert(sut.getCurrentState == ClosedAndLocked)
  }

  // When entering ClosedAndLocked, reset the arming timer
  "ClosedAndUnlocked" -> "ClosedAndLocked" := {
    assert(sut.getCurrentState == ClosedAndUnlocked)
    sut.Lock()
    arm_timer = 0
    assert(sut.getCurrentState == ClosedAndLocked)
  }

  "ClosedAndLocked" -> "ClosedAndLocked" := {
    assert(sut.getCurrentState == ClosedAndLocked)
    val wait_time = choose(0, 20)
    // if waiting would increase time to 20 seconds or more, model would advance to armed state
    require(wait_time + arm_timer < 20)
    arm_timer += wait_time
    sut.Wait(wait_time)
    assert(sut.getCurrentState == ClosedAndLocked)
  }

  "ClosedAndLocked" -> "Armed" := {
    assert(sut.getCurrentState == ClosedAndLocked)
    val wait_time = choose(0, 20)
    // only advance to armed state if total waiting time would surpass 20 seconds
    require(wait_time + arm_timer >= 20)
    sut.Wait(wait_time)
    arm_timer = 0
    assert(sut.getCurrentState == Armed)
  }

  // Opening any door advances to OpenAndLocked, same as ClosedAndUnlocked -> OpenAndUnlocked
  "ClosedAndLocked" -> "OpenAndUnlocked" := {
    assert(sut.getCurrentState == ClosedAndLocked)
    val door_index = choose(0, num_doors)
    val door_number = door_index + 1
    sut.Open(door_number)
    doors(door_index) = open
    assert(sut.getCurrentState == OpenAndLocked)
  }

  // Unlocking requires a Pin code
  "ClosedAndLocked" -> "ClosedAndUnlocked" := {
    assert(sut.getCurrentState == ClosedAndLocked)
    sut.PinUnlock(pin)
    assert(sut.getCurrentState == ClosedAndUnlocked)
  }


  // Same as unlocking in ClosedAndLocked
  "Armed" -> "ClosedAndUnlocked" := {
    assert(sut.getCurrentState == Armed)
    sut.PinUnlock(pin)
    assert(sut.getCurrentState == ClosedAndUnlocked)
  }

  // Same as unlocking in ClosedAndLocked
  "OpenAndLocked" -> "OpenAndUnlocked" := {
    assert(sut.getCurrentState == OpenAndLocked)
    sut.PinUnlock(pin)
    assert(sut.getCurrentState == OpenAndUnlocked)
  }

  // Alarm goes off when opening in Armed
  "Armed" -> "Alarm"  := {

  }

  // Alarm automatically turns off after 300s
  "Alarm" -> "SilentAndOpen" := {

  }


  //  "OpenAndUnlocked" -> "ClosedAndUnlocked" := {}
  //  "OpenAndUnlocked" -> "OpenAndLocked" := {}
  //  "ClosedAndUnlocked" -> "OpenAndUnlocked" := {}
  //  "ClosedAndUnlocked" -> "ClosedAndLocked" := {}
  //  "OpenAndLocked" -> "ClosedAndLocked" := {}
  //  "ClosedAndLocked" -> "ClosedAndUnlocked" := {}
  //  "ClosedAndLocked" -> "OpenAndLocked" := {}
  //  "ClosedAndLocked" -> "Armed" := {}
  "Armed" -> "Alarm" := {}
  //  "Armed" -> "ClosedAndUnlocked" := {}
  //  "Alarm" -> "OpenAndUnlocked" := {}
  //  "Alarm" -> "SilentAndOpen" := {}
  //  "SilentAndOpen" -> "OpenAndUnlocked" := {}
  //  "SilentAndOpen" -> "Armed" := {}
}

