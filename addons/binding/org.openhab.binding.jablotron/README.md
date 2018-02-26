# Jablotron Alarm Binding

This is the OH2.x binding for Jablotron alarms.
https://www.jablotron.com/en/jablotron-products/alarms/

## Supported Things

* bridge (the bridge to your jablonet cloud account)
* JA-80 OASIS alarm
 
Please contact me if you want to add other alarms (e.g. JA-100 etc)

## Discovery

This binding support auto discovery. Just manually add bridge thing and supply login & password to your Jablonet account.

## Binding Configuration

Binding itself doesn't require specific configuration.

## Thing Configuration

The bridge thing requires this configuration:
* login (login to your jablonet account)
* password (password to your jablonet account)

The oasis thing require this configuration (it is better to have it autodiscovered):
* serviceId (Jablotron internal service id of your alarm)
* url (an initialization url for the alarm)
* refresh (thing status refresh period in seconds)

## Channels

The bridge thing does not have any channels.
The oasis thing exposes these channels:

* statusA (the status of A section)
* statusB (the status of AB/B section)
* statusABC (the status of ABC section)
* statusPGX (the status of PGX)
* statusPGY (the status of PGY)
* command (the channel for sending codes to alarm)
* lastEvent (the code of the last checking)
* lastEventTime (the time of the last event)
* lastCheckTime (the time of the last checking)
* alarm (the alarm status OFF/ON)

## Full Example

#items file
```
String  HouseArm "Arm [%s]" <alarm>
String  JablotronCode { jablotron="code", autoupdate="false" }
Contact HouseAlarm "Alarm [%s]" <alarm> { jablotron="alarm" }
Switch	ArmSectionA	"Garage arming"	<jablotron>	(Alarm)	{ jablotron="A" }
Switch	ArmSectionAB	"1st floor arming"	<jablotron>	(Alarm)	{ jablotron="B" }
Switch	ArmSectionABC	"2nd floor arming"	<jablotron>	(Alarm)	{ jablotron="ABC" }
DateTime LastArmEvent "Last event [%1$td.%1$tm.%1$tY %1$tR]" <clock> { jablotron="lasteventtime" }
Switch	ArmControlPGX	"PGX"	<jablotron>	(Alarm)	{ jablotron="PGX" }
Switch	ArmControlPGY	"PGY"	<jablotron>	(Alarm)	{ jablotron="PGY" }
```

#sitemap example
```
Text item=HouseArm icon="alarm" {
    Switch item=ArmSectionA
    Switch item=ArmSectionAB
    Switch item=ArmSectionABC
    Text item=LastArmEvent
    Switch item=ArmControlPGX
    Switch item=ArmControlPGY
    Switch item=JablotronCode label="Arm" mappings=[1111=" A ",2222=" B ",3333="ABC"]
    Switch item=JablotronCode label="Disarm" mappings=[5555="Disarm"]
}
```

#rule example
```
rule "Arm"
when 
  Item ArmSectionA changed or Item ArmSectionAB changed or Item ArmSectionABC changed or 
  System started
then
   if( ArmSectionA.state.toString == "ON" || ArmSectionAB.state.toString == "ON" || ArmSectionABC.state.toString == "ON")
   {   postUpdate(HouseArm, "partial")  }
   if( ArmSectionA.state.toString == "OFF" && ArmSectionAB.state.toString == "OFF" && ArmSectionABC.state.toString == "OFF")
   {   postUpdate(HouseArm, "disarmed") }
   if( ArmSectionA.state.toString == "ON" && ArmSectionAB.state.toString == "ON" && ArmSectionABC.state.toString == "ON")
   {   postUpdate(HouseArm, "armed")    }
end
```