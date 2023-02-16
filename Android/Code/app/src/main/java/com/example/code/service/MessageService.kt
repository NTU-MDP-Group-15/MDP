package com.example.code.service

import android.util.Log

class MessageService {
    companion object {
        fun parseMessage(buffer: ByteArray, bytes: Int): HashMap<String, String> {
            // Note to change protocol for checklist
            val result = hashMapOf<String, String>("type" to "status")
            val msg = String(buffer, 0, bytes)
            val list: List<String> = msg.split(" ")
            val tag: String = list[0]
            var parsedMsg = ""
            when (tag) {
                "[C4]" -> parsedMsg = parseRobotStatus(list)
                "[C9]" -> parsedMsg = parseTargetIDFound(list, result)
                "[C10]" -> parsedMsg = parseRobotPosFacing(list, result)
                else -> {
                    Log.d("MessageService", "Unknown Format: $msg")
                    parsedMsg = msg
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
        private fun parseRobotStatus(list: List<String>): String {
            val action = list[1]
            val value = list[2]
            //
            Log.d("", value)
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
            val msg = "[C6], $action, $id, $x, $y"
            bts.write(toByteArray(msg))
        }

        // C7: Obstacle Image Facing
        // [Tag], Obstacle_ID, Facing
        fun sendObstacleFacing(bts: BluetoothService, id: Int, facing: String) {
            val msg = "[C7], $id, $facing"
            bts.write(toByteArray(msg))
        }

        // C9: Display Target ID Found
        // [Tag], Obstacle_ID, Target
        // [Tag], Obstacle_ID, Target, Facing
        private fun parseTargetIDFound(list: List<String>, result: HashMap<String, String>): String {
            val id = list[1]
            val targetValue = list[2]
            result.replace("type", "target")
            result["id"] = id
            result["value"] = targetValue
            var parsedMsg = "Obstacle ID: $id has a target value of $targetValue"
            if (list.size == 4) {
                parsedMsg += " and a facing of ${list[3]}"
            }
            return parsedMsg
        }

        // C10: Robot Position and Facing
        // [Tag], Robot_X_Coordinate, Robot_Y_Coordinate, Facing
        private fun parseRobotPosFacing(list: List<String>, result: HashMap<String, String>): String {
            val x = list[1]
            val y = list[2]
            val facing = list[3]
            result.replace("type", "robot")
            result["x"] = x
            result["y"] = y
            result["facing"] = facing
            return "Robot is currently at ($x, $y), facing $facing"
        }
    }
}