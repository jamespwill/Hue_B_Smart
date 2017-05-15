/**
 *  Hue B Smart Bridge
 *
 *  Copyright 2017 Anthony Pastor
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	Version 1.0
 *	Version 1.1 - added discoverBulbs, discoverGroups, discoverScenes, discoverSchedules, pollItems, pollBulbs, pollGroups, pollScenes, pollSchedules.
 * 				- Changed device to multiAttribute tile; added bridge & health check capability; added status attribute; added device-enroll
 *              - Receipt of successful change to a Group will now cause ST to immediately update status of all bulbs in that group!!!
 *
 */
metadata {
	definition (name: "Hue B Smart Bridge", namespace: "info_fiend", author: "Anthony Pastor") {
		capability "Actuator"
		capability "Bridge"
		capability "Health Check"


		attribute "serialNumber", "string"
		attribute "networkAddress", "string"
		attribute "status", "string"
		attribute "username", "string"
		attribute "host", "string"
        
		command "discoverItems"
        command "discoverBulbs"
        command "discoverGroups"
        command "discoverScenes"
        command "discoverSchedules"
        
        command "pollItems"
        command "pollBulbs"
        command "pollGroups"
        command "pollScenes"
        command "pollSchedules"
        
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
        standardTile("bridge", "device.username", width: 6, height: 4) {
        	state "default", label:"Hue Bridge", inactivelabel:true, icon:"st.Lighting.light99-hue", backgroundColor: "#F3C200"
        }
		main "bridge"
		details "bridge"
	}
}

void installed() {
	log.debug "Line 62 Installed with settings: ${settings}"
	//sendEvent(name: "DeviceWatch-Enroll", value: "{\"protocol\": \"LAN\", \"scheme\":\"untracked\", \"hubHardwareId\": \"${device.hub.hardwareID}\"}")

	initialize()
}

def updated() {
	log.debug "Line 69 Updated with settings: ${settings}"
	
	initialize()
}

def initialize() {
    def commandData = parent.getCommandData(device.deviceNetworkId)
    log.debug "Line 76 ${commandData}"
    sendEvent(name: "idNumber", value: commandData.deviceId, displayed:true, isStateChange: true)
    sendEvent(name: "networkAddress", value: commandData.ip, displayed:false, isStateChange: true)
    sendEvent(name: "username", value: commandData.username, displayed:false, isStateChange: true)
    state.host = this.device.currentValue("networkAddress") + ":80"
    state.userName = this.device.currentValue("username")
    
}


def discoverItems(inItems = null) {
	log.trace "Line 87 Bridge discovering all items on Hue hub."

	def host = state.host
	def username = state.userName
        
	log.debug "Line 92 *********** ${username} ********"
	def result 
        
    if (!inItems) {
	    result = new physicalgraph.device.HubAction(
			method: "GET",
			path: "/api/${username}/",
			headers: [
				HOST: host
			]
		)
    }    
                 
	return result

}

def pollItems() {
	log.trace "Line 110 pollItems: polling state of all items from Hue hub."

	def host = state.host
	def username = state.userName
        
	sendHubCommand(new physicalgraph.device.HubAction(
	method: "GET",
	path: "/api/${username}/",
		headers: [
			HOST: host
		]
	))
	    
}

def discoverBulbs() {
	log.trace "Line 126 discoverBulbs: discovering bulbs from Hue hub."

	def host = state.host
	def username = state.userName
        
	def result = new physicalgraph.device.HubAction(
	method: "GET",
	path: "/api/${username}/lights/",
		headers: [
			HOST: host
		]
	)
	
    return result
}

def pollBulbs() {
	log.trace "Line 143 ollBulbs: polling bulbs state from Hue hub."

	def host = state.host
	def username = state.userName
        
	sendHubCommand(new physicalgraph.device.HubAction(
	method: "GET",
	path: "/api/${username}/lights/",
		headers: [
			HOST: host
		]
	))
	    
}

def discoverGroups() {
	log.debug("Line 159 discoverGroups: discovering groups from Hue hub.")

	def host = state.host
	def username = state.userName
        
	def result = new physicalgraph.device.HubAction(
		method: "GET",
		path: "/api/${username}/groups/",
		headers: [
			HOST: host
		]
	)
    
	return result
}

