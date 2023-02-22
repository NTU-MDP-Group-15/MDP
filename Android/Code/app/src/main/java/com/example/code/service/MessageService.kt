package com.example.code.service

import android.util.Log

class MessageService {
    companion object {
        fun parseMessage(buffer: ByteArray, bytes: Int): HashMap<String, String> {
            // Note to change protocol for checklist
            val result = HashMap<String, String>()
            val msg = String(buffer, 0, bytes)
            val list: List<String> = msg.split(" ")

            var parsedMsg: String = when (list[0]) {
                "[C4]" -> parseRobotStatus(list, result)
                "[C9]" -> parseTargetIDFound(list, result)
                "[C10]" -> parseRobotPosFacing(list, result)
                else -> {
                    msg
                }
            }
            parsedMsg += "\n"
            result["msg"] = parsedMsg
            return result
        }

        private fun toByteArray(msg: String): ByteArray {
            return msg.toByteArray(Charsets.UTF_8)
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
            var parsedMsg = ""
            when (action) {
                "MOV" -> {
                    parsedMsg = "Moving "
                    when (value) {
                        "Fd" -> parsedMsg += "Forward"
                        "Bd" -> parsedMsg += "Backward"
                        "Lt" -> parsedMsg += "Left"
                        "Rt" -> parsedMsg += "Right"
                    }
                }
                "IMG" -> {
                    when (value) {
                        "TP" -> parsedMsg = "Taking a Photo"
                        "MI" -> parsedMsg = "Running Model on Image"
                    }
                }
                "TAR" -> parsedMsg = "Heading to Target $value"
            }
            return parsedMsg
        }

        // C6: Obstacle Placement
        // [Tag], Action, Obstacle_ID, Obstacle_X_Coordinate, Obstacle_Y_Coordinate
        fun sendObstaclePlacement(bts: BluetoothService, action: String, id: Int, x: Int, y: Int) {
            val msg = "[C6] $action $id $x $y"
            bts.write(toByteArray(msg))
        }

        // C7: Obstacle Image Facing
        // [Tag], Obstacle_ID, Facing
        fun sendObstacleFacing(bts: BluetoothService, id: Int, facing: String) {
            val msg = "[C7] $id $facing"
            bts.write(toByteArray(msg))
        }

        fun sendStartSignal(bts: BluetoothService) {
            val msg = "Ready to start"
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
            if (validIDs.contains(result["value"]!!.toInt())) {
                result["TAG"] = "C9"
            }
            else {
                result["TAG"] = "Error"
            }
            return "Invalid Image Target ID"
        }

        // C10: Robot Position and Facing
        // [Tag], Robot_X_Coordinate, Robot_Y_Coordinate, Facing
        private fun parseRobotPosFacing(
            list: List<String>,
            result: HashMap<String, String>
        ): String {
            val validCoordinates = (1..19).toList()
            val validFacings = listOf("N", "S", "E", "W")
            result["x"] = list[1]
            result["y"] = list[2]
            result["facing"] = list[3]
            if (
                (validCoordinates.contains(result["x"]!!.toInt())) &&
                (validCoordinates.contains(result["y"]!!.toInt())) &&
                (validFacings.contains(result["facing"]!!))
            ) {
                result["TAG"] = "C10"
            } else {
                result["TAG"] = "Error"
            }
            return "Invalid Robot Position or Facing"
        }
    }
}