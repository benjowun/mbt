package model

import caralarm.CarAlarm
import modbat.dsl._

class CarAlarmModel extends Model {
  // precondition with require (condition)
  // assertions with assert(assertion)
  // choose(actions: AnyFunc*): Takes a list of actions, randomly chooses one action, and executes it.
  // maybe (doSomething)

 val SUT = new CarAlarm() // SUT
//
  // transitions
//  "OpenAndUnlocked" -> "ClosedAndUnlocked" := {}
//  "OpenAndUnlocked" -> "OpenAndLocked" := {}
//  "ClosedAndUnlocked" -> "OpenAndUnlocked" := {}
//  "ClosedAndUnlocked" -> "ClosedAndLocked" := {}
//  "OpenAndLocked" -> "OpenAndUnlocked" := {}
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