def pollGroups() {
	log.trace "Line 176 pollGroups: polling groups state from Hue hub."

	def host = state.host
	def username = state.userName
        
	sendHubCommand(new physicalgraph.device.HubAction(
	method: "GET",
	path: "/api/${username}/groups/",
		headers: [
			HOST: host
		]
	))
	    
}

def discoverScenes() {
	log.debug("Line 192 discoverScenes: discovering scenes from Hue hub.")

	def host = state.host
	def username = state.userName
	
	def result = new physicalgraph.device.HubAction(
		method: "GET",
		path: "/api/${username}/scenes/",
		headers: [
			HOST: host
		]
	)
	
	return result
}

def pollScenes() {
	log.trace "Line 209 pollGroups: polling scenes state from Hue hub."

	def host = state.host
	def username = state.userName
        
	sendHubCommand(new physicalgraph.device.HubAction(
	method: "GET",
	path: "/api/${username}/scenes/",
		headers: [
			HOST: host
		]
	))
	    
}


def discoverSchedules() {
	log.trace "Line 226 discoverSchedules: discovering schedules from Hue hub."

	def host = state.host
	def username = state.userName
	
	def result = new physicalgraph.device.HubAction(
		method: "GET",
		path: "/api/${username}/schedules/",
		headers: [
			HOST: host
		]
	)
	
	return result
}


def handleParse(desc) {

	log.trace "Line 245 handleParse()"
	parse(desc)

}


// parse events into attributes

