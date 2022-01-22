javac .\caralarm\CarAlarm.java
scalac -cp "modbat.jar;." .\model\CarAlarmModel.scala
scala -cp . modbat.jar -s=10 -n=5 --abort-probability=0.0002 model.CarAlarmModel