# dsagen
Simple traffic generator for performance testing on IOT-DSA

# Usage
sbt 'run <number of nodes> <messages per sec> <duration in sec> --broker https://broker-host:port/conn'

Dsagen performs following sequence.

1. Create given number of DSA nodes named CountN with initial value -2.
1. Pause 10 seconds.
1. Count up CountN nodes from zero with given <message per sec> frequency in 1 minute.
1. Pause 1 minute.  Count is continue increased in the same pace but nodes are not updated.
1. Count up nodes again.  Value of node will jump.  Start timer expiring after given <duration in sec> seconds.
1. Stop counting when timer is fired.
