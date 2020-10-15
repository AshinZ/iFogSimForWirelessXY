#  ifogsim平台文档

# jars

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

Gui模块说明：要想学会使用Gui，首先需要学会使用iFogSim，才能对gui中的一些参数进行了解。

首先需要建立网络拓扑，接着我们需要输入应用（application）与模块（module），然后我们需要规定一些参数，我们才能使用iFogSim。

在目前的gui中，我们只能输入网络拓扑，并不能够输入其他数据，也即，我们并不能将所有的设置都在gui上完成，这也是我们将要修正的一个方向。

##### core

gui的核心类，主要用来记录静态数据。

######  ActuatorGui

用来记录actuator的数据，包括name和类型，并且在gui上展示出来。

###### ActuatorModule 

actuator模块

###### AppModule

app模块

###### Bridge

中间件，用来进行json数据和graph数据的转换，从而实现gui数据的文件保存于输入

###### Coordinates

地点参数保存

###### Edge

点参数

###### FogDeviceGui

雾设备类，用来存储fogdevice的数据，展示在gui上。

###### Graph

存储的是node数据，可以调整node数据，通过node和edge数据对应来进行合适的gui展示。

###### GraphView

核心类之一，用来在panel上展示数据。

###### HostNode

成员类，我们可以将其看成是host的数据存储。

###### Link

Link类主要存储了两个设备（如两个雾设备、一个雾设备一个传感器）之间的链接关系，以及他们的延时等信息。

###### Node

gui展示的点类，用来进行图形化的展示，存储着一个点的名字类型和他在图形上的位置。

###### NodeCellRenderer

点类的表格绘制器，用来在设备下面绘制设备的名字。

###### PlaceHolder

当我们选择link的时候，通过PlaceHolder类来给出目前尚未被选择的设备。

###### SensorGui

Sensor类的Gui，存储着一个Sensor的name、type等相关信息。

###### SensorModule

Sensor模块

###### SwitchNode

标明node之间的关系的显式图像。

###### VmNode

建立虚拟点

###### **ActuatorGuiData**

存储建立Actuator时存储的数据，用来在进行文件操作时模拟输入。

###### FogDeviceGuiData

存储建立FogDevice时存储的数据，用来在进行文件操作时模拟输入。

###### LinkGuiData

存储建立Link时存储的数据，用来在进行文件操作时模拟输入。

###### SensorGuiData

存储建立Sensor时存储的数据，用来在进行文件操作时模拟输入。

##### dialog

该包主要用于gui中的事件响应。

###### About

主要是用于界面初始化，绘制主要的框架。

###### AddActuator

往界面中添加actuator。

###### AddActuatorModule

往界面中添加actuatorModule，尚未使用。

###### AddAppEdge

往界面中添加AppEdge

###### AddApplicationModule

往界面中添加applicationModule

###### AddFogDevice

往界面中添加fogDevice

###### AddLink

往界面中添加Link

###### AddPhysicalEdge

往界面中添加PhysicalEdge

###### AddPhysicalNode

往界面添加PhysicalNode

###### AddSensor

往界面中添加Sensor

######  AddSensorModule

往界面中添加sensorModule

###### AddVirtualEdge

往界面中添加VirtualEdge

###### AddVirtualNode

往界面中添加VirtualNode

###### SDNRUN

运行模块

##### example

在此处添加属于你的Gui文件。



#### myFog

#### placement

#### policy

#### scheduler

#### test

#### utils

## topologies

# topologies

