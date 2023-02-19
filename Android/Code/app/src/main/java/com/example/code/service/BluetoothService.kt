package com.example.code.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import com.example.code.ui.viewmodels.ArenaViewModel
import com.example.code.ui.viewmodels.BluetoothViewModel
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.HashMap

class BluetoothService(
    // ViewModel
    private val bluetoothViewModel: BluetoothViewModel,
    private val arenaViewModel: ArenaViewModel
) {
    // Debugging
    private val TAG = "BT Service"

    // Name for the SDP record when creating server socket
    private val SDP_NAME = "MDP15"

    // Bluetooth Discoverable Name
    private val DEVICE_NAME = "MDP 15 Android"

    // Generic Bluetooth Serial Port Profile UUID for Debugging (as Server accepting connections)
    private val SERVER_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    // Unique UUID (to provide to RPi)
//    private val MY_UUID: UUID = UUID.fromString("d3da3f39-8688-4d15-a47f-50fb82315496")

    // UUID of RPi (as Client initiating connection)
//    private val MY_UUID: UUID = UUID.fromString("94F39D29-7D6D-437D-973B-FBA39E49D4EE")

    // For Yh's laptop
    // private val MY_UUID: UUID = UUID.fromString("4C4C4544-0053-3010-8053-CAC04F573933")

    // Bluetooth Adapter
    val mAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    // Member Threads
    private var mAcceptThread: AcceptThread? = null
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null
    private var mState = 0

    // Constants that indicate the current connection state
    // TODO: might want to move these into bluetoothViewModel
    val STATE_NONE = 0 // we're doing nothing
    val STATE_LISTEN = 1 // now listening for incoming connections
    val STATE_CONNECTING = 2 // now initiating an outgoing connection
    val STATE_CONNECTED = 3 // now connected to a remote device

    @SuppressLint("MissingPermission")
    // Register BroadcastReceiver
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        bluetoothViewModel.addDiscoveredDevice(device)
                        Log.d(TAG, "onReceive: Device Found")
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d(TAG, "onReceive: Started Discovery")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "onReceive: Finished Discovery")
                }
            }
        }
    }

    /**
     * Update UI status according to current state of connection
     */
    @Synchronized
    fun updateUI() {
        // TODO
    }

    /**
     * Scan for bluetooth devices
     */
    @SuppressLint("MissingPermission")
    fun scan() {
        bluetoothViewModel.clearDiscoveredDevices()
        if (mAdapter.isDiscovering) {
            mAdapter.cancelDiscovery()
            mAdapter.startDiscovery()
        } else {
            mAdapter.startDiscovery()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            mAdapter.cancelDiscovery()
        }, 10000L)
    }

    /**
     * Return the current connection state
     */
    // TODO: maybe use scaffold top bar to display connection status
    @Synchronized
    fun getState(): Int {
        return mState
    }

    /**
     * Start the bluetooth service. AcceptThread to begin a
     * session in listening (server) mode.
     */
    @Synchronized
    fun start() {
        Log.d(TAG, "start()")
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = AcceptThread()
            mAcceptThread!!.start()
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    @Synchronized
    fun connect(device: BluetoothDevice) {
        Log.d(TAG, "connect to: $device")
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread!!.cancel()
                mConnectThread = null
            }
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        // Start the thread to connect with the given device
        mConnectThread = ConnectThread(device)
        mConnectThread!!.start()
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @Synchronized
    fun connected(socket: BluetoothSocket?, device: BluetoothDevice) {
        Log.d(TAG, "connected(), device: $device")
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread!!.cancel()
            mAcceptThread = null
        }
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = socket?.let { ConnectedThread(it) }
        mConnectedThread!!.start()
    }

    /**
     * Stop all threads and adapter discovery
     */
    @SuppressLint("MissingPermission")
    @Synchronized
    fun stop() {
        Log.d(TAG, "stop")
        if (mAdapter.isDiscovering)
            mAdapter.cancelDiscovery()

        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        if (mAcceptThread != null) {
            mAcceptThread!!.cancel()
            mAcceptThread = null
        }
        mState = STATE_NONE
    }

    /**
     * Write to the ConnectedThread in an un-synchronized manner
     *
     * @param out The bytes to write
     */
    fun write(out: ByteArray?) {
        // Create temporary object
        var r: ConnectedThread
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (mState != STATE_CONNECTED) return
            r = mConnectedThread!!
        }
        r.write(out)
    }

    private fun connectionFailed() {
        Log.i(TAG, "connectionFailed()")
        mState = STATE_NONE
        // Start the service over to restart listening mode
        this@BluetoothService.start()
    }

    private fun connectionLost() {
        Log.i(TAG, "connectionLost()")
        mState = STATE_NONE
        // Start the service over to restart listening mode
        this@BluetoothService.start()
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    @SuppressLint("MissingPermission")
    private inner class AcceptThread : Thread() {
        private val mmServerSocket: BluetoothServerSocket?

        init {
            var tmp: BluetoothServerSocket? = null
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(SDP_NAME, SERVER_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "listen() failed", e)
            }
            mmServerSocket = tmp
            mState = STATE_LISTEN
            mAdapter.name = DEVICE_NAME
            Log.i(TAG, "mAcceptThread Initialised")
        }

        override fun run() {
            Log.d(TAG, "mAcceptThread Running: $this")
            var socket: BluetoothSocket?

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                Log.i(TAG, "Socket is accepting connections")
                socket = try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "accept() failed", e)
                    break
                }

                Log.i(TAG, "Socket Connection Accepted")
                // If a connection was accepted
                if (socket != null) {
                    synchronized(this@BluetoothService) {
                        when (mState) {
                            // Situation normal. Start the connected thread.
                            STATE_LISTEN, STATE_CONNECTING ->
                                connected(socket, socket.remoteDevice)
                            // Either not ready or already connected. Terminate new socket.
                            STATE_NONE, STATE_CONNECTED ->
                                try {
                                    socket.close()
                                } catch (e: IOException) {
                                    Log.e(TAG, "Could not close unwanted socket", e)
                                }
                            else -> {}
                        }
                    }
                }
            }
            Log.d(TAG, "END mAcceptThread")
        }

        fun cancel() {
            Log.d(TAG, "cancel $this")
            try {
                mmServerSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of server failed", e)
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    @SuppressLint("MissingPermission")
    private inner class ConnectThread(private val mmDevice: BluetoothDevice) :
        Thread() {
        private val mmSocket: BluetoothSocket?

        init {
            var tmp: BluetoothSocket? = null

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                // Bond with the device
                mmDevice.createBond()
                // Wait for the bond to be established
                while (mmDevice.bondState != BluetoothDevice.BOND_BONDED) {
                    try {
                        sleep(100)
                    } catch (e: InterruptedException) {
                        Log.e(TAG, "Unable to Bond: $e")
                    }
                }
                // Bluetooth Discovery Process to retrieve UUID
                mmDevice.fetchUuidsWithSdp()
                var mUuids: Array<ParcelUuid?>? = null
                while (mUuids == null) {
                    mUuids = mmDevice.uuids
                    Log.d(TAG, "mUuids: $mUuids")
                }
                // Create Socket
                Log.d(TAG, "Creating Socket... UUID: ${mUuids[0]?.uuid}")
                tmp = mmDevice.createRfcommSocketToServiceRecord(mUuids[0]?.uuid)
            } catch (e: IOException) {
                Log.e(TAG, "create() failed", e)
            }
            mmSocket = tmp
            mState = STATE_CONNECTING
            Log.d(TAG, "mConnectThread Initialised")
        }

        override fun run() {
            Log.i(TAG, "mConnectThread Running: $this")

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery()

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket!!.connect()
            } catch (e: IOException) {
                Log.e(TAG, "mConnectThread mmSocket failed to connect")
                Log.e(TAG, "Error: $e")
                // Close the socket
                try {
                    mmSocket!!.close()
                } catch (e2: IOException) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2)
                }
                connectionFailed()
                return
            }
            Log.i(TAG, "mConnectThread mmSocket Connected")

            // Reset the ConnectThread because we're done
            synchronized(this@BluetoothService) { mConnectThread = null }

            // Start the connected thread
            connected(mmSocket, mmDevice)
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private inner class ConnectedThread(socket: BluetoothSocket) :
        Thread() {
        private val mmSocket: BluetoothSocket
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            mmSocket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
                Log.e(TAG, "temp sockets not created", e)
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
            mState = STATE_CONNECTED
            Log.i(TAG, "ConnectedThread Initialised")
        }

        fun checkRobotBounds(coordinate: Int) : Boolean {
            if (coordinate in 0 until 20) {
                return true
            }
            return false
        }

        override fun run() {
            Log.d(TAG, "mConnectedThread Running: $this")
            val buffer = ByteArray(1024)

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    val bytes = mmInStream!!.read(buffer)
                    val result: HashMap<String, String> = MessageService.parseMessage(buffer, bytes)
                    if (result["type"] == "robot") {
                        if (checkRobotBounds(result["x"]!!.toInt())
                            && checkRobotBounds(result["y"]!!.toInt())) {
                            arenaViewModel.setRobotPosFacing(
                                x = result["x"]!!.toInt(),
                                y = result["y"]!!.toInt(),
                                facing = result["facing"]!!
                            )
                        }
                        else {
                            bluetoothViewModel.addReceivedMessage("Invalid Coordinates!!!")
                        }
                    }
                    else if (result["type"] == "target") {
                        if (checkValidValue(result["value"]!!.toInt())!=null) {
                            arenaViewModel.setObstacleValue(
                                id = result["id"]!!.toInt(),
                                value = result["value"]!!.toInt()
                            )
                        }
                        else {
                            bluetoothViewModel.addReceivedMessage("Invalid Image Value!!")
                        }
                    }
                    bluetoothViewModel.addReceivedMessage(result["msg"]!!)
                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                    connectionLost()
                    break
                }
            }
        }

        fun checkValidValue(value: Int): Int? {
            if (value>=11 && value<=40) {
                return value
            }
            else {
                return null
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        fun write(buffer: ByteArray?) {
            try {
                mmOutStream!!.write(buffer)
            } catch (e: IOException) {
                Log.e(TAG, "Exception during write", e)
            }
        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }
    }
}