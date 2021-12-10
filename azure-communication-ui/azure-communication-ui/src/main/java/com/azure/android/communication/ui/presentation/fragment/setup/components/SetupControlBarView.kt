// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.ui.presentation.fragment.setup.components

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.azure.android.communication.ui.R
import com.azure.android.communication.ui.redux.state.AudioDeviceSelectionStatus
import com.azure.android.communication.ui.redux.state.AudioOperationalStatus
import com.azure.android.communication.ui.redux.state.CameraOperationalStatus
import com.azure.android.communication.ui.redux.state.CameraState
import com.azure.android.communication.ui.redux.state.PermissionStatus
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class SetupControlBarView : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private lateinit var viewModel: SetupControlBarViewModel
    private lateinit var setupAudioButton: Button
    private lateinit var setupButtonHolder: LinearLayout
    private lateinit var setupCameraButton: Button
    private lateinit var setupAudioDeviceButton: Button

    override fun onFinishInflate() {
        super.onFinishInflate()
        setupButtonHolder = this
        setupAudioButton = findViewById(R.id.azure_communication_ui_setup_audio_button)
        setupCameraButton = findViewById(R.id.azure_communication_ui_setup_camera_button)
        setupAudioDeviceButton = findViewById(R.id.azure_communication_ui_setup_audio_device_button)
        setupAudioButton.setOnClickListener {
            toggleAudio()
        }
        setupCameraButton.setOnClickListener {
            toggleVideo()
        }
        setupAudioDeviceButton.setOnClickListener {
            openAudioDeviceList()
        }
    }

    fun start(
        viewLifecycleOwner: LifecycleOwner,
        setupControlBarViewModel: SetupControlBarViewModel,
    ) {
        viewModel = setupControlBarViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getCameraPermissionState().collect {
                setupCameraButton.isEnabled = it != PermissionStatus.DENIED
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getMicPermissionState().collect {
                if (it == PermissionStatus.NOT_ASKED) {
                    viewModel.requestAudioPermission()
                }
                setupButtonHolder.visibility =
                    if (it == PermissionStatus.DENIED) INVISIBLE else VISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAudioOperationalStatusStateFlow().collect {
                setAudioButtonState(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getCameraState().collect {
                setCameraButtonState(it)
                setButtonColorOnCameraState(it.operation)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAudioDeviceSelectionStatusStateFlow().collect {
                setAudioDeviceButtonState(it)
            }
        }
    }

    private fun setAudioButtonState(audioOperationalStatus: AudioOperationalStatus) {
        when (audioOperationalStatus) {
            AudioOperationalStatus.ON -> {
                setupAudioButton.isSelected = true
                setupAudioButton.isEnabled = true
                setupAudioButton.text =
                    context.getString(R.string.azure_communication_ui_setup_mic_on)
            }
            AudioOperationalStatus.OFF -> {
                setupAudioButton.isSelected = false
                setupAudioButton.isEnabled = true
                setupAudioButton.text =
                    context.getString(R.string.azure_communication_ui_setup_mic_off)
            }
            AudioOperationalStatus.PENDING -> {
                setupAudioButton.isEnabled = false
            }
        }
    }

    private fun setCameraButtonState(cameraState: CameraState) {
        when (cameraState.operation) {
            CameraOperationalStatus.ON -> {
                setupCameraButton.isSelected = true
                setupCameraButton.text =
                    context.getString(R.string.azure_communication_ui_setup_video_on)
            }
            CameraOperationalStatus.OFF -> {
                setupCameraButton.isSelected = false
                setupCameraButton.text =
                    context.getString(R.string.azure_communication_ui_setup_video_off)
            }
        }
    }

    private fun setButtonColorOnCameraState(cameraOperationalStatus: CameraOperationalStatus) {
        if (viewModel.getCameraPermissionState().value == PermissionStatus.GRANTED) {
            val buttonColor = if (cameraOperationalStatus == CameraOperationalStatus.ON)
                R.color.azure_communication_ui_color_on_surface_camera_active
            else R.color.azure_communication_ui_toggle_selector

            if (setupCameraButton.isEnabled) {
                setButtonColor(setupCameraButton, buttonColor)
                setButtonColor(setupAudioButton, buttonColor)
                setButtonColor(setupAudioDeviceButton, buttonColor)
            }
        }
    }

    private fun setAudioDeviceButtonState(audioDeviceSelectionStatus: AudioDeviceSelectionStatus) {
        when (audioDeviceSelectionStatus) {
            AudioDeviceSelectionStatus.SPEAKER_SELECTED -> {
                setupAudioDeviceButton.text = context.getString(
                    R.string.azure_communication_ui_setup_audio_device_speaker
                )
                setupAudioDeviceButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    R.drawable.azure_communication_ui_ic_fluent_speaker_2_24_filled_composite_button_enabled,
                    0,
                    0
                )
            }
            AudioDeviceSelectionStatus.RECEIVER_SELECTED -> {
                setupAudioDeviceButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    R.drawable.azure_communication_ui_ic_fluent_speaker_2_24_regular_composite_button_filled,
                    0,
                    0
                )
                setupAudioDeviceButton.text = context.getString(
                    R.string.azure_communication_ui_setup_audio_device_android
                )
            }
            else -> {
                setupAudioDeviceButton.text = ""
            }
        }

        val setupAudioDeviceButtonColor =
            if (setupCameraButton.isSelected) R.color.azure_communication_ui_color_on_surface_camera_active
            else R.color.azure_communication_ui_toggle_selector

        setupAudioDeviceButton.compoundDrawableTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    setupAudioDeviceButtonColor
                )
            )
    }

    private fun toggleAudio() {
        if (setupAudioButton.isSelected) {
            viewModel.turnMicOff()
        } else {
            viewModel.turnMicOn()
        }
    }

    private fun toggleVideo() {
        if (setupCameraButton.isSelected) {
            viewModel.turnCameraOff()
        } else {
            viewModel.turnCameraOn()
        }
    }

    private fun openAudioDeviceList() {
        viewModel.displayAudioDeviceSelectionMenu()
    }

    private fun setButtonColor(button: Button, colorId: Int) {
        button.setTextColor(ContextCompat.getColor(context, colorId))
        button.compoundDrawableTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, colorId))
    }
}