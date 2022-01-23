$target_mutation = $args[0]

if (-not(Test-Path -Path ./caralarm/CarAlarm_.java)) {
    mv ./caralarm/CarAlarm.java ./caralarm/CarAlarm_.java
}
foreach ($f in Get-ChildItem ./mutations) {
    $filename = $f.name
    if (($target_mutation -eq $filename) -or -not($target_mutation)) {
        echo "File $f"
        cp ./mutations/$f ./caralarm/CarAlarm.java
        javac ./caralarm/CarAlarm.java
        scalac -cp "modbat.jar;." .\model\CarAlarmModel.scala
        scala -cp . modbat.jar -s=10 -n=20 --abort-probability=0.0002 model.CarAlarmModel
        rm ./caralarm/CarAlarm.java
    }
}
mv -Force ./caralarm/CarAlarm_.java ./caralarm/CarAlarm.java