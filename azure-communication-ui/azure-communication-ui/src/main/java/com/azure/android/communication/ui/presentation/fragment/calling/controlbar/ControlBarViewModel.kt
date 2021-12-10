// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.ui.presentation.fragment.calling.controlbar

import com.azure.android.communication.ui.presentation.fragment.common.audiodevicelist.AudioDeviceListViewModel
import com.azure.android.communication.ui.redux.action.Action
import com.azure.android.communication.ui.redux.action.LocalParticipantAction
import com.azure.android.communication.ui.redux.state.AudioDeviceSelectionStatus
import com.azure.android.communication.ui.redux.state.AudioOperationalStatus
import com.azure.android.communication.ui.redux.state.AudioState
import com.azure.android.communication.ui.redux.state.CallingState
import com.azure.android.communication.ui.redux.state.CallingStatus
import com.azure.android.communication.ui.redux.state.CameraState
import com.azure.android.communication.ui.redux.state.PermissionState
import com.azure.android.communication.ui.redux.state.PermissionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class ControlBarViewModel(
    private val dispatch: (Action) -> Unit,
    private val audioDeviceListViewModel: AudioDeviceListViewModel,
) {
    private lateinit var cameraStateFlow: MutableStateFlow<CameraModel>
    private lateinit var audioOperationalStatusStateFlow: MutableStateFlow<AudioOperationalStatus>
    private lateinit var audioDeviceSelectionStatusStateFlow: MutableStateFlow<AudioDeviceSelectionStatus>
    private lateinit var shouldEnableMicButtonStateFlow: MutableStateFlow<Boolean>

    fun init(
        permissionState: PermissionState,
        cameraState: CameraState,
        audioState: AudioState,
        callingState: CallingState,
    ) {
        cameraStateFlow =
            MutableStateFlow(CameraModel(permissionState.cameraPermissionState, cameraState))
        audioOperationalStatusStateFlow = MutableStateFlow(audioState.operation)
        audioDeviceSelectionStatusStateFlow = MutableStateFlow(audioState.device)
        shouldEnableMicButtonStateFlow =
            MutableStateFlow(shouldEnableMicButton(audioState, callingState))
    }

    fun update(
        permissionState: PermissionState,
        cameraState: CameraState,
        audioState: AudioState,
        callingState: CallingState,
    ) {
        cameraStateFlow.value = CameraModel(permissionState.cameraPermissionState, cameraState)
        audioOperationalStatusStateFlow.value = audioState.operation
        audioDeviceSelectionStatusStateFlow.value = audioState.device
        shouldEnableMicButtonStateFlow.value = shouldEnableMicButton(audioState, callingState)
    }

    fun getAudioOperationalStatusStateFlow(): StateFlow<AudioOperationalStatus> {
        return audioOperationalStatusStateFlow
    }

    fun getCameraStateFlow(): StateFlow<CameraModel> {
        return cameraStateFlow
    }

    fun getAudioDeviceSelectionStatusStateFlow(): StateFlow<AudioDeviceSelectionStatus> {
        return audioDeviceSelectionStatusStateFlow
    }

    fun getShouldEnableMicButtonStateFlow(): StateFlow<Boolean> {
        return shouldEnableMicButtonStateFlow
    }

    fun turnMicOff() {
        dispatchAction(action = LocalParticipantAction.MicOffTriggered())
    }

    fun turnMicOn() {
        dispatchAction(action = LocalParticipantAction.MicOnTriggered())
    }

    fun turnCameraOn() {
        dispatchAction(action = LocalParticipantAction.CameraOnRequested())
    }

    fun turnCameraOff() {
        dispatchAction(action = LocalParticipantAction.CameraOffTriggered())
    }

    fun displayAudioDeviceSelectionMenu() {
        audioDeviceListViewModel.displayAudioDeviceSelectionMenu()
    }

    private fun shouldEnableMicButton(audioState: AudioState, callingState: CallingState): Boolean {
        return (audioState.operation != AudioOperationalStatus.PENDING && callingState.CallingStatus != CallingStatus.IN_LOBBY)
    }

    private fun dispatchAction(action: Action) {
        dispatch(action)
    }

    internal data class CameraModel(
        val cameraPermissionState: PermissionStatus,
        val cameraState: CameraState,
    )
}