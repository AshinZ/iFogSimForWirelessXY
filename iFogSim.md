#  ifogsim平台文档

# jjars

# out

# output

# results

# src

## images

## org

### cloudbus.cloudsim

### fog

#### application

#### entities

#### gui

##### core

######  ActuiatorGui

| 方法名          | 参数                                | 返回值            | 用途               |
| --------------- | ----------------------------------- | ----------------- | ------------------ |
| ActuatorGui     | String name <br>String actuatorType |                   | 创建Actuator的gui  |
| getName         |                                     | String            | 获得Actuator的name |
| toString        |                                     | String 固定字符串 | 返回Actuator []    |
| getActuatorType |                                     | String            | 返回actuatorType   |
| setActuatorType | String actuatorType                 | void              | 设置ActuatorType   |

###### ActuatorModule 

|  方法    |  参数     | 返回值      | 用途     |
| ---- | ---- | ---- | ---- |
|   ActuatorModule   |      |      |  未完成    |
|ActuatorModule|String actuatorType||设定ActuatorModule|
|getActuatorType||String|得到ActuatorType|
|setActuatorType|String actuatorType|void|设定ActuatorType|
|toString||String|返回固定字符串|

###### AppModule

| 方法 | 参数 | 返回值 | 用途 |
| ---- | ---- | ---- | ---- |
| AppModule |      |      | 未完成 |
| AppModule | String name ||设置AppModule|
|toString||string|返回固定字符串|

###### Bridge

| 方法 |参数| 返回值 | 用途 |
| ---- |----| ---- | ---- |
| getNode | Graph graph <br>String name | Node | 找出名字对应的node |
| 	 jsonToGraph |String fileName<br>int type	 |Graph |根据文件名返回graph数据 |
| graphToJson|Graph graph| String|将graph里的数据转换成json串|

###### Coordinates

| 方法        | 参数 | 返回值 | 用途 |
| :---------- | ---- | ------ | ---- |
| Coordinates |      |        |  设置xy为0    |
| Coordinates |   int x<br>int y   |        |   设置xy为输入   |
| getX        |      |  int     |    返回x  |
|    setX         | int x |    void    |   设置x   |
|     getY        |      |    int    |  返回y    |
|     setY        | int y |   void     |   设置y   |
|   toString          |      |    String    | 返回固定字符串     |

###### Edge

privite dest

| 方法         | 参数                                    | 返回值 | 用途           |
| ------------ | --------------------------------------- | ------ | -------------- |
| Edge         | Node to                                 |        | 设置edge       |
| Edge         | Node to<br>double latency               |        | 设置edge       |
| Edge         | Node to<br> String name<br> long bw     |        | 设置edge       |
| Edge         | Node to<br> Map<String<br> Object> info |        | 设置edge       |
| getNode      |                                         | Node   | 返回node       |
| getBandwidth |                                         | long   | 返回bw         |
| getLatency   |                                         | double | 返回latency    |
| setInfo      | Map<String<br> Object> info             | void   | 设置info       |
| toString     |                                         | String | 返回固定字符串 |

###### FogDeviceGui

| 方法           | 参数                                                         | 返回值 | 用途             |
| -------------- | ------------------------------------------------------------ | ------ | ---------------- |
| FogDeviceGui   | String name<br> long mips<br> int ram<br> long upBw<br> long downBw<br> int level<br> double rate |        | 设置DogDeviceGui |
| getName        |                                                              | String | 得到name         |
| setName        | String name                                                  | void   | 设置name         |
| getMips        |                                                              | long   | 得到mips         |
| setMips        | long mips                                                    | void   | 设置mips         |
| getRam         |                                                              | int    | 得到ram          |
| setRam         | int ram                                                      | void   | 设置ram          |
| getUpBw        |                                                              | long   | 得到UpBw         |
| setUpBw        | long upBw                                                    | void   | 设置UpBw         |
| getDownBw      |                                                              | long   | 得到DownBw       |
| setDownBw      | long downBw                                                  | void   | 设置DownBw       |
| toString       |                                                              | String | 返回固定字符串   |
| getLevel       |                                                              | int    | 得到层级         |
| setLevel       | int level                                                    | void   | 设置层级         |
| getRatePerMips |                                                              | double | 得到RatePerMips  |
| setRatePerMips | double ratePerMips                                           | void   | 设置RatePerMips  |

###### Graph






##### dialog

##### example

#### myFog

#### placement

#### policy

#### scheduler

#### test

#### utils

## topologies

# topologies

