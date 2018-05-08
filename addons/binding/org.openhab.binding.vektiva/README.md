# Vektiva Binding

This binding allows control of Vektiva products. (http://vektiva.com)

## Supported Things

The currently supported thing is Smarwi (https://vektiva.com/smarwi)

## Discovery

The automatic discovery is not currently supported by thing's API.

## Binding Configuration

This binding does not require specific configuration.

## Thing Configuration

To manually add a Smarwi thing just enter the local network IP address of the device. 
If you want to change the polling frequency of thing availability and status, please change the advanced parameter _refreshInterval_. 

## Channels

The only exposed channel is named _control_ and is of type _Rolleshutter_.
It reacts to standard roller shutter commands _UP/DOWN/STOP_. Percentual closure (dimmer) is not supported.

## Full Example

*.things:
```
Thing vektiva:smarwi:5d43c74f [ ip="192.168.1.22", refreshInterval=30 ]
```

*.items
```
Rollershutter Smarwi "Smarwi [%d %%]" { channel="vektiva:smarwi:5d43c74f:control" }
```

*.sitemap
```
Default item=Smarwi
```
## Note

This binding currently does not support control via vektiva.online cloud service.
All control uses local network API pusblished here: https://vektiva.gitlab.io/vektivadocs/api/api.html 