package com.googlecode.android_scripting.facade.bluetooth;

import java.util.List;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAvrcpController;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothUuid;
import android.os.ParcelUuid;

import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;

public class BluetoothAvrcpFacade extends RpcReceiver {
  static final ParcelUuid[] AVRCP_UUIDS = {
    BluetoothUuid.AvrcpTarget, BluetoothUuid.AvrcpController
  };
  private final Service mService;
  private final BluetoothAdapter mBluetoothAdapter;

  private static boolean sIsAvrcpReady = false;
  private static BluetoothAvrcpController sAvrcpProfile = null;

  public BluetoothAvrcpFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    mBluetoothAdapter.getProfileProxy(mService, new AvrcpServiceListener(),
        BluetoothProfile.AVRCP_CONTROLLER);
  }

  class AvrcpServiceListener implements BluetoothProfile.ServiceListener {
    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
      sAvrcpProfile = (BluetoothAvrcpController) proxy;
      sIsAvrcpReady = true;
    }

    @Override
    public void onServiceDisconnected(int profile) {
      sIsAvrcpReady = false;
    }
  }

  @Rpc(description = "Is Avrcp profile ready.")
  public Boolean bluetoothAvrcpIsReady() {
    return sIsAvrcpReady;
  }

  @Rpc(description = "Get all the devices connected through AVRCP.")
  public List<BluetoothDevice> bluetoothAvrcpGetConnectedDevices() {
    while (!sIsAvrcpReady);
    return sAvrcpProfile.getConnectedDevices();
  }

  @Rpc(description = "Send AVRPC passthrough command.")
  public void bluetoothAvrcpSendPassThroughCmd(
          @RpcParameter(name = "deviceID", description = "Name or MAC address of a bluetooth device.")
          String deviceID,
          @RpcParameter(name = "keyCode")
          Integer keyCode,
          @RpcParameter(name = "keyState")
          Integer keyState) throws Exception {
      BluetoothDevice mDevice = BluetoothFacade.getDevice(sAvrcpProfile.getConnectedDevices(), deviceID);
      sAvrcpProfile.sendPassThroughCmd(mDevice, keyCode, keyState);
  }

  @Override
  public void shutdown() {
  }
}
