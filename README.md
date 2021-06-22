# Match outcome prediction

A project for the Distributed Artificial Intelligence and Intelligent Agents course (FTN, University of Novi Sad), built using Java EE technologies and Angular.

## General ##
* An agent framework that enables building multi-agent systems
* For demonstration purposes, there have been implemented two types of agents
  * Chat application agents
    * UserAgent
    * UserHelperAgent
  * Match outcome prediction agents
    * CollectorAgent
    * PredictorAgent
    * MasterAgent

## Overview of communication between match outcome prediction agents ##
* When a prediction method is called, a master and predictor agent are started locally and a collector agent is started on each node in a cluster
* Each collector agent replies to the predictor agent with the match data for selected teams
* When data from all nodes has been forwarded to the predictor agent, it predicts the match outcome and sends the result to the master agent
* Master agent displays the result via websocket

## Setup ##
* Java Enterprise Application
  * Download Wildfly 11 application server
  * Replace existing standalone-full-ha.xml file with the one provided [here](https://github.com/tamararankovic/chat_zadatak/tree/master/wildfly-configuration)
  * For the non-master cluster node you must provide master node name in the ```connection.properties``` file
  * Publish chat-ear.ear to ```/standalone/deployments``` folder
* Angular Application
  * Download Node.js (version 14.15.0 used for development)
  * Install Angular CLI (version 10.2.0 used for dvelopment)
  * Navigate to ```match-score-prediction-client``` project and type: ```npm install```
  
## Start the application ##

* Java Enterprise Application

  * Navigate to ```/bin``` folder of Wildfly and type:
  #### Windows ####
  ```
  standalone.bat -c standalone-full-ha.xml
  ```
  #### Linux ####
  ```
  ./standalone.sh -c standalone-full-ha.xml
* Angular Application

  * Navigate to ```match-score-prediction-client``` project and type:
  </br>
  
  ```
  ng serve
  ```
  * To access system monitoring UI, type http://localhost:4200 in a browser
  * To access match outcome prediction UI, type http://localhost:4200/prediction in a browser