def parse(String description) {

	log.trace "Line 225 parse()"
	
	def parsedEvent = parseLanMessage(description)
	if (parsedEvent.headers && parsedEvent.body) {
		def headerString = parsedEvent.headers.toString()
		if (headerString.contains("application/json")) {
			def body = new groovy.json.JsonSlurper().parseText(parsedEvent.body)
			def bridge = parent.getBridge(parsedEvent.mac)
            def group 
			def commandReturn = []
            
			/* responses from bulb/group/scene/schedule command. Figure out which device it is, then pass it along to the device. */
			if (body[0] != null && body[0].success != null) {
            	log.trace "Line 268 ${body[0].success}"
				body.each{
					it.success.each { k, v ->
						def spl = k.split("/")
						log.debug "Line 272 k = ${k}, split1 = ${spl[1]}, split2 = ${spl[2]}, split3 = ${spl[3]}, split4= ${spl[4]}, value = ${v}"                            
						def devId = ""
                        def d
                        def groupScene
						
                      // SCHEDULES
						if (spl[4] == "schedules" || it.toString().contains("command")) {		
                   			/**
                   			devId = bridge.value.mac + "/SCHEDULE" + k
                   	        log.debug "SCHEDULES: k = ${k}, split3 = ${spl[1]}, split4= ${spl[2]}, value = ${v}"
                            sch = parent.getChildDevice(devId)
   	                        schId = spl[2]
//							def username = state.host
//							def username = state.userName
                            
							log.debug "schedule ${schId} successfully enabled/disabled."

//                   	        parent.doDeviceSync("schedules")
                   	        **/

						// SCENES			
						} else if (spl[4] == "scene" || it.toString().contains( "lastupdated") ) {	
							log.trace " Line 294 HBS Bridge:parse:scene - msg.body == ${body}"
                   			devId = bridge.value.mac + "/SCENE" + v
	                        d = parent.getChildDevice(devId)
    	                        groupScene = spl[2]
//								def username = state.host
//								def username = state.userName
                            
                            d.updateStatus(spl[3], spl[4], v) 
							log.debug "Line 302 Scene ${d.label} successfully run on group ${groupScene}."
					                        
			     	        //pollGroups() 	// parent.doDeviceSync("groups")
			     	        //pollBulbs() 	// parent.doDeviceSync("bulbs")
                    	
                    	// GROUPS
						} else if (spl[1] == "groups" && spl[2] != 0 ) {    
            	        	devId = bridge.value.mac + "/" + spl[1].toUpperCase()[0..-2] + spl[2]
        	    	        log.debug "Line 310 GROUP: devId = ${devId}"                            
	
							d = parent.getChildDevice(devId)

							d.updateStatus(spl[3], spl[4], v) 
							
                            //def gLights = []
                            //gLights = parent.getGLightsDNI(spl[2])
                            //gLights.each { gl ->
                            //	gl.updateStatus("state", spl[4], v) 
                            //}
                            
						// LIGHTS		
						} else if (spl[1] == "lights") {
							spl[1] = "BULBS"
								
							devId = bridge.value.mac + "/" + spl[1].toUpperCase()[0..-2] + spl[2]
							d = parent.getChildDevice(devId)
	                    	
	                    	d.updateStatus(spl[3], spl[4], v)
						
						} else {
							log.warn "Line 332 Response contains unknown device type ${ spl[1] } ."                                               	            
						}
                        
                        commandReturn
					}
				}	
			} else if (body[0] != null && body[0].error != null) {
				log.warn "Line 339 Error: ${body}"
			} else if (bridge) {
            	
				def bulbs = [:] 
				def groups = [:] 
				def scenes = [:] 
                def schedules = [:] 

				body?.lights?.each { k, v ->
					bulbs[k] = [id: k, label: v.name, type: v.type, state: v.state]
				}
				    
				state.bulbs = bulbs
				    
	            body?.groups?.each { k, v -> 
                   
    	            groups[k] = [id: k, label: v.name, type: v.type, action: v.action, all_on: v.state.all_on, any_on: v.state.any_on, lights: v.lights] //, groupLightDevIds: devIdsGLights]
				}
				
				state.groups = groups
				
	            body.scenes?.each { k, v -> 
                   	log.trace "Line 361 k=${k} and v=${v}"
                        				
                  	scenes[k] = [id: k, label: v.name, type: "scene", lights: v.lights]
                            
				}
                
                state.scenes = scenes
                    
                body.schedules?.each { k, v -> 
                  	log.trace "Line 370 schedules k=${k} and v=${v}"
                   	
                   	def schCommand = v.command.address
                  log.debug "Line 373 schCommand = ${schCommand}"
                
                    def splCmd = schCommand.split("/")
                 log.debug "Line 376 splCmd[1] = ${splCmd[1]} / splCmd[2] = ${splCmd[2]} / splCmd[3] = ${splCmd[3]} / splCmd[4] = ${splCmd[4]}"                        
                    def schGroupId = splCmd[4] 
					log.debug "Line 378 schGroupId = ${schGroupId}"
//                 	def schSceneId = bridge.value.mac + "/SCENES" + ${v.command.body.scene}
    	        
    	            schedules[k] = [id: k, name: v.name, type: "schedule", sceneId: v.command.body.scene, groupId: schGroupId, status: v.status]
				}

                	return createEvent(name: "itemDiscovery", value: device.hub.id, isStateChange: true, data: [bulbs, scenes, groups, schedules, bridge.value.mac])

/**
                if (bulbs && groups && scenes) {
                	return createEvent(name: "itemDiscovery", value: device.hub.id, isStateChange: true, data: [bulbs, scenes, groups, schedules, bridge.value.mac])
				} else {

					if (bulbs) {                
    	            	return createEvent(name: "bulbDiscovery", value: device.hub.id, isStateChange: true, data: [bulbs, bridge.value.mac])
					} 
				
					if (groups) {                
                		return createEvent(name: "groupDiscovery", value: device.hub.id, isStateChange: true, data: [groups, bridge.value.mac])
					}
					
					if (scenes) {                
            	    	return createEvent(name: "sceneDiscovery", value: device.hub.id, isStateChange: true, data: [scenes, bridge.value.mac])				
					} 
				
					if (schedules) {                
    	            	return createEvent(name: "scheduleDiscovery", value: device.hub.id, isStateChange: true, data: [schedules, bridge.value.mac])                    
        	     	}       
            	}    
**/                
			}
			
		} else {
			log.debug("Line 411 Unrecognized messsage: ${parsedEvent.body}")
		}
		
	}

	return []		//	?????????????????? NEEDED ???????
}
