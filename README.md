# Effective Software design: Automated Sensor Updates

## Introduction
The first excercise in the Effective Software Design Course. This course is focussing on problem analysis and then designing
an appropriate solution. Therefor, you will only find the general synopsis of the problem. The point is to start thinking 
for yourself, to start asking the right questions. In case you want to code this exercise, please refer to the full analysis
of this project in the exercise introduction of the course. 

This project is implemented using AI. I did that in different phases. You can find a full transcript of how I did that, with
prompts, context etc in the course. 

This project has multiple branches prefixed with `phase`. This is on purpose, you can see the code grow for each phase. Feel
free to check out any branch and have a look. 

## Context of the project

You are part of an IoT (IoT stands for Internet of Things) company shipping sensors to a vast amount of clients. We deal
in sensors that can measure for example temperature, pressure, humidity, etc… After measuring, these sensors send the data
over the internet. These sensors are manufactured somewhere else. Your company orders new sensors when needed in batches
of up to 250 sensors at the time. These sensors have firmware from their original manufacturer, which is pretty trustworthy.

In your company, before the sensors are shipped to customers, the people from the operations team have to configure these
sensors. In other words, they need to upload configuration to these sensors. An example of something that can be configured
is the server to where the sensors should send their measurements.

While testing sensors of type **TS50X** with the latest configuration, the people of the operations team noticed a
compatibility problem with older firmware. Some new features which can be configured do not seem to work with older
firmware. So the sensors only do a part of what they should be doing. Your company cannot ship those sensors to customers.
Your company stands for high quality and happy customers.

The people of the operations team should check every incoming batch of sensors (up to 250) if they can be shipped or not.
However, these people are very valuable. They shouldn’t manually check up to 250 sensors to know which sensors can be shipped
and which ones can't.

What they need is an automated solution that can check up to 250 sensor serial numbers (or ids) and give the current status
of that sensor.

If the firmware version is not compatible, the sensor can’t be shipped. So the solution should take the next step to make
it shippable.

In addition, while automating sensor work anyway, have the solution update the configuration automatically as well.

## Detailed use case

For every incoming shipment of sensors, the operations team can download [a .csv file containing the id's](src/main/resources/examples/sample.csv)
of the sensors that have been shipped. It is this list that needs to be veryfied as they want to know if these sensors are
ready to be shipped or not.
If not, you need to schedule the next step in order to make it shippable. A sensor is ready to be shipped when
its firmware is appropriate and if the configuration is the latest configuration.

So for each sensor with its id in the list, you need to check if the firmware is appropriate so that the latest configuration
can run without problems. Currently, firmware with version **59.1.12Rev4** or higher is appropriate. Schedule a firmware update
task when necessary. If the firmware is appropriate, check if the configuration is the latest one. If not, update the
configuration.

We also don't want to schedule a configuration update if there is a firmware update scheduled.

You need to use [the API of the original manufacturer](Manufacturers_api.md) to get the current sensor information and to schedule tasks such as
a firmware update and a configuration update.
