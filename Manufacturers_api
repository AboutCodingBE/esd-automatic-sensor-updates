# Original Manufacturers Sensor API

This is the minimal version of the API. It just represents the endpoints needed to complete what is asked.

Just for the record, this API is fake. It is a resemblance of something real, but the endpoints and so on are fake.

## Sensors

### Sensor Information

URL:    www.mysensor.io/api/sensors/{id}

Method: GET

Response: `200 OK`

Path Parameter: id - Specific sensor id

```json
{
  "serial": 123456789098789,
  "type": "TS50X",
  "status_id": 1,
  "current_configuration": "some_oonfiguration.cfg",
  "current_firmware": "59.1.12Rev4",
  "created_at": "2022-03-31 11:26:08",
  "updated_at": "2022-10-18 17:53:48",
  "status_name": "Idle",
  "next_task": null,
  "task_count": 5,
  "activity_status": "Online",
  "task_queue": [124355, 44435322] 
}
```
**Status_id**

A sensor can be in the following status: 

|Status name	|Status id	|Description|
|--|--|--|
|Idle	|1	|The sensor doesn’t have any tasks|
|Awaiting	|2	|A task has been scheduled, the device is waiting for execution|
|Updating	|3	|The device is doing an update|


**Activity_status**

A sensor can have the following activity status: 

|Status name	|Status id	|Description|
|--|--|--|
|Online	|1	|The sensor is online and capable of sending information|
|Disconnectd	|2	|The sensor is not online and cannot send information|
|Error	|3	|The sensor is in an error state and cannot function normally|


## Tasks

### Schedule a task

URL: www.mysensor.io/api/tasks

Method: PUT

Response: `201 CREATED`

Response information: Return the unique id of the scheduled task. 

Request body example: 

```json
{
  "sensor_serial": 234545767890987,
  "file_id": "a3e4aed2-b091-41a6-8265-2185040e2c32",
  "type": "update_configuration"
}
```
**Type**

A task can have the following types: 

|Type |	Description|
|--|--|
|update_firmware|	A task to update the firmware|
|update_configuration|	A task to update the configuration|


**Note**: 

Both types of tasks actually need a file (either the firmware installation file or the configuration file) to be able to
do their job. That is why the API is showing a file_id, which is a reference to an existing file. However, when scheduling
a task without the `file_id` field included, it will schedule a task to update to either the latest known firmware or the
latest known configuration file. 