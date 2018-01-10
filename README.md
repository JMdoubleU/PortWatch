# PortWatch
An infrastructure monitoring tool for actively detecting host and service changes.
### Alerts
Alerts are generated when a host:
* Is first scanned
* Has a service change its state or fingerprint
* Has become unreachable
* Has become reachable after being previously unreachable

## Configuration
A JSON configuration file is used to configure two main components: scanning and integrations.
Use the [sample config file](doc/config.sample.json) as a reference.

### Scanning

#### Variables
* `type`: Nmap scan type - "stealth" (SYN) or "version"
* `nmapPath`: Path to a _directory_ containing an Nmap binary
* `maxThreads`: Maximum number of threads to use for scanning
* `waitSeconds`: Seconds to wait between scan cycles
#### Hosts
Hosts are to be provided in an array of individual host config objects. To specify host ports there are two different types of configurations: range and list.
##### Port Range
The port range type is used to specify an inclusive range of ports to monitor.
```json
{
	"host":"<hostname>",
	"ports":{
		"type":"range",
		"lower":1,
		"upper":65535
	}
}
```
##### Port List
The port list type is used to specify a list of ports to monitor.
```json
{
	"host":"<hostname>",
	"ports":{
		"type":"list",
		"list":[21, 22, 25, 80]
	}
}
```

### Integrations
Integrations are to be provided in an array of individual integration config objects. If you do not want to configure any integrations you can either leave the array empty or simply not include the `integrations` object.
#### Slack
The Slack integration allows you to receive alerts through a desired Slack channel.
```json
{
	"type":"slack",
	"apiKey":"<bot user api key>",
	"channel":"general"
}
```
You must create a new [Slack app](https://api.slack.com/apps) and add it to your workspace. Change the `apiKey` value to the API key you are given for the bot user. Invite the bot user to the channel you'd like to receive alerts in and change the `channel` value to that channel's name.

## Usage
```
usage: PortWatch
 -c,--config <arg>   config path - required
 -d,--debug          enable debug message logging
 -l,--log <arg>      log path
```
 
Example usage: `java -jar PortWatch.jar --config myconfig.json`