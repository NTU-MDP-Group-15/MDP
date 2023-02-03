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
import android.util.Log
import com.example.code.ui.viewmodels.BluetoothViewModel
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothService(
    // ViewModel
    private val viewModel: BluetoothViewModel
) {
    // Debugging
    private val TAG = "BT Service"

    // Name for the SDP record when creating server socket
    private val SDP_NAME = "MDP15"

    // Unique UUID
//    private val MY_UUID: UUID = UUID.fromString("d3da3f39-8688-4d15-a47f-50fb82315496")

    // Generic UUID for Debugging
    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    // Bluetooth Adapter
    val mAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    // Member Threads
    private var mAcceptThread: AcceptThread? = null
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null
    private var mState = 0

    // Constants that indicate the current connection state
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
                        viewModel.addDiscoveredDevice(device)
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
    fun updateUIsomething() {
        // TODO
    }

    /**
     * Scan for bluetooth devices
     */
    @SuppressLint("MissingPermission")
    fun scan() {
        viewModel.clearDiscoveredDevices()
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
    @Synchronized
    fun getState(): Int {
        return mState
    }

    /**
     * Start the bluetooth service. AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
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
        // Update UI title
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

        // Update UI title
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @Synchronized
    fun connected(socket: BluetoothSocket?, device: BluetoothDevice) {
        Log.d(TAG, "connected()")

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

        // Send the name of the connected device back to the UI Activity
//        val msg: Message = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME)
//        val bundle = Bundle()
//        bundle.putString(Constants.DEVICE_NAME, device.name)
//        msg.setData(bundle)
//        mHandler.sendMessage(msg)

        // Update UI title
    }

    /**
     * Stop all threads and adapter
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
        // Update UI title
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread.write
     */
    fun write(out: ByteArray?) {
        // Create temporary object
        var r: ConnectedThread
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (mState != STATE_CONNECTED) return
            r = mConnectedThread!!
        }
        // Perform the write unsynchronized
        r.write(out)
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private fun connectionFailed() {
        // Send a failure message back to the Activity
//        val msg: Message = mHandler.obtainMessage(Constants.MESSAGE_TOAST)
//        val bundle = Bundle()
//        bundle.putString(Constants.TOAST, "Unable to connect device")
//        msg.setData(bundle)
//        mHandler.sendMessage(msg)
        mState = STATE_NONE
        // Update UI title

        // Start the service over to restart listening mode
        this@BluetoothService.start()
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private fun connectionLost() {
        // Send a failure message back to the Activity
//        val msg: Message = mHandler.obtainMessage(Constants.MESSAGE_TOAST)
//        val bundle = Bundle()
//        bundle.putString(Constants.TOAST, "Device connection was lost")
//        msg.setData(bundle)
//        mHandler.sendMessage(msg)
        mState = STATE_NONE
        // Update UI title

        // Start the service over to restart listening mode
        this@BluetoothService.start()
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    @SuppressLint("MissingPermission")
    private inner class AcceptThread() : Thread() {
        private val mmServerSocket: BluetoothServerSocket?

        init {
            var tmp: BluetoothServerSocket? = null
            // Create a new listening server socket
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(SDP_NAME, MY_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "listen() failed", e)
            }
            mmServerSocket = tmp
            mState = STATE_LISTEN
//            mAdapter?.setName("MDP Group 15")
            Log.i(TAG, "mAcceptThread Initialised")
        }

        override fun run() {
            Log.d(TAG, "mAcceptThread Running: $this")
//            name = "AcceptThread"
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
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "create() failed", e)
            }
            mmSocket = tmp
            mState = STATE_CONNECTING
            Log.d(TAG, "mConnectThread Initialised")
        }

        override fun run() {
            Log.i(TAG, "mConnectThread Running: $this")
//            name = "ConnectThread"

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

        override fun run() {
            Log.d(TAG, "mConnectedThread Running: $this")
            val buffer = ByteArray(1024)
            var bytes: Int

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream!!.read(buffer)

                    viewModel.addReceivedMessage(String(buffer) + "\n")
                    // Send the obtained bytes to the UI Activity
//                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
//                        .sendToTarget()
                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                    connectionLost()
                    break
                }
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
                // Share the sent message back to the UI Activity
//                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
//                    .sendToTarget()
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