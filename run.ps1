javac .\caralarm\CarAlarm.java
scalac -cp "modbat.jar;." .\model\CarAlarmModel.scala
scala -cp . modbat.jar -s=10 -n=5  model.CarAlarmModel