# dslink-scala-generator
Simple traffic generator for performance testing on IOT-DSA.
Use with dslink-scala-sink.

# Usage

```shell-session
$ sbt assembly
$ java -jar target/scala-2.12/dslink-scala-generator-assembly-0.1.0-SNAPSHOT.jar <number of nodes> <messages per sec> <duration in sec> --name <generator base name + index> --broker https://broker-host:port/conn
```

## Example Usage

Start dslink-scala-sink in another terminal.

```shell-session
$ java -jar target/scala-2.12/dslink-scala-sink-assembly-0.1.0-SNAPSHOT.jar Generator 1 1 1 ./ --broker https://localhost:8443/conn
```

Start dslink-scala-generator.
 
```shell-session
$ java -jar target/scala-2.12/dslink-scala-generator-assembly-0.1.0-SNAPSHOT.jar 1 2 60 --name Generator1 --broker https://localhost:8443/conn --name Generator1
```

## Permission note

Starting with Cisco Kinetic Edge and Fog Processing Module 1.2.1, permission feature is enable by default for new
installation.  With such default permission setting, broker permits nothing to DSLink connected from external host.
This is inconvenient when you run broker in a Docker container and you run your DSLink under development on host OS or
another Docker container.  To make broker permissive for external DSLink, you have to change server.json.

There is two simple way to permit everything for external DSLink.  One is making "defaultPermission" empty.

```json
"defaultPermission": null
```

The other way is changing value of "default" entry "config" from "none".

```json
"defaultPermission": [
  [":config","config"],
  [":write","write"],
  [":read","read"],
  [":user","read"],
  [":trustedLink","config"],
  ["default","config"]
```

Note that the above is not recommended permission for production.

# How it works

Dsagen performs following sequence.

1. Create given number of DSA nodes named cN with initial value -2.
1. Pause 10 seconds.
1. Count up CountN nodes from zero with given <message per sec> frequency in 1 minute.
1. Pause 1 minute.  Count is continue increased in the same pace but nodes are not updated.
1. Count up nodes again.  Value of node will jump.  Start timer expiring after given <duration in sec> seconds.
1. Stop counting when timer is fired then set node value to -1.
