package com.example.code.service

import android.util.Log
import com.example.code.ui.states.Obstacle

class MessageService {
    companion object {
        // Message parsing for messages received from rPi
        fun parseMessage(buffer: ByteArray, bytes: Int): HashMap<String, String> {
            val result = HashMap<String, String>()
            val msg = String(buffer, 0, bytes)
            val list: List<String> = msg.split(" ")

            var parsedMsg: String = when (list[0]) {
                "[C4]" -> parseRobotStatus(list, result)
                "[C9]" -> parseTargetIDFound(list, result)
                "[C10]" -> parseRobotPosFacing(list, result)
                "[C11]" -> updateRobotPosFacing(result)
                else -> {
                    msg
                }
            }
            parsedMsg += "\n"
            result["msg"] = parsedMsg
            return result
        }

        private fun toByteArray(msg: String): ByteArray {
            val payload = msg + " "
            return payload.toByteArray(Charsets.UTF_8)
        }

        // C4: Robot Status
        // [Tag], Action, <>
        /*
        [C4] MOV F
        [C4] MOV L
        [C4] IMG T (taking a picture)
        [C4] IMG M (running model on picture)
        [C4] TAR 1 (heading to target 1)
         */
        private fun parseRobotStatus(list: List<String>, result: HashMap<String, String>): String {
            result["TAG"] = "C4"
            val action = list[1]
            val value = list[2]
            var parsedMsg = value
            when (action) {
                "MOV" -> {
                    parsedMsg = "Moving "
                    when (value) {
                        "Fd" -> parsedMsg += "Forward"
                        "Bd" -> parsedMsg += "Backward"
                        "Lt" -> parsedMsg += "Left"
                        "Rt" -> parsedMsg += "Right"
                        else -> result["TAG"] = "Error"
                    }
                }
                "IMG" -> {
                    when (value) {
                        "TP" -> parsedMsg = "Taking a Photo"
                        "MI" -> parsedMsg = "Running Model on Image"
                        else -> result["TAG"] = "Error"
                    }
                }
                "TAR" -> {
                    if (value.toIntOrNull() != null) {
                        parsedMsg = "Heading to Target $value"
                    }
                    else {
                        result["TAG"] = "Error"
                    }
                }
                else -> result["TAG"] = "Error"
            }
            return parsedMsg
        }

        // Start command for STM, transmitted through rPi
        fun sendStartSignal(bts: BluetoothService) {
            val msg = "START"
            bts.write(toByteArray(msg))
        }

        // C9: Display Target ID Found
        // [Tag], Obstacle_ID, Target
        private fun parseTargetIDFound(
            list: List<String>,
            result: HashMap<String, String>
        ): String {
            val validIDs = (11..40).toList()
            result["id"] = list[1]
            result["value"] = list[2]
            if ((!isInteger(result["id"])) || (!isInteger(result["value"])))
                result["TAG"] = "Error"
            else {
                if (validIDs.contains(result["value"]!!.toInt())) {
                    result["TAG"] = "C9"
                }
                else {
                    result["TAG"] = "Error"
                }
            }
            return "Invalid Image Target ID"
        }

        // C10: Robot Position and Facing
        // [Tag] ListOfPayload
        private fun parseRobotPosFacing(
            list: List<String>,
            result: HashMap<String, String>
        ): String {
            result["TAG"] = "C10"
            result["payload"] = list[1]
            return "Invalid Robot Position or Facing"
        }
        
        private fun isInteger(s: String?): Boolean {
            return s!!.toIntOrNull() != null
        }

        // Obstacle layout: sent to algorithm to perform path planning
        fun sendObstacles(bts: BluetoothService, obstacleList: List<Obstacle>,
                          robotX: Int, robotY: Int, robotFacing: String) {


            var resultString = "START/EXPLORE/(R,$robotX,$robotY,${toDegree(robotFacing)})/"

            for (obstacle in obstacleList) {
                val x = obstacle.xPos.toString().padStart(2,'0')
                val y = obstacle.yPos.toString().padStart(2,'0')
                val id = obstacle.id.toString().padStart(2,'0')
                var degrees = toDegree(obstacle.facing!!)

                val formedObstacle = "($id,$x,$y,$degrees)/"
                resultString+=formedObstacle
            }
            resultString=resultString.dropLast(1)
            bts.write(toByteArray(resultString))
        }

        private fun toDegree(facing: String) : String {
            var degree = ""

            when (facing) {
                "N" -> degree = "0"
                "S" -> degree = "180"
                "E" -> degree = "-90"
                "W" -> degree = "90"
            }
            return degree
        }

        // Update robot position on tablet display
        private fun updateRobotPosFacing(result: HashMap<String, String>) : String {
            result["TAG"] = "C11"
            return "Update Robot Position"
        }
    }
}